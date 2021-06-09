package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.util.WorldUtil;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.minecraft.block.DoorBlock.OPEN;

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
    private static final double MIN_DISTANCE = 4D;

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

    /**
     * Offset delay to prevent toggling at exactly the same rates
     */
    private final int offSet;

    public EntityAIInteractToggleAble(@NotNull final MobEntity entityIn, final ToggleAble... toggleAbles)
    {
        super();
        this.entity = entityIn;
        this.toggleAbles = Arrays.asList(toggleAbles);
        if (!(entityIn.getNavigation() instanceof GroundPathNavigator))
        {
            throw new IllegalArgumentException("Unsupported mob type for EntityAIInteractToggleAble");
        }

        offSet = entityIn.level.random.nextInt(20);
    }

    /**
     * Checks if the Interaction should be executed.
     *
     * @return true or false depending on the conditions.
     */
    @Override
    public boolean canUse()
    {
        // Reactive check for detected collisions
        if (this.entity.horizontalCollision || entity.verticalCollision && !entity.isOnGround())
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
        @NotNull final GroundPathNavigator pathnavigateground = (GroundPathNavigator) this.entity.getNavigation();
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
        if (path == null || path.isDone())
        {
            resetAll();
            return;
        }

        final int maxLengthToCheck = Math.min(path.getNextNodeIndex() + LENGTH_TO_CHECK, path.getNodeCount());
        for (int i = Math.max(0, path.getNextNodeIndex() - 1); i < maxLengthToCheck; i++)
        {
            if (i == path.getNodeCount() - 1)
            {
                // Reached path end
                return;
            }

            // We need current + next to determine the move direction to find out whether sth is blocking in that way
            final PathPoint current = path.getNode(i);
            final PathPoint next = path.getNode(i + 1);

            // Skip same nodes
            if (next.x == current.x && next.y == current.y && next.z == current.z)
            {
                continue;
            }

            // Find the moving direction
            final Direction dir;
            if (current.x == next.x && current.z == next.z)
            {
                // Up/Down we just use east
                dir = Direction.EAST;
            }
            else
            {
                dir = Direction.getNearest(next.x - current.x, 0, next.z - current.z);
            }

            // Check necessary height levels
            for (int level = 0; level < getHeightToCheck(path, i); level++)
            {
                checkPosAndAdd(entity, dir, new BlockPos(current.x, current.y + level, current.z));
                checkPosAndAdd(entity, dir, new BlockPos(next.x, next.y + level, next.z));
            }
        }
    }

    /**
     * Checks if the pos has a toggleable hindering movement in the given direction and adds it to our toggle positions
     *
     * @param entity entity to check for
     * @param dir    Direction to check in
     * @param pos    position to check
     */
    private void checkPosAndAdd(final Entity entity, Direction dir, final BlockPos pos)
    {
        if (toggleAblePositions.containsKey(pos))
        {
            return;
        }

        final BlockState state = entity.level.getBlockState(pos);
        if (this.entity.distanceToSqr(pos.getX(), this.entity.getY(), pos.getZ()) <= MIN_DISTANCE && isValidBlockState(state))
        {
            // See if current pos collision shape can fit our entity in
            final VoxelShape collisionShape = state.getCollisionShape(entity.level, pos);
            dir = dir.getClockWise();
            if (collisionShape.min(dir.getAxis()) + 0.1 < entity.getBbWidth() && collisionShape.max(dir.getAxis()) + 0.1 + entity.getBbWidth() > 1)
            {
                toggleAblePositions.put(pos, state.getValue(BlockStateProperties.OPEN));
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
        @NotNull final GroundPathNavigator pathnavigateground = (GroundPathNavigator) this.entity.getNavigation();
        final Path path = pathnavigateground.getPath();

        if (path == null || path.isDone())
        {
            resetAll();
            return false;
        }

        final int maxLengthToCheck = Math.min(path.getNextNodeIndex() + LENGTH_TO_CHECK, path.getNodeCount());
        for (int i = Math.max(0, path.getNextNodeIndex() - 1); i < maxLengthToCheck; ++i)
        {
            final PathPoint pathpoint = path.getNode(i);

            for (int level = 0; level < getHeightToCheck(path, i); level++)
            {
                BlockPos pos = new BlockPos(pathpoint.x, pathpoint.y + level, pathpoint.z);

                // We only allows blocks we're on or right above
                if (!entity.blockPosition().equals(pos) && !entity.blockPosition().below().equals(pos))
                {
                    continue;
                }

                BlockState state = entity.level.getBlockState(pos);
                if (this.entity.distanceToSqr(pos.getX(), entity.getY(), pos.getZ()) <= MIN_DISTANCE && isValidBlockState(state))
                {
                    if (level > 0)
                    {
                        // Above current pathing node, so need to use this toggleable block
                        toggleAblePositions.put(pos, entity.level.getBlockState(pos).getValue(BlockStateProperties.OPEN));
                    }
                    else if (i < path.getNodeCount() - 1)
                    {
                        // Check if the next pathing node is below
                        final PathPoint nextPoint = path.getNode(i + 1);
                        if (pos.getX() == nextPoint.x && pos.getY() > nextPoint.y && pos.getZ() == nextPoint.z)
                        {
                            toggleAblePositions.put(pos, entity.level.getBlockState(pos).getValue(BlockStateProperties.OPEN));
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
        if (path == null || index < 0 || index >= path.getNodeCount())
        {
            return DEFAULT_HEIGHT_TO_CHECK;
        }

        final PathPoint current = path.getNode(index);

        int prevDist = 0;
        if (index > 0)
        {
            final PathPoint prev = path.getNode(index - 1);
            prevDist = prev.y - current.y;
        }

        int nextDist = 0;
        if (index + 1 < path.getNodeCount())
        {
            final PathPoint next = path.getNode(index + 1);
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
    public boolean canContinueToUse()
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
            for (final ToggleAble toggleAble : toggleAbles)
            {
                final BlockState state = entity.level.getBlockState(pos);
                if (toggleAble.isBlockToggleAble(state))
                {
                    toggleAble.toggleBlockClosed(state, entity.level, pos);
                    break;
                }
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
        updateTimer = 20 + offSet;

        if (!checkPath())
        {
            return;
        }

        final Iterator<BlockPos> it = toggleAblePositions.keySet().iterator();
        final List<BlockPos> posList = new ArrayList<>();

        while (it.hasNext())
        {
            final BlockPos pos = it.next();
            final BlockState state = entity.level.getBlockState(pos);

            // Recheck validity maybe the block changed
            if (!isValidBlockState(state))
            {
                it.remove();
                continue;
            }

            if (this.entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > MAX_DISTANCE)
            {
                it.remove();
                final BlockState blockState = entity.level.getBlockState(pos);
                for (final ToggleAble toggleAble : toggleAbles)
                {
                    if (toggleAble.isBlockToggleAble(blockState))
                    {
                        toggleAble.toggleBlockClosed(blockState, entity.level, pos);
                        break;
                    }
                }
                continue;
            }

            posList.add(pos);
        }

        if (!posList.isEmpty())
        {
            final BlockPos chosen = posList.get(entity.level.random.nextInt(posList.size()));
            {
                final BlockState state = entity.level.getBlockState(chosen);
                for (final ToggleAble toggleAble : toggleAbles)
                {
                    if (toggleAble.isBlockToggleAble(state))
                    {
                        toggleAble.toggleBlock(state, entity.level, chosen);
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
            WorldUtil.setBlockState(world, pos, state.cycle(BlockStateProperties.OPEN));
        }

        @Override
        public void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos)
        {
            WorldUtil.setBlockState(world, pos, state.setValue(BlockStateProperties.OPEN, false));
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
            WorldUtil.setBlockState(world, pos, state.cycle(BlockStateProperties.OPEN));
        }

        @Override
        public void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos)
        {
            WorldUtil.setBlockState(world, pos, state.setValue(BlockStateProperties.OPEN, false));
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
            // Custom vanilla doors opening logic
            if (state.getBlock().getClass() == DoorBlock.class)
            {
                final boolean isOpening = !state.getValue(BlockStateProperties.OPEN);
                WorldUtil.setBlockState(world, pos, state.setValue(OPEN, isOpening), 10);

                final BlockPos otherPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
                final BlockState otherState = world.getBlockState(otherPos);
                if (otherState.getBlock().getClass() == DoorBlock.class)
                {
                    WorldUtil.setBlockState(world, otherPos, otherState.setValue(OPEN, isOpening), 10);
                }

                ((DoorBlock) state.getBlock()).playSound(world, pos, isOpening);
            }
            else
            {
                ((DoorBlock) state.getBlock()).setOpen(world, state, pos, !state.getValue(BlockStateProperties.OPEN));
            }
        }

        @Override
        public void toggleBlockClosed(final BlockState state, final World world, final BlockPos pos)
        {
            ((DoorBlock) state.getBlock()).setOpen(world, state, pos, false);
        }
    }
}
