package com.minecolonies.core.entity.ai.workers.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.*;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingAlchemist;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobAlchemist;
import com.minecolonies.core.network.messages.client.BlockParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.BAKER_HAS_NO_FURNACES_MESSAGE;

/**
 * Crafts brewing recipes.
 */
public class EntityAIWorkAlchemist extends AbstractEntityAICrafting<JobAlchemist, BuildingAlchemist>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 5;

    /**
     * Average delay to switch to netherwart harvesting.
     */
    private static final int DELAY_TO_HARVEST_NETHERWART = 30;

    /**
     * Average delay to switch to mistletoe harvesting.
     */
    private static final int DELAY_TO_HARVEST_MISTLETOE = 30;

    /**
     * BrewingStand to fuel
     */
    private BlockPos fuelPos = null;

    /**
     * State before we decided to fuel
     */
    private IAIState preFuelState = null;

    /**
     * Walking position.
     */
    private BlockPos walkTo;

    /**
     * Initialize the stone smeltery and add all his tasks.
     *
     * @param alchemistJob the job he has.
     */
    public EntityAIWorkAlchemist(@NotNull final JobAlchemist alchemistJob)
    {
        super(alchemistJob);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AIEventTarget(AIBlockingEventType.EVENT, this::isFuelNeeded, this::checkBrewingStandFuel, TICKS_SECOND * 10),
          new AIEventTarget(AIBlockingEventType.EVENT, this::accelerateBrewingStand, this::getState, TICKS_SECOND),
          new AITarget(START_USING_BREWINGSTAND, this::fillUpBrewingStand, TICKS_SECOND),
          new AITarget(RETRIEVING_END_PRODUCT_FROM_BREWINGSTAMD, this::retrieveBrewableFromBrewingStand, TICKS_SECOND),
          new AITarget(RETRIEVING_USED_FUEL_FROM_BREWINGSTAND, this::retrieveUsedFuel, TICKS_SECOND),
          new AITarget(ADD_FUEL_TO_BREWINGSTAND, this::addFuelToBrewingStand, TICKS_SECOND),
          new AITarget(HARVEST_MISTLETOE, this::harvestMistleToe, TICKS_SECOND),
          new AITarget(HARVEST_NETHERWART, this::harvestNetherWart, TICKS_SECOND)
        );
    }

    /**
     * Pick a random soil position and try to harvest/plant netherwart on it.
     *
     * @return next state to go to.
     */
    private IAIState harvestNetherWart()
    {
        if (walkTo == null)
        {
            final List<BlockPos> soilList = building.getAllSoilPositions();

            if (soilList.isEmpty())
            {
                return IDLE;
            }

            final BlockPos randomSoil = soilList.get(worker.getRandom().nextInt(soilList.size()));

            if (WorldUtil.isBlockLoaded(world, randomSoil))
            {
                if (world.getBlockState(randomSoil).getBlock() == Blocks.SOUL_SAND)
                {
                    if (world.getBlockState(randomSoil.above()).getBlock() == Blocks.NETHER_WART)
                    {
                        walkTo = randomSoil;
                        return HARVEST_NETHERWART;
                    }
                    else if (world.isEmptyBlock(randomSoil.above()))
                    {
                        if (!checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.NETHER_WART, 1), 16, 1))
                        {
                            return IDLE;
                        }
                    }
                    walkTo = randomSoil;
                }
                else
                {
                    building.removeSoilPosition(randomSoil);
                }
            }
            return HARVEST_NETHERWART;
        }

        if (WorldUtil.isBlockLoaded(world, walkTo) && world.getBlockState(walkTo).getBlock() == Blocks.SOUL_SAND)
        {
            if (walkToBlock(walkTo))
            {
                return HARVEST_NETHERWART;
            }

            final BlockState aboveState = world.getBlockState(walkTo.above());
            if (!(aboveState.getBlock() instanceof AirBlock))
            {
                if (aboveState.getBlock() == Blocks.NETHER_WART && aboveState.getValue(NetherWartBlock.AGE) < 2)
                {
                    walkTo = null;
                    return IDLE;
                }

                if (mineBlock(walkTo.above()))
                {
                    walkTo = null;
                    worker.decreaseSaturationForContinuousAction();
                    return IDLE;
                }
            }
            else
            {
                if (!checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.NETHER_WART, 1), 16, 1))
                {
                    walkTo = null;
                    return IDLE;
                }

                final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(Items.NETHER_WART);
                if (slot == -1)
                {
                    walkTo = null;
                    return IDLE;
                }

                world.setBlockAndUpdate(walkTo.above(), Blocks.NETHER_WART.defaultBlockState());
                worker.decreaseSaturationForContinuousAction();
                getInventory().extractItem(slot, 1, false);
                walkTo = null;
                return IDLE;
            }
        }
        else
        {
            walkTo = null;
            return IDLE;
        }

        return HARVEST_NETHERWART;
    }

    /**
     * Go to a random position with leaves and hit the leaves until getting a mistletoe.
     *
     * @return next state to go to.
     */
    private IAIState harvestMistleToe()
    {
        if (checkForToolOrWeapon(ModEquipmentTypes.shears.get()))
        {
            return IDLE;
        }

        if (walkTo == null)
        {
            final List<BlockPos> leaveList = building.getAllLeavePositions();

            if (leaveList.isEmpty())
            {
                return IDLE;
            }

            final BlockPos randomLeaf = leaveList.get(worker.getRandom().nextInt(leaveList.size()));
            if (WorldUtil.isBlockLoaded(world, randomLeaf))
            {
                if (world.getBlockState(randomLeaf).getBlock() instanceof LeavesBlock)
                {
                    walkTo = randomLeaf;
                }
                else
                {
                    building.removeLeafPosition(randomLeaf);
                }
            }
            return HARVEST_MISTLETOE;
        }

        if (WorldUtil.isBlockLoaded(world, walkTo) && world.getBlockState(walkTo).getBlock() instanceof LeavesBlock)
        {
            if (walkToBlock(walkTo))
            {
                return HARVEST_MISTLETOE;
            }

            final BlockState state = world.getBlockState(walkTo);

            final int slot =
              InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(worker.getInventoryCitizen(), ModEquipmentTypes.shears.get(), TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel());
            worker.getCitizenItemHandler().setHeldItem(InteractionHand.MAIN_HAND, slot);

            worker.swing(InteractionHand.MAIN_HAND);
            world.playSound(null,
              walkTo,
              state.getSoundType(world, walkTo, worker).getBreakSound(),
              SoundSource.BLOCKS,
              state.getSoundType(world, walkTo, worker).getVolume(),
              state.getSoundType(world, walkTo, worker).getPitch());
            Network.getNetwork().sendToTrackingEntity(new BlockParticleEffectMessage(walkTo, state, worker.getRandom().nextInt(7) - 1), worker);
            if (worker.getRandom().nextInt(40) <= 0)
            {
                worker.decreaseSaturationForContinuousAction();
                InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), new ItemStack(ModItems.mistletoe, 1));
                walkTo = null;
                worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);
                return INVENTORY_FULL;
            }
        }
        else
        {
            walkTo = null;
            return IDLE;
        }

        return HARVEST_MISTLETOE;
    }

    @Override
    protected IAIState decide()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        if (job.getTaskQueue().isEmpty() || job.getCurrentTask() == null)
        {
            if (worker.getNavigation().isDone())
            {
                if (worker.getRandom().nextInt(DELAY_TO_HARVEST_NETHERWART) <= 1)
                {
                    return HARVEST_NETHERWART;
                }

                if (worker.getRandom().nextInt(DELAY_TO_HARVEST_MISTLETOE) <= 1)
                {
                    return HARVEST_MISTLETOE;
                }

                if (building.isInBuilding(worker.blockPosition()))
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

    @Override
    public Class<BuildingAlchemist> getExpectedBuildingClass()
    {
        return BuildingAlchemist.class;
    }

    @Override
    protected int getExtendedCount(final ItemStack stack)
    {
        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.BREWING_STAND)
        {
            int count = 0;
            for (final BlockPos pos : building.getAllBrewingStandPositions())
            {
                if (WorldUtil.isBlockLoaded(world, pos))
                {
                    final BlockEntity entity = world.getBlockEntity(pos);
                    if (entity instanceof BrewingStandBlockEntity)
                    {
                        final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;

                        for (int slot = 0; slot < 4; slot++)
                        {
                            final ItemStack stackInSlot = brewingStand.getItem(slot);
                            if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, stackInSlot))
                            {
                                count += stackInSlot.getCount();
                            }
                        }
                    }
                    else
                    {
                        building.removeBrewingStand(pos);
                    }
                }
            }

            return count;
        }

        return 0;
    }

    @Override
    protected IAIState getRecipe()
    {
        final IRequest<? extends PublicCrafting> currentTask = job.getCurrentTask();

        if (currentTask == null)
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        job.setMaxCraftingCount(currentTask.getRequest().getCount());

        final BlockPos brewingStandPos = getPositionOfBrewingStandToRetrieveFrom();
        if (brewingStandPos != null)
        {
            currentRequest = currentTask;
            walkTo = brewingStandPos;
            return RETRIEVING_END_PRODUCT_FROM_BREWINGSTAMD;
        }

        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.BREWING_STAND)
        {
            for (final BlockPos pos : building.getAllBrewingStandPositions())
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandBlockEntity)
                {
                    final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                    if (brewingStand.brewTime > 0 || !isEmpty(brewingStand.getItem(INGREDIENT_SLOT)))
                    {
                        return CRAFT;
                    }
                }
                else
                {
                    building.removeBrewingStand(pos);
                }
            }
        }
        return super.getRecipe();
    }

    /**
     * Check to see how many brewingStands are still processing
     *
     * @return the count.
     */
    private int countOfBubblingBrewingStands()
    {
        int count = 0;
        final Level world = building.getColony().getWorld();
        for (final BlockPos pos : building.getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandBlockEntity)
                {
                    final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                    if (brewingStand.brewTime > 0)
                    {
                        count += 1;
                    }
                }
                else
                {
                    building.removeBrewingStand(pos);
                }
            }
        }
        return count;
    }

    /**
     * Actually accelerate the brewingStand
     */
    private boolean accelerateBrewingStand()
    {
        final int accelerationTicks = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getSecondarySkill()) / 10) * 2;
        final Level world = building.getColony().getWorld();
        for (final BlockPos pos : building.getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandBlockEntity)
                {
                    final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                    for (int i = 0; i < accelerationTicks; i++)
                    {
                        if (brewingStand.brewTime > 0)
                        {
                            BrewingStandBlockEntity.serverTick(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), brewingStand);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Quick check to see if there is a brewingStand that needs fuel
     */
    private boolean isFuelNeeded()
    {
        if (currentRecipeStorage == null || currentRecipeStorage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return false;
        }

        for (final BlockPos pos : building.getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (!(entity instanceof BrewingStandBlockEntity))
                {
                    building.removeBrewingStand(pos);
                    continue;
                }
                final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                if (brewingStand.brewTime <= 0 && (hasBrewableAndNoFuel(brewingStand) || hasNeitherFuelNorBrewable(brewingStand)))
                {
                    //We only want to return true if we're not already gathering materials.
                    return getState() != GATHERING_REQUIRED_MATERIALS;
                }
            }
        }
        return false;
    }

    /**
     * Check Fuel levels in the brewingStand
     */
    private IAIState checkBrewingStandFuel()
    {
        if (currentRecipeStorage == null || currentRecipeStorage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return getState();
        }

        final Level world = building.getColony().getWorld();

        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER)
              && !InventoryUtils.hasItemInProvider(building, Items.BLAZE_POWDER)
              && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(Stack.class)))
        {
            worker.getCitizenData().createRequestAsync(new Stack(new ItemStack(Items.BLAZE_POWDER), BREWING_MIN_FUEL_COUNT * building.getAllBrewingStandPositions().size(), 1));
            return getState();
        }

        for (final BlockPos pos : building.getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandBlockEntity)
                {
                    final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                    if (brewingStand.brewTime <= 0 && (hasBrewableAndNoFuel(brewingStand) || hasNeitherFuelNorBrewable(brewingStand)))
                    {
                        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER))
                        {
                            if (InventoryUtils.hasItemInProvider(building, Items.BLAZE_POWDER))
                            {
                                needsCurrently = new Tuple<>(item -> item.getItem() == Items.BLAZE_POWDER, BREWING_MIN_FUEL_COUNT);
                                walkTo = null;
                                return GATHERING_REQUIRED_MATERIALS;
                            }
                            //We need to wait for Fuel to arrive
                            return getState();
                        }

                        fuelPos = pos;
                        if (preFuelState == null)
                        {
                            preFuelState = getState();
                        }
                        return ADD_FUEL_TO_BREWINGSTAND;
                    }
                }
                else
                {
                    building.removeBrewingStand(pos);
                }
            }
        }
        return getState();
    }

    /**
     * Add brewing stand fuel when necessary
     *
     * @return
     */
    private IAIState addFuelToBrewingStand()
    {
        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER))
        {
            if (InventoryUtils.hasItemInProvider(building, Items.BLAZE_POWDER))
            {
                needsCurrently = new Tuple<>(item -> item.getItem() == Items.BLAZE_POWDER, STACKSIZE);
                return GATHERING_REQUIRED_MATERIALS;
            }
            //We shouldn't get here, unless something changed between the checkBrewingStandFuel and the addFueltoBrewingStand calls
            preFuelState = null;
            fuelPos = null;
            return START_WORKING;
        }

        if (fuelPos == null || walkToBlock(fuelPos))
        {
            return getState();
        }

        if (WorldUtil.isBlockLoaded(world, fuelPos))
        {
            final BlockEntity entity = world.getBlockEntity(fuelPos);
            if (entity instanceof BrewingStandBlockEntity)
            {
                final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                //Stoke the brewing stands
                if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER)
                      && (hasBrewableAndNoFuel(brewingStand) || hasNeitherFuelNorBrewable(brewingStand)))
                {
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                      worker.getInventoryCitizen(), item -> item.getItem() == Items.BLAZE_POWDER, BREWING_MIN_FUEL_COUNT,
                      new InvWrapper(brewingStand), BREWING_FUEL_SLOT);

                    if (preFuelState != null && preFuelState != ADD_FUEL_TO_BREWINGSTAND)
                    {
                        IAIState returnState = preFuelState;
                        preFuelState = null;
                        fuelPos = null;
                        return returnState;
                    }
                }
            }
        }

        //Fueling is confused, start over. 
        preFuelState = null;
        fuelPos = null;
        return START_WORKING;
    }

    private int getMaxUsableBrewingStands()
    {
        final int maxSkillBrewingStand = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getPrimarySkill()) / 10) + 1;
        return Math.min(maxSkillBrewingStand, building.getAllBrewingStandPositions().size());
    }

    /**
     * Get the brewingStand which has finished smeltables. For this check each brewingStand which has been registered to the building. Check if the brewingStand is turned off and has something in
     * the result slot or check if the brewingStand has more than x results.
     *
     * @return the position of the brewingStand.
     */
    private BlockPos getPositionOfBrewingStandToRetrieveFrom()
    {
        if (currentRecipeStorage == null || currentRecipeStorage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return null;
        }

        for (final BlockPos pos : building.getAllBrewingStandPositions())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof BrewingStandBlockEntity)
            {
                final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
                int countInResultSlot = 0;

                for (int slot = 0; slot < 3; slot++)
                {
                    if (!isEmpty(brewingStand.getItem(slot)) && ItemStackUtils.compareItemStacksIgnoreStackSize(currentRecipeStorage.getPrimaryOutput(),
                      brewingStand.getItem(slot)))
                    {
                        countInResultSlot = brewingStand.getItem(slot).getCount();
                    }
                }

                if (brewingStand.brewTime <= 0 && countInResultSlot > 0 && isEmpty(brewingStand.getItem(INGREDIENT_SLOT)))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    @Override
    protected IAIState checkForItems(@NotNull final IRecipeStorage storage)
    {
        if (storage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return super.checkForItems(storage);
        }

        final List<ItemStorage> input = storage.getCleanedInput();
        final int countInBewingStand = getExtendedCount(storage.getPrimaryOutput());
        int outputInInv =
          InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, storage.getPrimaryOutput()));

        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, inputStorage.getItemStack());
            int inputInBrewingStand = getExtendedCount(inputStorage.getItemStack());
            int inputInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate);

            if (countInBewingStand + inputInBrewingStand + inputInInv + outputInInv < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                if (InventoryUtils.hasItemInProvider(building, predicate))
                {
                    needsCurrently = new Tuple<>(predicate, inputStorage.getAmount() * (job.getMaxCraftingCount() - countInBewingStand - inputInBrewingStand));
                    return GATHERING_REQUIRED_MATERIALS;
                }
            }

            //if we don't have enough at all, cancel
            int countOfInput = inputInInv + InventoryUtils.getCountFromBuilding(building, predicate) + countInBewingStand + inputInBrewingStand + outputInInv;
            if (countOfInput < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                job.finishRequest(false);
                resetValues();
            }
        }

        return CRAFT;
    }

    /**
     * Retrieve ready bars from the brewingStand. If no position has been set return. Else navigate to the position of the brewingStand. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveBrewableFromBrewingStand()
    {
        if (walkTo == null || currentRequest == null)
        {
            return START_WORKING;
        }

        final BlockEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof BrewingStandBlockEntity))
        {
            walkTo = null;
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }
        walkTo = null;

        final int preExtractCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
          stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRequest.getRequest().getStack(), stack));

        for (int slot = 0; slot < 3; slot++)
        {
            if (!isEmpty(((BrewingStandBlockEntity) entity).getItem(slot)))
            {
                extractFromBrewingStandSlot((BrewingStandBlockEntity) entity, slot);
            }
        }

        //Do we have the requested item in the inventory now?
        final int resultCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
          stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRequest.getRequest().getStack(), stack)) - preExtractCount;

        if (resultCount > 0)
        {
            final ItemStack stack = currentRequest.getRequest().getStack().copy();
            stack.setCount(resultCount);
            currentRequest.addDelivery(stack);

            final int step = resultCount / currentRecipeStorage.getPrimaryOutput().getCount();

            job.setCraftCounter(job.getCraftCounter() + step);
            job.setProgress(job.getProgress() - step);
            if (job.getMaxCraftingCount() == 0)
            {
                job.setMaxCraftingCount(currentRequest.getRequest().getCount());
            }
            if (job.getCraftCounter() >= job.getMaxCraftingCount() && job.getProgress() <= 0)
            {
                job.finishRequest(true);
                resetValues();
                currentRecipeStorage = null;
                incrementActionsDoneAndDecSaturation();
                return INVENTORY_FULL;
            }
        }

        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Retrieve used fuel from the brewingStand. If no position has been set return. Else navigate to the position of the brewingStand. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveUsedFuel()
    {
        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        final BlockEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof BrewingStandBlockEntity) || (ItemStackUtils.isEmpty(((BrewingStandBlockEntity) entity).getItem(BREWING_FUEL_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        extractFromBrewingStandSlot((BrewingStandBlockEntity) entity, BREWING_FUEL_SLOT);
        return START_WORKING;
    }

    /**
     * Very simple action, straightly extract from a brewingStand slot.
     *
     * @param brewingStand the brewingStand to retrieve from.
     */
    private void extractFromBrewingStandSlot(final BrewingStandBlockEntity brewingStand, final int slot)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(new InvWrapper(brewingStand), slot, worker.getInventoryCitizen());
        if (slot <= 3 && slot >= 0)
        {
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }
    }

    /**
     * Checks if the brewingStand are ready to start smelting.
     *
     * @return START_USING_BREWINGSTAND if enough, else check for additional worker specific jobs.
     */
    private IAIState checkIfAbleToSmelt()
    {
        // We're fully committed currently, try again later.
        final int burning = countOfBubblingBrewingStands();
        if (burning > 0 && (burning >= getMaxUsableBrewingStands() || (job.getCraftCounter() + job.getProgress()) >= job.getMaxCraftingCount()))
        {
            setDelay(TICKS_SECOND);
            return getState();
        }

        for (final BlockPos pos : building.getAllBrewingStandPositions())
        {
            final BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof BrewingStandBlockEntity)
            {
                if (isEmpty(((BrewingStandBlockEntity) entity).getItem(INGREDIENT_SLOT)))
                {
                    walkTo = pos;
                    return START_USING_BREWINGSTAND;
                }
            }
            else
            {
                building.removeBrewingStand(pos);
            }
        }

        if (burning > 0)
        {
            setDelay(TICKS_SECOND);
        }

        return getState();
    }

    /**
     * Brew the ingredient after the required items are in the inv.
     *
     * @return the next state to go to.
     */
    private IAIState fillUpBrewingStand()
    {
        if (building.getAllBrewingStandPositions().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.BREWING_STAND)
        {
            walkTo = null;
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        final int burningCount = countOfBubblingBrewingStands();
        final BlockEntity entity = world.getBlockEntity(walkTo);
        if (entity instanceof BrewingStandBlockEntity && currentRecipeStorage != null)
        {
            final BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) entity;
            final int maxBrewingStands = getMaxUsableBrewingStands();
            final int resultInBrewingStand = getExtendedCount(currentRecipeStorage.getPrimaryOutput());
            final int resultInCitizenInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
              stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));

            if (isEmpty(((BrewingStandBlockEntity) entity).getItem(0)) || isEmpty(((BrewingStandBlockEntity) entity).getItem(1))
                  || isEmpty(((BrewingStandBlockEntity) entity).getItem(2)))
            {
                final ItemStack potionStack = currentRecipeStorage.getCleanedInput().get(1).getItemStack();

                final Predicate<ItemStack> potion = stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(potionStack, stack);

                final int potionInBrewingStand = getExtendedCount(potionStack);
                final int targetCount =
                  currentRequest.getRequest().getCount() * currentRecipeStorage.getPrimaryOutput().getCount() - potionInBrewingStand - resultInBrewingStand - resultInCitizenInv;
                if (targetCount <= 0)
                {
                    return START_WORKING;
                }

                final int amountOfPotionInBuilding = InventoryUtils.getCountFromBuilding(building, potion);
                final int amountOfPotionInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), potion);
                if (worker.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                {
                    worker.setItemInHand(InteractionHand.MAIN_HAND, potionStack.copy());
                }

                if (amountOfPotionInInv > 0)
                {
                    if (hasFuelAndNoBrewable(brewingStand) || hasNeitherFuelNorBrewable(brewingStand))
                    {
                        for (int slot = 0; slot < 3; slot++)
                        {
                            if (!isEmpty(((BrewingStandBlockEntity) entity).getItem(slot)))
                            {
                                continue;
                            }

                            int toTransfer = 0;
                            if (burningCount < maxBrewingStands)
                            {
                                toTransfer = 1;
                            }
                            if (toTransfer > 0)
                            {
                                if (walkToBlock(walkTo))
                                {
                                    return getState();
                                }
                                worker.getCitizenItemHandler().hitBlockWithToolInHand(walkTo);
                                InventoryUtils.transferXInItemHandlerIntoSlotInItemHandler(
                                  worker.getInventoryCitizen(),
                                  potion,
                                  toTransfer,
                                  new InvWrapper(brewingStand),
                                  slot);
                            }
                        }
                    }
                }
                else if (amountOfPotionInBuilding >= targetCount - amountOfPotionInInv && currentRecipeStorage.getIntermediate() == Blocks.BREWING_STAND)
                {
                    needsCurrently = new Tuple<>(potion, targetCount);
                    return GATHERING_REQUIRED_MATERIALS;
                }
                else
                {
                    //This is a safety net for the AI getting way out of sync with it's tracking. It shouldn't happen.
                    job.finishRequest(false);
                    resetValues();
                    walkTo = null;
                    return IDLE;
                }
            }
            else if (isEmpty(((BrewingStandBlockEntity) entity).getItem(INGREDIENT_SLOT)))
            {
                final ItemStack ingredientStack = currentRecipeStorage.getCleanedInput().get(0).getItemStack();
                final Predicate<ItemStack> ingredient = stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(ingredientStack, stack);
                final int ingredientInBrewingStand = getExtendedCount(ingredientStack);
                final int targetCount =
                  currentRequest.getRequest().getCount() * currentRecipeStorage.getPrimaryOutput().getCount() - ingredientInBrewingStand * 3 - resultInBrewingStand
                    - resultInCitizenInv;
                if (targetCount <= 0)
                {
                    return START_WORKING;
                }
                final int amountOfIngredientInBuilding = InventoryUtils.getCountFromBuilding(building, ingredient);
                final int amountOfIngredientInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), ingredient);
                if (worker.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                {
                    worker.setItemInHand(InteractionHand.MAIN_HAND, ingredientStack.copy());
                }

                if (amountOfIngredientInInv > 0)
                {
                    if (hasFuelAndNoBrewable(brewingStand) || hasNeitherFuelNorBrewable(brewingStand))
                    {
                        int toTransfer = 0;
                        if (burningCount < maxBrewingStands)
                        {
                            toTransfer = 1;
                        }
                        if (toTransfer > 0)
                        {
                            if (walkToBlock(walkTo))
                            {
                                return getState();
                            }
                            worker.getCitizenItemHandler().hitBlockWithToolInHand(walkTo);
                            InventoryUtils.transferXInItemHandlerIntoSlotInItemHandler(
                              worker.getInventoryCitizen(),
                              ingredient,
                              toTransfer,
                              new InvWrapper(brewingStand),
                              INGREDIENT_SLOT);
                        }
                    }
                }
                else if (amountOfIngredientInBuilding >= targetCount - amountOfIngredientInInv && currentRecipeStorage.getIntermediate() == Blocks.BREWING_STAND)
                {
                    needsCurrently = new Tuple<>(ingredient, targetCount);
                    return GATHERING_REQUIRED_MATERIALS;
                }
                else
                {
                    //This is a safety net for the AI getting way out of sync with it's tracking. It shouldn't happen.
                    job.finishRequest(false);
                    resetValues();
                    walkTo = null;
                    return IDLE;
                }
            }
        }
        else if (!(world.getBlockState(walkTo).getBlock() instanceof BrewingStandBlock))
        {
            building.removeBrewingStand(walkTo);
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    @Override
    protected IAIState craft()
    {
        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return getState();
        }

        if (currentRecipeStorage != null && currentRequest == null)
        {
            currentRequest = job.getCurrentTask();
        }

        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return super.craft();
        }

        if (building.getAllBrewingStandPositions().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        final BlockPos posOfOven = getPositionOfBrewingStandToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            return RETRIEVING_END_PRODUCT_FROM_BREWINGSTAMD;
        }

        // Safety net, should get caught removing things from the brewingStand.
        if (currentRequest != null && job.getMaxCraftingCount() > 0 && job.getCraftCounter() >= job.getMaxCraftingCount())
        {
            job.finishRequest(true);
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return INVENTORY_FULL;
        }

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return START_WORKING;
        }

        return checkIfAbleToSmelt();
    }
}
