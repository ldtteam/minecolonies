package com.minecolonies.core.entity.pathfinding.navigation;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.entity.ai.workers.util.IBuilderUndestroyable;
import com.minecolonies.api.entity.pathfinding.IStuckHandler;
import com.minecolonies.api.entity.pathfinding.IStuckHandlerEntity;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static com.minecolonies.api.util.BlockPosUtil.HORIZONTAL_DIRS;

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
     * Constants related to tp.
     */
    private static final int MIN_TP_DELAY    = 120 * 20;
    private static final int MIN_DIST_FOR_TP = 10;

    /**
     * Rough amount of ticks taken to travel one block
     */
    private static final int TICKS_PER_BLOCK = 7;

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
     * Chance to bypass moving away.
     */
    private double chanceToByPassMovingAway = 0;

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

    /**
     * The start position of moving away unstuck
     */
    private BlockPos  moveAwayStartPos = BlockPos.ZERO;
    private Direction movingAwayDir    = Direction.EAST;

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
            resetGlobalStuckTimers();
            return;
        }

        final double distanceToGoal =
          navigator.getOurEntity().position().distanceTo(new Vec3(navigator.getDesiredPos().getX(), navigator.getDesiredPos().getY(), navigator.getDesiredPos().getZ()));

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
            if (globalTimeout > Math.max(MIN_TP_DELAY, timePerBlockDistance * Math.max(MIN_DIST_FOR_TP, distanceToGoal)))
            {
                completeStuckAction(navigator);
            }
        }
        else
        {
            resetGlobalStuckTimers();
        }

        delayToNextUnstuckAction--;
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
                if (lastPathIndex != -1)
                {
                    if (lastPathIndex != navigator.getPath().getNextNodeIndex())
                    {
                        // Delay next action when the entity is moving
                        delayToNextUnstuckAction = Math.max(delayToNextUnstuckAction, 100);
                    }
                    else if (lastPathIndex < 2 && navigator.getPath().getNodeCount() > 2)
                    {
                        // Skip ahead on the node index, incase the starting position is bad
                        navigator.getPath().setNextNodeIndex(2);
                    }

                    if ((stuckLevel == 0 || navigator.getPath().getTarget().distSqr(prevDestination) < 25))
                    {
                        progressedNodes = navigator.getPath().getNextNodeIndex() > lastPathIndex ? progressedNodes + 1 : progressedNodes - 1;

                        if (progressedNodes > 5 && (navigator.getPath().getEndNode() == null || !moveAwayStartPos.equals(navigator.getPath().getEndNode().asBlockPos())))
                        {
                            // Not stuck when progressing
                            resetStuckTimers();
                        }
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
        final Level world = navigator.getOurEntity().level();
        final Mob entity = navigator.getOurEntity();

        if (canTeleportGoal)
        {
            final BlockPos tpPos = BlockPosUtil.findAround(world, desired, 10, 10,
              (posworld, pos) -> SurfaceType.getSurfaceType(posworld, posworld.getBlockState(pos.below()), pos.below()) == SurfaceType.WALKABLE
                                   && SurfaceType.getSurfaceType(posworld, posworld.getBlockState(pos), pos) == SurfaceType.DROPABLE
                                   && SurfaceType.getSurfaceType(posworld, posworld.getBlockState(pos.above()), pos.above()) == SurfaceType.DROPABLE);
            if (tpPos != null)
            {
                entity.teleportTo(tpPos.getX() + 0.5, tpPos.getY(), tpPos.getZ() + 0.5);
            }
        }
        if (takeDamageOnCompleteStuck)
        {
            entity.hurt(world.damageSources().source(DamageSourceKeys.STUCK_DAMAGE), entity.getMaxHealth() * damagePct);
        }

        if (completeStuckBlockBreakRange > 0)
        {
            final Direction facing = BlockPosUtil.getFacing(BlockPos.containing(entity.position()), navigator.getDesiredPos());

            for (int i = 1; i <= completeStuckBlockBreakRange; i++)
            {
                if (!world.isEmptyBlock(BlockPos.containing(entity.position()).relative(facing, i)) || !world.isEmptyBlock(BlockPos.containing(entity.position())
                  .relative(facing, i)
                  .above()))
                {
                    breakBlocksAhead(world, BlockPos.containing(entity.position()).relative(facing, i - 1), facing);
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
        if (delayToNextUnstuckAction > 0)
        {
            return;
        }
        delayToNextUnstuckAction = 100;

        // Clear path
        if (stuckLevel == 0)
        {
            stuckLevel++;
            delayToNextUnstuckAction = 100;
            navigator.getOurEntity().stopRiding();
            BlockPos desired = navigator.getDesiredPos();
            navigator.stop();
            navigator.setDesiredPos(desired);
            return;
        }

        // Move away, with chance to skip this.
        if (stuckLevel == 1 && rand.nextDouble() > chanceToByPassMovingAway
              || ((stuckLevel >= 3 && stuckLevel <= 8) && !(canBreakBlocks || canBuildLeafBridges || canPlaceLadders) && rand.nextBoolean()))
        {
            delayToNextUnstuckAction = 600;

            if (navigator.getPath() != null)
            {
                moveAwayStartPos = navigator.getPath().getNodePos(navigator.getPath().getNextNodeIndex());
            }
            else
            {
                moveAwayStartPos = navigator.getOurEntity().blockPosition().above();
            }

            navigator.stop();
            final int range = ColonyConstants.rand.nextInt(20) + Math.min(100, Math.max(20, BlockPosUtil.distManhattan(navigator.ourEntity.blockPosition(), prevDestination)));
            navigator.moveTowards(navigator.getOurEntity().blockPosition().relative(movingAwayDir, 40), range, 1.0f);
            movingAwayDir = movingAwayDir.getClockWise();
            navigator.setPauseTicks(range * TICKS_PER_BLOCK);
            return;
        }

        // Skip ahead
        if (stuckLevel == 2 && teleportRange > 0 && hadPath)
        {
            int index = Math.min(navigator.getPath().getNextNodeIndex() + teleportRange, navigator.getPath().getNodeCount() - 1);
            final Node togo = navigator.getPath().getNode(index);
            navigator.getOurEntity().teleportTo(togo.x + 0.5d, togo.y, togo.z + 0.5d);
            delayToNextUnstuckAction = 300;
        }

        // Place ladders & leaves
        if (stuckLevel >= 3 && stuckLevel <= 5)
        {
            delayToNextUnstuckAction = 200;
            if (canPlaceLadders && rand.nextBoolean())
            {
                placeLadders(navigator);
            }
            else if (canBuildLeafBridges && rand.nextBoolean())
            {
                delayToNextUnstuckAction = 100;
                placeLeaves(navigator);
            }
            else if (canPlaceLadders || canBuildLeafBridges)
            {
                return;
            }
        }

        // break blocks
        if (stuckLevel >= 6 && stuckLevel <= 8 && canBreakBlocks)
        {
            delayToNextUnstuckAction = 200;
            breakBlocks(navigator);
        }

        chanceStuckLevel();

        if (stuckLevel == 9)
        {
            completeStuckAction(navigator);
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
            stuckLevel = Math.max(1, stuckLevel - 2);
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
        moveAwayStartPos = BlockPos.ZERO;
    }

    /**
     * Attempt to break blocks that are blocking the entity to reach its destination.
     *
     * @param world  the world it is in.
     * @param start  the position the entity is at.
     * @param facing the direction the goal is in.
     */
    private boolean breakBlocksAhead(final Level world, final BlockPos start, final Direction facing)
    {
        // In entity
        if (!world.isEmptyBlock(start))
        {
            setAirIfPossible(world, start);
            return true;
        }

        // Above entity
        if (!world.isEmptyBlock(start.above(3)))
        {
            setAirIfPossible(world, start.above(3));
            return true;
        }

        // Goal direction up
        if (!world.isEmptyBlock(start.above().relative(facing)))
        {
            setAirIfPossible(world, start.above().relative(facing));
            return true;
        }

        // In goal direction
        if (!world.isEmptyBlock(start.relative(facing)))
        {
            setAirIfPossible(world, start.relative(facing));
            return true;
        }
        return false;
    }

    /**
     * Check if the block at the position is indestructible, if not, attempt to break it.
     *
     * @param world the world the block is in.
     * @param pos   the pos the block is at.
     */
    private void setAirIfPossible(final Level world, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        final Block blockAtPos = state.getBlock();
        if (blockAtPos instanceof IBuilderUndestroyable || state.is(ModTags.indestructible))
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
        final Level world = navigator.getOurEntity().level;
        final Mob entity = navigator.getOurEntity();

        BlockPos entityPos = entity.blockPosition();

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
        final Level world = navigator.getOurEntity().level();
        final Mob entity = navigator.getOurEntity();

        final Direction badFacing = BlockPosUtil.getFacing(BlockPos.containing(entity.position()), navigator.getDesiredPos()).getOpposite();

        for (final Direction dir : HORIZONTAL_DIRS)
        {
            if (dir == badFacing)
            {
                continue;
            }

            for (int i = 1; i <= (dir == badFacing.getOpposite() ? 3 : 1); i++)
            {
                if (!tryPlaceLeaveOnPos(world, BlockPos.containing(entity.position()).below().relative(dir, i)))
                {
                    break;
                }
            }
        }
    }

    /**
     * Places a leave block in the world
     *
     * @param world
     * @param pos
     * @return
     */
    private boolean tryPlaceLeaveOnPos(final Level world, final BlockPos pos)
    {
        if (world.isEmptyBlock(pos))
        {
            world.setBlockAndUpdate(pos, Blocks.ACACIA_LEAVES.defaultBlockState());
            return true;
        }
        return false;
    }

    /**
     * Tries to randomly break blocks
     *
     * @param navigator navigator to use
     */
    private void breakBlocks(final AbstractAdvancedPathNavigate navigator)
    {
        final Level world = navigator.getOurEntity().level();
        final Mob entity = navigator.getOurEntity();

        final Direction facing = BlockPosUtil.getFacing(BlockPos.containing(entity.position()), navigator.getDesiredPos());

        if (breakBlocksAhead(world, entity.blockPosition(), facing) && entity.getHealth() >= entity.getMaxHealth() / 3)
        {
            entity.hurt(world.damageSources().source(DamageSourceKeys.STUCK_DAMAGE), (float) Math.max(0.5, entity.getHealth() / 20.0));
        }
    }

    /**
     * Tries to place a ladder at the given position
     *
     * @param world world to use
     * @param pos   position to set
     */
    private void tryPlaceLadderAt(final Level world, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if ((canBreakBlocks || state.canBeReplaced() || state.isAir()) && state.getBlock() != Blocks.LADDER && !(state.getBlock() instanceof IBuilderUndestroyable) && !state.is(
          ModTags.indestructible))
        {
            for (final Direction dir : HORIZONTAL_DIRS)
            {
                final BlockState toPlace = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, dir.getOpposite());
                if (BlockUtils.isAnySolid(world.getBlockState(pos.relative(dir))) && Blocks.LADDER.canSurvive(toPlace, world, pos))
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

    public PathingStuckHandler withChanceToByPassMovingAway(final double chance)
    {
        chanceToByPassMovingAway = chance;
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
