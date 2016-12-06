package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.Structure;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.minecolonies.coremod.entity.ai.util.AIState.IDLE;
import static com.minecolonies.coremod.entity.ai.util.AIState.START_WORKING;

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
    private static final int    MIN_WORKING_RANGE             = 7;

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
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING),
          /**
           * Check if we have to build something.
           */
          new AITarget(this::isThereAStructureToBuild, () -> AIState.START_BUILDING),
          /**
           * Select the appropriate State to do next.
           */
          new AITarget(AIState.START_BUILDING, this::startBuilding),
          /**
           * Clear out the building area.
           */
          new AITarget(AIState.CLEAR_STEP, generateStructureGenerator(this::clearStep, AIState.BUILDER_STRUCTURE_STEP)),
          /**
           * Build the structure and foundation of the building.
           * todo: implement
           */
          new AITarget(AIState.BUILDING_STEP, () -> AIState.IDLE),
          /**
           * Decorate the AbstractBuilding with torches etc.
           * todo: implement
           */
          new AITarget(AIState.DECORATION_STEP, () -> AIState.IDLE),
          /**
           * Spawn entities on the structure.
           * todo: implement
           */
          new AITarget(AIState.SPAWN_STEP, () -> AIState.IDLE),
          /**
           * Finalize the building and give back control to the ai.
           * todo: implement
           */
          new AITarget(AIState.COMPLETE_BUILD, () -> AIState.IDLE)
        );
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
        if(currentStructure == null)
        {
            return () -> getState();
        }

        //do not replace with method reference, this one stays the same on changing reference for currentStructure
        //URGENT: DO NOT REPLACE FOR ANY MEANS THIS WILL CRASH THE GAME.
        @NotNull final Supplier<Structure.StructureBlock> getCurrentBlock = () -> currentStructure.getCurrentBlock();
        @NotNull final Supplier<Structure.Result> advanceBlock = () -> currentStructure.advanceBlock();

        return () ->
        {
            if(checkForLostWorkOrder())
            {
                return nextState;
            }

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

        int rotation = 0;
        if (workOrder.getRotation() == 0 && !(workOrder instanceof WorkOrderBuildDecoration))
        {
            final IBlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof AbstractBlockHut)
            {
                rotation = BlockUtils.getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING));
            }
        }
        else
        {
            rotation = workOrder.getRotation();
        }

        loadStructure(workOrder.getStructureName(), rotation, pos);

        workOrder.setCleared(false);
        workOrder.setRequested(false);
    }

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
            try
            {
                StructureWrapper wrapper = new StructureWrapper(world, name);
                ((AbstractJobStructure) job).setStructure(wrapper);
                currentStructure = new Structure(world, wrapper, job instanceof JobMiner ? Structure.Stage.BUILD : Structure.Stage.CLEAR);
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

    /**
     * Checks if the workOrder or structure is lost or the AI is in an invalid state.
     * @return true if invalid execution, try next state.
     */
    private boolean checkForLostWorkOrder()
    {
        if(job instanceof JobBuilder)
        {
            final WorkOrderBuild wo = ((JobBuilder) job).getWorkOrder();

            if(wo == null || (getState().equals(AIState.CLEAR_STEP) && wo.isCleared()))
            {
                return true;
            }

            if (((JobBuilder) job).getStructure() == null)
            {
                //fix for bad structures
                ((JobBuilder) job).complete();
                return true;
            }
        }
        else
        {
            if (getState().equals(AIState.CLEAR_STEP))
            {
                return true;
            }
        }
        return false;
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
        return job instanceof JobMiner
                || worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE)
                || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < MIN_WORKING_RANGE;
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
     * Works on clearing the area of unneeded blocks.
     *
     * @return the next step once done.
     */
    private boolean clearStep(@NotNull final Structure.StructureBlock currentBlock)
    {
        //Don't break bedrock etc.
        if (!BlockUtils.shouldNeverBeMessedWith(currentBlock.worldBlock))
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position.
            if (walkToConstructionSite())
            {
                return false;
            }

            worker.faceBlock(currentBlock.blockPosition);

            //We need to deal with materials
            if (Configurations.builderInfiniteResources  || currentBlock.worldMetadata.getMaterial().isLiquid())
            {
                worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
                world.setBlockToAir(currentBlock.blockPosition);
                worker.swingArm(worker.getActiveHand());
                setDelay(UNLIMITED_RESOURCES_TIMEOUT);
            }
            else
            {
                if (!mineBlock(currentBlock.blockPosition))
                {
                    return false;
                }
            }
        }
        //todo request materials
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
}
