package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.colony.buildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.Structure;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

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
    private static final double   RUN_AWAY_SPEED = 4.1D;
    /**
     * The minimum range to keep from the current building place.
     */
    private static final int MIN_ADDITIONAL_RANGE_TO_BUILD = 3;
    /**
     * The maximum range to keep from the current building place.
     */
    private static final int MAX_ADDITIONAL_RANGE_TO_BUILD = 25;
    /**
     * The amount of ticks to wait when not needing any tools to break blocks.
     */
    private static final int UNLIMITED_RESOURCES_TIMEOUT   = 5;
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
    private static final int    STANDARD_WORKING_RANGE        = 5;
    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int    MIN_WORKING_RANGE             = 12;

    private int rotation = 0;

    /**
     * String which shows if something is a waypoint.
     */
    private static final CharSequence WAYPOINT_STRING = "waypoint";

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
          new AITarget(BUILDING_STEP, generateStructureGenerator(this::structureStep, AIState.DECORATION_STEP)),
          /**
           * Decorate the AbstractBuilding with torches etc.
           */
          new AITarget(DECORATION_STEP, generateStructureGenerator(this::decorationStep, AIState.COMPLETE_BUILD)),
          /**
           * Spawn entities on the structure.
           */
          new AITarget(SPAWN_STEP, () -> AIState.IDLE),
          /**
           * Finalize the building and give back control to the ai.
           */
          new AITarget(COMPLETE_BUILD, this::completeBuild)
        );
    }

    private AIState completeBuild()
    {
        if(job instanceof AbstractJobStructure)
        {
            if (((AbstractJobStructure) job).getStructure() == null && job instanceof JobBuilder)
            {
                //fix for bad structures
                ((JobBuilder) job).complete();
            }

            if(job instanceof JobBuilder)
            {
                final String structureName = ((AbstractJobStructure) job).getStructure().getName();

                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                        "entity.builder.messageBuildComplete",
                        structureName);
                ((JobBuilder) job).getStructure().getEntities().forEach(this::spawnEntity);

                final WorkOrderBuild wo = ((JobBuilder) job).getWorkOrder();
                if (wo == null)
                {
                    Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing work order(%d)",
                            worker.getColony().getID(),
                            worker.getCitizenData().getId(),
                            ((JobBuilder) job).getWorkOrderId()));
                }
                else
                {
                    if (wo instanceof WorkOrderBuildDecoration)
                    {
                        if (structureName.contains(WAYPOINT_STRING))
                        {
                            worker.getColony().addWayPoint(wo.getBuildingLocation(), world.getBlockState(wo.getBuildingLocation()));
                        }
                    }
                    else
                    {
                        final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                        if (building == null)
                        {
                            Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                                    worker.getColony().getID(),
                                    worker.getCitizenData().getId(),
                                    wo.getBuildingLocation()));
                        }
                        else
                        {
                            building.setBuildingLevel(wo.getUpgradeLevel());
                        }
                    }
                    ((JobBuilder) job).complete();
                }

                final AbstractBuilding workerBuilding = getOwnBuilding();
                if (workerBuilding instanceof BuildingBuilder)
                {
                    ((BuildingBuilder) workerBuilding).resetNeededResources();
                }
                resetTask();
            }
            else if(job instanceof JobMiner)
            {
                final BuildingMiner minerBuilding = (BuildingMiner) getOwnBuilding();
                //If shaft isn't cleared we're in shaft clearing mode.
                if(minerBuilding.clearedShaft)
                {
                    minerBuilding.getCurrentLevel().closeNextNode(rotation);
                }
                else
                {
                    @NotNull final Level currentLevel = new Level(minerBuilding, ((JobMiner) job).getStructure().getPosition().getY());
                    minerBuilding.addLevel(currentLevel);
                    minerBuilding.setCurrentLevel(minerBuilding.getNumberOfLevels());
                    minerBuilding.resetStartingLevelShaft();
                }
                //Send out update to client
                getOwnBuilding().markDirty();

                ((JobMiner) job).setStructure(null);
            }
            worker.addExperience(XP_EACH_BUILDING);
            workFrom = null;
        }

        currentStructure = null;

        return AIState.IDLE;
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
            if (!Configurations.builderInfiniteResources
                    && !handleMaterials(block, blockState))
            {
                return false;
            }

            placeBlockAt(block, blockState, structureBlock.blockPosition);
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
                    || structureBlock.doesStructureBlockEqualWorldBlock()
                    || (!structureBlock.metadata.getMaterial().isSolid() && structureBlock.block != Blocks.AIR))
            {
                //findNextBlock count was reached and we can ignore this block
                return true;
            }

            @Nullable Block block = structureBlock.block;
            @Nullable IBlockState blockState = structureBlock.metadata;
            if(structureBlock.block == ModBlocks.blockSolidSubstitution)
            {
                if(!(job instanceof JobMiner && structureBlock.worldBlock instanceof BlockOre)
                        && structureBlock.worldMetadata.getMaterial().isSolid())
                {
                    return true;
                }
                block = getSolidSubstitution(structureBlock.blockPosition);
                blockState = block.getDefaultState();

            }

            worker.faceBlock(structureBlock.blockPosition);

            //should never happen
            if (block == null)
            {
                @NotNull final BlockPos local = structureBlock.blockPosition;
                Log.getLogger().error(String.format("StructureProxy has null block at %s - local(%s)", currentStructure.getCurrentBlockPosition(), local));
                return true;
            }

            //We need to deal with materials
            if (!Configurations.builderInfiniteResources
                    && !handleMaterials(block, blockState))
            {
                return false;
            }

            placeBlockAt(block, blockState, structureBlock.blockPosition);
        }
        return true;
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
    private void switchStage(AIState state)
    {
        if(state.equals(AIState.BUILDING_STEP))
        {
            currentStructure.setStage(Structure.Stage.BUILD);
        }
        else if(state.equals(AIState.DECORATION_STEP))
        {
            currentStructure.setStage(Structure.Stage.DECORATE);
        }
        else if(state.equals(AIState.COMPLETE_BUILD))
        {
            currentStructure.setStage(Structure.Stage.COMPLETE);
        }
    }

    /**
     * Load the structure, special builder use with workOrders.
     * Extracts data from workOrder and hands it to generic loading.
     */
    public void loadStructure()
    {
        WorkOrderBuild workOrder = null;
        if(job instanceof JobBuilder)
        {
            workOrder = ((JobBuilder) job).getWorkOrder();
        }

        if (workOrder == null)
        {
            return;
        }

        final BlockPos pos = workOrder.getBuildingLocation();
        if (!(workOrder instanceof WorkOrderBuildDecoration) && worker.getColony().getBuilding(pos) == null)
        {
            Log.getLogger().warn("AbstractBuilding does not exist - removing build request");
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        int tempRotation = 0;
        if (workOrder.getRotation() == 0 && !(workOrder instanceof WorkOrderBuildDecoration))
        {
            final IBlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof AbstractBlockHut)
            {
                tempRotation = BlockUtils.getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING));
            }
        }
        else
        {
            tempRotation = workOrder.getRotation();
        }

        loadStructure(workOrder.getStructureName(), tempRotation, pos);

        workOrder.setCleared(false);
        workOrder.setRequested(false);

        requestMaterials();
    }

    /**
     * Searches a handy block to substitute a non-solid space which should be guaranteed solid.
     * @param location the location the block should be at.
     * @return the Block.
     */
    public abstract Block getSolidSubstitution(BlockPos location);

    /**
     * Loads the structure given the name, rotation and position.
     * @param name the name to retrieve  it.
     * @param rotateTimes number of times to rotate it.
     * @param position the position to set it.
     */
    public void loadStructure(@NotNull final String name, int rotateTimes, BlockPos position)
    {
        if(job instanceof AbstractJobStructure)
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

            ((AbstractJobStructure) job).getStructure().rotate(rotateTimes);
            ((AbstractJobStructure) job).getStructure().setPosition(position);
        }
    }

    private void requestMaterials()
    {
        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && job instanceof JobBuilder)
        {
            while (((JobBuilder) job).getStructure().findNextBlock())
            {
                @Nullable final Block block = ((JobBuilder) job).getStructure().getBlock();
                @NotNull final IBlockState blockState = ((JobBuilder) job).getStructure().getBlockState();

                if (((JobBuilder) job).getStructure().doesStructureBlockEqualWorldBlock()
                        || (blockState instanceof BlockBed && blockState.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.FOOT))
                        || (blockState instanceof BlockDoor && blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER)))
                {
                    continue;
                }

                final Block worldBlock = BlockPosUtil.getBlock(world, ((JobBuilder) job).getStructure().getBlockPosition());

                if (block != null
                        && block != Blocks.AIR
                        && worldBlock != Blocks.BEDROCK
                        && !(worldBlock instanceof AbstractBlockHut)
                        && !isBlockFree(block, 0))
                {
                    final AbstractBuilding building = getOwnBuilding();
                    if (building instanceof BuildingBuilder)
                    {
                        ((BuildingBuilder) building).addNeededResource(block, 1);
                    }
                }
            }
            ((JobBuilder) job).getWorkOrder().setRequested(true);
        }
    }


    /**
     * Works on clearing the area of unneeded blocks.
     *
     * @return the next step once done.
     */
    private boolean clearStep(@NotNull final Structure.StructureBlock currentBlock)
    {
        if((job instanceof JobBuilder && ((JobBuilder) job).getWorkOrder().isCleared()) || !currentStructure.getStage().equals(Structure.Stage.CLEAR))
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
            if (Configurations.builderInfiniteResources  || currentBlock.worldMetadata.getMaterial().isLiquid())
            {
                worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                world.setBlockToAir(currentBlock.blockPosition);
                world.setBlockState(currentBlock.blockPosition, Blocks.AIR.getDefaultState());
                worker.swingArm(worker.getActiveHand());
                setDelay(UNLIMITED_RESOURCES_TIMEOUT);
            }
            else
            {
                if (!mineBlock(currentBlock.blockPosition, workFrom == null ? getWorkingPosition() : workFrom))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Gets a floorPosition in a particular direction.
     *
     * @param facing   the direction.
     * @param distance the distance.
     * @return a BlockPos position.
     */
    @NotNull
    private BlockPos getPositionInDirection(final EnumFacing facing, final int distance)
    {
        return getFloor(currentStructure.getCurrentBlockPosition().offset(facing, distance));
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @return returns BlockPos position with air above.
     */
    @NotNull
    private BlockPos getFloor(@NotNull final BlockPos position)
    {
        //If the position is floating in Air go downwards
        if (!EntityUtils.solidOrLiquid(world, position))
        {
            return getFloor(position.down());
        }
        //If there is no air above the block go upwards
        if (!EntityUtils.solidOrLiquid(world, position.up()))
        {
            return position;
        }
        return getFloor(position.up());
    }

    /**
     * Check if there is a Structure to be build.
     *
     * @return true if we should start building.
     */
    private boolean isThereAStructureToBuild()
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
            workFrom = getWorkingPosition();
        }

        //The miner shouldn't search for a save position. Just let him build from where he currently is.
        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < MIN_WORKING_RANGE;
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition()
    {
        if(job instanceof JobMiner)
        {
            getNodeMiningPosition(currentStructure.getCurrentBlockPosition());
        }
        return getWorkingPosition(0);
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param offset the extra distance to apply away from the building.
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition(final int offset)
    {
        if (offset > MAX_ADDITIONAL_RANGE_TO_BUILD)
        {
            return currentStructure.getCurrentBlockPosition();
        }
        //get length or width either is larger.
        final int length = currentStructure.getLength();
        final int width = currentStructure.getWidth();
        final int distance = Math.max(width, length) + MIN_ADDITIONAL_RANGE_TO_BUILD + offset;
        @NotNull final EnumFacing[] directions = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};

        //then get a solid place with two air spaces above it in any direction.
        for (final EnumFacing direction : directions)
        {
            @NotNull final BlockPos positionInDirection = getPositionInDirection(direction, distance);
            if (EntityUtils.checkForFreeSpace(world, positionInDirection))
            {
                return positionInDirection;
            }
        }

        //if necessary we can could implement calling getWorkingPosition recursively and add some "offset" to the sides.
        return getWorkingPosition(offset + 1);
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

    private boolean handleMaterials(@NotNull final Block block, @NotNull final IBlockState blockState)
    {
        //Breaking blocks doesn't require taking materials from the citizens inventory
        if (block == Blocks.AIR)
        {
            return true;
        }

        if (isBlockFree(block, block.getMetaFromState(blockState)))
        {
            return true;
        }

        return !checkOrRequestItems(BlockUtils.getItemStackFromBlockState(blockState));
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
                || (block.equals(Blocks.DOUBLE_PLANT) && Utils.testFlag(metadata, 0x08))
                || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08))
                || block.equals(Blocks.GRASS);
    }

    private void placeBlockAt(@NotNull final Block block, @NotNull final IBlockState blockState, @NotNull final BlockPos coords)
    {
        if (block == Blocks.AIR)
        {
            worker.resetActiveHand();
            worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);

            if (!world.setBlockToAir(coords))
            {
                Log.getLogger().error(String.format("Block break failure at %s", coords));
            }
        }
        else
        {
            final Item item = Item.getItemFromBlock(block);
            worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item == null ? ItemStack.EMPTY : new ItemStack(item, 1));

            if (!placeBlock(coords, block, blockState))
            {
                Log.getLogger().error(String.format("Block place failure %s at %s", block.getUnlocalizedName(), coords));
            }
            worker.swingArm(worker.getActiveHand());
        }
    }

    private boolean placeBlock(@NotNull final BlockPos pos, final Block block, @NotNull final IBlockState blockState)
    {
        //Move out of the way when placing blocks
        if (MathHelper.floor(worker.posX) == pos.getX()
                && MathHelper.abs(pos.getY() - (int) worker.posY) <= 1
                && MathHelper.floor(worker.posZ) == pos.getZ()
                && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, RUN_AWAY_SPEED, 1.0);
        }

        //Workaround as long as we didn't rescan all of our buildings since BlockStairs now have different metadata values.
        if (blockState.getBlock() instanceof BlockStairs
                && world.getBlockState(pos).getBlock() instanceof BlockStairs
                && world.getBlockState(pos).getValue(BlockStairs.FACING) == blockState.getValue(BlockStairs.FACING)
                && blockState.getBlock() == world.getBlockState(pos).getBlock())
        {
            return true;
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && world.getBlockState(pos).getBlock() != Blocks.AIR)
        {
            final List<ItemStack> items = BlockPosUtil.getBlockDrops(world, pos, 0);
            for (final ItemStack item : items)
            {
                InventoryUtils.setStack(worker.getInventoryCitizen(), item);
            }
        }

        if (block instanceof BlockDoor)
        {
            if (blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
            {
                ItemDoor.placeDoor(world, pos, blockState.getValue(BlockDoor.FACING), block, false);
            }
        }
        else if (block instanceof BlockBed)
        {
            world.setBlockState(pos, blockState, 0x03);
            final EnumFacing facing = blockState.getValue(BlockBed.FACING);

            //Set other part of the bed, to the opposite PartType
            if (blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
            {
                //pos.offset(facing) will get the other part of the bed
                world.setBlockState(pos, blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT), 0x03);
                world.setBlockState(pos.offset(facing), blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD), 0x03);
            }
            else
            {
                return true;
            }
        }
        else if (block instanceof BlockDoublePlant)
        {
            world.setBlockState(pos, blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 0x03);
            world.setBlockState(pos.up(), blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 0x03);
        }
        else if (block instanceof BlockEndPortal || block instanceof BlockMobSpawner || block instanceof BlockDragonEgg || block instanceof BlockPortal)
        {
            return true;
        }
        else
        {
            if (!world.setBlockState(pos, blockState, 0x03))
            {
                return false;
            }
        }

        //It will crash at blocks like water which is actually free, we don't have to decrease the stacks we have.
        if (isBlockFree(block, block.getMetaFromState(blockState)))
        {
            return true;
        }

        @Nullable final ItemStack stack = BlockUtils.getItemStackFromBlockState(blockState);
        if (stack == null)
        {
            Log.getLogger().error("Block causes NPE: " + blockState.getBlock());
            return false;
        }

        final int slot = worker.findFirstSlotInInventoryWith(stack.getItem());
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
            reduceNeededResources(block);
        }
        return true;
    }

    /**
     * Reduces the needed resources by 1.
     *
     * @param block the block which has been used now.
     */
    public void reduceNeededResources(final Block block)
    {
        final AbstractBuilding workerBuilding = this.getOwnBuilding();
        if (workerBuilding instanceof BuildingBuilder)
        {
            ((BuildingBuilder) workerBuilding).reduceNeededResource(block, 1);
        }
    }

    private void spawnEntity(@Nullable final Entity entity)
    {
        if (entity != null && job instanceof JobBuilder)
        {
            final BlockPos pos = ((JobBuilder) job).getStructure().getOffsetPosition();

            if (entity instanceof EntityHanging)
            {
                @NotNull final EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.posX += pos.getX();
                entityHanging.posY += pos.getY();
                entityHanging.posZ += pos.getZ();
                //also sets position based on tile
                entityHanging.setPosition(
                        entityHanging.getHangingPosition().getX(),
                        entityHanging.getHangingPosition().getY(),
                        entityHanging.getHangingPosition().getZ());

                entityHanging.setWorld(world);
                entityHanging.dimension = world.provider.getDimension();

                world.spawnEntity(entityHanging);
            }
            else if (entity instanceof EntityMinecart)
            {
                @Nullable final EntityMinecart minecart = (EntityMinecart) entity;
                minecart.posX += pos.getX();
                minecart.posY += pos.getY();
                minecart.posZ += pos.getZ();

                minecart.setWorld(world);
                minecart.dimension = world.provider.getDimension();

                world.spawnEntity(minecart);
            }
        }
    }

    /**
     * Create a save mining position for the miner.
     * @param blockToMine block which should be mined or placed.
     * @return the save position.
     */
    private BlockPos getNodeMiningPosition(BlockPos blockToMine)
    {
        if(((BuildingMiner) getOwnBuilding()).getCurrentLevel() == null)
        {
            return blockToMine;
        }
        final Point2D pos = ((BuildingMiner) getOwnBuilding()).getCurrentLevel().getRandomNode().getParent();
        return new BlockPos(pos.getX(), ((BuildingMiner) getOwnBuilding()).getCurrentLevel().getDepth(), pos.getY());
    }
}
