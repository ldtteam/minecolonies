package com.minecolonies.coremod.entity.ai.basic;

import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.StructurePlacementUtils;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.entity.ai.util.StructureIterator;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.BLOCK_PLACE_SPEED;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.Suppression.MULTIPLE_LOOPS_OVER_THE_SAME_SET_SHOULD_BE_COMBINED;

/**
 * This base ai class is used by ai's who need to build entire structures.
 * These structures have to be supplied as schematics files.
 * <p>
 * Once an ai starts building a structure, control over it is only given back once that is done.
 * <p>
 * If the ai resets, the structure is gone,
 * so just restart building and no progress will be reset.
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIStructure<J extends AbstractJobStructure> extends AbstractEntityAIInteract<J>
{
    /**
     * String which shows if something is a waypoint.
     */
    public static final String WAYPOINT_STRING = "infrastructure";

    /**
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally).
     */
    private static final double XP_EACH_BUILDING = 10.0D;

    /**
     * Amount of xp the builder gains for placing a block.
     */
    private static final double XP_EACH_BLOCK = 0.1D;

    /**
     * Increase this value to make the building speed slower.
     * Used to balance worker level speed increase.
     */
    private static final int PROGRESS_MULTIPLIER = 10;

    /**
     * Speed the builder should run away when he castles himself in.
     */
    private static final double RUN_AWAY_SPEED = 4.1D;

    /**
     * The minimum range to keep from the current building place.
     */
    private static final int MIN_ADDITIONAL_RANGE_TO_BUILD = 3;

    /**
     * The amount of ticks to wait when not needing any tools to break blocks.
     */
    private static final int UNLIMITED_RESOURCES_TIMEOUT = 5;

    /**
     * The standard range the builder should reach until his target.
     */
    private static final int STANDARD_WORKING_RANGE = 5;

    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int MIN_WORKING_RANGE = 12;

    /**
     * The current structure task to be build.
     */
    protected StructureIterator currentStructure;

    /**
     * Position where the Builders constructs from.
     */
    protected BlockPos workFrom;

    /**
     * The rotation of the current build.
     */
    private int rotation = 0;

    /**
     * Creates this ai base class and set's up important things.
     * <p>
     * Always use this constructor!
     *
     * @param job the job class of the ai using this base class.
     */
    protected AbstractEntityAIStructure(@NotNull final J job)
    {
        super(job);
        this.registerTargets(

          /*
           * Pick up stuff which might've been
           */
          new AITarget(PICK_UP_RESIDUALS, this::pickUpResiduals, TICKS_SECOND),
          /*
           * Check if tasks should be executed.
           */
          new AIEventTarget(AIBlockingEventType.STATE_BLOCKING, this::checkIfCanceled, IDLE, 1),
          /*
           * Select the appropriate State to do next.
           */
          new AITarget(START_BUILDING, this::startBuilding, 1),
          /*
           * Check if we have to build something.
           */
          new AITarget(IDLE, this::isThereAStructureToBuild, () -> START_BUILDING, 100),
          /*
           * Clean up area completely.
           */
          new AITarget(REMOVE_STEP, generateStructureGenerator(this::clearStep, COMPLETE_BUILD), STANDARD_DELAY),
          /*
           * Clear out the building area.
           */
          new AITarget(CLEAR_STEP, generateStructureGenerator(this::clearStep, BUILDING_STEP), STANDARD_DELAY),
          /*
           * Build the structure and foundation of the building.
           */
          new AITarget(BUILDING_STEP, generateStructureGenerator(this::structureStep, FLUID_DETECT_STEP), STANDARD_DELAY),
          /*
           * Detect fluids that aren't part of the structure.
           */
          new AITarget(FLUID_DETECT_STEP, generateStructureGenerator(this::fluidDetectStep, FLUID_REMOVE_STEP), 1),
          /*
           * Remove fluids that aren't part of the structure.
           */
          new AITarget(FLUID_REMOVE_STEP, this::fluidRemoveStep, 1),
          /*
           * Spawn entities on the structure.
           */
          new AITarget(SPAWN_STEP, generateStructureGenerator(this::addEntity, DECORATION_STEP), STANDARD_DELAY),
          /*
           * Decorate the AbstractBuilding with torches etc.
           */
          new AITarget(DECORATION_STEP, generateStructureGenerator(this::decorationStep, COMPLETE_BUILD), STANDARD_DELAY),
          /*
           * Finalize the building and give back control to the ai.
           */
          new AITarget(COMPLETE_BUILD, this::completeBuild, STANDARD_DELAY)
        );
    }

    @Override
    public Class<AbstractBuildingStructureBuilder> getExpectedBuildingClass()
    {
        return AbstractBuildingStructureBuilder.class;
    }

    /**
     * Generate a function that will iterate over a structure.
     * <p>
     * It will pass the current block (with all infos) to the evaluation function.
     *
     * @param evaluationFunction the function to be called each block.
     * @param nextState          the next state to change to once done iterating.
     * @return the new state this AI will be in after one pass.
     */
    private Supplier<IAIState> generateStructureGenerator(@NotNull final Function<StructureIterator.StructureBlock, Boolean> evaluationFunction, @NotNull final IAIState nextState)
    {
        //do not replace with method reference, this one stays the same on changing reference for currentStructure
        //URGENT: DO NOT REPLACE FOR ANY MEANS THIS WILL CRASH THE GAME.
        @NotNull final Supplier<StructureIterator.StructureBlock> getCurrentBlock = () -> currentStructure.getCurrentBlock();
        @NotNull final Supplier<StructureIterator.Result> advanceBlock = () -> currentStructure.advanceBlock(this);

        return () ->
        {
            final StructureIterator.StructureBlock currentBlock = getCurrentBlock.get();
            /*
            check if we have not found a block (when block == null
            if we have a block, apply the eval function
            (which changes stuff, so only execute on valid block!)
            */
            if (currentBlock.block == null
                  || evaluationFunction.apply(currentBlock))
            {
                final StructureIterator.Result result = advanceBlock.get();
                storeProgressPos(currentStructure.getLocalBlockPosition(), currentStructure.getStage());
                if (result == StructureIterator.Result.AT_END)
                {
                    return switchStage(nextState);
                }
                if (result == StructureIterator.Result.CONFIG_LIMIT)
                {
                    return getState();
                }
            }
            return getState();
        };
    }

    /**
     * Store the progressPos in the building if possible for the worker.
     * @param blockPos the progressResult.
     * @param stage the current stage.
     */
    public void storeProgressPos(final BlockPos blockPos, final StructureIterator.Stage stage)
    {
        /*
         * Override if needed.
         */
    }

    /**
     * Get the last progress.
     * @return the blockPos or null.
     */
    @Nullable
    public Tuple<BlockPos, StructureIterator.Stage> getProgressPos()
    {
        return null;
    }

    /**
     * Switches the structures stage after the current one has been completed.
     * @param state the current state.
     * @return the next stage.
     */
    public IAIState switchStage(final IAIState state)
    {
        if (state.equals(REMOVE_STEP))
        {
            currentStructure.setStage(StructureIterator.Stage.REMOVE);
        }
        else if (state.equals(BUILDING_STEP))
        {
            currentStructure.setStage(StructureIterator.Stage.BUILD);
        }
        else if (state.equals(FLUID_DETECT_STEP))
        {
            currentStructure.setStage(StructureIterator.Stage.FLUID_DETECT);
        }
        else if (state.equals(DECORATION_STEP))
        {
            currentStructure.setStage(StructureIterator.Stage.DECORATE);
        }
        else if (state.equals(SPAWN_STEP))
        {
            currentStructure.setStage(StructureIterator.Stage.SPAWN);
        }
        else if (state.equals(COMPLETE_BUILD))
        {
            currentStructure.setStage(StructureIterator.Stage.COMPLETE);
        }
        return state;
    }

    private IAIState pickUpResiduals()
    {
        if (currentStructure.getStage() != StructureIterator.Stage.COMPLETE)
        {
            return IDLE;
        }

        if (getItemsForPickUp() == null)
        {
            fillItemsList();
        }

        if (getItemsForPickUp() != null && !getItemsForPickUp().isEmpty())
        {
            gatherItems();
            return getState();
        }
        resetGatheringItems();
        workFrom = null;
        currentStructure = null;

        return IDLE;
    }

    /**
     * Fill the list of the item positions to gather.
     */
    @Override
    public void fillItemsList()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.gathering"));

        if (currentStructure == null)
        {
            return;
        }

        final BlockPos centerPos = currentStructure.getCenter();
        if (centerPos.getY() == 0)
        {
            return;
        }

        searchForItems(new AxisAlignedBB(centerPos).expand(currentStructure.getLength() / 2.0, currentStructure.getHeight(), currentStructure.getWidth()));
    }

    private IAIState completeBuild()
    {
        storeProgressPos(null, StructureIterator.Stage.CLEAR);
        incrementActionsDoneAndDecSaturation();
        if (job instanceof AbstractJobStructure)
        {
            executeSpecificCompleteActions();
            worker.getCitizenExperienceHandler().addExperience(XP_EACH_BUILDING);
        }

        return PICK_UP_RESIDUALS;
    }

    /**
     * Execute specific actions on loading a structure.
     */
    protected abstract void executeSpecificCompleteActions();

    private Boolean decorationStep(final StructureIterator.StructureBlock structureBlock)
    {
        checkForExtraBuildingActions();
        if (!(structureBlock.worldBlock instanceof IBuilderUndestroyable) || structureBlock.worldBlock == Blocks.BEDROCK)
        {
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.decorating"));

            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (!walkToConstructionSite(currentStructure.getCurrentBlockPosition()))
            {
                return false;
            }

            if (structureBlock.block == null
                  || structureBlock.doesStructureBlockEqualWorldBlock()
                  || structureBlock.metadata.getMaterial().isSolid())
            {
                //findNextBlock count was reached and we can ignore this block
                return true;
            }

            WorkerUtil.faceBlock(structureBlock.blockPosition, worker);

            @Nullable final BlockState blockState = structureBlock.metadata;
            //We need to deal with materials
            return placeBlockAt(blockState, structureBlock.blockPosition);
        }
        return true;
    }

    /**
     * Walk to the current construction site.
     * <p>
     * Calculates and caches the position where to walk to.
     * @param currentBlock the current block it is working on.
     * @return true while walking to the site.
     */
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        if (workFrom == null)
        {
            workFrom = getWorkingPosition(currentBlock);
        }

        //The miner shouldn't search for a save position. Just let him build from where he currently is.
        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < MIN_WORKING_RANGE;
    }

    /**
     * Specific actions to execute when building over a block.
     *
     * @param pos the position to build at.
     */
    private void handleBuildingOverBlock(@NotNull final BlockPos pos)
    {
        final List<ItemStack> items = BlockPosUtil.getBlockDrops(world, pos, 0, worker.getHeldItemMainhand());
        for (final ItemStack item : items)
        {
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, worker.getInventoryCitizen());
        }
    }

    private boolean placeBlockAt(@NotNull final BlockState blockState, @NotNull final BlockPos coords)
    {
        if (blockState.getBlock() instanceof GrassPathBlock)
        {
            holdEfficientTool(blockState.getBlock(), coords);
        }

        final ItemStack item = BlockUtils.getItemStackFromBlockState(blockState);
        worker.setItemStackToSlot(EquipmentSlotType.MAINHAND, item == null ? ItemStackUtils.EMPTY : item);

        final BlockState decrease;
        for (final IPlacementHandler handlers : PlacementHandlers.handlers)
        {
            if (handlers.canHandle(world, coords, blockState))
            {
                boolean sameInWorld = false;
                if (world.getBlockState(coords).getBlock() == blockState.getBlock())
                {
                    sameInWorld = true;
                }
                if (!sameInWorld && !MineColonies.getConfig().getCommon().builderInfiniteResources.get())
                {
                    final List<ItemStack> requiredItems = handlers.getRequiredItems(world, coords, blockState, job.getStructure().getTileEntityData(job.getStructure().getLocalPosition()), false);

                    final List<ItemStack> itemList = new ArrayList<>();
                    for (final ItemStack stack : requiredItems)
                    {
                        for (final ToolType toolType : ToolType.values())
                        {
                            if (ItemStackUtils.isTool(stack, toolType))
                            {
                                if (checkForToolOrWeapon(toolType))
                                {
                                    return false;
                                }
                            }
                        }
                        itemList.add(this.getTotalAmount(stack));
                    }

                    if (checkForListInInvAndRequest(this, itemList, itemList.size() > 1))
                    {
                        return false;
                    }
                }


                final BlockState worldState = world.getBlockState(coords);

                if (!sameInWorld
                      && worldState.getMaterial() != Material.AIR
                      && !(worldState.getBlock() instanceof DoublePlantBlock && worldState.get(DoublePlantBlock.HALF).equals(DoubleBlockHalf.UPPER)))
                {
                    handleBuildingOverBlock(coords);
                    world.removeBlock(coords, false);
                }

                final Object result = handlers.handle(world, coords, blockState, job.getStructure().getTileEntityData(job.getStructure().getLocalPosition()), false, job.getWorkOrder().getBuildingLocation(), job.getStructure().getSettings());
                if (result instanceof IPlacementHandler.ActionProcessingResult)
                {
                    if (result == IPlacementHandler.ActionProcessingResult.ACCEPT)
                    {
                        return true;
                    }

                    if (result == IPlacementHandler.ActionProcessingResult.DENY)
                    {
                        return false;
                    }
                    continue;
                }

                if (result instanceof BlockState)
                {
                    decrease = (BlockState) result;
                    if (!sameInWorld)
                    {
                        decreaseInventory(coords, decrease.getBlock(), decrease);
                    }
                    connectBlockToBuildingIfNecessary(decrease, coords);
                    worker.swingArm(worker.getActiveHand());
                    worker.getCitizenExperienceHandler().addExperience(XP_EACH_BLOCK);
                    worker.decreaseSaturationForContinuousAction();
                    return true;
                }

                if (result instanceof ItemStack)
                {
                    final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), s -> s.isItemEqual((ItemStack) result));
                    if (slot != -1)
                    {
                        final ItemStack itemStack = worker.getInventoryCitizen().getStackInSlot(slot);
                        worker.getInventoryCitizen().getStackInSlot(slot);
                        worker.setItemStackToSlot(EquipmentSlotType.MAINHAND, itemStack);
                        worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
                    }
                }
            }
        }

        Log.getLogger().warn("Couldn't handle block: " + blockState.getBlock().getTranslationKey());
        return true;
    }

    /**
     * Check the placers inventory for the items in the itemList and remove it of the list if found.
     *
     * @param placer   the placer.
     * @param itemList the list to check.
     * @param force if force insertion.
     * @return true if need to request.
     */
    public static boolean checkForListInInvAndRequest(@NotNull final AbstractEntityAIStructure<?> placer, final List<ItemStack> itemList, final boolean force)
    {
        final List<ItemStack> foundStacks = InventoryUtils.filterItemHandler(placer.getWorker().getInventoryCitizen(),
          itemStack -> itemList.stream().anyMatch(targetStack -> targetStack.isItemEqual(itemStack)));
        if (force)
        {
            for (final ItemStack foundStack : new ArrayList<>(foundStacks))
            {
                final Optional<ItemStack> opt = itemList.stream().filter(targetStack -> targetStack.isItemEqual(foundStack)).findFirst();
                if (opt.isPresent())
                {
                    final ItemStack stack = opt.get();
                    itemList.remove(stack);
                    if (stack.getCount() > foundStack.getCount())
                    {
                        stack.setCount(stack.getCount() - foundStack.getCount());
                        itemList.add(stack);
                    }
                }
            }
        }
        else
        {
            itemList.removeIf(itemStack -> ItemStackUtils.isEmpty(itemStack) || foundStacks.stream().anyMatch(target -> target.isItemEqual(itemStack)));
        }
        itemList.removeIf(itemstack -> itemstack.getItem() instanceof BlockItem && isBlockFree(((BlockItem) itemstack.getItem()).getBlock()));

        final Map<ItemStorage, Integer> list = new HashMap<>();
        for (final ItemStack stack : itemList)
        {
            ItemStorage tempStorage = new ItemStorage(stack.copy());
            if (list.containsKey(tempStorage))
            {
                final int oldSize = list.get(tempStorage);
                tempStorage.setAmount(tempStorage.getAmount() + oldSize);
            }
            list.put(tempStorage, tempStorage.getAmount());
        }

        for (final Map.Entry<ItemStorage, Integer> placedStack : list.entrySet())
        {
            if (ItemStackUtils.isEmpty(placedStack.getKey().getItemStack()))
            {
                return true;
            }

            if (placer.getOwnBuilding()
                  .getOpenRequestsOfTypeFiltered(
                    placer.getWorker().getCitizenData(),
                    TypeConstants.DELIVERABLE,
                    (IRequest<? extends IDeliverable> r) -> r.getRequest().matches(placedStack.getKey().getItemStack()))
                  .isEmpty())
            {
                final Stack stackRequest = new Stack(placedStack.getKey().getItemStack());
                stackRequest.setCount(placedStack.getValue());
                placer.getWorker().getCitizenData().createRequest(stackRequest);
                placer.registerBlockAsNeeded(placedStack.getKey().getItemStack());
                return true;
            }
            return true;
        }
        return false;
    }

    /**
     * Register the block as needed at the building if possible.
     * @param stack the stack.
     */
    public void registerBlockAsNeeded(final ItemStack stack)
    {
        /*
         * Override in child if possible.
         */
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param targetPosition the position to work at.
     * @return BlockPos position to work from.
     */
    @Override
    public BlockPos getWorkingPosition(final BlockPos targetPosition)
    {
        //get length or width either is larger.
        final int length = currentStructure.getLength();
        final int width = currentStructure.getWidth();
        final int distance = Math.max(width, length) + MIN_ADDITIONAL_RANGE_TO_BUILD;

        return getWorkingPosition(distance, targetPosition, 0);
    }

    private boolean decreaseInventory(@NotNull final BlockPos pos, final Block block, @NotNull final BlockState state)
    {
        @NotNull final BlockState stateToPlace = state;

        //Move out of the way when placing blocks
        if (MathHelper.floor(worker.getPosX()) == pos.getX()
              && MathHelper.abs(pos.getY() - (int) worker.getPosY()) <= 1
              && MathHelper.floor(worker.getPosZ()) == pos.getZ()
              && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, RUN_AWAY_SPEED, 1);
        }

        @NotNull final Block blockToPlace = block;

        //It will crash at blocks like water which is actually free, we don't have to decrease the stacks we have.
        if (isBlockFree(blockToPlace))
        {
            return true;
        }

        @Nullable final ItemStack stack = BlockUtils.getItemStackFromBlockState(stateToPlace);
        if (ItemStackUtils.isEmpty(stack))
        {
            Log.getLogger().error("Block causes NPE: " + stateToPlace.getBlock(), new Exception());
            return false;
        }

        final List<ItemStack> itemList = new ArrayList<>();
        if (!ChiselAndBitsCheck.isChiselAndBitsBlock(stateToPlace))
        {
            itemList.add(stack);
        }
        itemList.addAll(getItemsFromTileEntity());

        for (final ItemStack tempStack : itemList)
        {
            if (!ItemStackUtils.isEmpty(tempStack))
            {
                final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(tempStack.getItem());
                if (slot != -1)
                {
                    final ItemStack container = tempStack.getItem().getContainerItem(tempStack);
                    getInventory().extractItem(slot, tempStack.getCount(), false);
                    if (!ItemStackUtils.isEmpty(container))
                    {
                        getInventory().insertItem(slot, container, false);
                    }
                    reduceNeededResources(tempStack);
                }
            }
        }

        if (MineColonies.getConfig().getCommon().builderBuildBlockDelay.get() > 0 && !(blockToPlace instanceof AirBlock))
        {
            double decrease = 1;
            final MultiplierModifierResearchEffect effect = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(BLOCK_PLACE_SPEED, MultiplierModifierResearchEffect.class);
            if (effect != null)
            {
                decrease = 1 - effect.getEffect();
            }

            setDelay((int) ((MineColonies.getConfig().getCommon().builderBuildBlockDelay.get() * PROGRESS_MULTIPLIER / (worker.getCitizenData().getJobModifier() + PROGRESS_MULTIPLIER)) * decrease));
        }

        return true;
    }

    /**
     * On placement of a Block execute this to store the location in the regarding building when needed.
     *
     * @param blockState itself
     * @param pos        the position of the block.
     */
    public void connectBlockToBuildingIfNecessary(@NotNull final BlockState blockState, @NotNull final BlockPos pos)
    {
        /*
         * Classes can overwrite this if necessary.
         */
    }

    /**
     * Defines blocks that can be built for free.
     *
     * @param block    The block to check if it is free.
     * @return true or false.
     */
    public static boolean isBlockFree(@Nullable final Block block)
    {
        return block == null
                 || BlockUtils.isWater(block.getDefaultState())
                 || block.isIn(BlockTags.LEAVES)
                 || block == ModBlocks.blockDecorationPlaceholder;
    }

    /*
     * Get specific data of a tileEntity.
     * Workers should implement this correctly if they require this behavior.
     * @return list of items of the tile.
     */
    public List<ItemStack> getItemsFromTileEntity()
    {
        return Collections.emptyList();
    }

    /**
     * Reduces the needed resources by 1.
     *
     * @param stack the stack which has been used now.
     */
    public void reduceNeededResources(final ItemStack stack)
    {
        /*
         * Nothing to be done here. Workers overwrite this if necessary.
         */
    }

    /**
     * Check for extra building options to do with each block.
     */
    public void checkForExtraBuildingActions()
    {
        /*
         * Override by worker if necessary.
         */
    }

    /**
     * Works on loading the structure.
     * @param structureBlock the block that is currently being worked on.
     * @return the next step once done.
     */
    private boolean structureStep(final StructureIterator.StructureBlock structureBlock)
    {
        checkForExtraBuildingActions();
        if (!(structureBlock.worldBlock instanceof IBuilderUndestroyable) || structureBlock.worldBlock == Blocks.BEDROCK)
        {
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.building"));

            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (!walkToConstructionSite(currentStructure.getCurrentBlockPosition()))
            {
                return false;
            }

            if (structureBlock.block == null
                  || (!structureBlock.metadata.getMaterial().isSolid() && !(structureBlock.block instanceof AirBlock)))
            {
                //findNextBlock count was reached and we can ignore this block
                return true;
            }

            if (structureBlock.doesStructureBlockEqualWorldBlock())
            {
                connectBlockToBuildingIfNecessary(structureBlock.metadata, structureBlock.blockPosition);
                //findNextBlock count was reached and we can ignore this block
                return true;
            }

            @Nullable Block block = structureBlock.block;
            @Nullable BlockState blockState = structureBlock.metadata;
            if (block == com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution
                  || shallReplaceSolidSubstitutionBlock(structureBlock.worldBlock, structureBlock.worldMetadata))
            {
                blockState = getSolidSubstitution(structureBlock.blockPosition);
                block = blockState.getBlock();
            }

            WorkerUtil.faceBlock(structureBlock.blockPosition, worker);

            return placeBlockAt(blockState, structureBlock.blockPosition);
        }
        return true;
    }

    /**
     * Check if a solid substitution block should be overwritten in a specific case.
     *
     * @param worldBlock    the worldblock.
     * @param worldMetadata the world metadata.
     * @return true if should be overwritten.
     */
    public abstract boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata);

    /**
     * Searches a handy block to substitute a non-solid space which should be guaranteed solid.
     *
     * @param location the location the block should be at.
     * @return the Block.
     */
    public abstract BlockState getSolidSubstitution(BlockPos location);

    /**
     * Loads the structure given the name, rotation and position.
     *
     * @param name        the name to retrieve  it.
     * @param rotateTimes number of times to rotateWithMirror it.
     * @param position    the position to set it.
     * @param isMirrored  is the structure mirroed?
     * @param removal     is this a removal task?
     */
    public void loadStructure(@NotNull final String name, final int rotateTimes, final BlockPos position, final boolean isMirrored, final boolean removal)
    {
        rotation = rotateTimes;
        try
        {
            final com.ldtteam.structures.helpers.Structure structure = new com.ldtteam.structures.helpers.Structure(world, name, new PlacementSettings());
            job.setStructure(structure);
            currentStructure = new StructureIterator(world, structure, removal ? StructureIterator.Stage.REMOVE : StructureIterator.Stage.CLEAR);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn(String.format("StructureProxy: (%s) does not exist - removing build request", name), e);
            job.setStructure(null);
        }

        try
        {
            job.getStructure().rotate(BlockPosUtil.getRotationFromRotations(rotateTimes), world, position, isMirrored ? Mirror.FRONT_BACK : Mirror.NONE);
            job.getStructure().setPosition(position);
            job.getStructure().setPlacementSettings(new PlacementSettings(isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotateTimes)));

        }
        catch (final NullPointerException ex)
        {
            handleSpecificCancelActions();
            job.setStructure(null);
            Log.getLogger().warn("StructureIterator couldn't be found which caused an NPE, removed workOrder, more details in log", ex);
        }
        if (getProgressPos() != null)
        {
            this.currentStructure.setStage(getProgressPos().getB());
        }
    }

    /**
     * Specific actions to handle a cancelation of a structure.
     */
    public void handleSpecificCancelActions()
    {
        /*
         * Child classes have to override this.
         */
    }

    /**
     * Check if the structure tusk has been canceled.
     *
     * @return true if reset to idle.
     */
    protected abstract boolean checkIfCanceled();

    /**
     * Works on clearing the area of unneeded blocks.
     * @param currentBlock the block that is currently being worked on.
     * @return the next step once done.
     */
    private boolean clearStep(@NotNull final StructureIterator.StructureBlock currentBlock)
    {
        checkForExtraBuildingActions();
        if (isAlreadyCleared() || (!currentStructure.getStage().equals(StructureIterator.Stage.CLEAR) && !currentStructure.getStage().equals(StructureIterator.Stage.REMOVE)))
        {
            return true;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.clearing"));

        //Don't break bedrock etc.
        if (!(currentBlock.worldBlock instanceof IBuilderUndestroyable) || currentBlock.worldBlock == Blocks.BEDROCK && currentBlock.worldBlock != Blocks.TORCH)
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (!walkToConstructionSite(currentStructure.getCurrentBlockPosition()))
            {
                return false;
            }

            if (StructurePlacementUtils.isStructureBlockEqualWorldBlock(world, currentBlock.blockPosition, job.getStructure().getBlockstate())
                  || (currentBlock.block instanceof BedBlock && currentBlock.metadata.get(BedBlock.PART).equals(BedPart.FOOT))
                  || (currentBlock.block instanceof DoorBlock && currentBlock.metadata.get(DoorBlock.HALF).equals(DoubleBlockHalf.UPPER)))
            {
                return true;
            }

            WorkerUtil.faceBlock(currentBlock.blockPosition, worker);

            //We need to deal with materials
            if (MineColonies.getConfig().getCommon().builderInfiniteResources.get() || currentBlock.worldMetadata.getMaterial().isLiquid())
            {
                worker.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStackUtils.EMPTY);
                world.removeBlock(currentBlock.blockPosition, false);
                world.setBlockState(currentBlock.blockPosition, Blocks.AIR.getDefaultState());
                worker.swingArm(worker.getActiveHand());
                setDelay(UNLIMITED_RESOURCES_TIMEOUT * PROGRESS_MULTIPLIER / (worker.getCitizenData().getJobModifier() + PROGRESS_MULTIPLIER));
            }
            else
            {
                if (!mineBlock(currentBlock.blockPosition, getCurrentWorkingPosition()))
                {
                    return false;
                }
                else
                {
                    worker.decreaseSaturationForContinuousAction();
                }
            }
        }

        return true;
    }

    /**
     * Let childs overwrite this if necessary.
     *
     * @return true if so.
     */
    protected boolean isAlreadyCleared()
    {
        return false;
    }

    /**
     * Get the current working position for the worker. If workFrom is null calculate a new one.
     */
    private BlockPos getCurrentWorkingPosition()
    {
        return workFrom == null ? getWorkingPosition(currentStructure.getCurrentBlockPosition()) : workFrom;
    }

    /**
     * Check if there is a StructureIterator to be build.
     *
     * @return true if we should start building.
     */
    protected boolean isThereAStructureToBuild()
    {
        if (currentStructure == null)
        {
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.waitingForBuild"));
        }
        return currentStructure != null;
    }

    /**
     * Works on detecting fluid blocks that should get removed.
     * @param currentBlock the block that is currently being worked on.
     * @return the next step once done.
     */
    private boolean fluidDetectStep(@NotNull final StructureIterator.StructureBlock currentBlock)
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.detecting_fluids"));
        if (!walkToConstructionSite(currentStructure.getCurrentBlockPosition()))
        {
            return false;
        }

        final Map<Integer,List<BlockPos>> fluidsToRemove = getOwnBuilding(AbstractBuildingStructureBuilder.class).getFluidsToRemove();
        if (currentBlock.block instanceof FlowingFluidBlock || currentBlock.worldBlock instanceof FlowingFluidBlock)
        {
            if(!(currentBlock.block instanceof FlowingFluidBlock))
            {
                List<BlockPos> layer;
                if(fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    layer = fluidsToRemove.get(currentBlock.blockPosition.getY());
                }
                else
                {
                    layer = new ArrayList<>();
                }
                layer.add(currentBlock.blockPosition);
                if(!fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    fluidsToRemove.put(currentBlock.blockPosition.getY(), layer);
                }
            }
            else if(currentBlock.worldBlock instanceof FlowingFluidBlock && currentBlock.worldBlock != currentBlock.block)
            {
                List<BlockPos> layer;
                if(fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    layer = fluidsToRemove.get(currentBlock.blockPosition.getY());
                }
                else
                {
                    layer = new ArrayList<>();
                }
                layer.add(currentBlock.blockPosition);
                if(!fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    fluidsToRemove.put(currentBlock.blockPosition.getY(), layer);
                }
            }
        }
        else if (!currentBlock.metadata.getFluidState().isEmpty() || !currentBlock.worldMetadata.getFluidState().isEmpty())
        {
            if(currentBlock.metadata.getFluidState().isEmpty())
            {
                List<BlockPos> layer;
                if(fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    layer = fluidsToRemove.get(currentBlock.blockPosition.getY());
                }
                else
                {
                    layer = new ArrayList<>();
                }
                layer.add(currentBlock.blockPosition);
                if(!fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    fluidsToRemove.put(currentBlock.blockPosition.getY(), layer);
                }
            }
            else if(!currentBlock.worldMetadata.getFluidState().isEmpty() && currentBlock.worldMetadata.getFluidState().getFluid() != currentBlock.metadata.getFluidState().getFluid())
            {
                List<BlockPos> layer;
                if(fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    layer = fluidsToRemove.get(currentBlock.blockPosition.getY());
                }
                else
                {
                    layer = new ArrayList<>();
                }
                layer.add(currentBlock.blockPosition);
                if(!fluidsToRemove.containsKey(currentBlock.blockPosition.getY()))
                {
                    fluidsToRemove.put(currentBlock.blockPosition.getY(), layer);
                }
            }
        }
        return true;
    }

    /**
     * Works on removing the fluid blocks that should get removed.
     * @param currentBlock
     * @return the next step once done.
     */
    private IAIState fluidRemoveStep()
    {
        checkForExtraBuildingActions();
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.removing_fluids"));

        final Map<Integer,List<BlockPos>> fluidsToRemove = getOwnBuilding(AbstractBuildingStructureBuilder.class).getFluidsToRemove();
        if (fluidsToRemove.isEmpty())
        {
            return AIWorkerState.DECORATION_STEP;
        }
        else
        {
            int y = fluidsToRemove.keySet().iterator().next();
        	List<BlockPos> fluids = fluidsToRemove.get(y);
            fluids.forEach(fluid -> {
            	BlockState blockState = world.getBlockState(fluid);
            	Block block = blockState.getBlock();
                if(block instanceof IBucketPickupHandler)
                {
                    ((IBucketPickupHandler)block).pickupFluid(world, fluid, blockState);
                }
            });
            fluidsToRemove.remove(y);
            return getState();
        }
    }

    /**
     * Start building this StructureIterator.
     * <p>
     * Will determine where to start.
     *
     * @return the new State to start in.
     */
    @NotNull
    private IAIState startBuilding()
    {
        if (currentStructure == null)
        {
            onStartWithoutStructure();
            return IDLE;
        }
        switch (currentStructure.getStage())
        {
            case REMOVE:
                return REMOVE_STEP;
            case CLEAR:
                return CLEAR_STEP;
            case BUILD:
                return BUILDING_STEP;
            case FLUID_DETECT:
            	return FLUID_DETECT_STEP;
            case DECORATE:
                return DECORATION_STEP;
            case SPAWN:
                return SPAWN_STEP;
            default:
                return COMPLETE_BUILD;
        }
    }

    protected abstract void onStartWithoutStructure();

    /**
     * Check how much of a certain stuck is actually required.
     *
     * @param stack the stack to check.
     * @return the new stack with the correct amount.
     */
    @Nullable
    public ItemStack getTotalAmount(@Nullable final ItemStack stack)
    {
        return stack;
    }

    /**
     * Set the currentStructure to null.
     */
    public void resetCurrentStructure()
    {
        workFrom = null;
        currentStructure = null;
    }

    /**
     * Iterates through all entities and spawns them
     * Suppressing Sonar Rule Squid:S3047
     * The rule thinks we can merge the two forge loops iterating over resources
     * But in this case the rule does not apply because that would destroy the logic.
     */
    @SuppressWarnings(MULTIPLE_LOOPS_OVER_THE_SAME_SET_SHOULD_BE_COMBINED)
    private Boolean addEntity(@NotNull final StructureIterator.StructureBlock currentBlock)
    {
        final CompoundNBT[] entityInfos = currentBlock.entity;
        if (entityInfos.length == 0)
        {
            return true;
        }
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.spawning"));

        for (final CompoundNBT entityInfo : entityInfos)
        {
            if (entityInfo == null)
            {
                continue;
            }

            final Entity entity = ItemStackUtils.getEntityFromEntityInfoOrNull(entityInfo, world);
            final BlockPos pos = currentStructure.getPos();
            if (entity != null)
            {
                final Vec3d worldPos = entity.getPositionVector().add(pos.getX(), pos.getY(), pos.getZ());
                entity.setLocationAndAngles(
                  worldPos.x,
                  worldPos.y,
                  worldPos.z,
                  entity.rotationYaw,
                  entity.rotationPitch);

                if (!EntityUtils.isEntityAtPosition(entity, world, worker))
                {
                    final List<ItemStack> request = new ArrayList<>();
                    if (entity instanceof ItemFrameEntity)
                    {
                        final ItemStack stack = ((ItemFrameEntity) entity).getDisplayedItem();
                        if (!ItemStackUtils.isEmpty(stack))
                        {
                            ItemStackUtils.setSize(stack, 1);
                            request.add(stack);
                        }
                        request.add(new ItemStack(Items.ITEM_FRAME, 1));
                    }
                    else if (entity instanceof ArmorStandEntity)
                    {
                        request.add(entity.getPickedResult(new EntityRayTraceResult(worker)));
                        entity.getArmorInventoryList().forEach(request::add);
                        entity.getHeldEquipment().forEach(request::add);
                    }

                    request.removeIf(ItemStackUtils::isEmpty);

                    if (!MineColonies.getConfig().getCommon().builderInfiniteResources.get())
                    {
                        if (checkForListInInvAndRequest(this, new ArrayList<>(request), true))
                        {
                            return false;
                        }

                        //Surpress
                        for (final ItemStack stack : request)
                        {
                            if (ItemStackUtils.isEmpty(stack))
                            {
                                continue;
                            }
                            final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(stack.getItem());
                            if (slot != -1)
                            {
                                getInventory().extractItem(slot, 1, false);
                                reduceNeededResources(stack);
                            }
                        }
                    }

                    entity.setUniqueId(UUID.randomUUID());
                    if (!world.addEntity(entity))
                    {
                        Log.getLogger().info("Failed to spawn entity");
                    }
                }
            }
        }

        return true;
    }

    /**
     * Get the worker of the AI.
     *
     * @return the EntityCitizen object.
     */
    public AbstractEntityCitizen getWorker()
    {
        return this.worker;
    }

    /**
     * Get the rotation of the current build.
     *
     * @return the rotation
     */
    public int getRotation()
    {
        return rotation;
    }
}
