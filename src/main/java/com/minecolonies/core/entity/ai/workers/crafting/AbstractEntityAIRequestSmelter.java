package com.minecolonies.core.entity.ai.workers.crafting;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.StatsUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.FurnaceUserModule;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.BAKER_HAS_NO_FURNACES_MESSAGE;
import static com.minecolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_FUEL;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEMS_SMELTED_DETAIL;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.FURNACE;

/**
 * Crafts furnace stone related block when needed.
 */
public abstract class AbstractEntityAIRequestSmelter<J extends AbstractJobCrafter<?, J>, B extends AbstractBuilding> extends AbstractEntityAICrafting<J, B>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 5;

    /**
     * Furnace position.
     */
    private BlockPos furnacePos = null;

    /**
     * Initialize the stone smeltery and add all his tasks.
     *
     * @param smelteryJob the job he has.
     */
    public AbstractEntityAIRequestSmelter(@NotNull final J smelteryJob)
    {
        super(smelteryJob);
        super.registerTargets(
            // Background tasks. Make sure there is enough fuel and accelerate furnaces based on speed.
            new AIEventTarget(AIBlockingEventType.EVENT, this::checkFurnaceFuel, this::getState, TICKS_SECOND * 10),
            new AIEventTarget(AIBlockingEventType.EVENT, this::accelerateFurnaces, this::getState, TICKS_SECOND),
            new AIEventTarget(AIBlockingEventType.EVENT, this::checkForLeftOvers, RETRIEVING_UNRELATED_PRODUCT_FROM_FURNACE, TICKS_SECOND * 10),

            // Additional AI tasks compared to normal crafting AI
            new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveProductFromFurnace, TICKS_SECOND),
            new AITarget(RETRIEVING_UNRELATED_PRODUCT_FROM_FURNACE, this::retrieveUnrelatedProductFromFurnace, TICKS_SECOND),
            new AITarget(FILL_UP_FURNACES, this::fillUpFurnace, TICKS_SECOND),
            new AITarget(ADD_FUEL_TO_FURNACE, this::addFuelToFurnace, TICKS_SECOND)
        );
    }

    /**
     * Check for left overs in the furnaces and clear out.
     * @return true if doing a clear out.
     */
    private boolean checkForLeftOvers()
    {
        final BlockPos checkPos = getFurnaceToRetrieveUnrelatedInputFrom();
        if (checkPos != null)
        {
            furnacePos = checkPos;
            return true;
        }
        return false;
    }

    /**
     * Check Fuel levels in the furnace
     */
    private boolean checkFurnaceFuel()
    {
        final List<ItemStack> possibleFuels = getActivePossibleFuels();
        final FurnaceUserModule module = building.getModule(FURNACE);
        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels))
            && InventoryUtils.hasBuildingEnoughElseCount(building, isCorrectFuel(possibleFuels), 1) < 1
            && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)))
        {
            worker.getCitizenData().createRequestAsync(new StackList(possibleFuels, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, STACKSIZE * module.getFurnaces().size(), 1));
        }

        return false;
    }

    /**
     * Actually accelerate the furnaces
     */
    private boolean accelerateFurnaces()
    {
        final int accelerationTicks = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getSecondarySkill()) / 10) * 2;
        final Level world = building.getColony().getWorld();
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity furnace)
                {
                    for (int i = 0; i < accelerationTicks; i++)
                    {
                        if (furnace.isLit())
                        {
                            FurnaceBlockEntity.serverTick(furnace.getLevel(), entity.getBlockPos(), entity.getBlockState(), furnace);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected int getExtendedCount(final ItemStack stack)
    {
        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            int count = 0;
            for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity furnaceBlockEntity)
                {
                    final ItemStack outputSlot = furnaceBlockEntity.getItem(SMELTABLE_SLOT);
                    final ItemStack resultSlot = furnaceBlockEntity.getItem(RESULT_SLOT);
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, outputSlot))
                    {
                        count += outputSlot.getCount();
                    }
                    else if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resultSlot))
                    {
                        count += resultSlot.getCount();
                    }
                }
            }

            return count;
        }

        return 0;
    }

    /**
     * Add furnace fuel when necessary
     * @return next AI state to transfer to.
     */
    private IAIState addFuelToFurnace()
    {
        final List<ItemStack> possibleFuels = getActivePossibleFuels();
        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels)))
        {
            if (InventoryUtils.hasBuildingEnoughElseCount(building, isCorrectFuel(possibleFuels), 1) >= 1)
            {
                needsCurrently = new Tuple<>(isCorrectFuel(possibleFuels), STACKSIZE);
                return GATHERING_REQUIRED_MATERIALS;
            }

            furnacePos = null;
            return IDLE;
        }

        if (furnacePos == null)
        {
            return IDLE;
        }

        if (!walkToWorkPos(furnacePos))
        {
            return getState();
        }

        if (WorldUtil.isBlockLoaded(world, furnacePos))
        {
            final BlockEntity entity = world.getBlockEntity(furnacePos);
            if (entity instanceof FurnaceBlockEntity furnace)
            {
                if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels)) && isEmpty(furnace.getItem(FUEL_SLOT)))
                {
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                      worker.getInventoryCitizen(), isCorrectFuel(possibleFuels), STACKSIZE,
                      new InvWrapper(furnace), FUEL_SLOT);
                }
            }
        }

        furnacePos = null;
        return IDLE;
    }

    /**
     * Retrieve ready products from the furnaces. If no position has been set return. Else navigate to the position of the furnace. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveProductFromFurnace()
    {
        if (furnacePos == null || currentRecipeStorage == null || currentRequest == null)
        {
            return START_WORKING;
        }

        final BlockEntity entity = world.getBlockEntity(furnacePos);
        if (!(entity instanceof FurnaceBlockEntity furnaceBlockEntity) || isEmpty(furnaceBlockEntity.getItem(RESULT_SLOT)))
        {
            furnacePos = null;
            return START_WORKING;
        }

        if (!walkToWorkPos(furnacePos))
        {
            return getState();
        }
        furnacePos = null;

        final ItemStack stack = furnaceBlockEntity.getItem(RESULT_SLOT).copy();
        if (extractFromFurnaceSlot(furnaceBlockEntity, RESULT_SLOT))
        {
            final int count = stack.getCount();
            if (count > 0)
            {
                if (ItemStackUtils.compareItemStacksIgnoreStackSize(currentRecipeStorage.getPrimaryOutput(), stack))
                {
                    final ItemStack requestStack = currentRequest.getRequest().getStack().copy();
                    requestStack.setCount(count);
                    currentRequest.addDelivery(requestStack);

                    job.setCraftCounter(job.getCraftCounter() + count);
                    if (job.getMaxCraftingCount() == 0)
                    {
                        job.setMaxCraftingCount(currentRequest.getRequest().getCount());
                    }
                    if (job.getCraftCounter() >= job.getMaxCraftingCount())
                    {
                        return finalizeCraftingTask();
                    }
                }
                else
                {
                    job.getSecondaryOutputs().addTo(new ItemStorage(stack), stack.getCount());
                }
            }

        }
        return START_WORKING;
    }

    /**
     * Retrieve ready products from the furnaces. If no position has been set return. Else navigate to the position of the furnace. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveUnrelatedProductFromFurnace()
    {
        if (furnacePos == null)
        {
            return START_WORKING;
        }

        final BlockEntity entity = world.getBlockEntity(furnacePos);
        if (!(entity instanceof FurnaceBlockEntity furnaceBlockEntity)
            || isEmpty(furnaceBlockEntity.getItem(SMELTABLE_SLOT))
            || (currentRecipeStorage != null
            && ItemStackUtils.compareItemStacksIgnoreStackSize(currentRecipeStorage.getCleanedInput().get(0).getItemStack(), furnaceBlockEntity.getItem(SMELTABLE_SLOT))))
        {
            furnacePos = null;
            return START_WORKING;
        }

        if (!walkToWorkPos(furnacePos))
        {
            return getState();
        }
        furnacePos = null;

        final ItemStack stack = furnaceBlockEntity.getItem(SMELTABLE_SLOT);
        if (extractFromFurnaceSlot(furnaceBlockEntity, SMELTABLE_SLOT))
        {
            final int count = stack.getCount();
            if (count > 0)
            {
                job.getSecondaryOutputs().addTo(new ItemStorage(stack), stack.getCount());
                return INVENTORY_FULL;
            }

        }
        return START_WORKING;
    }

    /**
     * Smelt the smeltable after the required items are in the inv.
     *
     * @return the next state to go to.
     */
    private IAIState fillUpFurnace()
    {
        if (furnacePos == null || currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        if (job.getMaxCraftingCount() == 0)
        {
            job.setMaxCraftingCount(currentRequest.getRequest().getCount());
        }

        final ItemStack inputStack = currentRecipeStorage.getCleanedInput().get(0).getItemStack();
        final Predicate<ItemStack> smeltablePredicate = stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(inputStack, stack);
        final int smeltableInInventory = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
            stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, inputStack));

        final int smeltableInFurnaces = getExtendedCount(inputStack);
        final int resultInFurnaces = getExtendedCount(currentRecipeStorage.getPrimaryOutput());
        final int targetCount = (job.getMaxCraftingCount() - job.getCraftCounter()) - smeltableInFurnaces - resultInFurnaces - smeltableInInventory;

        if ((job.getMaxCraftingCount() - job.getCraftCounter()) - smeltableInFurnaces - resultInFurnaces <= 0)
        {
            return START_WORKING;
        }

        if (smeltableInInventory == 0)
        {
            needsCurrently = new Tuple<>(smeltablePredicate, targetCount);
            furnacePos = null;
            return GATHERING_REQUIRED_MATERIALS;
        }

        final int burningFurnaces = countOfBurningFurnaces();
        final int maxFurnaces = getMaxUsableFurnaces();
        if (burningFurnaces > maxFurnaces)
        {
            return START_WORKING;
        }

        if (!walkToWorkPos(furnacePos))
        {
            return getState();
        }

        final int pendingCount = (job.getMaxCraftingCount() - job.getCraftCounter()) - smeltableInFurnaces - resultInFurnaces;
        if (pendingCount <= 0)
        {
            return START_WORKING;
        }

        final int availableSmeltable = Math.min(pendingCount, smeltableInInventory);

        final BlockEntity entity = world.getBlockEntity(furnacePos);
        furnacePos = null;
        if (entity instanceof FurnaceBlockEntity furnace)
        {
            if (worker.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
            {
                worker.setItemInHand(InteractionHand.MAIN_HAND, inputStack.copy());
            }

            if (hasFuelInFurnaceAndNoSmeltable(furnace))
            {
                int toTransfer = 0;
                int availableFurnaces = maxFurnaces - burningFurnaces;
                if (availableFurnaces >= 1)
                {
                    if (availableSmeltable > STACKSIZE * availableFurnaces)
                    {
                        toTransfer = STACKSIZE;
                    }
                    else if (availableFurnaces == 1)
                    {
                        toTransfer = availableSmeltable;
                    }
                    else
                    {
                        //We need to split stacks and spread them across furnaces for best performance
                        //We will front-load the remainder
                        toTransfer = Math.min((availableSmeltable / availableFurnaces) + (availableSmeltable % availableFurnaces), STACKSIZE);
                    }
                }
                if (toTransfer > 0)
                {
                    CitizenItemUtils.hitBlockWithToolInHand(worker, furnacePos);
                    InventoryUtils.transferXInItemHandlerIntoSlotInItemHandler(
                      worker.getInventoryCitizen(),
                      smeltablePredicate,
                      toTransfer,
                      new InvWrapper(furnace),
                      SMELTABLE_SLOT);
                }
            }
        }

        return CRAFT;
    }

    @Override
    public IAIState executeCraftingAction(final int toolSlot)
    {
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        if (currentRecipeStorage.getIntermediate() != Blocks.FURNACE)
        {
            return super.executeCraftingAction(toolSlot);
        }

        if (building.getModule(FURNACE).getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                    .triggerInteraction(new StandardInteraction(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
            return START_WORKING;
        }

        if (!areFurnacesLoaded())
        {
            return START_WORKING;
        }

        final List<ItemStack> possibleFuels = getAllowedFuel();
        if (possibleFuels.isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_FUEL), ChatPriority.BLOCKING));
            }
            return getState();
        }

        furnacePos = getFurnaceToRetrieveOutputFrom();
        if (furnacePos != null)
        {
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        furnacePos = getFurnaceToRetrieveUnrelatedInputFrom();
        if (furnacePos != null)
        {
            return RETRIEVING_UNRELATED_PRODUCT_FROM_FURNACE;
        }

        if (InventoryUtils.hasBuildingEnoughElseCount(building, isCorrectFuel(possibleFuels), 1) > 1 || InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels)))
        {
            furnacePos = getFurnaceWithoutFuel();
            if (furnacePos != null)
            {
                return ADD_FUEL_TO_FURNACE;
            }
        }

        final int burning = countOfBurningFurnaces();
        if (burning > 0 && burning >= getMaxUsableFurnaces())
        {
            setDelay(TICKS_SECOND);
            return getState();
        }

        furnacePos = getEmptyFurnaceWithFuel();
        if (furnacePos != null)
        {
            return FILL_UP_FURNACES;
        }

        return START_WORKING;
    }

    // --------------------- Utility Methods --------------------- //

    /**
     * Get a furnace with fuel but without smeltable.
     * @return the block pos of it.
     */
    private BlockPos getEmptyFurnaceWithFuel()
    {
        final FurnaceUserModule module = building.getModule(FURNACE);
        for (final BlockPos pos : module.getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity furnaceBlockEntity)
            {
                if (isEmpty(furnaceBlockEntity.getItem(SMELTABLE_SLOT))
                    && isEmpty(furnaceBlockEntity.getItem(RESULT_SLOT))
                    && !isEmpty(furnaceBlockEntity.getItem(FUEL_SLOT)))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Check for furnaces with missing fuel.
     * @return the pos of the first without fuel.
     */
    private BlockPos getFurnaceWithoutFuel()
    {
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity furnace && !furnace.isLit() && furnace.getItem(FUEL_SLOT).isEmpty())
            {
                return pos;
            }
        }
        return null;
    }


    /**
     * Get the furnace which has finished smeltables. For this check each furnace which has been registered to the building. Check if the furnace is turned off and has something in
     * the result slot or check if the furnace has more than x results.
     *
     * @return the position of the furnace.
     */
    private BlockPos getFurnaceToRetrieveOutputFrom()
    {
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity furnace && (!furnace.isLit() || furnace.getItem(SMELTABLE_SLOT).isEmpty()))
            {
                int countInResultSlot = 0;
                if (!isEmpty(furnace.getItem(RESULT_SLOT)))
                {
                    countInResultSlot = furnace.getItem(RESULT_SLOT).getCount();
                    if (countInResultSlot >= furnace.getItem(RESULT_SLOT).getMaxStackSize())
                    {
                        return pos;
                    }
                }

                if (countInResultSlot > 0)
                {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Check furnaces for unrelated inputs to our crafting tasks.
     * @return the furnace pos with waste input in it.
     */
    private BlockPos getFurnaceToRetrieveUnrelatedInputFrom()
    {
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity furnace)
            {
                if (!furnace.getItem(SMELTABLE_SLOT).isEmpty()
                    && (currentRecipeStorage == null
                    || !ItemStackUtils.compareItemStacksIgnoreStackSize(furnace.getItem(SMELTABLE_SLOT), currentRecipeStorage.getCleanedInput().get(0).getItemStack())))
                {
                    return pos;
                }
            }
            else
            {
                building.getModule(FURNACE).removeFromFurnaces(pos);
                return null;
            }
        }
        return null;
    }

    /**
     * Very simple action, straightly extract from a furnace slot.
     *
     * @param furnace the furnace to retrieve from.
     */
    private boolean extractFromFurnaceSlot(final FurnaceBlockEntity furnace, final int slot)
    {
        ItemStack stack = furnace.getItem(slot);
        final Component name = stack.getHoverName();
        final int count = stack.getCount();
        if (stack.isEmpty())
        {
            return false;
        }
        final boolean success = InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(new InvWrapper(furnace), slot, worker.getInventoryCitizen());
        if (slot == RESULT_SLOT && success)
        {
            recordSmeltingBuildingStats(name, count);
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }
        return success;
    }

    /**
     * Check if all furnace positions are loaded.
     * @return true if so.
     */
    private boolean areFurnacesLoaded()
    {
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            if (!WorldUtil.isBlockLoaded(world, pos))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check to see how many furnaces are still processing
     *
     * @return the count.
     */
    private int countOfBurningFurnaces()
    {
        int count = 0;
        final Level world = building.getColony().getWorld();
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity furnace)
                {
                    if (furnace.isLit() && !furnace.getItem(SMELTABLE_SLOT).isEmpty())
                    {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Max number of furnaces the worker can use in parallel.
     * @return the number.
     */
    private int getMaxUsableFurnaces()
    {
        final int maxSkillFurnaces = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getPrimarySkill()) / 10) + 1;

        int count = 0;
        for (final BlockPos pos : building.getModule(FURNACE).getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity furnace && !furnace.getItem(FUEL_SLOT).isEmpty())
            {
                count++;
            }
        }

        return Math.min(maxSkillFurnaces, count);
    }

    /**
     * Get a copy of the list of allowed fuel.
     *
     * @return the list.
     */
    private List<ItemStack> getAllowedFuel()
    {
        final List<ItemStack> list = new ArrayList<>();
        for (final ItemStorage storage : building.getModule(BuildingModules.ITEMLIST_FUEL).getList())
        {
            final ItemStack stack = storage.getItemStack().copy();
            stack.setCount(stack.getMaxStackSize());
            list.add(stack);
        }
        return list;
    }

    /**
     * Get the list of possible fuels, adjusted for any inputs/outputs of the current recipe to avoid interference
     */
    private List<ItemStack> getActivePossibleFuels()
    {
        final List<ItemStack> possibleFuels = getAllowedFuel();
        if (possibleFuels.isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_FUEL), ChatPriority.IMPORTANT));
            }
            return ImmutableList.of();
        }

        if (currentRecipeStorage != null)
        {
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));
            // There is always only one input.
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getCleanedInput().get(0).getItemStack()));
        }
        return possibleFuels;
    }

    /**
     * Predicate for checking fuel in inventories
     */
    private static Predicate<ItemStack> isCorrectFuel(final List<ItemStack> possibleFuels)
    {
        return item -> ItemStackUtils.compareItemStackListIgnoreStackSize(possibleFuels, item);
    }

    /**
     * Returns the name of the smelting stat that is used in the building's statistics.
     * Override this in your subclass to change the description of the smelting stat.
     * @return the name of the smelting stat.
     */
    protected String getSmeltingStatName()
    {
        return ITEMS_SMELTED_DETAIL;
    }

    /**
     * Records the smelting request in the building's statistics.
     * Override this in your subclass to change the description of the smelting stat.
     *
     * @param hoverName the stack name.
     * @param count the stack count.
     */
    protected void recordSmeltingBuildingStats(final Component hoverName, final int count)
    {
        StatsUtil.trackStatByName(building, getSmeltingStatName(), hoverName, count);
    }
}
