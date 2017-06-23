package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.Structure;
import com.minecolonies.coremod.placementhandlers.IPlacementHandler;
import com.minecolonies.coremod.placementhandlers.PlacementHandlers;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.Suppression.MULTIPLE_LOOPS_OVER_THE_SAME_SET_SHOULD_BE_COMBINED;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;
import static com.minecolonies.coremod.placementhandlers.IPlacementHandler.ActionProcessingResult.ACCEPT;
import static com.minecolonies.coremod.placementhandlers.IPlacementHandler.ActionProcessingResult.DENY;

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
public abstract class AbstractEntityAIStructure<J extends AbstractJob> extends AbstractEntityAIInteract<J>
{
    /**
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally).
     */
    private static final double XP_EACH_BUILDING              = 2.5;
    /**
     * Speed the builder should run away when he castles himself in.
     */
    private static final double RUN_AWAY_SPEED                = 4.1D;
    /**
     * The minimum range to keep from the current building place.
     */
    private static final int    MIN_ADDITIONAL_RANGE_TO_BUILD = 3;
    /**
     * The amount of ticks to wait when not needing any tools to break blocks.
     */
    private static final int    UNLIMITED_RESOURCES_TIMEOUT   = 5;
    /**
     * The current structure task to be build.
     */
    private Structure currentStructure;
    /**
     * Position where the Builders constructs from.
     */
    private BlockPos  workFrom;
    /**
     * The standard range the builder should reach until his target.
     */
    private static final int STANDARD_WORKING_RANGE = 5;
    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int MIN_WORKING_RANGE      = 12;

    /**
     * The rotation of the current build.
     */
    private int rotation = 0;

    /**
     * String which shows if something is a waypoint.
     */
    public static final String WAYPOINT_STRING = "waypoint";

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

                /**
                 * Pick up stuff which might've been
                 */
                new AITarget(PICK_UP_RESIDUALS, this::pickUpResiduals),
                /**
                 * Check if tasks should be executed.
                 */
                new AITarget(this::checkIfCanceled, IDLE),
                /**
                 * Select the appropriate State to do next.
                 */
                new AITarget(START_BUILDING, this::startBuilding),
                /**
                 * Check if we have to build something.
                 */
                new AITarget(IDLE, this::isThereAStructureToBuild, () -> AIState.START_BUILDING),
                /**
                 * Clear out the building area.
                 */
                new AITarget(CLEAR_STEP, generateStructureGenerator(this::clearStep, AIState.BUILDING_STEP)),
                /**
                 * Build the structure and foundation of the building.
                 */
                new AITarget(BUILDING_STEP, generateStructureGenerator(this::structureStep, AIState.SPAWN_STEP)),
                /**
                 * Spawn entities on the structure.
                 */
                new AITarget(SPAWN_STEP, generateStructureGenerator(this::spawnEntity, AIState.DECORATION_STEP)),
                /**
                 * Decorate the AbstractBuilding with torches etc.
                 */
                new AITarget(DECORATION_STEP, generateStructureGenerator(this::decorationStep, AIState.COMPLETE_BUILD)),
                /**
                 * Finalize the building and give back control to the ai.
                 */
                new AITarget(COMPLETE_BUILD, this::completeBuild)
        );
    }

    private AIState pickUpResiduals()
    {
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

        return AIState.IDLE;
    }


    /**
     * Fill the list of the item positions to gather.
     */
    @Override
    public void fillItemsList()
    {
        if(currentStructure == null)
        {
            return;
        }

        final BlockPos centerPos = currentStructure.getCenter();
        if(centerPos.getY() == 0)
        {
            return;
        }

        searchForItems(new AxisAlignedBB(centerPos).expand(currentStructure.getLength() / 2.0, currentStructure.getHeight(), currentStructure.getWidth()));
    }

    private AIState completeBuild()
    {
        if (job instanceof AbstractJobStructure)
        {
            executeSpecificCompleteActions();
            worker.addExperience(XP_EACH_BUILDING);
        }

        return AIState.PICK_UP_RESIDUALS;
    }

    private Boolean decorationStep(final Structure.StructureBlock structureBlock)
    {
        if (!BlockUtils.shouldNeverBeMessedWith(structureBlock.worldBlock))
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (!walkToConstructionSite())
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

            worker.faceBlock(structureBlock.blockPosition);

            @Nullable final Block block = structureBlock.block;

            //should never happen
            if (block == null)
            {
                @NotNull final BlockPos local = structureBlock.blockPosition;
                Log.getLogger().error(String.format("StructureProxy has null block at %s - local(%s)", currentStructure.getCurrentBlockPosition(), local));
                return true;
            }

            @Nullable final IBlockState blockState = structureBlock.metadata;
            //We need to deal with materials
            return placeBlockAt(blockState, structureBlock.blockPosition);
        }
        return true;
    }

    private Boolean structureStep(final Structure.StructureBlock structureBlock)
    {
        if (!BlockUtils.shouldNeverBeMessedWith(structureBlock.worldBlock))
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (!walkToConstructionSite())
            {
                return false;
            }

            if (structureBlock.block == null
                    || (!structureBlock.metadata.getMaterial().isSolid() && structureBlock.block != Blocks.AIR))
            {
                //findNextBlock count was reached and we can ignore this block
                return true;
            }

            if (structureBlock.doesStructureBlockEqualWorldBlock())
            {
                connectBlockToBuildingIfNecessary(structureBlock.block, structureBlock.blockPosition);
                //findNextBlock count was reached and we can ignore this block
                return true;
            }

            @Nullable Block block = structureBlock.block;
            @Nullable IBlockState blockState = structureBlock.metadata;
            if (block == ModBlocks.blockSolidSubstitution
                    && shallReplaceSolidSubstitutionBlock(structureBlock.worldBlock, structureBlock.worldMetadata))
            {
                blockState = getSolidSubstitution(structureBlock.blockPosition);
                block = blockState.getBlock();
            }

            worker.faceBlock(structureBlock.blockPosition);

            //should never happen
            if (block == null)
            {
                @NotNull final BlockPos local = structureBlock.blockPosition;
                Log.getLogger().error(String.format("StructureProxy has null block at %s - local(%s)", currentStructure.getCurrentBlockPosition(), local));
                return true;
            }

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
    public abstract boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final IBlockState worldMetadata);

    /**
     * Generate a function that will iterate over a structure.
     * <p>
     * It will pass the current block (with all infos) to the evaluation function.
     *
     * @param evaluationFunction the function to be called each block.
     * @param nextState          the next state to change to once done iterating.
     * @return the new state this AI will be in after one pass.
     */
    private Supplier<AIState> generateStructureGenerator(@NotNull final Function<Structure.StructureBlock, Boolean> evaluationFunction, @NotNull final AIState nextState)
    {
        //do not replace with method reference, this one stays the same on changing reference for currentStructure
        //URGENT: DO NOT REPLACE FOR ANY MEANS THIS WILL CRASH THE GAME.
        @NotNull final Supplier<Structure.StructureBlock> getCurrentBlock = () -> currentStructure.getCurrentBlock();
        @NotNull final Supplier<Structure.Result> advanceBlock = () -> currentStructure.advanceBlock();

        return () ->
        {
            final Structure.StructureBlock currentBlock = getCurrentBlock.get();
            /*
            check if we have not found a block (when block == null
            if we have a block, apply the eval function
            (which changes stuff, so only execute on valid block!)
            */
            if (currentBlock.block == null
                    || evaluationFunction.apply(currentBlock))
            {
                final Structure.Result result = advanceBlock.get();
                if (result == Structure.Result.AT_END)
                {
                    switchStage(nextState);
                    return nextState;
                }
                if (result == Structure.Result.CONFIG_LIMIT)
                {
                    return getState();
                }
            }
            return getState();
        };
    }

    /**
     * Switches the structures stage after the current one has been completed.
     */
    private void switchStage(final AIState state)
    {
        if (state.equals(AIState.BUILDING_STEP))
        {
            currentStructure.setStage(Structure.Stage.BUILD);
        }
        else if (state.equals(AIState.DECORATION_STEP))
        {
            currentStructure.setStage(Structure.Stage.DECORATE);
        }
        else if (state.equals(AIState.SPAWN_STEP))
        {
            currentStructure.setStage(Structure.Stage.SPAWN);
        }
        else if (state.equals(AIState.COMPLETE_BUILD))
        {
            currentStructure.setStage(Structure.Stage.COMPLETE);
        }
    }

    /**
     * Execute specific actions on loading a structure.
     */
    protected abstract void executeSpecificCompleteActions();

    /**
     * Requests Materials if required.
     */
    public void requestMaterialsIfRequired()
    {
        /**
         * Extending entities implement this if required.
         */
    }

    /**
     * Searches a handy block to substitute a non-solid space which should be guaranteed solid.
     *
     * @param location the location the block should be at.
     * @return the Block.
     */
    public abstract IBlockState getSolidSubstitution(BlockPos location);

    /**
     * Loads the structure given the name, rotation and position.
     *
     * @param name        the name to retrieve  it.
     * @param rotateTimes number of times to rotateWithMirror it.
     * @param position    the position to set it.
     * @param isMirrored  is the structure mirroed?
     */
    public void loadStructure(@NotNull final String name, final int rotateTimes, final BlockPos position, final boolean isMirrored)
    {
        if (job instanceof AbstractJobStructure)
        {
            rotation = rotateTimes;
            try
            {
                final StructureWrapper wrapper = new StructureWrapper(world, name);

                ((AbstractJobStructure) job).setStructure(wrapper);
                currentStructure = new Structure(world, wrapper, Structure.Stage.CLEAR);
            }
            catch (final IllegalStateException e)
            {
                Log.getLogger().warn(String.format("StructureProxy: (%s) does not exist - removing build request", name), e);
                ((AbstractJobStructure) job).setStructure(null);
            }

            ((AbstractJobStructure) job).getStructure().rotate(rotateTimes, world, position, isMirrored ? Mirror.FRONT_BACK : Mirror.NONE);
            ((AbstractJobStructure) job).getStructure().setPosition(position);
        }
    }

    /**
     * Check if the structure tusk has been canceled.
     *
     * @return true if reset to idle.
     */
    protected abstract boolean checkIfCanceled();

    /**
     * Let childs overwrite this if necessary.
     * @return true if so.
     */
    protected boolean isAlreadyCleared()
    {
        return false;
    }

    /**
     * Works on clearing the area of unneeded blocks.
     *
     * @return the next step once done.
     */
    private boolean clearStep(@NotNull final Structure.StructureBlock currentBlock)
    {
        if (isAlreadyCleared() || !currentStructure.getStage().equals(Structure.Stage.CLEAR))
        {
            return true;
        }

        //Don't break bedrock etc.
        if (!BlockUtils.shouldNeverBeMessedWith(currentBlock.worldBlock))
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (!walkToConstructionSite())
            {
                return false;
            }

            worker.faceBlock(currentBlock.blockPosition);

            //We need to deal with materials
            if (Configurations.gameplay.builderInfiniteResources || currentBlock.worldMetadata.getMaterial().isLiquid())
            {
                worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
                world.setBlockToAir(currentBlock.blockPosition);
                world.setBlockState(currentBlock.blockPosition, Blocks.AIR.getDefaultState());
                worker.swingArm(worker.getActiveHand());
                setDelay(UNLIMITED_RESOURCES_TIMEOUT);
            }
            else
            {
                if (!mineBlock(currentBlock.blockPosition, getCurrentWorkingPosition()))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get the current working position for the worker. If workFrom is null calculate a new one.
     */
    private BlockPos getCurrentWorkingPosition()
    {
        return workFrom == null ? getWorkingPosition(currentStructure.getCurrentBlockPosition()) : workFrom;
    }

    /**
     * Check if there is a Structure to be build.
     *
     * @return true if we should start building.
     */
    protected boolean isThereAStructureToBuild()
    {
        return currentStructure != null;
    }

    /**
     * Walk to the current construction site.
     * <p>
     * Calculates and caches the position where to walk to.
     *
     * @return true while walking to the site.
     */
    public boolean walkToConstructionSite()
    {
        if (workFrom == null)
        {
            workFrom = getWorkingPosition(currentStructure.getCurrentBlockPosition());
        }

        //The miner shouldn't search for a save position. Just let him build from where he currently is.
        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < MIN_WORKING_RANGE;
    }

    /**
     * Start building this Structure.
     * <p>
     * Will determine where to start.
     *
     * @return the new State to start in.
     */
    @NotNull
    private AIState startBuilding()
    {
        if (currentStructure == null)
        {
            onStartWithoutStructure();
            return AIState.IDLE;
        }
        switch (currentStructure.getStage())
        {
            case CLEAR:
                return AIState.CLEAR_STEP;
            case BUILD:
                return AIState.BUILDING_STEP;
            case DECORATE:
                return AIState.DECORATION_STEP;
            case SPAWN:
                return AIState.SPAWN_STEP;
            default:
                return AIState.COMPLETE_BUILD;
        }
    }

    protected abstract void onStartWithoutStructure();

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
     * Defines blocks that can be built for free.
     *
     * @param block    The block to check if it is free.
     * @param metadata The metadata of the block.
     * @return true or false.
     */
    public static boolean isBlockFree(@Nullable final Block block, final int metadata)
    {
        return block == null
                || BlockUtils.isWater(block.getDefaultState())
                || block.equals(Blocks.LEAVES)
                || block.equals(Blocks.LEAVES2)
                || (block.equals(Blocks.DOUBLE_PLANT) && Utils.testFlag(metadata, 0x08));
    }

    private boolean placeBlockAt(@NotNull final IBlockState blockState, @NotNull final BlockPos coords)
    {
        final ItemStack item = BlockUtils.getItemStackFromBlockState(blockState);
        worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item == null ? ItemStackUtils.EMPTY : item);
        final IBlockState decrease;
        for(final IPlacementHandler handlers :PlacementHandlers.handlers)
        {
            final Object result = handlers.handle(world, coords, blockState, this);
            if(result instanceof IPlacementHandler.ActionProcessingResult)
            {
                if(result == ACCEPT)
                {
                    return true;
                }

                if(result == DENY)
                {
                    return false;
                }
                continue;
            }

            if(result  instanceof IBlockState)
            {
                decrease = (IBlockState) result;
                decreaseInventory(coords, decrease.getBlock(), decrease);
                worker.swingArm(worker.getActiveHand());

                return true;
            }
        }

        Log.getLogger().warn("Couldn't handle block: " + blockState.getBlock().getUnlocalizedName());
        return true;
    }

    public void handleBuildingOverBlock(@NotNull final BlockPos pos)
    {
        final List<ItemStack> items = BlockPosUtil.getBlockDrops(world, pos, 0);
        for (final ItemStack item : items)
        {
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), item);
        }
    }

    private boolean decreaseInventory(@NotNull final BlockPos pos, final Block block, @NotNull final IBlockState state)
    {
        @NotNull final IBlockState stateToPlace = state;

        //Move out of the way when placing blocks
        if (MathHelper.floor(worker.posX) == pos.getX()
                && MathHelper.abs(pos.getY() - (int) worker.posY) <= 1
                && MathHelper.floor(worker.posZ) == pos.getZ()
                && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, RUN_AWAY_SPEED, 1.0);
        }

        @NotNull final Block blockToPlace = block;
        connectBlockToBuildingIfNecessary(blockToPlace, pos);

        //It will crash at blocks like water which is actually free, we don't have to decrease the stacks we have.
        if (isBlockFree(blockToPlace, blockToPlace.getMetaFromState(stateToPlace)))
        {
            return true;
        }

        @Nullable final ItemStack stack = BlockUtils.getItemStackFromBlockState(stateToPlace);
        if (ItemStackUtils.isEmpty(stack))
        {
            Log.getLogger().error("Block causes NPE: " + stateToPlace.getBlock());
            return false;
        }

        final List<ItemStack> itemList = new ArrayList<>();
        itemList.add(stack);
        itemList.addAll(getItemsFromTileEntity());

        for (final ItemStack tempStack : itemList)
        {
            if (!ItemStackUtils.isEmpty(tempStack))
            {
                final int slot = worker.findFirstSlotInInventoryWith(tempStack.getItem(), tempStack.getItemDamage());
                if (slot != -1)
                {
                    new InvWrapper(getInventory()).extractItem(slot, 1, false);
                    reduceNeededResources(tempStack);
                }
            }
        }

        if (Configurations.gameplay.builderBuildBlockDelay > 0 && blockToPlace != Blocks.AIR)
        {
            setDelay(Configurations.gameplay.builderBuildBlockDelay);
        }

        return true;
    }

    /**
     * Handle flower pots and spawn the right flower in it.
     * @param pos the position.
     */
    public void handleFlowerPots(@NotNull final BlockPos pos)
    {
        /**
         * Should be overwritten and implemented by certain entity if required.
         */
    }

    /**
     * On placement of a Block execute this to store the location in the regarding building when needed.
     *
     * @param block itself
     * @param pos the position of the block.
     */
    public void connectBlockToBuildingIfNecessary(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        /**
         * Classes can overwrite this if necessary.
         */
    }

    /**
     * Reduces the needed resources by 1.
     *
     * @param stack the stack which has been used now.
     */
    public void reduceNeededResources(final ItemStack stack)
    {
        /**
         * Nothing to be done here. Workers overwrite this if necessary.
         */
    }

    /**
     * Get the worker of the AI.
     * @return the EntityCitizen object.
     */
    public EntityCitizen getWorker()
    {
        return this.worker;
    }

    /**
     * Get the entity of an entityInfo object.
     *
     * @param entityInfo the input.
     * @return the output object or null.
     */
    @Nullable
    public Entity getEntityFromEntityInfoOrNull(final Template.EntityInfo entityInfo)
    {
        try
        {
            return EntityList.createEntityFromNBT(entityInfo.entityData, world);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Couldn't restore entitiy", e);
            return null;
        }
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
    private Boolean spawnEntity(@NotNull final Structure.StructureBlock currentBlock)
    {
        final Template.EntityInfo entityInfo = currentBlock.entity;
        if (entityInfo == null)
        {
            return true;
        }

        final Entity entity = getEntityFromEntityInfoOrNull(entityInfo);
        if (entity != null && !isEntityAtPosition(entity, world))
        {
            final List<ItemStack> request = new ArrayList<>();

            if (entity instanceof EntityItemFrame)
            {
                final ItemStack stack = ((EntityItemFrame) entity).getDisplayedItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    ItemStackUtils.changeSize(stack, 1);
                    request.add(stack);
                }
                request.add(new ItemStack(Items.ITEM_FRAME, 1));
            }
            else if (entity instanceof EntityArmorStand)
            {
                request.add(entity.getPickedResult(new RayTraceResult(worker)));
                entity.getArmorInventoryList().forEach(request::add);
            }
            else
            {
                request.add(entity.getPickedResult(new RayTraceResult(worker)));
            }

            if (!Configurations.gameplay.builderInfiniteResources)
            {
                for (final ItemStack stack : request)
                {
                    if (checkOrRequestItems(stack))
                    {
                        return false;
                    }
                }

                //Surpress
                for (final ItemStack stack : request)
                {
                    if (ItemStackUtils.isEmpty(stack))
                    {
                        continue;
                    }
                    final int slot = worker.findFirstSlotInInventoryWith(stack.getItem(), stack.getItemDamage());
                    if (slot != -1)
                    {
                        new InvWrapper(getInventory()).extractItem(slot, 1, false);
                        reduceNeededResources(stack);
                    }
                }
            }

            entity.setUniqueId(UUID.randomUUID());
            entity.setLocationAndAngles(
                    entity.posX,
                    entity.posY,
                    entity.posZ,
                    entity.rotationYaw,
                    entity.rotationPitch);
            if (!world.spawnEntity(entity))
            {
                Log.getLogger().info("Failed to spawn entity");
            }
        }

        return true;
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

    /**
     * Checks if a certain entity is in the world at a certain position already.
     *
     * @param entity the entity.
     * @param world  the world.
     * @return true if there.
     */
    private static boolean isEntityAtPosition(final Entity entity, final World world)
    {
        return !world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ)).isEmpty();
    }

    /**
     * Get the rotation of the current build.
     * @return the rotation
     */
    public int getRotation()
    {
        return rotation;
    }
}
