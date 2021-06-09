package com.minecolonies.api.entity.pathfinding;

import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Stuck handler for pathing
 */
public class PathingStuckHandler implements IStuckHandler
{
    /**
     * The distance at which we consider a target to arrive
     */
    private static final double MIN_TARGET_DIST = 3;

    /**
     * All directions.
     */
    private final List<Direction> directions = Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    /**
     * Amount of path steps allowed to teleport on stuck, 0 = disabled
     */
    private int teleportRange = 0;

    /**
     * Max timeout per block to go, default = 10sec per block
     */
    private int timePerBlockDistance = 200;

    /**
     * The current stucklevel, determines actions taken
     */
    private int stuckLevel = 0;

    /**
     * Global timeout counter, used to determine when we're completly stuck
     */
    private int globalTimeout = 0;

    /**
     * The previously desired go to position of the entity
     */
    private BlockPos prevDestination = BlockPos.ZERO;

    /**
     * Whether breaking blocks is enabled
     */
    private boolean canBreakBlocks = false;

    /**
     * Whether placing ladders is enabled
     */
    private boolean canPlaceLadders = false;

    /**
     * Whether leaf bridges are enabled
     */
    private boolean canBuildLeafBridges = false;

    /**
     * Whether teleport to goal at full stuck is enabled
     */
    private boolean canTeleportGoal = false;

    /**
     * Whether take damage on stuck is enabled
     */
    private boolean takeDamageOnCompleteStuck = false;
    private float   damagePct                 = 0.2f;

    /**
     * BLock break range on complete stuck
     */
    private int completeStuckBlockBreakRange = 0;

    /**
     * Temporary comparison variables to compare with last update
     */
    private boolean hadPath         = false;
    private int     lastPathIndex   = -1;
    private int     progressedNodes = 0;

    /**
     * Delay before taking unstuck actions in ticks, default 60 seconds
     */
    private int delayBeforeActions       = 10 * 20;
    private int delayToNextUnstuckAction = delayBeforeActions;

    private Random rand = new Random();

    private PathingStuckHandler()
    {
    }

    /**
     * Creates a new stuck handler
     *
     * @return new stuck handler
     */
    public static PathingStuckHandler createStuckHandler()
    {
        return new PathingStuckHandler();
    }

    /**
     * Checks the entity for stuck
     *
     * @param navigator navigator to check
     */
    @Override
    public void checkStuck(final AbstractAdvancedPathNavigate navigator)
    {
        if (navigator.getDesiredPos() == null || navigator.getDesiredPos().equals(BlockPos.ZERO))
        {
            resetGlobalStuckTimers();
            return;
        }

        if (navigator.getOurEntity() instanceof IStuckHandlerEntity && !((IStuckHandlerEntity) navigator.getOurEntity()).canBeStuck())
        {
            return;
        }

        final double distanceToGoal =
          navigator.getOurEntity().position().distanceTo(new Vector3d(navigator.getDesiredPos().getX(), navigator.getDesiredPos().getY(), navigator.getDesiredPos().getZ()));

        // Close enough to be considered at the goal
        if (distanceToGoal < MIN_TARGET_DIST)
        {
            resetGlobalStuckTimers();
            return;
        }

        // Global timeout check
        if (prevDestination.equals(navigator.getDesiredPos()))
        {
            globalTimeout++;

            // Try path first, if path fits target pos
            if (globalTimeout > timePerBlockDistance * distanceToGoal)
            {
                completeStuckAction(navigator);
            }
        }
        else
        {
            resetGlobalStuckTimers();
        }

        prevDestination = navigator.getDesiredPos();

        if (navigator.getPath() == null || navigator.getPath().isDone())
        {
            // With no path reset the last path index point to -1
            lastPathIndex = -1;
            progressedNodes = 0;

            // Stuck when we have no path and had no path last update before
            if (!hadPath)
            {
                tryUnstuck(navigator);
            }
        }
        else
        {
            if (navigator.getPath().getNextNodeIndex() == lastPathIndex)
            {
                // Stuck when we have a path, but are not progressing on it
                tryUnstuck(navigator);
            }
            else
            {
                if (lastPathIndex != -1 && navigator.getPath().getTarget().distSqr(prevDestination) < 25)
                {
                    progressedNodes = navigator.getPath().getNextNodeIndex() > lastPathIndex ? progressedNodes + 1 : progressedNodes - 1;

                    if (progressedNodes > 5)
                    {
                        // Not stuck when progressing
                        resetStuckTimers();
                    }
                }
            }
        }

        lastPathIndex = navigator.getPath() != null ? navigator.getPath().getNextNodeIndex() : -1;

        hadPath = navigator.getPath() != null && !navigator.getPath().isDone();
    }

    /**
     * Resets global stuck timers
     */
    private void resetGlobalStuckTimers()
    {
        globalTimeout = 0;
        prevDestination = BlockPos.ZERO;
        resetStuckTimers();
    }

    /**
     * Final action when completly stuck before resetting stuck handler and path
     */
    private void completeStuckAction(final AbstractAdvancedPathNavigate navigator)
    {
        final BlockPos desired = navigator.getDesiredPos();
        final World world = navigator.getOurEntity().level;
        final MobEntity entity = navigator.getOurEntity();

        if (canTeleportGoal)
        {
            for (final Direction dir : directions)
            {
                // need two air
                if (world.isEmptyBlock(desired.relative(dir)) && world.isEmptyBlock(desired.relative(dir).above()))
                {
                    // Teleport
                    entity.teleportTo(desired.relative(dir).getX() + 0.5d, desired.relative(dir).getY(), desired.relative(dir).getZ() + 0.5d);
                    break;
                }
            }
        }
        if (takeDamageOnCompleteStuck)
        {
            entity.hurt(new EntityDamageSource("Stuck-damage", entity), entity.getMaxHealth() * damagePct);
        }

        if (completeStuckBlockBreakRange > 0)
        {
            final Direction facing = BlockPosUtil.getFacing(new BlockPos(entity.position()), navigator.getDesiredPos());

            for (int i = 1; i <= completeStuckBlockBreakRange; i++)
            {
                if (!world.isEmptyBlock(new BlockPos(entity.position()).relative(facing, i)) || !world.isEmptyBlock(new BlockPos(entity.position()).relative(facing, i).above()))
                {
                    breakBlocksAhead(world, new BlockPos(entity.position()).relative(facing, i - 1), facing);
                    break;
                }
            }
        }

        navigator.stop();
        resetGlobalStuckTimers();
    }

    /**
     * Tries unstuck options depending on the level
     */
    private void tryUnstuck(final AbstractAdvancedPathNavigate navigator)
    {
        if (delayToNextUnstuckAction-- > 0)
        {
            return;
        }

        // Clear path
        if (stuckLevel == 0)
        {
            stuckLevel++;
            delayToNextUnstuckAction = 600;
            navigator.stop();
            return;
        }

        // Move away
        if (stuckLevel == 1)
        {
            stuckLevel++;
            delayToNextUnstuckAction = 300;
            navigator.stop();
            navigator.moveAwayFromXYZ(new BlockPos(navigator.getOurEntity().position()), 10, 1.0f);
            return;
        }

        // Skip ahead
        if (stuckLevel == 2)
        {
            if (hadPath && teleportRange > 0)
            {
                delayToNextUnstuckAction = 100;
                int index = navigator.getPath().getNextNodeIndex() + teleportRange;
                if (index < navigator.getPath().getNodeCount())
                {
                    final PathPoint togo = navigator.getPath().getNode(index);
                    navigator.getOurEntity().teleportTo(togo.x + 0.5d, togo.y, togo.z + 0.5d);
                    delayToNextUnstuckAction = 300;
                }
            }
        }

        // Place ladders & leaves
        if (stuckLevel >= 3 && stuckLevel <= 5)
        {
            if (canPlaceLadders && rand.nextBoolean())
            {
                delayToNextUnstuckAction = 300;
                placeLadders(navigator);
            }
            else if (canBuildLeafBridges && rand.nextBoolean())
            {
                delayToNextUnstuckAction = 100;
                placeLeaves(navigator);
            }
        }

        // break blocks
        if (stuckLevel == 6 && canBreakBlocks)
        {
            delayToNextUnstuckAction = 300;
            breakBlocks(navigator);
        }

        chanceStuckLevel();

        if (stuckLevel == 8)
        {
            resetStuckTimers();
        }
    }

    /**
     * Random chance to decrease to a previous level of stuck
     */
    private void chanceStuckLevel()
    {
        stuckLevel++;
        // 20 % to decrease to the previous level again
        if (stuckLevel > 1 && rand.nextInt(6) == 0)
        {
            stuckLevel -= 2;
        }
    }

    /**
     * Resets timers
     */
    private void resetStuckTimers()
    {
        delayToNextUnstuckAction = delayBeforeActions;
        lastPathIndex = -1;
        progressedNodes = 0;
        stuckLevel = 0;
    }

    /**
     * Attempt to break blocks that are blocking the entity to reach its destination.
     * @param world the world it is in.
     * @param start the position the entity is at.
     * @param facing the direction the goal is in.
     */
    private void breakBlocksAhead(final World world, final BlockPos start, final Direction facing)
    {
        // Above entity
        if (!world.isEmptyBlock(start.above(3)))
        {
            setAirIfPossible(world, start.above(3));
            return;
        }

        // In goal direction
        if (!world.isEmptyBlock(start.relative(facing)))
        {
            setAirIfPossible(world, start.relative(facing));
            return;
        }

        // Goal direction up
        if (!world.isEmptyBlock(start.above().relative(facing)))
        {
            setAirIfPossible(world, start.above().relative(facing));
        }
    }

    /**
     * Check if the block at the position is indestructible, if not, attempt to break it.
     * @param world the world the block is in.
     * @param pos the pos the block is at.
     */
    private void setAirIfPossible(final World world, final BlockPos pos)
    {
        final Block blockAtPos = world.getBlockState(pos).getBlock();
        if (blockAtPos instanceof IBuilderUndestroyable || ModTags.indestructible.contains(blockAtPos))
        {
            return;
        }
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    /**
     * Places ladders
     *
     * @param navigator navigator to use
     */
    private void placeLadders(final AbstractAdvancedPathNavigate navigator)
    {
        final World world = navigator.getOurEntity().level;
        final MobEntity entity = navigator.getOurEntity();

        BlockPos entityPos = new BlockPos(entity.position());

        while (world.getBlockState(entityPos).getBlock() == Blocks.LADDER)
        {
            entityPos = entityPos.above();
        }

        tryPlaceLadderAt(world, entityPos);
        tryPlaceLadderAt(world, entityPos.above());
        tryPlaceLadderAt(world, entityPos.above(2));
    }

    /**
     * Tries to place leaves
     *
     * @param navigator navigator to use
     */
    private void placeLeaves(final AbstractAdvancedPathNavigate navigator)
    {
        final World world = navigator.getOurEntity().level;
        final MobEntity entity = navigator.getOurEntity();

        final Direction badFacing = BlockPosUtil.getFacing(new BlockPos(entity.position()), navigator.getDesiredPos()).getOpposite();

        for (final Direction dir : directions)
        {
            if (dir == badFacing)
            {
                continue;
            }

            if (world.isEmptyBlock(new BlockPos(entity.position()).below().relative(dir)))
            {
                world.setBlockAndUpdate(new BlockPos(entity.position()).below().relative(dir), Blocks.ACACIA_LEAVES.defaultBlockState());
            }
        }
    }

    /**
     * Tries to randomly break blocks
     *
     * @param navigator navigator to use
     */
    private void breakBlocks(final AbstractAdvancedPathNavigate navigator)
    {
        final World world = navigator.getOurEntity().level;
        final MobEntity entity = navigator.getOurEntity();

        final Direction facing = BlockPosUtil.getFacing(new BlockPos(entity.position()), navigator.getDesiredPos());

        breakBlocksAhead(world, new BlockPos(entity.position()), facing);
    }

    /**
     * Tries to place a ladder at the given position
     *
     * @param world world to use
     * @param pos   position to set
     */
    private void tryPlaceLadderAt(final World world, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if (state.getBlock() != Blocks.LADDER && !state.canOcclude() && world.getFluidState(pos).isEmpty())
        {
            for (final Direction dir : directions)
            {
                final BlockState toPlace = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, dir.getOpposite());
                if (world.getBlockState(pos.relative(dir)).getMaterial().isSolid() && Blocks.LADDER.canSurvive(toPlace, world, pos))
                {
                    world.setBlockAndUpdate(pos, toPlace);
                    break;
                }
            }
        }
    }

    public PathingStuckHandler withBlockBreaks()
    {
        canBreakBlocks = true;
        return this;
    }

    public PathingStuckHandler withPlaceLadders()
    {
        canPlaceLadders = true;
        return this;
    }

    public PathingStuckHandler withBuildLeafBridges()
    {
        canBuildLeafBridges = true;
        return this;
    }

    /**
     * Enables teleporting a certain amount of steps along a generated path
     *
     * @param steps steps to teleport
     * @return this
     */
    public PathingStuckHandler withTeleportSteps(int steps)
    {
        teleportRange = steps;
        return this;
    }

    public PathingStuckHandler withTeleportOnFullStuck()
    {
        canTeleportGoal = true;
        return this;
    }

    public PathingStuckHandler withTakeDamageOnStuck(float damagePct)
    {
        this.damagePct = damagePct;
        takeDamageOnCompleteStuck = true;
        return this;
    }

    /**
     * Sets the time per block distance to travel, before timing out
     *
     * @param time in ticks to set
     * @return this
     */
    public PathingStuckHandler withTimePerBlockDistance(int time)
    {
        timePerBlockDistance = time;
        return this;
    }

    /**
     * Sets the delay before taking stuck actions
     *
     * @param delay to set
     * @return this
     */
    public PathingStuckHandler withDelayBeforeStuckActions(int delay)
    {
        delayBeforeActions = delay;
        return this;
    }

    /**
     * Sets the block break range on complete stuck
     *
     * @param range to set
     * @return this
     */
    public PathingStuckHandler withCompleteStuckBlockBreak(int range)
    {
        completeStuckBlockBreakRange = range;
        return this;
    }
}
