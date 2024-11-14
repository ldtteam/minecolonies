package com.minecolonies.core.entity.pathfinding.navigation;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import com.minecolonies.api.entity.pathfinding.IStuckHandler;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.entity.pathfinding.PathFindingStatus;
import com.minecolonies.core.entity.pathfinding.PathPointExtended;
import com.minecolonies.core.entity.pathfinding.Pathfinding;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.pathjobs.*;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.entity.pathfinding.pathresults.TreePathResult;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.core.entity.pathfinding.PathFindingStatus.IN_PROGRESS_FOLLOWING;
import static com.minecolonies.core.entity.pathfinding.pathjobs.AbstractPathJob.MAX_NODES;

/**
 * Minecolonies async PathNavigate.
 */
// TODO: Rework
public class MinecoloniesAdvancedPathNavigate extends AbstractAdvancedPathNavigate implements IDynamicHeuristicNavigator
{
    private static final double ON_PATH_SPEED_MULTIPLIER = 1.3D;
    public static final  double MIN_Y_DISTANCE           = 0.001;
    public static final  int    MAX_SPEED_ALLOWED        = 2;
    public static final  double MIN_SPEED_ALLOWED        = 0.1;

    @Nullable
    private PathResult<AbstractPathJob> pathResult;

    /**
     * The world time when a path was added.
     */
    private long pathStartTime = 0;

    /**
     * Spawn pos of minecart.
     */
    private BlockPos spawnedPos = BlockPos.ZERO;

    /**
     * Desired position to reach
     */
    private BlockPos desiredPos;

    /**
     * Timeout for the desired pos, resets when its no longer wanted
     */
    private int desiredPosTimeout = 0;

    /**
     * The stuck handler to use
     */
    private IStuckHandler stuckHandler;

    /**
     * Whether we did set sneaking
     */
    private boolean isSneaking = true;

    /**
     * Speed factor for swimming
     */
    private double swimSpeedFactor = 1.0;

    /**
     * Average heuristic
     */
    private double heuristicAvg = 1;

    /**
     * Paused ticks, during those no new pathjob is allowed
     */
    private int pauseTicks = 0;

    /**
     * Temporary block position
     */
    private BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos();

    /**
     * wanted position for movecontrol
     */
    private Vec3 wantedPosition = null;

    /**
     * Instantiates the navigation of an ourEntity.
     *
     * @param entity the ourEntity.
     * @param world  the world it is in.
     */
    public MinecoloniesAdvancedPathNavigate(@NotNull final Mob entity, final Level world)
    {
        super(entity, world);

        entity.moveControl = new MovementHandler(entity);
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        getPathingOptions().setEnterDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        getPathingOptions().setCanOpenDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        getPathingOptions().setCanSwim(true);

        stuckHandler = PathingStuckHandler.createStuckHandler().withTakeDamageOnStuck(0.2f).withTeleportSteps(6).withTeleportOnFullStuck();
    }

    @Override
    public BlockPos getDestination()
    {
        return destination;
    }

    @Nullable
    public PathResult<AbstractPathJob> moveAwayFromXYZ(final BlockPos avoid, final double range, final double speedFactor, final boolean safeDestination)
    {
        @NotNull final BlockPos start = PathfindingUtils.prepareStart(ourEntity);

        return setPathJob(new PathJobMoveAwayFromLocation(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          avoid,
          (int) range,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speedFactor, safeDestination);
    }

    @Nullable
    @Override
    public PathResult<AbstractPathJob> moveTowards(final BlockPos towards, final double range, final double speedFactor)
    {
        return setPathJob(new PathJobMoveTowards(CompatibilityUtils.getWorldFromEntity(ourEntity),
          PathfindingUtils.prepareStart(ourEntity),
          towards,
          (int) range,
          ourEntity), null, speedFactor, false);
    }

    @Nullable
    public PathResult<AbstractPathJob> moveToRandomPos(final double range, final double speedFactor)
    {
        if (pathResult != null && pathResult.isInProgress() && pathResult.getJob() instanceof PathJobRandomPos)
        {
            return pathResult;
        }

        desiredPos = BlockPos.ZERO;
        final int theRange = (int) (mob.getRandom().nextInt((int) range) + range / 2);
        @NotNull final BlockPos start = PathfindingUtils.prepareStart(ourEntity);

        return setPathJob(new PathJobRandomPos(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          theRange,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speedFactor, true);
    }

    @Nullable
    public PathResult<AbstractPathJob> moveToRandomPosAroundX(final int range, final double speedFactor, final BlockPos pos)
    {
        if (pathResult != null
              && pathResult.isInProgress()
              && pathResult.getJob() instanceof PathJobRandomPos
              && ((((PathJobRandomPos) pathResult.getJob()).posAndRangeMatch(range, pos))))
        {
            return pathResult;
        }

        desiredPos = BlockPos.ZERO;
        final PathResult<AbstractPathJob> result = setPathJob(new PathJobRandomPos(CompatibilityUtils.getWorldFromEntity(ourEntity),
          PathfindingUtils.prepareStart(ourEntity),
          3,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          range,
          ourEntity, pos), pos, speedFactor, false);

        if (result == null)
        {
            return null;
        }

        result.getJob().getPathingOptions().withToggleCost(1).withJumpCost(1).withDropCost(1);
        return result;
    }

    @Override
    public PathResult<AbstractPathJob> moveToRandomPos(
      final int range,
      final double speedFactor,
      final net.minecraft.util.Tuple<BlockPos, BlockPos> corners)
    {
        if (pathResult != null && pathResult.isInProgress() && pathResult.getJob() instanceof PathJobRandomPos)
        {
            return pathResult;
        }

        desiredPos = BlockPos.ZERO;
        final int theRange = (mob.getRandom().nextInt(range) + range / 2);
        @NotNull final BlockPos start = PathfindingUtils.prepareStart(ourEntity);

        final PathResult<AbstractPathJob> result = setPathJob(new PathJobRandomPos(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          theRange,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          ourEntity,
          corners.getA(),
          corners.getB()), null, speedFactor, true);

        if (result == null)
        {
            return null;
        }

        result.getJob().getPathingOptions().withJumpCost(1).withDropCost(1);
        return result;
    }

    @Nullable
    public PathResult<AbstractPathJob> setPathJob(
      @NotNull final AbstractPathJob job,
      final BlockPos dest,
      final double speedFactor, final boolean safeDestination)
    {
        if (pauseTicks > 0)
        {
            return null;
        }

        stop();

        this.destination = dest;
        this.originalDestination = dest;
        if (safeDestination)
        {
            desiredPos = dest;
            if (dest != null)
            {
                desiredPosTimeout = 50 * 20;
            }
        }

        this.walkSpeedFactor = speedFactor;

        if (speedFactor > MAX_SPEED_ALLOWED || speedFactor < MIN_SPEED_ALLOWED)
        {
            Log.getLogger().error("Tried to set a bad speed:" + speedFactor + " for entity:" + ourEntity, new Exception());
            return null;
        }

        job.setPathingOptions(getPathingOptions());
        pathResult = job.getResult();
        pathResult.startJob(Pathfinding.getExecutor());
        return pathResult;
    }

    @Override
    public boolean isDone()
    {
        return (pathResult == null || pathResult.isDone() && pathResult.getStatus() != PathFindingStatus.CALCULATION_COMPLETE) && super.isDone();
    }

    @Override
    public void tick()
    {
        if (desiredPosTimeout > 0)
        {
            if (--desiredPosTimeout <= 0)
            {
                desiredPos = null;
            }
        }

        if (pauseTicks > 0)
        {
            pauseTicks--;
        }

        if (pathResult != null)
        {
            if (!pathResult.isDone())
            {
                return;
            }
            else if (pathResult.getStatus() == PathFindingStatus.CALCULATION_COMPLETE)
            {
                processCompletedCalculationResult();
                wantedPosition = null;
            }
        }

        int oldIndex = this.isDone() ? 0 : this.getPath().getNextNodeIndex();

        this.ourEntity.setYya(0);
        if (handleLadders(oldIndex))
        {
            followThePath();
            stuckHandler.checkStuck(this);
            return;
        }

        if (isSneaking)
        {
            isSneaking = false;
            mob.setShiftKeyDown(false);
        }

        if (handleRails())
        {
            stuckHandler.checkStuck(this);
            return;
        }

        ++this.tick;
        if (this.hasDelayedRecomputation)
        {
            this.recomputePath();
        }

        // The following block replaces mojangs super.tick(). Why you may ask? Because it's broken, that's why.
        // The moveHelper won't move up if standing in a block with an empty bounding box (put grass, 1 layer snow, mushroom in front of a solid block and have them try jump up).
        if (!this.isDone())
        {
            final int currentPathIndex = path.getNextNodeIndex();
            if (this.canUpdatePath())
            {
                this.followThePath();
            }
            else if (this.path != null && !this.path.isDone())
            {
                Vec3 vector3d = this.getTempMobPos();
                Vec3 vector3d1 = this.path.getNextEntityPos(this.mob);
                if (vector3d.y > vector3d1.y && !this.mob.onGround() && Mth.floor(vector3d.x) == Mth.floor(vector3d1.x) && Mth.floor(vector3d.z) == Mth.floor(vector3d1.z))
                {
                    this.path.advance();
                }
            }

            if (this.path != null && !this.path.isDone())
            {
                if ((wantedPosition == null || currentPathIndex != path.getNextNodeIndex() && path.getNextNodeIndex() < path.getNodeCount()))
                {
                    Vec3 vector3d2 = path.getNextEntityPos(mob);
                    tempPos.set(Mth.floor(vector3d2.x), Mth.floor(vector3d2.y), Mth.floor(vector3d2.z));
                    if (wantedPosition == null || ChunkPos.asLong(tempPos) == mob.chunkPosition().toLong() || WorldUtil.isEntityBlockLoaded(level, tempPos))
                    {
                        wantedPosition = new Vec3(vector3d2.x,
                            getSmartGroundY(this.level, tempPos, vector3d2.y),
                            vector3d2.z);
                    }
                }
            }
            if (wantedPosition != null)
            {
                mob.getMoveControl().setWantedPosition(wantedPosition.x, wantedPosition.y, wantedPosition.z, speedModifier);
            }
        }
        // End of super.tick.

        if (pathResult != null && isDone())
        {
            pathResult.setStatus(PathFindingStatus.COMPLETE);
        }

        stuckHandler.checkStuck(this);
    }

    /**
     * Similar to WalkNodeProcessor.getGroundY but not broken.
     * This checks if the block below the position we're trying to move to reaches into the block above, if so, it has to aim a little bit higher.
     *
     * @param world the world.
     * @param pos   the position to check.
     * @param orgY  original y level
     * @return the next y level to go to.
     */
    public static double getSmartGroundY(final BlockGetter world, final BlockPos.MutableBlockPos pos, final double orgY)
    {
        BlockState state = world.getBlockState(pos);

        if (!state.isAir())
        {
            if (state.getBlock() instanceof FenceGateBlock || state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock)
            {
                return orgY;
            }

            final VoxelShape voxelshape = state.getCollisionShape(world, pos);
            if (!ShapeUtil.isEmpty(voxelshape))
            {
                return pos.getY() + ShapeUtil.max(voxelshape, Direction.Axis.Y);
            }
        }

        pos.set(pos.getX(), pos.getY() - 1, pos.getZ());

        state = world.getBlockState(pos);
        if (!state.isAir())
        {
            final VoxelShape voxelshape = state.getCollisionShape(world, pos);
            if (!ShapeUtil.isEmpty(voxelshape))
            {
                return pos.getY() + ShapeUtil.max(voxelshape, Direction.Axis.Y);
            }
        }

        return orgY;
    }

    @Nullable
    public PathResult<AbstractPathJob> moveToXYZ(final double x, final double y, final double z, final double speedFactor)
    {
        final int newX = Mth.floor(x);
        final int newY = (int) y;
        final int newZ = Mth.floor(z);

        if (pathResult != null && pathResult.getJob() instanceof PathJobMoveToLocation)
        {
            if (pathResult.isComputing())
            {
                return pathResult;
            }

            if (((destination != null && BlockPosUtil.equals(destination, newX, newY, newZ)) || (originalDestination != null && BlockPosUtil.equals(originalDestination,
              newX,
              newY,
              newZ))))
            {
                if (pathResult.getStatus() == IN_PROGRESS_FOLLOWING || ColonyConstants.rand.nextInt(20) != 0)
                {
                    return pathResult;
                }
            }
        }

        @NotNull final BlockPos start = PathfindingUtils.prepareStart(ourEntity);
        desiredPos = new BlockPos(newX, newY, newZ);

        if (start.distSqr(desiredPos) > 500 * 500)
        {
            Log.getLogger()
              .error(
                "Entity: " + ourEntity.getDisplayName().getString() + " is trying to walk too far! distance:" + Math.sqrt(start.distSqr(desiredPos)) + " from:" + start + " to:"
                  + desiredPos, new Exception());
            return null;
        }

        return setPathJob(
          new PathJobMoveToLocation(CompatibilityUtils.getWorldFromEntity(ourEntity),
            start,
            desiredPos,
            (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
            ourEntity),
          desiredPos, speedFactor, true);
    }

    @Override
    public boolean tryMoveToBlockPos(final BlockPos pos, final double speedFactor)
    {
        moveToXYZ(pos.getX(), pos.getY(), pos.getZ(), speedFactor);
        return true;
    }

    @Override
    protected PathFinder createPathFinder(final int p_179679_1_)
    {
        return null;
    }

    @Override
    protected boolean canUpdatePath()
    {
        // Auto dismount when trying to path.
        if (ourEntity.vehicle != null)
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getNode(this.getPath().getNextNodeIndex());
            if (pEx.isRailsExit())
            {
                final Entity entity = ourEntity.vehicle;
                ourEntity.stopRiding();
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
            else if (!pEx.isOnRails())
            {
                if (ourEntity.vehicle instanceof MinecoloniesMinecart)
                {
                    final Entity entity = ourEntity.vehicle;
                    ourEntity.stopRiding();
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                else
                {
                    if (destination == null || mob.distanceToSqr(destination.getX(), destination.getY(), destination.getZ()) > 2)
                    {
                        ourEntity.stopRiding();
                    }
                }
            }
            else if ((Math.abs(pEx.x - mob.getX()) > 7 || Math.abs(pEx.z - mob.getZ()) > 7) && ourEntity.vehicle != null)
            {
                final Entity entity = ourEntity.vehicle;
                ourEntity.stopRiding();
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        return true;
    }

    @NotNull
    @Override
    protected Vec3 getTempMobPos()
    {
        return this.ourEntity.position();
    }

    @Override
    public Path createPath(final BlockPos pos, final int p_179680_2_)
    {
        //Because this directly returns Path we can't do it async.
        return null;
    }

    @Override
    protected boolean canMoveDirectly(final Vec3 start, final Vec3 end)
    {
        // TODO improve road walking. This is better in some situations, but still not great.
        return !WorkerUtil.isPathBlock(level.getBlockState(BlockPos.containing(start.x, start.y - 1, start.z)).getBlock())
                 && super.canMoveDirectly(start, end);
    }

    public double getSpeedFactor()
    {
        if (ourEntity.isInWater())
        {
            speedModifier = walkSpeedFactor * swimSpeedFactor;
            return speedModifier;
        }

        speedModifier = walkSpeedFactor;
        return walkSpeedFactor;
    }

    @Override
    public void setSpeedModifier(final double speedFactor)
    {
        if (speedFactor > MAX_SPEED_ALLOWED || speedFactor < MIN_SPEED_ALLOWED)
        {
            Log.getLogger().error("Tried to set a bad speed:" + speedFactor + " for entity:" + ourEntity, new Exception());
            return;
        }
        walkSpeedFactor = speedFactor;
    }

    /**
     * Deprecated - try to use BlockPos instead
     */
    @Override
    public boolean moveTo(final double x, final double y, final double z, final double speedFactor)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return false;
        }

        moveToXYZ(x, y, z, speedFactor);
        return true;
    }

    @Override
    public boolean moveTo(final Entity entityIn, final double speedFactor)
    {
        return tryMoveToBlockPos(entityIn.blockPosition(), speedFactor);
    }

    // Removes stupid vanilla stuff, causing our pathpoints to occasionally be replaced by vanilla ones.
    @Override
    protected void trimPath() {}

    @Override
    public boolean moveTo(@Nullable final Path path, final double speedFactor)
    {
        if (path == null)
        {
            super.stop();
            return false;
        }
        pathStartTime = level.getGameTime();
        return super.moveTo(convertPath(path), speedFactor);
    }

    /**
     * Converts the given path to a minecolonies path if needed.
     *
     * @param path given path
     * @return resulting path
     */
    private Path convertPath(final Path path)
    {
        final int pathLength = path.getNodeCount();
        Path tempPath = null;
        if (pathLength > 0 && !(path.getNode(0) instanceof PathPointExtended))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            @NotNull final PathPointExtended[] newPoints = new PathPointExtended[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                final Node point = path.getNode(i);
                if (!(point instanceof PathPointExtended))
                {
                    newPoints[i] = new PathPointExtended(new BlockPos(point.x, point.y, point.z));
                }
                else
                {
                    newPoints[i] = (PathPointExtended) point;
                }
            }

            tempPath = new Path(Arrays.asList(newPoints), path.getTarget(), path.canReach());

            final PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z);
        }

        return tempPath == null ? path : tempPath;
    }

    /**
     * Processes the pathresult when it finished computing
     */
    private void processCompletedCalculationResult()
    {
        if (pathResult == null)
        {
            return;
        }

        if (pathResult != null)
        {
            pathResult.setStatus(IN_PROGRESS_FOLLOWING);
        }

        // Calculate an overtime-heuristic adjustment for pathfinding to use which fits the terrain
        if (pathResult.hasPath() && pathResult.getPathLength() > 2 && pathResult.costPerDist != 1)
        {
            final double factor = 1 + pathResult.getPathLength() / 30.0;
            heuristicAvg -= heuristicAvg / (50 / factor);
            heuristicAvg += pathResult.costPerDist / (50 / factor);
        }

        if (pathResult.failedToReachDestination())
        {
            if (pathResult.searchedNodes >= MAX_NODES)
            {
                pauseTicks = 50;
            }
        }

        moveTo(pathResult.getPath(), getSpeedFactor());
    }

    private boolean handleLadders(int oldIndex)
    {
        //  Ladder Workaround
        if (!this.isDone())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getNode(this.getPath().getNextNodeIndex());
            final PathPointExtended pExNext = getPath().getNodeCount() > this.getPath().getNextNodeIndex() + 1
                                                ? (PathPointExtended) this.getPath()
              .getNode(this.getPath()
                .getNextNodeIndex() + 1) : null;

            final BlockPos pos = new BlockPos(pEx.x, pEx.y, pEx.z);
            if (pEx.isOnLadder() && pExNext != null && (pEx.y != pExNext.y || mob.getY() > pEx.y) && PathfindingUtils.isLadder(level.getBlockState(pos),
              pathResult != null ? pathResult.getJob().getPathingOptions() : getPathingOptions())
                  && level.getBlockState(pos).getFluidState().isEmpty())
            {
                return handlePathPointOnLadder(pEx);
            }
            else if (ourEntity.isInWater())
            {
                return handleEntityInWater(oldIndex, pEx);
            }
            else if (level.random.nextInt(20) == 0)
            {
                if (!pEx.isOnLadder() && pExNext != null && pExNext.isOnLadder())
                {
                    speedModifier = getSpeedFactor() / 4.0;
                }
                else if (WorkerUtil.isPathBlock(level.getBlockState(findBlockUnderEntity(ourEntity)).getBlock()))
                {
                    speedModifier = ON_PATH_SPEED_MULTIPLIER * getSpeedFactor();
                }
                else
                {
                    speedModifier = getSpeedFactor();
                }
            }
        }
        return false;
    }

    /**
     * Determine what block the entity stands on
     *
     * @param parEntity the entity that stands on the block
     * @return the Blockstate.
     */
    private BlockPos findBlockUnderEntity(@NotNull final Entity parEntity)
    {
        int blockX = (int) Math.round(parEntity.getX());
        int blockY = Mth.floor(parEntity.getY() - 0.2D);
        int blockZ = (int) Math.round(parEntity.getZ());
        return new BlockPos(blockX, blockY, blockZ);
    }

    /**
     * Handle rails navigation.
     *
     * @return true if block.
     */
    private boolean handleRails()
    {
        if (!this.isDone())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getNode(this.getPath().getNextNodeIndex());
            PathPointExtended pExNext = getPath().getNodeCount() > this.getPath().getNextNodeIndex() + 1
                                          ? (PathPointExtended) this.getPath()
              .getNode(this.getPath()
                .getNextNodeIndex() + 1) : null;

            if (pExNext != null && pEx.x == pExNext.x && pEx.z == pExNext.z)
            {
                pExNext = getPath().getNodeCount() > this.getPath().getNextNodeIndex() + 2
                            ? (PathPointExtended) this.getPath()
                  .getNode(this.getPath()
                    .getNextNodeIndex() + 2) : null;
            }

            if (pEx.isOnRails() || pEx.isRailsExit())
            {
                return handlePathOnRails(pEx, pExNext);
            }
        }
        return false;
    }

    /**
     * Handle pathing on rails.
     *
     * @param pEx     the current path point.
     * @param pExNext the next path point.
     * @return if go to next point.
     */
    private boolean handlePathOnRails(final PathPointExtended pEx, final PathPointExtended pExNext)
    {
        if (pEx.isRailsEntry())
        {
            final BlockPos blockPos = new BlockPos(pEx.x, pEx.y, pEx.z);
            if (!spawnedPos.equals(blockPos))
            {
                final BlockState blockstate = level.getBlockState(blockPos);
                RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
                                        ? ((BaseRailBlock) blockstate.getBlock()).getRailDirection(blockstate, level, blockPos, null)
                                        : RailShape.NORTH_SOUTH;
                double yOffset = 0.0D;
                if (railshape.isAscending())
                {
                    yOffset = 0.5D;
                }

                if (mob.vehicle instanceof MinecoloniesMinecart)
                {
                    ((MinecoloniesMinecart) mob.vehicle).setHurtDir(1);
                }
                else
                {
                    MinecoloniesMinecart minecart = ModEntities.MINECART.create(level);
                    final double x = pEx.x + 0.5D;
                    final double y = pEx.y + 0.625D + yOffset;
                    final double z = pEx.z + 0.5D;
                    minecart.setPos(x, y, z);
                    minecart.setDeltaMovement(Vec3.ZERO);
                    minecart.xo = x;
                    minecart.yo = y;
                    minecart.zo = z;


                    level.addFreshEntity(minecart);
                    minecart.setHurtDir(1);
                    mob.startRiding(minecart, true);
                }
                spawnedPos = blockPos;
            }
        }
        else
        {
            spawnedPos = BlockPos.ZERO;
        }

        if (mob.vehicle instanceof MinecoloniesMinecart && pExNext != null)
        {
            final BlockPos blockPos = new BlockPos(pEx.x, pEx.y, pEx.z);
            final BlockPos blockPosNext = new BlockPos(pExNext.x, pExNext.y, pExNext.z);
            final Vec3 motion = mob.vehicle.getDeltaMovement();
            double forward;
            switch (BlockPosUtil.getXZFacing(blockPos, blockPosNext).getOpposite())
            {
                case EAST:
                    forward = Math.min(Math.max(motion.x() - 1 * 0.01D, -1), 0);
                    mob.vehicle.setDeltaMovement(motion.add(forward == -1 ? -1 : -0.01D, 0.0D, 0.0D));
                    break;
                case WEST:
                    forward = Math.max(Math.min(motion.x() + 0.01D, 1), 0);
                    mob.vehicle.setDeltaMovement(motion.add(forward == 1 ? 1 : 0.01D, 0.0D, 0.0D));
                    break;
                case NORTH:
                    forward = Math.max(Math.min(motion.z() + 0.01D, 1), 0);
                    mob.vehicle.setDeltaMovement(motion.add(0.0D, 0.0D, forward == 1 ? 1 : 0.01D));
                    break;
                case SOUTH:
                    forward = Math.min(Math.max(motion.z() - 1 * 0.01D, -1), 0);
                    mob.vehicle.setDeltaMovement(motion.add(0.0D, 0.0D, forward == -1 ? -1 : -0.01D));
                    break;

                case DOWN:
                case UP:
                    // unreachable
                    break;
            }
        }
        return false;
    }

    private boolean handlePathPointOnLadder(final PathPointExtended pEx)
    {
        Vec3 vec3 = this.getPath().getNextEntityPos(this.ourEntity);
        final BlockPos entityPos = this.ourEntity.blockPosition();
        if (vec3.distanceToSqr(ourEntity.getX(), vec3.y, ourEntity.getZ()) < 0.6 && Math.abs(vec3.y - entityPos.getY()) <= 2.0)
        {
            //This way he is less nervous and gets up the ladder
            double newSpeed = 0.3;
            switch (pEx.getLadderFacing())
            {
                //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                case NORTH:
                    vec3 = vec3.add(0, 0, 0.4);
                    break;
                case SOUTH:
                    vec3 = vec3.add(0, 0, -0.4);
                    break;
                case WEST:
                    vec3 = vec3.add(0.4, 0, 0);
                    break;
                case EAST:
                    vec3 = vec3.add(-0.4, 0, 0);
                    break;
                case UP:
                    vec3 = vec3.add(0, 1, 0);
                    break;
                //  Any other value is going down, so lets not move at all
                default:
                    newSpeed = 0;
                    if (!isSneaking)
                    {
                        mob.setShiftKeyDown(true);
                        isSneaking = true;
                    }
                    this.ourEntity.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, 0.2);
                    break;
            }

            if (newSpeed > 0)
            {
                if (!(level.getBlockState(ourEntity.blockPosition()).getBlock() instanceof LadderBlock))
                {
                    this.ourEntity.setDeltaMovement(this.ourEntity.getDeltaMovement().add(0, 0.1D, 0));
                }
                this.ourEntity.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, newSpeed);
            }
            else
            {
                if (PathfindingUtils.isLadder(level.getBlockState(entityPos.below()), getPathingOptions()))
                {
                    this.ourEntity.setYya(-0.5f);
                }
                else
                {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleEntityInWater(int oldIndex, final PathPointExtended pEx)
    {
        if (!ourEntity.getEyeInFluidType().isAir())
        {
            return false;
        }

        //  Prevent shortcuts when swimming
        final int curIndex = this.getPath().getNextNodeIndex();
        if (curIndex > 0
              && (curIndex + 1) < this.getPath().getNodeCount()
              && this.getPath().getNode(curIndex - 1).y != pEx.y)
        {
            //  Work around the initial 'spin back' when dropping into water
            oldIndex = curIndex + 1;
        }

        this.getPath().setNextNodeIndex(oldIndex);

        Vec3 Vector3d = this.getPath().getNextEntityPos(this.ourEntity);

        if (Vector3d.distanceToSqr(new Vec3(ourEntity.getX(), Vector3d.y, ourEntity.getZ())) < 0.1
              && Math.abs(ourEntity.getY() - Vector3d.y) < 0.5)
        {
            this.getPath().advance();
            if (this.isDone())
            {
                return true;
            }

            Vector3d = this.getPath().getNextEntityPos(this.ourEntity);
        }

        this.ourEntity.getMoveControl().setWantedPosition(Vector3d.x, Vector3d.y, Vector3d.z, getSpeedFactor());
        return false;
    }

    @Override
    protected void followThePath()
    {
        getSpeedFactor();
        final int curNode = path.getNextNodeIndex();
        final int curNodeNext = curNode + 1;
        if (curNodeNext < path.getNodeCount())
        {
            if (!(path.getNode(curNode) instanceof PathPointExtended))
            {
                path = convertPath(path);
            }

            final PathPointExtended pEx = (PathPointExtended) path.getNode(curNode);
            final PathPointExtended pExNext = (PathPointExtended) path.getNode(curNodeNext);

            //  If current node is bottom of a ladder, then stay on this node until
            //  the ourEntity reaches the bottom, otherwise they will try to head out early
            if (pEx.isOnLadder() && pEx.getLadderFacing() == Direction.DOWN
                  && !pExNext.isOnLadder())
            {
                final Vec3 vec3 = getTempMobPos();
                if ((vec3.y - (double) pEx.y) < MIN_Y_DISTANCE)
                {
                    this.path.setNextNodeIndex(curNodeNext);
                }
                return;
            }
        }

        this.maxDistanceToWaypoint = 0.5F;
        boolean wentAhead = false;
        boolean isTracking = PathfindingUtils.trackingMap.containsValue(ourEntity.getUUID());

        HashSet<BlockPos> reached = null;
        if (isTracking)
        {
            reached = new HashSet<>();
        }

        // Look at multiple points, incase we're too fast
        for (int i = this.path.getNextNodeIndex(); i < Math.min(this.path.getNodeCount(), this.path.getNextNodeIndex() + 4); i++)
        {
            // TODO: Only keep advancing if distance gets closer, instead of looping many points, check if entity pos at node is even needed, normal pos probably fine
            Vec3 next = this.path.getEntityPosAtNode(this.mob, i);
            if (Math.abs(this.mob.getX() - next.x) < (double) this.maxDistanceToWaypoint - Math.abs(this.mob.getY() - (next.y)) * 0.1
                  && Math.abs(this.mob.getZ() - next.z) < (double) this.maxDistanceToWaypoint - Math.abs(this.mob.getY() - (next.y)) * 0.1 &&
                  Math.abs(this.mob.getY() - next.y) <= 1.0D)
            {
                this.path.advance();
                wentAhead = true;

                if (isTracking)
                {
                    final Node point = path.getNode(i);
                    reached.add(new BlockPos(point.x, point.y, point.z));
                }
            }
        }

        if (isTracking)
        {
            PathfindingUtils.syncDebugReachedPositions(reached, pathResult.getDebugWatchers());
            reached.clear();
        }

        if (path.isDone())
        {
            onPathFinish();
            return;
        }

        if (wentAhead)
        {
            return;
        }

        if (curNode >= path.getNodeCount() || curNode <= 1)
        {
            return;
        }

        // Check some past nodes case we fell behind.
        final Vec3 curr = this.path.getEntityPosAtNode(this.mob, curNode - 1);
        final Vec3 next = this.path.getEntityPosAtNode(this.mob, curNode);

        if (mob.position().distanceTo(curr) >= 2.0 && mob.position().distanceTo(next) >= 2.0)
        {
            int currentIndex = curNode - 1;
            while (currentIndex > 0)
            {
                final Vec3 tempoPos = this.path.getEntityPosAtNode(this.mob, currentIndex);
                if (mob.position().distanceTo(tempoPos) <= 1.0)
                {
                    this.path.setNextNodeIndex(currentIndex);
                }
                else if (isTracking)
                {
                    reached.add(BlockPos.containing(tempoPos.x, tempoPos.y, tempoPos.z));
                }
                currentIndex--;
            }
        }

        if (isTracking)
        {
            PathfindingUtils.syncDebugReachedPositions(reached, pathResult.getDebugWatchers());
            reached.clear();
        }
    }

    /**
     * Called upon reaching the path end, reset values
     */
    private void onPathFinish()
    {
        super.stop();
    }

    public void recomputePath() {}

    /**
     * Don't let vanilla rapidly discard paths, set a timeout before its allowed to use stuck.
     */
    @Override
    protected void doStuckDetection(@NotNull final Vec3 positionVec3)
    {
        // Do nothing, unstuck is checked on tick, not just when we have a path
    }

    @Override
    public void stop()
    {
        if (pathResult != null)
        {
            pathResult.cancel();
            pathResult.setStatus(PathFindingStatus.CANCELLED);
            pathResult = null;
        }

        desiredPos = BlockPos.ZERO;
        destination = null;
        super.stop();
    }

    @Override
    public TreePathResult moveToTree(
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final double speed,
      final List<ItemStorage> excludedTrees,
      final int dyntreesize,
      final IColony colony)
    {
        @NotNull final BlockPos start = PathfindingUtils.prepareStart(ourEntity);
        final BlockPos furthestRestriction = BlockPosUtil.getFurthestCorner(start, startRestriction, endRestriction);

        final PathJobFindTree job =
          new PathJobFindTree(CompatibilityUtils.getWorldFromEntity(mob),
            start,
            startRestriction,
            endRestriction,
            furthestRestriction,
            excludedTrees,
            dyntreesize,
            colony,
            ourEntity);

        return (TreePathResult) setPathJob(job, null, speed, true);
    }

    @Override
    public TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> excludedTrees, final int dyntreesize, final IColony colony)
    {
        @NotNull BlockPos start = PathfindingUtils.prepareStart(ourEntity);
        final BlockPos buildingPos = ((AbstractEntityCitizen) mob).getCitizenColonyHandler().getWorkBuilding().getPosition();

        if (BlockPosUtil.getDistance2D(buildingPos, ((AbstractEntityCitizen) mob).blockPosition()) > range * 4)
        {
            start = buildingPos;
        }

        return (TreePathResult) setPathJob(
          new PathJobFindTree(CompatibilityUtils.getWorldFromEntity(mob), start, buildingPos, range, excludedTrees, dyntreesize, colony, ourEntity), null, speed, true);
    }

    @Nullable
    @Override
    public PathResult<AbstractPathJob> moveToLivingEntity(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.getX(), e.getY(), e.getZ(), speed);
    }

    @Nullable
    @Override
    public PathResult<AbstractPathJob> moveAwayFromLivingEntity(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(e.blockPosition(), distance, speed, true);
    }

    @Override
    public void setCanFloat(boolean canSwim)
    {
        super.setCanFloat(canSwim);
        getPathingOptions().setCanSwim(canSwim);
    }

    @Override
    public BlockPos getDesiredPos()
    {
        return desiredPos;
    }

    @Override
    public void setDesiredPos(final BlockPos pos)
    {
        desiredPos = pos;
    }

    /**
     * Sets the stuck handler
     *
     * @param stuckHandler handler to set
     */
    @Override
    public void setStuckHandler(final IStuckHandler stuckHandler)
    {
        this.stuckHandler = stuckHandler;
    }

    @Override
    public void setSwimSpeedFactor(final double factor)
    {
        this.swimSpeedFactor = factor;
    }

    @Override
    public double getAvgHeuristicModifier()
    {
        return heuristicAvg;
    }

    @Override
    public void setPauseTicks(final int pauseTicks)
    {
        if (pauseTicks > TICKS_SECOND * 120)
        {
            Log.getLogger().warn("Tried to pause entity pathfinding for " + mob + " too long for " + pauseTicks + " ticks.", new Exception());
            this.pauseTicks = 50;
        }
        else
        {
            this.pauseTicks = pauseTicks;
        }
    }

    @Override
    public PathResult getPathResult()
    {
        return pathResult;
    }
}
