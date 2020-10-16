package com.minecolonies.coremod.entity.ai.minimal;

import net.minecraft.block.*;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * AI Task for toggling blocks open/closed when collided
 */
public class EntityAIInteractToggleAble extends Goal
{
    /**
     * Number of blocks to check for the fence gate - height.
     */
    private static final int DEFAULT_HEIGHT_TO_CHECK = 2;

    /**
     * Number of blocks to check for the fence gate - length.
     */
    private static final int LENGTH_TO_CHECK = 2;

    /**
     * The min distance the gate has to be from the citizen.
     */
    private static final double MIN_DISTANCE = 2.25D;

    /**
     * The max distance the gate has to be from the citizen.
     */
    private static final double MAX_DISTANCE = 6.25D;

    /**
     * Default toggleables which can be used in this AI
     */
    public static final ToggleAble FENCE_TOGGLE = new FenceToggle();
    public static final ToggleAble TRAP_TOGGLE  = new TrapToggle();
    public static final ToggleAble DOOR_TOGGLE  = new DoorToggle();


    /**
     * Our citizen.
     */
    protected MobEntity entity;

    /**
     * Map of positions and initial state
     */
    private Map<BlockPos, Boolean> toggleAblePositions = new HashMap<>();

    /**
     * List of toggleAbles
     */
    private final List<ToggleAble> toggleAbles;

    /**
     * Update timer while active, delay between toggle actions
     */
    private int updateTimer = 10;

    /**
     * Execution timer for occasionally checking for toggleables
     */
    private int executeTimerSlow = 100;

    public EntityAIInteractToggleAble(@NotNull final MobEntity entityIn, final ToggleAble... toggleAbles)
    {
        super();
        this.entity = entityIn;
        this.toggleAbles = Arrays.asList(toggleAbles);
        if (!(entityIn.getNavigator() instanceof GroundPathNavigator))
        {
            throw new IllegalArgumentException("Unsupported mob type for EntityAIInteractToggleAble");
        }
    }

    /**
     * Checks if the Interaction should be executed.
     *
     * @return true or false depending on the conditions.
     */
    @Override
    public boolean shouldExecute()
    {
        // Reactive check for detected collisions
        if (this.entity.collidedHorizontally || entity.collidedVertically && !entity.isOnGround())
        {
            return checkPath();
        }

        // Occasional checks for current path, collisions do not cover all cases
        if (executeTimerSlow-- <= 0)
        {
            executeTimerSlow = 50;
            return checkPathBlocksBelow();
        }

        return false;
    }

    /**
     * Checks if there exists a path.
     *
     * @return true if the fence gate can be passed.
     */
    private boolean checkPath()
    {
        @NotNull final GroundPathNavigator pathnavigateground = (GroundPathNavigator) this.entity.getNavigator();
        final Path path = pathnavigateground.getPath();
        checkPathBlocksCollided(path);
        return !toggleAblePositions.isEmpty();
    }

    /**
     * Checks the path blocks when collided with something
     *
     * @param path the path through the fence.
     */
    private void checkPathBlocksCollided(final Path path)
    {
        if (path == null || path.isFinished())
        {
            resetAll();
            return;
        }

        final int maxLengthToCheck = Math.min(path.getCurrentPathIndex() + LENGTH_TO_CHECK, path.getCurrentPathLength());
        for (int i = Math.max(0, path.getCurrentPathIndex() - 1); i < maxLengthToCheck; ++i)
        {
            final PathPoint pathpoint = path.getPathPointFromIndex(i);

            for (int level = 0; level < getHeightToCheck(path, i); level++)
            {
                BlockPos pos = new BlockPos(pathpoint.x, pathpoint.y + level, pathpoint.z);
                BlockState state = entity.world.getBlockState(pos);
                if (this.entity.getDistanceSq(pos.getX(), this.entity.getPosY(), pos.getZ()) <= MIN_DISTANCE && isValidBlockState(state))
                {
                    if (i < path.getCurrentPathLength() - 1)
                    {
                        final PathPoint next = path.getPathPointFromIndex(i + 1);

                        // Skip same nodes
                        if (next.x == pathpoint.x && next.y == pathpoint.y && next.z == pathpoint.z)
                        {
                            continue;
                        }

                        final Direction dir;
                        if (pathpoint.x == next.x && pathpoint.z == next.z)
                        {
                            // Up/Down we just use east
                            dir = Direction.EAST;
                        }
                        else
                        {
                            dir = Direction.getFacingFromVector(pathpoint.x - next.x, 0, pathpoint.z - next.z).rotateY();
                        }

                        // See if collision shape can fit our entity in
                        final VoxelShape collisionShape = state.getCollisionShape(entity.world, pos);
                        if (collisionShape.getStart(dir.getAxis()) + 0.1 < entity.getWidth() && collisionShape.getEnd(dir.getAxis()) + 0.1 + entity.getWidth() > 1)
                        {
                            toggleAblePositions.put(pos, state.get(BlockStateProperties.OPEN));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the path for toggleables below, where we need to go through
     *
     * @return true if there is a toggleable block below us we need to go through
     */
    private boolean checkPathBlocksBelow()
    {
        @NotNull final GroundPathNavigator pathnavigateground = (GroundPathNavigator) this.entity.getNavigator();
        final Path path = pathnavigateground.getPath();

        if (path == null || path.isFinished())
        {
            resetAll();
            return false;
        }

        final int maxLengthToCheck = Math.min(path.getCurrentPathIndex() + LENGTH_TO_CHECK, path.getCurrentPathLength());
        for (int i = Math.max(0, path.getCurrentPathIndex() - 1); i < maxLengthToCheck; ++i)
        {
            final PathPoint pathpoint = path.getPathPointFromIndex(i);

            for (int level = 0; level < getHeightToCheck(path, i); level++)
            {
                BlockPos pos = new BlockPos(pathpoint.x, pathpoint.y + level, pathpoint.z);

                // We only allows blocks we're on or right above
                if (!entity.getPosition().equals(pos) && !entity.getPosition().down().equals(pos))
                {
                    continue;
                }

                BlockState state = entity.world.getBlockState(pos);
                if (this.entity.getDistanceSq(pos.getX(), entity.getPosY(), pos.getZ()) <= MIN_DISTANCE && isValidBlockState(state))
                {
                    if (level > 0)
                    {
                        // Above current pathing node, so need to use this toggleable block
                        toggleAblePositions.put(pos, entity.world.getBlockState(pos).get(BlockStateProperties.OPEN));
                    }
                    else if (i < path.getCurrentPathLength() - 1)
                    {
                        // Check if the next pathing node is below
                        final PathPoint nextPoint = path.getPathPointFromIndex(i + 1);
                        if (pos.getX() == nextPoint.x && pos.getY() > nextPoint.y && pos.getZ() == nextPoint.z)
                        {
                            toggleAblePositions.put(pos, entity.world.getBlockState(pos).get(BlockStateProperties.OPEN));
                        }
                    }
                }
            }
        }

        return !toggleAblePositions.isEmpty();
    }

    /**
     * Gets the required height to check for the given index. By default it is two blocks, and increases by the y diff if the next or previous node are higher
     *
     * @param path  to check
     * @param index to use
     * @return height to check
     */
    private int getHeightToCheck(final Path path, final int index)
    {
        if (path == null || index < 0 || index >= path.getCurrentPathLength())
        {
            return DEFAULT_HEIGHT_TO_CHECK;
        }

        final PathPoint current = path.getPathPointFromIndex(index);

        int prevDist = 0;
        if (index > 0)
        {
            final PathPoint prev = path.getPathPointFromIndex(index - 1);
            prevDist = prev.y - current.y;
        }

        int nextDist = 0;
        if (index + 1 < path.getCurrentPathLength())
        {
            final PathPoint next = path.getPathPointFromIndex(index + 1);
            nextDist = next.y - current.y;
        }

        return Math.max(DEFAULT_HEIGHT_TO_CHECK, DEFAULT_HEIGHT_TO_CHECK + Math.max(prevDist, nextDist));
    }

    /**
     * Checks if the execution is still ongoing.
     *
     * @return true or false.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !toggleAblePositions.isEmpty();
    }

    /**
     * Resets all positions to closed
     */
    private void resetAll()
    {
        for (final BlockPos pos : toggleAblePositions.keySet())
        {
            if (isValidBlockState(entity.world.getBlockState(pos)))
            {
                entity.world.setBlockState(pos, entity.world.getBlockState(pos).with(BlockStateProperties.OPEN, false));
            }
        }
        toggleAblePositions.clear();
    }

    /**
     * Test whether the given blockstate is a valid toggle block
     *
     * @param state to test
     * @return true if valid
     */
    private boolean isValidBlockState(final BlockState state)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            return false;
        }

        for (final ToggleAble toggleAble : toggleAbles)
        {
            if (toggleAble.isBlockToggleAble(state) && state.hasProperty(BlockStateProperties.OPEN))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Updates the task and checks if the citizen passed the gate already.
     */
    @Override
    public void tick()
    {
        if (--updateTimer >= 0)
        {
            return;
        }
        updateTimer = 20;

        if (!checkPath())
        {
            return;
        }

        final Iterator<BlockPos> it = toggleAblePositions.keySet().iterator();
        final List<BlockPos> posList = new ArrayList<>();

        while (it.hasNext())
        {
            final BlockPos pos = it.next();
            final BlockState state = entity.world.getBlockState(pos);

            // Recheck validity maybe the block changed
            if (!isValidBlockState(state))
            {
                it.remove();
                continue;
            }

            if (this.entity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) > MAX_DISTANCE)
            {
                it.remove();
                final BlockState blockState = entity.world.getBlockState(pos);
                for (final ToggleAble toggleAble : toggleAbles)
                {
                    if (toggleAble.isBlockToggleAble(blockState))
                    {
                        toggleAble.toggleBlockClosed(blockState, entity.world, pos);
                        break;
                    }
                }
                continue;
            }

            posList.add(pos);
        }

        if (!posList.isEmpty())
        {
            final BlockPos chosen = posList.get(entity.world.rand.nextInt(posList.size()));
            {
                final BlockState state = entity.world.getBlockState(chosen);
                for (final ToggleAble toggleAble : toggleAbles)
                {
                    if (toggleAble.isBlockToggleAble(state))
                    {
                        toggleAble.toggleBlock(state, entity.world, chosen);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Helper class for determining toggleables and toggling them.
     */
    public static abstract class ToggleAble
    {
        /**
         * Determines whether the given blockstate is valid
         *
         * @param state state to check
         * @return true if valid
         */
        public abstract boolean isBlockToggleAble(final BlockState state);

        /**
         * Toggles the given state
         *
         * @param state state to toggle
         * @param world world to use
         * @param pos   position the block is at
         */
        public abstract void toggleBlock(final BlockState state, final World world, final BlockPos pos);

        /**
         * Toggles the given state to closed
         *
         * @param state state to toggle
         * @param world world to use
         * @param pos   position the block is at
         */
        public abstract void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos);
    }

    /**
     * Toggle for fence gates
     */
    private static class FenceToggle extends ToggleAble
    {
        @Override
        public boolean isBlockToggleAble(final BlockState state)
        {
            return state.getBlock() instanceof FenceGateBlock;
        }

        @Override
        public void toggleBlock(final BlockState state, final World world, final BlockPos pos)
        {
            world.setBlockState(pos, state.func_235896_a_(BlockStateProperties.OPEN));
        }

        @Override
        public void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos)
        {
            world.setBlockState(pos, state.with(BlockStateProperties.OPEN, false));
        }
    }

    /**
     * Toggle for trap doors
     */
    private static class TrapToggle extends ToggleAble
    {
        @Override
        public boolean isBlockToggleAble(final BlockState state)
        {
            return state.getBlock() instanceof TrapDoorBlock;
        }

        @Override
        public void toggleBlock(final BlockState state, final World world, final BlockPos pos)
        {
            world.setBlockState(pos, state.func_235896_a_(BlockStateProperties.OPEN));
        }

        @Override
        public void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos)
        {
            world.setBlockState(pos, state.with(BlockStateProperties.OPEN, false));
        }
    }

    /**
     * Toggle for Doors
     */
    private static class DoorToggle extends ToggleAble
    {
        @Override
        public boolean isBlockToggleAble(final BlockState state)
        {
            return state.getBlock() instanceof DoorBlock;
        }

        @Override
        public void toggleBlock(final BlockState state, final World world, final BlockPos pos)
        {
            ((DoorBlock) state.getBlock()).openDoor(world, state, pos, !state.get(BlockStateProperties.OPEN));
        }

        @Override
        public void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos)
        {
            ((DoorBlock) state.getBlock()).openDoor(world, state, pos,false);
        }
    }

}
