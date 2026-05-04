package com.minecolonies.core.entity.ai.workers.builder;

import com.ldtteam.structurize.placement.StructurePlacer;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.modules.settings.BuilderModeSetting;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.colony.workorders.WorkOrderBuilding;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.core.entity.ai.workers.util.BuildingProgressStage;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveCloseToXNearY;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.INITIAL_RUN_SPEED_AVOID;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_MANUAL_SUFFIX;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder, BuildingBuilder>
{
    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 4096;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS = 4;

    /**
     * Tick interval for regular lava checks near the active work site.
     */
    private static final int LAVA_CHECK_INTERVAL = 15;

    /**
     * Faster re-check interval shortly after a lava hit was detected.
     */
    private static final int LAVA_CHECK_INTERVAL_WHEN_THREATENED = 5;

    /**
     * Window in which the builder stays on the faster lava re-check interval.
     */
    private static final int LAVA_CHECK_RECENT_THREAT_WINDOW = 40;

    /**
     * Short retry delay after completing a lava retreat.
     */
    private static final int LAVA_RETRY_DELAY = 15;

    /**
     * Longer retry delay once the same lava hazard keeps blocking the same work target.
     */
    private static final int LAVA_PERSISTENT_RETRY_DELAY = 100;

    /**
     * Amount of repeated detections before treating the hazard as persistent.
     */
    private static final int LAVA_PERSISTENT_THREAT_THRESHOLD = 3;

    /**
     * Window in which repeated detections count towards the persistent hazard threshold.
     */
    private static final int LAVA_PERSISTENT_THREAT_WINDOW = 80;

    /**
     * Minimum distance to open from a lava hazard before continuing work.
     */
    private static final int LAVA_RETREAT_DISTANCE = 4;

    /**
     * Only check for lava when already near the current work target.
     */
    private static final int LAVA_CHECK_ACTIVATION_DISTANCE = 12;

    /**
     * Keep using stands clearly outside the most recently blocked lava hazard zone for the same work target.
     */
    private static final int LAVA_BLOCKED_STAND_DISTANCE = 6;

    /**
     * Search slightly beyond the retreat radius when looking for an alternative safe stand.
     */
    private static final int LAVA_SAFE_STAND_SEARCH_RADIUS = 6;

    /**
     * Maximum safe fall height after breaking the block currently supporting the builder.
     */
    private static final int MAX_SAFE_MINING_FALL_DISTANCE = 2;

    /**
     * Short retry delay after choosing a different stand for a mining action.
     */
    private static final int MINING_STAND_RETRY_DELAY = 5;

    /**
     * Current goto path
     */
    PathResult gotoPath = null;

    /**
     * Active worksite hazard the builder is currently retreating from.
     */
    @Nullable
    private BlockPos activeHazardRetreatPos = null;

    /**
     * Last tick on which the builder performed a lava check.
     */
    private long lastLavaCheckTick = -LAVA_CHECK_INTERVAL;

    /**
     * Last tick on which the builder detected a lava threat.
     */
    private long lastLavaThreatTick = -LAVA_CHECK_RECENT_THREAT_WINDOW;

    /**
     * Last work position used for throttling lava checks.
     */
    @Nullable
    private BlockPos lastLavaCheckTarget = null;

    /**
     * Last worker position used for throttling lava checks.
     */
    @Nullable
    private BlockPos lastLavaCheckStandPos = null;

    /**
     * Last lava hazard position used for repeat detection.
     */
    @Nullable
    private BlockPos lastHazardPos = null;

    /**
     * Last work target blocked by the current lava hazard.
     */
    @Nullable
    private BlockPos lastHazardTarget = null;

    /**
     * Last tick on which the same lava hazard was recorded.
     */
    private long lastHazardTick = Long.MIN_VALUE;

    /**
     * Repeated detections for the same hazard/target pair.
     */
    private int repeatedHazardDetections = 0;

    /**
     * Tick until which retrying the same blocked work target should be delayed.
     */
    private long hazardRetryBlockedUntilTick = Long.MIN_VALUE;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    /**
     * Reset builder-local lava hazard bookkeeping.
     */
    private void resetLavaHazardState()
    {
        activeHazardRetreatPos = null;
        lastLavaCheckTick = -LAVA_CHECK_INTERVAL;
        lastLavaThreatTick = -LAVA_CHECK_RECENT_THREAT_WINDOW;
        lastLavaCheckTarget = null;
        lastLavaCheckStandPos = null;
        lastHazardPos = null;
        lastHazardTarget = null;
        lastHazardTick = Long.MIN_VALUE;
        repeatedHazardDetections = 0;
        hazardRetryBlockedUntilTick = Long.MIN_VALUE;
    }

    /**
     * Cancel the cached goto path if one is still active.
     */
    private void cancelPendingGotoPath()
    {
        if (gotoPath != null)
        {
            gotoPath.cancel();
            gotoPath = null;
        }
    }

    /**
     * True when pathfinding debug output is enabled for the current world.
     */
    private boolean isPathfindingDebugEnabled()
    {
        return MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > 0;
    }

    /**
     * Emit a compact builder-specific debug line while pathfinding debug is enabled.
     */
    private void logHazardDebug(@NotNull final String message)
    {
        if (isPathfindingDebugEnabled())
        {
            Log.getLogger().info("Builder {} ({}): {}", worker.getCitizenData().getName(), worker.getCitizenData().getId(), message);
        }
    }

    @Override
    public Component getDebugInfo()
    {
        final MutableComponent info = Component.empty().append(super.getDebugInfo());
        info.append(Component.literal("Builder debug:\n"));
        if (building.getWorkOrder() != null)
        {
            info.append(Component.literal("workOrderType=" + building.getWorkOrder().getWorkOrderType() + "\n"));
            info.append(Component.literal("workOrderLocation=" + formatDebugPos(building.getWorkOrder().getLocation()) + "\n"));
        }
        info.append(Component.literal("gotoPath=" + getGotoPathState() + "\n"));
        info.append(Component.literal("activeHazardRetreatPos=" + formatDebugPos(activeHazardRetreatPos) + "\n"));
        info.append(Component.literal("lastLavaCheckTarget=" + formatDebugPos(lastLavaCheckTarget) + "\n"));
        info.append(Component.literal("lastLavaCheckStandPos=" + formatDebugPos(lastLavaCheckStandPos) + "\n"));
        info.append(Component.literal("lastLavaThreatTick=" + lastLavaThreatTick + "\n"));
        info.append(Component.literal("lastHazardPos=" + formatDebugPos(lastHazardPos) + "\n"));
        info.append(Component.literal("lastHazardTarget=" + formatDebugPos(lastHazardTarget) + "\n"));
        info.append(Component.literal("repeatedHazardDetections=" + repeatedHazardDetections + "\n"));
        info.append(Component.literal("hazardRetryBlockedUntilTick=" + hazardRetryBlockedUntilTick + "\n"));
        return info;
    }

    /**
     * Compact debug label for the cached goto path.
     */
    private String getGotoPathState()
    {
        if (gotoPath == null)
        {
            return "null";
        }

        if (gotoPath.isCancelled())
        {
            return "cancelled";
        }

        if (gotoPath.isDone())
        {
            return gotoPath.getPath() == null ? "done-no-path" : "done-with-path";
        }

        return "running";
    }

    @Override
    public void resetCurrentStructure()
    {
        super.resetCurrentStructure();
        cancelPendingGotoPath();
        resetLavaHazardState();
    }

    /**
     * Continue an already triggered retreat from a nearby worksite hazard.
     *
     * @return true while the builder is still handling the retreat.
     */
    private boolean handleActiveHazardRetreat()
    {
        if (activeHazardRetreatPos == null)
        {
            return false;
        }

        if (BlockPosUtil.getDistance2D(worker.blockPosition(), activeHazardRetreatPos) >= LAVA_RETREAT_DISTANCE)
        {
            logHazardDebug("lava retreat completed at " + formatDebugPos(worker.blockPosition()) + ", hazard=" + formatDebugPos(activeHazardRetreatPos));
            activeHazardRetreatPos = null;
            worker.getNavigation().stop();
            setDelay(getHazardRetryDelay());
            return true;
        }

        if (!EntityNavigationUtils.walkAwayFrom(worker, activeHazardRetreatPos, LAVA_RETREAT_DISTANCE, INITIAL_RUN_SPEED_AVOID, true))
        {
            return true;
        }

        logHazardDebug("lava retreat path finished at " + formatDebugPos(worker.blockPosition()) + ", hazard=" + formatDebugPos(activeHazardRetreatPos));
        activeHazardRetreatPos = null;
        setDelay(getHazardRetryDelay());
        return true;
    }

    /**
     * Get the currently applicable retry delay after a lava retreat.
     */
    private int getHazardRetryDelay()
    {
        final long remainingTicks = hazardRetryBlockedUntilTick - world.getGameTime();
        return remainingTicks > 0 ? (int) Math.min(Integer.MAX_VALUE, remainingTicks) : LAVA_RETRY_DELAY;
    }

    /**
     * Delay retriggering the same blocked work target while the lava hazard is still considered persistent.
     */
    private boolean isHazardRetryBlocked(@NotNull final BlockPos currentBlock)
    {
        if (lastHazardTarget == null || !currentBlock.equals(lastHazardTarget))
        {
            return false;
        }

        final long remainingTicks = hazardRetryBlockedUntilTick - world.getGameTime();
        if (remainingTicks <= 0)
        {
            return false;
        }

        cancelPendingGotoPath();
        worker.getNavigation().stop();
        setDelay((int) Math.min(Integer.MAX_VALUE, remainingTicks));
        return true;
    }

    /**
     * Record repeated lava detections for the same work target and widen the retry delay if needed.
     */
    private void rememberHazardRetry(@NotNull final BlockPos hazardPos, @NotNull final BlockPos currentBlock)
    {
        final long gameTime = world.getGameTime();
        final boolean sameHazard = lastHazardPos != null
            && BlockPosUtil.getDistance2D(hazardPos, lastHazardPos) <= LAVA_BLOCKED_STAND_DISTANCE
            && currentBlock.equals(lastHazardTarget)
            && gameTime - lastHazardTick <= LAVA_PERSISTENT_THREAT_WINDOW;

        repeatedHazardDetections = sameHazard ? repeatedHazardDetections + 1 : 1;
        lastHazardPos = hazardPos;
        lastHazardTarget = currentBlock.immutable();
        lastHazardTick = gameTime;
        hazardRetryBlockedUntilTick = gameTime + (repeatedHazardDetections >= LAVA_PERSISTENT_THREAT_THRESHOLD ? LAVA_PERSISTENT_RETRY_DELAY : LAVA_RETRY_DELAY);

        if (repeatedHazardDetections == LAVA_PERSISTENT_THREAT_THRESHOLD)
        {
            logHazardDebug("persistent lava hazard at " + formatDebugPos(lastHazardPos)
                + ", workTarget=" + formatDebugPos(lastHazardTarget)
                + ", backing off for " + LAVA_PERSISTENT_RETRY_DELAY + " ticks");
        }
    }

    /**
     * Check whether the current situation warrants a fresh lava scan.
     */
    private boolean shouldCheckForLava(@NotNull final BlockPos currentBlock)
    {
        if (workFrom == null && BlockPosUtil.getDistance2D(worker.blockPosition(), currentBlock) > LAVA_CHECK_ACTIVATION_DISTANCE)
        {
            return false;
        }

        final BlockPos standPos = worker.blockPosition();
        final long gameTime = world.getGameTime();
        final int interval = gameTime - lastLavaThreatTick <= LAVA_CHECK_RECENT_THREAT_WINDOW
            ? LAVA_CHECK_INTERVAL_WHEN_THREATENED
            : LAVA_CHECK_INTERVAL;

        if (currentBlock.equals(lastLavaCheckTarget)
              && standPos.equals(lastLavaCheckStandPos)
              && gameTime - lastLavaCheckTick < interval)
        {
            return false;
        }

        lastLavaCheckTick = gameTime;
        lastLavaCheckTarget = currentBlock.immutable();
        lastLavaCheckStandPos = standPos.immutable();
        return true;
    }

    /**
     * Find lava that is actually dangerous to the builder's current position.
     */
    @Nullable
    private BlockPos findCurrentStandLava(@NotNull final BlockPos currentBlock)
    {
        if (!shouldCheckForLava(currentBlock))
        {
            return null;
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        return findLavaNearStand(cursor, worker.blockPosition());
    }

    /**
     * Get the builder's currently planned working stand if one is already known.
     */
    @Nullable
    private BlockPos getPlannedWorkingStand()
    {
        if (workFrom != null)
        {
            return workFrom;
        }

        if (gotoPath != null && gotoPath.isDone() && gotoPath.getPath() != null)
        {
            return gotoPath.getPath().getTarget();
        }

        return null;
    }

    /**
     * Determine whether a candidate stand is usable for building without stepping into a lava hazard.
     */
    private boolean isSafeWorkingStand(@NotNull final BlockPos currentBlock, @NotNull final BlockPos stand, final BlockPos.MutableBlockPos cursor)
    {
        if (BlockPosUtil.getDistance2D(stand, currentBlock) > 5)
        {
            return false;
        }

        if (stand.getX() == currentBlock.getX() && stand.getZ() == currentBlock.getZ() && stand.getY() >= currentBlock.getY())
        {
            return false;
        }

        if (isBlockedByRecentHazard(currentBlock, stand))
        {
            return false;
        }

        return canUseMiningStand(stand) && findLavaNearStand(cursor, stand) == null;
    }

    /**
     * Avoid retrying stands that stay inside the last blocked lava zone for the same work target.
     */
    private boolean isBlockedByRecentHazard(@NotNull final BlockPos currentBlock, @NotNull final BlockPos stand)
    {
        return lastHazardPos != null
                 && lastHazardTarget != null
                 && currentBlock.equals(lastHazardTarget)
                 && BlockPosUtil.getDistance2D(stand, lastHazardPos) <= LAVA_BLOCKED_STAND_DISTANCE;
    }

    /**
     * Find a safe stand close to the preferred work position before the builder commits to it.
     */
    @Nullable
    private BlockPos findSafeWorkingStand(@NotNull final BlockPos currentBlock, @Nullable final BlockPos preferredStand)
    {
        if (preferredStand == null)
        {
            return null;
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final BlockPos currentStand = worker.blockPosition();
        if (isSafeWorkingStand(currentBlock, currentStand, cursor))
        {
            return currentStand.immutable();
        }

        if (isSafeWorkingStand(currentBlock, preferredStand, cursor))
        {
            return preferredStand;
        }

        for (int radius = 1; radius <= LAVA_SAFE_STAND_SEARCH_RADIUS; radius++)
        {
            for (int dx = -radius; dx <= radius; dx++)
            {
                for (int dz = -radius; dz <= radius; dz++)
                {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius)
                    {
                        continue;
                    }

                    final BlockPos candidate = preferredStand.offset(dx, 0, dz);
                    if (isSafeWorkingStand(currentBlock, candidate, cursor))
                    {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Reject an unsafe working stand, record the blocking hazard, and optionally reroute to a nearby safe one.
     */
    @Nullable
    private BlockPos resolveWorkingStand(@NotNull final BlockPos currentBlock, @Nullable final BlockPos preferredStand)
    {
        if (preferredStand == null)
        {
            return null;
        }

        final BlockPos safeStand = findSafeWorkingStand(currentBlock, preferredStand);
        if (safeStand != null)
        {
            if (!safeStand.equals(preferredStand))
            {
                logHazardDebug("rerouted stand from " + formatDebugPos(preferredStand)
                    + " to " + formatDebugPos(safeStand)
                    + ", target=" + formatDebugPos(currentBlock));
            }
            return safeStand;
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final BlockPos lavaPos = findLavaNearStand(cursor, preferredStand);
        if (lavaPos != null)
        {
            lastLavaThreatTick = world.getGameTime();
            rememberHazardRetry(lavaPos, currentBlock);
            logHazardDebug("rejecting unsafe stand=" + formatDebugPos(preferredStand)
                + ", hazard=" + formatDebugPos(lavaPos)
                + ", target=" + formatDebugPos(currentBlock));
        }

        return null;
    }

    /**
     * Find lava at a stand position or directly adjacent to it at the same height band.
     */
    @Nullable
    private BlockPos findLavaNearStand(final BlockPos.MutableBlockPos cursor, final BlockPos stand)
    {
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                BlockPos lavaPos = findLavaAtOrAbove(cursor, stand.offset(dx, 0, dz));
                if (lavaPos != null)
                {
                    return lavaPos;
                }

                lavaPos = findLavaAtOrAbove(cursor, stand.offset(dx, -1, dz));
                if (lavaPos != null)
                {
                    return lavaPos;
                }
            }
        }

        return null;
    }

    /**
     * Find lava at the given position or directly above it.
     */
    @Nullable
    private BlockPos findLavaAtOrAbove(final BlockPos.MutableBlockPos cursor, final BlockPos origin)
    {
        if (isLavaAt(cursor, origin.getX(), origin.getY(), origin.getZ()))
        {
            return cursor.immutable();
        }

        if (isLavaAt(cursor, origin.getX(), origin.getY() + 1, origin.getZ()))
        {
            return cursor.immutable();
        }

        return null;
    }

    /**
     * Cheap lava check for a single block position.
     */
    private boolean isLavaAt(final BlockPos.MutableBlockPos cursor, final int x, final int y, final int z)
    {
        cursor.set(x, y, z);
        return PathfindingUtils.isLava(world, cursor, world.getBlockState(cursor), world.getFluidState(cursor));
    }

    /**
     * Start a retreat after detecting a nearby worksite hazard.
     */
    private void retreatFromHazard(@NotNull final BlockPos hazardPos, @NotNull final BlockPos currentBlock)
    {
        final BlockPos plannedStand = getPlannedWorkingStand();
        activeHazardRetreatPos = new BlockPos(hazardPos.getX(), worker.blockPosition().getY(), hazardPos.getZ());
        rememberHazardRetry(activeHazardRetreatPos, currentBlock);
        logHazardDebug("detected dangerous lava near stand=" + formatDebugPos(plannedStand)
            + ", retreating from " + formatDebugPos(activeHazardRetreatPos)
            + ", target=" + formatDebugPos(currentBlock));
        workFrom = null;
        prevBlockPosition = null;
        cancelPendingGotoPath();
        worker.getNavigation().stop();
    }

    /**
     * Determine whether a specific stand position would make mining this block unsafe.
     */
    private boolean isUnsafeMiningStand(@NotNull final BlockPos blockToMine, @NotNull final BlockPos standPos)
    {
        if (blockToMine.equals(standPos.below()) && wouldCauseUnsafeFall(blockToMine))
        {
            return true;
        }

        return blockToMine.equals(standPos.above()) && isFallingBlock(world.getBlockState(blockToMine.above()));
    }

    /**
     * Try to find a nearby stand position from which the mining action is still safe.
     */
    @Nullable
    private BlockPos findAlternativeMiningStand(@NotNull final BlockPos blockToMine, @NotNull final BlockPos currentStand)
    {
        if (!isUnsafeMiningStand(blockToMine, currentStand))
        {
            return null;
        }

        final BlockPos center = new BlockPos(blockToMine.getX(), currentStand.getY(), blockToMine.getZ());
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (final Direction direction : Direction.Plane.HORIZONTAL)
        {
            final BlockPos candidate = center.relative(direction);
            if (canUseMiningStand(candidate)
                  && !isUnsafeMiningStand(blockToMine, candidate)
                  && !isBlockedByRecentHazard(blockToMine, candidate)
                  && findLavaNearStand(cursor, candidate) == null)
            {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Count whether breaking the support block below the builder would exceed the allowed fall depth.
     */
    private boolean wouldCauseUnsafeFall(@NotNull final BlockPos supportBlock)
    {
        int emptyBlocksBelow = 0;
        final BlockPos.MutableBlockPos cursor = supportBlock.mutable().move(Direction.DOWN);
        while (cursor.getY() >= world.getMinBuildHeight())
        {
            final BlockState state = world.getBlockState(cursor);
            if (state.blocksMotion() || !state.getFluidState().isEmpty())
            {
                return false;
            }

            emptyBlocksBelow++;
            if (emptyBlocksBelow > MAX_SAFE_MINING_FALL_DISTANCE)
            {
                return true;
            }

            cursor.move(Direction.DOWN);
        }

        return true;
    }

    /**
     * True if the block would fall once its support is removed.
     */
    private boolean isFallingBlock(@NotNull final BlockState state)
    {
        return state.getBlock() instanceof FallingBlock;
    }

    /**
     * Basic stand-position validation for builder mining repositioning.
     */
    private boolean canUseMiningStand(@NotNull final BlockPos standPos)
    {
        final BlockState floor = world.getBlockState(standPos.below());
        final BlockState feet = world.getBlockState(standPos);
        final BlockState head = world.getBlockState(standPos.above());
        return floor.blocksMotion()
                 && !feet.blocksMotion()
                 && feet.getFluidState().isEmpty()
                 && !head.blocksMotion()
                 && head.getFluidState().isEmpty();
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getPrimarySkillLevel();
    }

    @Override
    public Class<BuildingBuilder> getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    /**
     * Checks if we got a valid workorder.
     *
     * @return true if we got a workorder to work with
     */
    private boolean checkForWorkOrder()
    {
        if (!building.hasWorkOrder())
        {
            cancelPendingGotoPath();
            resetLavaHazardState();
            building.setProgressPos(null, BuildingProgressStage.CLEAR);
            worker.getCitizenData().setStatusPosition(null);
            return false;
        }

        final IWorkOrder wo = building.getWorkOrder();

        if (wo == null)
        {
            cancelPendingGotoPath();
            resetLavaHazardState();
            building.setWorkOrder(null);
            building.setProgressPos(null, null);
            worker.getCitizenData().setStatusPosition(null);
            return false;
        }

        final IBuilding building = job.getColony().getServerBuildingManager().getBuilding(wo.getLocation());
        if (building == null && wo instanceof WorkOrderBuilding && wo.getWorkOrderType() != WorkOrderType.REMOVE)
        {
            cancelPendingGotoPath();
            resetLavaHazardState();
            this.building.complete(worker.getCitizenData());
            return false;
        }

        return true;
    }

    @Override
    public void setStructurePlacer(final BuildingStructureHandler<JobBuilder, BuildingBuilder> structure)
    {
        resetLavaHazardState();
        if (building.getWorkOrder().getIteratorType().isEmpty())
        {
            final String mode = BuilderModeSetting.getActualValue(building);
            building.getWorkOrder().setIteratorType(mode);
        }

        structurePlacer = new Tuple<>(new StructurePlacer(structure, building.getWorkOrder().getIteratorType()), structure);
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return !checkForWorkOrder();
    }

    @Override
    protected IAIState startWorkingAtOwnBuilding()
    {
        if (!walkToBuilding())
        {
            return getState();
        }

        if (checkForWorkOrder())
        {
            final IAIState state = super.startWorkingAtOwnBuilding();
            if (state == IDLE)
            {
                return LOAD_STRUCTURE;
            }
            return state;
        }
        return IDLE;
    }

    /**
     * Kill all mobs at the building site.
     */
    private void killMobs()
    {
        if (building.getBuildingLevel() >= LEVEL_TO_PURGE_MOBS && building.getWorkOrder() != null && building.getWorkOrder().getWorkOrderType() == WorkOrderType.BUILD)
        {
            final BlockPos buildingPos = building.getWorkOrder().getLocation();
            final IBuilding building = worker.getCitizenColonyHandler().getColonyOrRegister().getServerBuildingManager().getBuilding(buildingPos);
            if (building != null)
            {
                WorldUtil.getEntitiesWithinBuilding(world, Monster.class, building, null).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
            }
        }
    }

    @Override
    public void checkForExtraBuildingActions()
    {
        if (!building.hasPurgedMobsToday())
        {
            killMobs();
            building.setPurgedMobsToday(true);
        }
    }

    @Override
    protected boolean mineBlock(@NotNull final BlockPos blockToMine, @Nullable final BlockPos safeStand)
    {
        final BlockPos standPos = safeStand == null ? worker.blockPosition() : safeStand;
        final BlockPos alternativeStand = findAlternativeMiningStand(blockToMine, standPos);
        if (alternativeStand != null)
        {
            workFrom = alternativeStand;
            prevBlockPosition = null;
            cancelPendingGotoPath();
            worker.getNavigation().stop();
            setDelay(MINING_STAND_RETRY_DELAY);
            return false;
        }

        if (isUnsafeMiningStand(blockToMine, standPos))
        {
            workFrom = null;
            prevBlockPosition = null;
            cancelPendingGotoPath();
            worker.getNavigation().stop();
            setDelay(MINING_STAND_RETRY_DELAY);
            return false;
        }

        return mineBlock(blockToMine, standPos, true, !IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)), null);
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        if (handleActiveHazardRetreat())
        {
            return false;
        }

        if (isHazardRetryBlocked(currentBlock))
        {
            return false;
        }

        final BlockPos lavaPos = findCurrentStandLava(currentBlock);
        if (lavaPos != null)
        {
            lastLavaThreatTick = world.getGameTime();
            retreatFromHazard(lavaPos, currentBlock);
            handleActiveHazardRetreat();
            return false;
        }

        if (workFrom != null && workFrom.getX() == currentBlock.getX() && workFrom.getZ() == currentBlock.getZ() && workFrom.getY() >= currentBlock.getY())
        {
            // Reset working position when standing ontop
            workFrom = null;
        }

        if (workFrom != null)
        {
            workFrom = resolveWorkingStand(currentBlock, workFrom);
            if (workFrom == null)
            {
                setDelay(getHazardRetryDelay());
                return false;
            }
        }

        if (workFrom == null)
        {
            if (gotoPath == null || gotoPath.isCancelled())
            {
                final PathJobMoveCloseToXNearY pathJob = new PathJobMoveCloseToXNearY(world,
                    currentBlock,
                    building.getWorkOrder().getLocation(),
                    4,
                    worker);
                gotoPath = ((MinecoloniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(pathJob, currentBlock, 1.0, false);
                pathJob.getPathingOptions().canDrop = false;
                pathJob.extraNodes = 0;
            }
            else if (gotoPath.isDone())
            {
                if (gotoPath.getPath() != null)
                {
                    workFrom = resolveWorkingStand(currentBlock, gotoPath.getPath().getTarget());
                }
                gotoPath = null;
                if (workFrom == null)
                {
                    setDelay(getHazardRetryDelay());
                    return false;
                }
            }

            if (prevBlockPosition != null)
            {
                return BlockPosUtil.dist(prevBlockPosition, currentBlock) <= 10;
            }
            return false;
        }

        if (!walkToSafePos(workFrom))
        {
            // Something might have changed, new wall and we can't reach the position anymore. Reset workfrom if stuck.
            if (worker.getNavigation() instanceof MinecoloniesAdvancedPathNavigate pathNavigate && pathNavigate.isStuck())
            {
                workFrom = null;
            }
            return false;
        }

        if (BlockPosUtil.getDistance2D(worker.blockPosition(), currentBlock) > 5)
        {
            if (BlockPosUtil.dist(workFrom, building.getWorkOrder().getLocation()) < 100)
            {
                prevBlockPosition = currentBlock;
                workFrom = null;
                return true;
            }
            workFrom = null;
            return false;
        }

        prevBlockPosition = currentBlock;
        return true;
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public int getBlockMiningTime(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        return (int) (super.getBlockMiningTime(state, pos) * SPEED_BUFF_0);
    }

    /**
     * Calculates after how many actions the AI should dump its inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }

    @Override
    protected void sendCompletionMessage(final IWorkOrder wo)
    {
        super.sendCompletionMessage(wo);

        final BlockPos position = wo.getLocation();
        boolean showManualSuffix = false;
        if (building.getManualMode())
        {
            showManualSuffix = true;
            for (final IWorkOrder workorder : building.getColony().getWorkManager().getWorkOrders().values())
            {
                if (workorder.getID() != wo.getID() && building.getID().equals(workorder.getClaimedBy()))
                {
                    showManualSuffix = false;
                }
            }
        }

        final MutableComponent message = Component.translatableEscape(
                wo.getWorkOrderType().getCompletionMessageID(),
                wo.getDisplayName(),
                BlockPosUtil.calcDirection(building.getColony().getCenter(), position).getLongText())
            .withStyle(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Component.translatable("message.positiondist",
                        position.getX(),
                        position.getY(),
                        position.getZ(),
                        (int) BlockPosUtil.dist(building.getColony().getCenter(), position)))))
            .withStyle(ChatFormatting.GREEN);

        if (showManualSuffix)
        {
            message.append(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_MANUAL_SUFFIX));
        }

        MessageUtils.forCitizen(worker, message).sendTo(worker.getCitizenColonyHandler().getColonyOrRegister().getImportantMessageEntityPlayers());
    }

    @Override
    public boolean canGoIdle()
    {
        return !building.hasWorkOrder();
    }
}
