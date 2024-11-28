package com.minecolonies.core.entity.ai.workers.crafting;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.CraftingWorkerBuildingModule;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.core.network.messages.client.BlockParticleEffectMessage;
import com.minecolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEMS_CRAFTED;
import static com.minecolonies.core.util.WorkerUtil.hasTooManyExternalItemsInInv;

/**
 * Abstract class for the principal crafting AIs.
 */
public abstract class AbstractEntityAICrafting<J extends AbstractJobCrafter<?, J>, B extends AbstractBuilding> extends AbstractEntityAIInteract<J, B>
{
    /**
     * Time the worker delays until the next hit.
     */
    protected static final int HIT_DELAY = 10;

    /**
     * Increase this value to make the product creation progress way slower.
     */
    public static final int PROGRESS_MULTIPLIER = 10;

    /**
     * Max level which should have an effect on the speed of the worker.
     */
    protected static final int MAX_LEVEL = 50;

    /**
     * Times the product needs to be hit.
     */
    private static final int HITTING_TIME = 3;

    /**
     * The current request that is being crafted;
     */
    public IRequest<? extends PublicCrafting> currentRequest;

    /**
     * The current recipe that is being crafted.
     */
    protected IRecipeStorage currentRecipeStorage;

    /**
     * Player damage source.
     */
    private DamageSource playerDamageSource;

    /**
     * Already dumped during this iteration.
     */
    private boolean dumped = false;

    /**
     * The number of actions a crafting "success" is worth. By default, that's 1 action for 1 crafting success. Override this in your subclass to make crafting recipes worth more
     * actions :-)
     *
     * @return The number of actions a crafting "success" is worth.
     */
    protected int getActionRewardForCraftingSuccess()
    {
        return 1;
    }

    /**
     * Initialize the crafter job and add all his tasks.
     *
     * @param job the job he has.
     */
    public AbstractEntityAICrafting(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AITarget(IDLE, () -> START_WORKING, 1),
          new AITarget(START_WORKING, this::decide, STANDARD_DELAY),
          new AITarget(QUERY_ITEMS, this::queryItems, STANDARD_DELAY),
          new AITarget(GET_RECIPE, this::getRecipe, STANDARD_DELAY),
          new AITarget(CRAFT, this::craft, HIT_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getState() == CRAFT ? RENDER_META_WORKING : "");
    }

    /**
     * Main method to decide on what to do.
     *
     * @return the next state to go to.
     */
    protected IAIState decide()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        if (job.getTaskQueue().isEmpty())
        {
            if (worker.getNavigation().isDone())
            {
                if (building.isInBuilding(worker.blockPosition()) && ColonyConstants.rand.nextInt(20) != 0)
                {
                    setDelay(TICKS_20 * 20);
                    worker.getNavigation().moveToRandomPos(10, DEFAULT_SPEED, building.getCorners());
                }
                else
                {
                    walkToBuilding();
                }
            }
            return IDLE;
        }

        if (job.getCurrentTask() == null)
        {
            return IDLE;
        }

        if (walkToBuilding())
        {
            return START_WORKING;
        }

        if (job.getActionsDone() >= getActionsDoneUntilDumping())
        {
            // Wait to dump before continuing.
            return getState();
        }

        return getNextCraftingState();
    }

    /**
     * Gets the next crafting state required, if a task exists.
     *
     * @return next state
     */
    protected IAIState getNextCraftingState()
    {
        if (job.getCurrentTask() == null)
        {
            return getState();
        }

        if (currentRecipeStorage != null && !dumped && hasTooManyExternalItemsInInv(currentRecipeStorage, worker.getInventoryCitizen()))
        {
            dumped = true;
            return INVENTORY_FULL;
        }

        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    /**
     * Query the IRecipeStorage of the first request in the queue.
     *
     * @return the next state to go to.
     */
    protected IAIState getRecipe()
    {
        final IRequest<? extends PublicCrafting> currentTask = job.getCurrentTask();

        if (currentTask == null)
        {
            return START_WORKING;
        }

        final ICraftingBuildingModule module = building.getCraftingModuleForRecipe(currentTask.getRequest().getRecipeID());
        if (module == null)
        {
            job.finishRequest(false);
            incrementActionsDone(getActionRewardForCraftingSuccess());
            return START_WORKING;
        }
        currentRecipeStorage = module.getFirstFulfillableRecipe(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentTask.getRequest().getStack()), 1, false);
        if (currentRecipeStorage == null)
        {
            job.finishRequest(false);
            incrementActionsDone(getActionRewardForCraftingSuccess());
            return START_WORKING;
        }

        if (!dumped && hasTooManyExternalItemsInInv(currentRecipeStorage, worker.getInventoryCitizen()))
        {
            dumped = true;
            currentRecipeStorage = null;
            return INVENTORY_FULL;
        }

        if (currentRecipeStorage.getRequiredTool() != ModEquipmentTypes.none.get())
        {
            if (checkForToolOrWeapon(currentRecipeStorage.getRequiredTool()))
            {
                currentRecipeStorage = null;
                job.finishRequest(false);
                incrementActionsDone(getActionRewardForCraftingSuccess());
                return START_WORKING;
            }
        }

        currentRequest = currentTask;
        job.setMaxCraftingCount(currentRequest.getRequest().getCount());
        final int currentCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
          stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));
        final int inProgressCount = getExtendedCount(currentRecipeStorage.getPrimaryOutput());

        final int countPerIteration = currentRecipeStorage.getPrimaryOutput().getCount();
        final int doneOpsCount = currentCount / countPerIteration;
        final int progressOpsCount = inProgressCount / countPerIteration;

        final int remainingOpsCount = currentRequest.getRequest().getCount() - doneOpsCount - progressOpsCount;

        final List<ItemStorage> input = currentRecipeStorage.getCleanedInput();
        for (final ItemStorage inputStorage : input)
        {
            final ItemStack container = inputStorage.getItemStack().getCraftingRemainingItem();
            final int remaining;
            if (!currentRecipeStorage.getCraftingToolsAndSecondaryOutputs().isEmpty()
                  && ItemStackUtils.compareItemStackListIgnoreStackSize(currentRecipeStorage.getCraftingToolsAndSecondaryOutputs(), inputStorage.getItemStack(), false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else if (!ItemStackUtils.isEmpty(container) && ItemStackUtils.compareItemStacksIgnoreStackSize(inputStorage.getItemStack(), container, false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else
            {
                remaining = inputStorage.getAmount() * remainingOpsCount;
            }
            if (InventoryUtils.getCountFromBuilding(building, itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, inputStorage.getItemStack(), false, true))
                  + InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
              itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, inputStorage.getItemStack(), false, true))
                  + getExtendedCount(inputStorage.getItemStack())
                  < remaining)
            {
                currentRecipeStorage = null;
                job.finishRequest(false);
                incrementActionsDone(getActionRewardForCraftingSuccess());
                return START_WORKING;
            }
        }

        job.setCraftCounter(doneOpsCount);
        return QUERY_ITEMS;
    }

    /**
     * Get an extended count that can be overriden.
     *
     * @param stack the stack to add.
     * @return the additional quantities (for example in a furnace).
     */
    protected int getExtendedCount(final ItemStack stack)
    {
        return 0;
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return GET_RECIPE;
    }

    /**
     * Query the required items to take them in the inventory to craft.
     *
     * @return the next state to go to.
     */
    private IAIState queryItems()
    {
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        return checkForItems(currentRecipeStorage);
    }

    /**
     * Check for all items of the required recipe.
     *
     * @param storage the recipe storage.
     * @return the next state to go to.
     */
    protected IAIState checkForItems(@NotNull final IRecipeStorage storage)
    {
        final int inProgressCount = getExtendedCount(currentRecipeStorage.getPrimaryOutput());
        final int countPerIteration = currentRecipeStorage.getPrimaryOutput().getCount();
        final int progressOpsCount = inProgressCount / Math.max(countPerIteration, 1);

        final List<ItemStorage> input = storage.getCleanedInput();
        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack, false).matches(inputStorage.getItemStack());
            final int invCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate);
            final ItemStack container = inputStorage.getItemStack().getCraftingRemainingItem();
            final int remaining;
            if (!currentRecipeStorage.getCraftingToolsAndSecondaryOutputs().isEmpty()
                  && ItemStackUtils.compareItemStackListIgnoreStackSize(currentRecipeStorage.getCraftingToolsAndSecondaryOutputs(), inputStorage.getItemStack(), false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else if (!ItemStackUtils.isEmpty(container) && ItemStackUtils.compareItemStacksIgnoreStackSize(inputStorage.getItemStack(), container, false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else
            {
                remaining = inputStorage.getAmount() * Math.max(job.getMaxCraftingCount(), 1);
            }

            if (invCount + inProgressCount <= 0
                  || invCount + ((job.getCraftCounter() + progressOpsCount) * inputStorage.getAmount()) < remaining)
            {
                if (InventoryUtils.hasItemInProvider(building, predicate))
                {
                    needsCurrently = new Tuple<>(predicate, remaining);
                    return GATHERING_REQUIRED_MATERIALS;
                }
                currentRecipeStorage = null;
                currentRequest = null;
                return GET_RECIPE;
            }
        }

        return CRAFT;
    }

    /**
     * The actual crafting logic.
     *
     * @return the next state to go to.
     */
    protected IAIState craft()
    {
        if (currentRecipeStorage == null || job.getCurrentTask() == null)
        {
            return START_WORKING;
        }

        if (currentRequest == null && job.getCurrentTask() != null)
        {
            return GET_RECIPE;
        }

        if (walkToBuilding())
        {
            return getState();
        }

        job.setProgress(job.getProgress() + 1);

        int toolSlot = -1;
        if (currentRecipeStorage.getRequiredTool() != ModEquipmentTypes.none.get())
        {
            toolSlot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> currentRecipeStorage.getRequiredTool().checkIsEquipment(stack));
        }
        if (toolSlot >= 0)
        {
            worker.getInventoryCitizen().setHeldItem(InteractionHand.MAIN_HAND, toolSlot);
            worker.setItemInHand(InteractionHand.MAIN_HAND, worker.getInventoryCitizen().getStackInSlot(toolSlot));
            worker.setItemInHand(InteractionHand.OFF_HAND,
              currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
        }
        else
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND,
              currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
            worker.setItemInHand(InteractionHand.OFF_HAND, currentRecipeStorage.getPrimaryOutput().copy());
        }
        hitBlockWithToolInHand(building.getPosition());
        Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(worker.getMainHandItem(), building.getPosition().above()), worker);

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            return START_WORKING;
        }

        if (job.getProgress() >= getRequiredProgressForMakingRawMaterial())
        {
            final IAIState check = checkForItems(currentRecipeStorage);
            if (check == CRAFT)
            {
                if (!currentRecipeStorage.fullfillRecipe(getLootContext(), ImmutableList.of(worker.getItemHandlerCitizen())))
                {
                    currentRequest = null;
                    incrementActionsDone(getActionRewardForCraftingSuccess());
                    job.finishRequest(false);
                    resetValues();
                    return START_WORKING;
                }

                currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                job.setCraftCounter(job.getCraftCounter() + 1);
                if (toolSlot != -1)
                {
                    CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);
                }

                if (job.getCraftCounter() >= job.getMaxCraftingCount())
                {
                    incrementActionsDone(getActionRewardForCraftingSuccess());
                    final ICraftingBuildingModule module = building.getCraftingModuleForRecipe(currentRecipeStorage.getToken());
                    if (module != null)
                    {
                        module.improveRecipe(currentRecipeStorage, job.getCraftCounter(), worker.getCitizenData());
                    }

                    currentRecipeStorage = null;
                    resetValues();

                    if (inventoryNeedsDump() && job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
                    {
                        worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
                    }
                    return INVENTORY_FULL;
                }
                else if (toolSlot >= 0 && worker.getInventoryCitizen().getHeldItem(InteractionHand.MAIN_HAND).isEmpty())
                {
                    // tool broke, abort crafting
                    currentRequest = null;
                    job.finishRequest(false);
                    incrementActionsDoneAndDecSaturation();
                    resetValues();
                    return START_WORKING;
                }
                else
                {
                    job.setProgress(0);
                    return GET_RECIPE;
                }
            }
            else
            {
                currentRequest = null;
                job.finishRequest(false);
                incrementActionsDoneAndDecSaturation();
                resetValues();
            }
            return START_WORKING;
        }

        return getState();
    }

    public void hitBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        worker.getLookControl().setLookAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), FACING_DELTA_YAW, worker.getMaxHeadXRot());

        worker.swing(worker.getUsedItemHand());

        final BlockState blockState = worker.level().getBlockState(blockPos);
        final BlockPos vector = blockPos.subtract(worker.blockPosition());
        final Direction facing = BlockPosUtil.directionFromDelta(vector.getX(), vector.getY(), vector.getZ()).getOpposite();

        Network.getNetwork().sendToPosition(
          new BlockParticleEffectMessage(blockPos, blockState, facing.ordinal()),
          new PacketDistributor.TargetPoint(blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE, worker.level().dimension()));

        job.playSound(blockPos, (EntityCitizen) worker);
    }

    /**
     * Reset all the values.
     */
    public void resetValues()
    {
        job.setMaxCraftingCount(0);
        job.setProgress(0);
        job.setCraftCounter(0);
        worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
        worker.setItemInHand(InteractionHand.OFF_HAND, ItemStackUtils.EMPTY);
        dumped = false;
    }

    @Override
    public IAIState afterDump()
    {
        if (job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
        {
            // Fallback security blanket. Normally, the craft() method should have dealt with the request.
            if (currentRequest.getState() == RequestState.IN_PROGRESS)
            {
                worker.getCitizenColonyHandler()
                  .getColonyOrRegister()
                  .getStatisticsManager()
                  .incrementBy(ITEMS_CRAFTED, currentRequest.getRequest().getCount(), worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
                job.finishRequest(true);
                worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
            }
            currentRequest = null;
            resetValues();
        }

        return super.afterDump();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    /**
     * Get the required progress to execute a recipe.
     *
     * @return the amount of hits required.
     */
    private int getRequiredProgressForMakingRawMaterial()
    {
        final int jobModifier = worker.getCitizenData().getCitizenSkillHandler().getLevel(((CraftingWorkerBuildingModule) getModuleForJob()).getCraftSpeedSkill()) / 2;
        return PROGRESS_MULTIPLIER / Math.min(jobModifier + 1, MAX_LEVEL) * HITTING_TIME;
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return currentRequest == null;
    }

    /**
     * get the LootContextBuilder for
     *
     * @return the LootContext to use for crafting
     */
    protected LootParams getLootContext()
    {
        return getLootContext(false);
    }

    /**
     * get the LootContextBuilder for
     *
     * @param includeKiller true for killer-based parameters
     * @return the LootContext to use for crafting
     */
    protected LootParams getLootContext(boolean includeKiller)
    {
        if (playerDamageSource == null)
        {
            playerDamageSource = world.damageSources().playerAttack(getFakePlayer());
        }

        LootParams.Builder builder = (new LootParams.Builder((ServerLevel) this.world))
                                       .withParameter(LootContextParams.ORIGIN, worker.position())
                                       .withParameter(LootContextParams.THIS_ENTITY, worker)
                                       .withParameter(LootContextParams.TOOL, worker.getMainHandItem())
                                       .withLuck((float) getEffectiveSkillLevel(getPrimarySkillLevel()));

        if (includeKiller)
        {
            builder = builder
                        .withParameter(LootContextParams.DAMAGE_SOURCE, playerDamageSource)
                        .withParameter(LootContextParams.KILLER_ENTITY, playerDamageSource.getEntity())
                        .withParameter(LootContextParams.DIRECT_KILLER_ENTITY, playerDamageSource.getDirectEntity());
        }

        return builder.create(RecipeStorage.recipeLootParameters);
    }
}
