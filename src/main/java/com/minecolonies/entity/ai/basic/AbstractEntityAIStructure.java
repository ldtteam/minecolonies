package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.entity.ai.util.Structure;
import com.minecolonies.util.BlockUtils;
import com.minecolonies.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * This base ai class is used by ai's who need to build entire structures.
 * These structures have to be supplied as schematics files.
 * <p>
 * Once an ai starts building a structure, control over it is only given back once that is done.
 * <p>
 * If the ai resets, the structure is gone,
 * so just restart building and no progress will be reset
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIStructure<J extends Job> extends AbstractEntityAIInteract<J>
{

    /**
     * The minimum range to keep from the current building place
     */
    private static final int MIN_ADDITIONAL_RANGE_TO_BUILD = 3;
    /**
     * The maximum range to keep from the current building place
     */
    private static final int MAX_ADDITIONAL_RANGE_TO_BUILD = 25;
    /**
     * The amount of blocks away from his working position until the builder will build
     */
    private static final int BUILDING_WALK_RANGE           = 10;
    /**
     * The current structure task to be build.
     */
    private Structure currentStructure;
    /**
     * Position where the Builders constructs from.
     */
    private BlockPos  workFrom;

    /**
     * Creates this ai base class and set's up important things.
     * <p>
     * Always use this constructor!
     *
     * @param job the job class of the ai using this base class
     */
    protected AbstractEntityAIStructure(J job)
    {
        super(job);
        this.registerTargets(
                /**
                 * Check if we have to build something.
                 */
                new AITarget(this::isThereAStructureToBuild, () -> AIState.START_BUILDING),
                /**
                 * Select the appropriate State to do next.
                 */
                new AITarget(AIState.START_BUILDING, this::startBuilding),
                /**
                 * Clear out the building area
                 * todo: implement
                 */
                new AITarget(AIState.CLEAR_STEP, this::clearStep),
                /**
                 * Build the structure and foundation of the building
                 * todo: implement
                 */
                new AITarget(AIState.BUILDING_STEP, () -> AIState.IDLE),
                /**
                 * Decorate the Building with torches etc.
                 * todo: implement
                 */
                new AITarget(AIState.DECORATION_STEP, () -> AIState.IDLE),
                /**
                 * Spawn entities on the structure
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
     * Walk to the current construction site.
     * <p>
     * Calculates and caches the position where to walk to.
     *
     * @return true while walking to the site
     */
    private boolean walkToConstructionSite()
    {
        if (workFrom == null)
        {
            workFrom = getWorkingPosition();
        }
        return walkToBlock(workFrom, BUILDING_WALK_RANGE);
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
     * @param offset the extra distance to apply away from the building
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition(int offset)
    {
        if (offset > MAX_ADDITIONAL_RANGE_TO_BUILD)
        {
            return currentStructure.getCurrentBlockPosition();
        }
        //get length or width either is larger.
        int          length     = currentStructure.getLength();
        int          width      = currentStructure.getWidth();
        int          distance   = Math.max(width, length) + MIN_ADDITIONAL_RANGE_TO_BUILD + offset;
        EnumFacing[] directions = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};

        //then get a solid place with two air spaces above it in any direction.
        for (EnumFacing direction : directions)
        {
            BlockPos positionInDirection = getPositionInDirection(direction, distance);
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
     * @return the next step once done
     */
    private AIState clearStep()
    {


        //get the current working position and block
        BlockPos coordinates = currentStructure.getCurrentBlockPosition();
        Block    worldBlock  = world.getBlockState(coordinates).getBlock();

        //Don't break bedrock etc.
        if (!BlockUtils.shouldNeverBeMessedWith(worldBlock))
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position
            if (walkToConstructionSite())
            {
                return this.getState();
            }

            worker.faceBlock(coordinates);
            //We need to deal with materials
            if (Configurations.builderInfiniteResources)
            {
                worker.setCurrentItemOrArmor(0, null);

                world.setBlockToAir(coordinates);
                worker.swingItem();
            }
            else
            {
                if (!mineBlock(coordinates))
                {
                    return this.getState();
                }
            }
        }

        return this.getState();
    }

    /**
     * Gets a floorPosition in a particular direction
     *
     * @param facing   the direction
     * @param distance the distance
     * @return a BlockPos position.
     */
    private BlockPos getPositionInDirection(EnumFacing facing, int distance)
    {
        return getFloor(currentStructure.getCurrentBlockPosition().offset(facing, distance));
    }

    /**
     * Calculates the floor level
     *
     * @param position input position
     * @return returns BlockPos position with air above
     */
    private BlockPos getFloor(BlockPos position)
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
