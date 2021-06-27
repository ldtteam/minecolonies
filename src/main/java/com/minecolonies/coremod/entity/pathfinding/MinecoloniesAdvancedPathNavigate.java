package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.pathfinding.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.*;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Minecolonies async PathNavigate.
 */
public class MinecoloniesAdvancedPathNavigate extends AbstractAdvancedPathNavigate
{
    private static final double ON_PATH_SPEED_MULTIPLIER = 1.3D;
    private static final double PIRATE_SWIM_BONUS        = 1.5;
    private static final double BARBARIAN_SWIM_BONUS     = 1.2;
    private static final double CITIZEN_SWIM_BONUS       = 1.1;
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
     * Instantiates the navigation of an ourEntity.
     *
     * @param entity the ourEntity.
     * @param world  the world it is in.
     */
    public MinecoloniesAdvancedPathNavigate(@NotNull final MobEntity entity, final World world)
    {
        super(entity, world);

        this.nodeProcessor = new WalkNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        getPathingOptions().setEnterDoors(true);
        this.nodeProcessor.setCanOpenDoors(true);
        getPathingOptions().setCanOpenDoors(true);
        this.nodeProcessor.setCanSwim(true);
        getPathingOptions().setCanSwim(true);

        stuckHandler = PathingStuckHandler.createStuckHandler().withTakeDamageOnStuck(0.2f).withTeleportSteps(6).withTeleportOnFullStuck();
    }

    @Override
    public BlockPos getDestination()
    {
        return destination;
    }

    @Nullable
    public PathResult moveAwayFromXYZ(final BlockPos avoid, final double range, final double speedFactor)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);

        return setPathJob(new PathJobMoveAwayFromLocation(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          avoid,
          (int) range,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speedFactor);
    }

    @Nullable
    public PathResult moveToRandomPos(final double range, final double speedFactor)
    {
        if (pathResult != null && pathResult.getJob() instanceof PathJobRandomPos)
        {
            return pathResult;
        }

        final int theRange = (int) (entity.getRNG().nextInt((int) range) + range / 2);
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);

        return setPathJob(new PathJobRandomPos(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          theRange,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speedFactor);
    }

    @Nullable
    public PathResult moveToRandomPosAroundX(final int range, final double speedFactor, final BlockPos pos)
    {
        if (pathResult != null
              && pathResult.getJob() instanceof PathJobRandomPos
              && ((((PathJobRandomPos) pathResult.getJob()).posAndRangeMatch(range, pos))))
        {
            return pathResult;
        }

        return setPathJob(new PathJobRandomPos(CompatibilityUtils.getWorldFromEntity(ourEntity),
          AbstractPathJob.prepareStart(ourEntity),
          1,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          range,
          ourEntity, pos), pos, speedFactor);
    }

    @Nullable
    public PathResult setPathJob(
      @NotNull final AbstractPathJob job,
      final BlockPos dest,
      final double speedFactor)
    {
        clearPath();

        this.destination = dest;
        this.originalDestination = dest;
        if (dest != null)
        {
            desiredPos = dest;
            desiredPosTimeout = 50 * 20;
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
    public boolean noPath()
    {
        return (pathResult == null || pathResult.isDone() && pathResult.getStatus() != PathFindingStatus.CALCULATION_COMPLETE) && super.noPath();
    }

    @Override
    public void tick()
    {
        if (desiredPosTimeout > 0)
        {
            if (desiredPosTimeout-- <= 0)
            {
                desiredPos = null;
            }
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
            }
        }

        int oldIndex = this.noPath() ? 0 : this.getPath().getCurrentPathIndex();

        if (isSneaking)
        {
            isSneaking = false;
            entity.setSneaking(false);
        }
        this.ourEntity.setMoveVertical(0);
        if (handleLadders(oldIndex))
        {
            pathFollow();
            return;
        }
        if (handleRails())
        {
            return;
        }

        ++this.totalTicks;
        if (this.tryUpdatePath)
        {
            this.updatePath();
        }

        // The following block replaces mojangs super.tick(). Why you may ask? Because it's broken, that's why.
        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                this.pathFollow();
            }
            else if (this.currentPath != null && !this.currentPath.isFinished())
            {
                Vector3d vector3d = this.getEntityPosition();
                Vector3d vector3d1 = this.currentPath.getPosition(this.entity);
                if (vector3d.y > vector3d1.y && !this.entity.isOnGround() && MathHelper.floor(vector3d.x) == MathHelper.floor(vector3d1.x) && MathHelper.floor(vector3d.z) == MathHelper.floor(vector3d1.z))
                {
                    this.currentPath.incrementPathIndex();
                }
            }

            DebugPacketSender.sendPath(this.world, this.entity, this.currentPath, this.maxDistanceToWaypoint);
            if (!this.noPath())
            {
                Vector3d vector3d2 = this.currentPath.getPosition(this.entity);
                BlockPos blockpos = new BlockPos(vector3d2);
                this.entity.getMoveHelper().setMoveTo(vector3d2.x, this.world.getBlockState(blockpos.down()).isAir() ? vector3d2.y : getSmartGroundY(this.world, blockpos), vector3d2.z, this.speed);
            }
        }
        // End of super.tick.

        if (pathResult != null && noPath())
        {
            pathResult.setStatus(PathFindingStatus.COMPLETE);
            pathResult = null;
        }

        stuckHandler.checkStuck(this);
    }

    /**
     * Similar to WalkNodeProcessor.getGroundY but not broken.
     * @param world the world.
     * @param pos the position to check.
     * @return the next y level to go to.
     */
    public static double getSmartGroundY(final IBlockReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.down();
        final VoxelShape voxelshape = world.getBlockState(blockpos).getCollisionShape(world, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 1.0D : voxelshape.getEnd(Direction.Axis.Y));
    }

    @Nullable
    public PathResult moveToXYZ(final double x, final double y, final double z, final double speedFactor)
    {
        final int newX = MathHelper.floor(x);
        final int newY = (int) y;
        final int newZ = MathHelper.floor(z);

        if (pathResult != null && pathResult.getJob() instanceof PathJobMoveToLocation &&
              (
                pathResult.isComputing()
                  || (destination != null && BlockPosUtil.isEqual(destination, newX, newY, newZ))
                  || (originalDestination != null && BlockPosUtil.isEqual(originalDestination, newX, newY, newZ))
              )
        )
        {
            return pathResult;
        }

        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        desiredPos = new BlockPos(newX, newY, newZ);

        return setPathJob(
          new PathJobMoveToLocation(CompatibilityUtils.getWorldFromEntity(ourEntity),
            start,
            desiredPos,
            (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
            ourEntity),
          desiredPos, speedFactor);
    }

    @Override
    public boolean tryMoveToBlockPos(final BlockPos pos, final double speedFactor)
    {
        moveToXYZ(pos.getX(), pos.getY(), pos.getZ(), speedFactor);
        return true;
    }

    @Override
    protected PathFinder getPathFinder(final int p_179679_1_)
    {
        return null;
    }

    @Override
    protected boolean canNavigate()
    {
        // Auto dismount when trying to path.
        if (ourEntity.ridingEntity != null)
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());
            if (pEx.isRailsExit())
            {
                final Entity entity = ourEntity.ridingEntity;
                ourEntity.stopRiding();
                entity.remove();
            }
            else if (!pEx.isOnRails())
            {
                if (ourEntity.ridingEntity instanceof MinecoloniesMinecart)
                {
                    final Entity entity = ourEntity.ridingEntity;
                    ourEntity.stopRiding();
                    entity.remove();
                }
                else
                {
                    ourEntity.stopRiding();
                }
            }
            else if ((Math.abs(pEx.x - entity.getPosX()) > 7 || Math.abs(pEx.z - entity.getPosZ()) > 7) && ourEntity.ridingEntity != null)
            {
                final Entity entity = ourEntity.ridingEntity;
                ourEntity.stopRiding();
                entity.remove();
            }
        }
        return true;
    }

    @NotNull
    @Override
    protected Vector3d getEntityPosition()
    {
        return this.ourEntity.getPositionVec();
    }

    @Override
    public Path getPathToPos(final BlockPos pos, final int p_179680_2_)
    {
        //Because this directly returns Path we can't do it async.
        return null;
    }

    @Override
    protected boolean isDirectPathBetweenPoints(final Vector3d start, final Vector3d end, final int sizeX, final int sizeY, final int sizeZ)
    {
        // TODO improve road walking. This is better in some situations, but still not great.
        return !WorkerUtil.isPathBlock(world.getBlockState(new BlockPos(start.x, start.y - 1, start.z)).getBlock())
                 && super.isDirectPathBetweenPoints(start, end, sizeX, sizeY, sizeZ);
    }

    public double getSpeedFactor()
    {
        if (ourEntity instanceof AbstractEntityPirate && ourEntity.isInWater())
        {
            speed = walkSpeedFactor * PIRATE_SWIM_BONUS;
            return speed;
        }
        else if (ourEntity instanceof AbstractEntityBarbarian && ourEntity.isInWater())
        {
            speed = walkSpeedFactor * BARBARIAN_SWIM_BONUS;
            return speed;
        }
        else if (ourEntity instanceof AbstractEntityCitizen && ourEntity.isInWater())
        {
            speed = walkSpeedFactor * CITIZEN_SWIM_BONUS;
            return speed;
        }

        speed = walkSpeedFactor;
        return walkSpeedFactor;
    }

    @Override
    public void setSpeed(final double speedFactor)
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
    public boolean tryMoveToXYZ(final double x, final double y, final double z, final double speedFactor)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return false;
        }

        moveToXYZ(x, y, z, speedFactor);
        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(final Entity entityIn, final double speedFactor)
    {
        return tryMoveToBlockPos(entityIn.getPosition(), speedFactor);
    }

    // Removes stupid vanilla stuff, causing our pathpoints to occasionally be replaced by vanilla ones.
    @Override
    protected void trimPath() {}

    @Override
    public boolean setPath(@Nullable final Path path, final double speedFactor)
    {
        if (path == null)
        {
            clearPath();
            return false;
        }
        pathStartTime = world.getGameTime();
        return super.setPath(convertPath(path), speedFactor);
    }

    /**
     * Converts the given path to a minecolonies path if needed.
     *
     * @param path given path
     * @return resulting path
     */
    private Path convertPath(final Path path)
    {
        final int pathLength = path.getCurrentPathLength();
        Path tempPath = null;
        if (pathLength > 0 && !(path.getPathPointFromIndex(0) instanceof PathPointExtended))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            @NotNull final PathPointExtended[] newPoints = new PathPointExtended[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                final PathPoint point = path.getPathPointFromIndex(i);
                if (!(point instanceof PathPointExtended))
                {
                    newPoints[i] = new PathPointExtended(new BlockPos(point.x, point.y, point.z));
                }
                else
                {
                    newPoints[i] = (PathPointExtended) point;
                }
            }

            tempPath = new Path(Arrays.asList(newPoints), path.getTarget(), path.reachesTarget());

            final PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z);
        }

        return tempPath == null ? path : tempPath;
    }

    private boolean processCompletedCalculationResult()
    {
        setPath(pathResult.getPath(), getSpeedFactor());
        pathResult.setStatus(PathFindingStatus.IN_PROGRESS_FOLLOWING);
        return false;
    }

    private boolean handleLadders(int oldIndex)
    {
        //  Ladder Workaround
        if (!this.noPath())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());
            final PathPointExtended pExNext = getPath().getCurrentPathLength() > this.getPath().getCurrentPathIndex() + 1
                                                ? (PathPointExtended) this.getPath()
                                                                        .getPathPointFromIndex(this.getPath()
                                                                                                 .getCurrentPathIndex() + 1)
                                                : null;

            for (int i = this.currentPath.getCurrentPathIndex(); i < Math.min(this.currentPath.getCurrentPathLength(), this.currentPath.getCurrentPathIndex() + 3); i++)
            {
                final PathPointExtended nextPoints = (PathPointExtended) this.getPath().getPathPointFromIndex(i);
                if (nextPoints.isOnLadder())
                {
                    Vector3d motion = this.entity.getMotion();
                    double x = motion.x < -0.1 ? -0.1 : Math.min(motion.x, 0.1);
                    double z = motion.x < -0.1 ? -0.1 : Math.min(motion.z, 0.1);

                    this.ourEntity.setMotion(x, motion.y, z);
                    break;
                }
            }

            if (pEx.isOnLadder() && pExNext != null && (pEx.y != pExNext.y || entity.getPosY() > pEx.y))
            {
                return handlePathPointOnLadder(pEx);
            }
            else if (ourEntity.isInWater())
            {
                return handleEntityInWater(oldIndex, pEx);
            }
            else if (world.rand.nextInt(10) == 0)
            {
                if (WorkerUtil.isPathBlock(world.getBlockState(findBlockUnderEntity(ourEntity)).getBlock()))
                {
                    speed = ON_PATH_SPEED_MULTIPLIER * getSpeedFactor();
                }
                else
                {
                    speed = getSpeedFactor();
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
        int blockX = (int) Math.round(parEntity.getPosX());
        int blockY = MathHelper.floor(parEntity.getPosY() - 0.2D);
        int blockZ = (int) Math.round(parEntity.getPosZ());
        return new BlockPos(blockX, blockY, blockZ);
    }

    /**
     * Handle rails navigation.
     *
     * @return true if block.
     */
    private boolean handleRails()
    {
        if (!this.noPath())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());
            final PathPointExtended pExNext = getPath().getCurrentPathLength() > this.getPath().getCurrentPathIndex() + 1
                                                ? (PathPointExtended) this.getPath()
                                                                        .getPathPointFromIndex(this.getPath()
                                                                                                 .getCurrentPathIndex() + 1)
                                                : null;

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
                final BlockState blockstate = world.getBlockState(blockPos);
                RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock
                                        ? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockPos, null)
                                        : RailShape.NORTH_SOUTH;
                double yOffset = 0.0D;
                if (railshape.isAscending())
                {
                    yOffset = 0.5D;
                }

                if (entity.ridingEntity instanceof MinecoloniesMinecart)
                {
                    ((MinecoloniesMinecart) entity.ridingEntity).setRollingDirection(1);
                }
                else
                {
                    MinecoloniesMinecart minecart = (MinecoloniesMinecart) ModEntities.MINECART.create(world);
                    final double x = pEx.x + 0.5D;
                    final double y = pEx.y + 0.625D + yOffset;
                    final double z = pEx.z + 0.5D;
                    minecart.setPosition(x, y, z);
                    minecart.setMotion(Vector3d.ZERO);
                    minecart.prevPosX = x;
                    minecart.prevPosY = y;
                    minecart.prevPosZ = z;


                    world.addEntity(minecart);
                    minecart.setRollingDirection(1);
                    entity.startRiding(minecart, true);
                }
                spawnedPos = blockPos;
            }
        }
        else
        {
            spawnedPos = BlockPos.ZERO;
        }

        if (entity.ridingEntity instanceof MinecoloniesMinecart && pExNext != null)
        {
            final BlockPos blockPos = new BlockPos(pEx.x, pEx.y, pEx.z);
            final BlockPos blockPosNext = new BlockPos(pExNext.x, pExNext.y, pExNext.z);
            final Vector3d motion = entity.ridingEntity.getMotion();
            double forward;
            switch (BlockPosUtil.getXZFacing(blockPos, blockPosNext).getOpposite())
            {
                case EAST:
                    forward = Math.min(Math.max(motion.getX() - 1 * 0.01D, -1), 0);
                    entity.ridingEntity.setMotion(motion.add(forward == -1 ? -1 : -1 * 0.01D, 0.0D, 0.0D));
                    break;
                case WEST:
                    forward = Math.max(Math.min(motion.getX() + 0.01D, 1), 0);
                    entity.ridingEntity.setMotion(motion.add(forward == 1 ? 1 : 0.01D, 0.0D, 0.0D));
                    break;
                case NORTH:
                    forward = Math.max(Math.min(motion.getZ() + 0.01D, 1), 0);
                    entity.ridingEntity.setMotion(motion.add(0.0D, 0.0D, forward == 1 ? 1 : 0.01D));
                    break;
                case SOUTH:
                    forward = Math.min(Math.max(motion.getZ() - 1 * 0.01D, -1), 0);
                    entity.ridingEntity.setMotion(motion.add(0.0D, 0.0D, forward == -1 ? -1 : -1 * 0.01D));
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
        Vector3d vec3 = this.getPath().getPosition(this.ourEntity);
        final BlockPos entityPos = new BlockPos(this.ourEntity.getPositionVec());
        if (vec3.squareDistanceTo(ourEntity.getPosX(), vec3.y, ourEntity.getPosZ()) < Math.random() * 0.1)
        {
            //This way he is less nervous and gets up the ladder
            double newSpeed = 0.3;
            switch (pEx.getLadderFacing())
            {
                //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                case NORTH:
                    vec3 = vec3.add(0, 0, 1);
                    break;
                case SOUTH:
                    vec3 = vec3.add(0, 0, -1);
                    break;
                case WEST:
                    vec3 = vec3.add(1, 0, 0);
                    break;
                case EAST:
                    vec3 = vec3.add(-1, 0, 0);
                    break;
                case UP:
                    vec3 = vec3.add(0, 1, 0);
                    break;
                //  Any other value is going down, so lets not move at all
                default:
                    newSpeed = 0;
                    entity.setSneaking(true);
                    isSneaking = true;
                    break;
            }

            if (newSpeed > 0)
            {
                if (!(world.getBlockState(ourEntity.getPosition()).getBlock() instanceof LadderBlock))
                {
                    this.ourEntity.setMotion(this.ourEntity.getMotion().add(0, 0.1D, 0));
                }
                this.ourEntity.getMoveHelper().setMoveTo(vec3.x, vec3.y, vec3.z, newSpeed);
            }
            else
            {
                if (world.getBlockState(entityPos.down()).isLadder(world, entityPos.down(), ourEntity))
                {
                    this.ourEntity.setMoveVertical(-0.5f);
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
        //  Prevent shortcuts when swimming
        final int curIndex = this.getPath().getCurrentPathIndex();
        if (curIndex > 0
              && (curIndex + 1) < this.getPath().getCurrentPathLength()
              && this.getPath().getPathPointFromIndex(curIndex - 1).y != pEx.y)
        {
            //  Work around the initial 'spin back' when dropping into water
            oldIndex = curIndex + 1;
        }

        this.getPath().setCurrentPathIndex(oldIndex);

        Vector3d Vector3d = this.getPath().getPosition(this.ourEntity);

        if (Vector3d.squareDistanceTo(new Vector3d(ourEntity.getPosX(), Vector3d.y, ourEntity.getPosZ())) < 0.1
              && Math.abs(ourEntity.getPosY() - Vector3d.y) < 0.5)
        {
            this.getPath().incrementPathIndex();
            if (this.noPath())
            {
                return true;
            }

            Vector3d = this.getPath().getPosition(this.ourEntity);
        }

        this.ourEntity.getMoveHelper().setMoveTo(Vector3d.x, Vector3d.y, Vector3d.z, getSpeedFactor());
        return false;
    }

    @Override
    protected void pathFollow()
    {
        getSpeedFactor();
        final int curNode = currentPath.getCurrentPathIndex();
        final int curNodeNext = curNode + 1;
        if (curNodeNext < currentPath.getCurrentPathLength())
        {
            if (!(currentPath.getPathPointFromIndex(curNode) instanceof PathPointExtended))
            {
                currentPath = convertPath(currentPath);
            }

            final PathPointExtended pEx = (PathPointExtended) currentPath.getPathPointFromIndex(curNode);
            final PathPointExtended pExNext = (PathPointExtended) currentPath.getPathPointFromIndex(curNodeNext);

            //  If current node is bottom of a ladder, then stay on this node until
            //  the ourEntity reaches the bottom, otherwise they will try to head out early
            if (pEx.isOnLadder() && pEx.getLadderFacing() == Direction.DOWN
                  && !pExNext.isOnLadder())
            {
                final Vector3d vec3 = getEntityPosition();
                if ((vec3.y - (double) pEx.y) < MIN_Y_DISTANCE)
                {
                    this.currentPath.setCurrentPathIndex(curNodeNext);
                }
                return;
            }
        }

        this.maxDistanceToWaypoint = 0.5F;
        boolean wentAhead = false;


        // Look at multiple points, incase we're too fast
        for (int i = this.currentPath.getCurrentPathIndex(); i < Math.min(this.currentPath.getCurrentPathLength(), this.currentPath.getCurrentPathIndex() + 4); i++)
        {
            Vector3d next = this.currentPath.getVectorFromIndex(this.entity, i);
            if (Math.abs(this.entity.getPosX() - next.x) < (double) this.maxDistanceToWaypoint - Math.abs(this.entity.getPosY() - (next.y)) * 0.1
                  && Math.abs(this.entity.getPosZ() - next.z) < (double) this.maxDistanceToWaypoint - Math.abs(this.entity.getPosY() - (next.y)) * 0.1 &&
                  Math.abs(this.entity.getPosY() - next.y) < 1.0D)
            {
                this.currentPath.incrementPathIndex();
                wentAhead = true;
                // Mark reached nodes for debug path drawing
                if (AbstractPathJob.lastDebugNodesPath != null)
                {
                    final PathPoint point = currentPath.getPathPointFromIndex(i);
                    final BlockPos pos = new BlockPos(point.x, point.y, point.z);
                    for (final Node node : AbstractPathJob.lastDebugNodesPath)
                    {
                        if (!node.isReachedByWorker() && node.pos.equals(pos))
                        {
                            node.setReachedByWorker(true);
                            break;
                        }
                    }
                }
            }
        }

        if (currentPath.isFinished())
        {
            onPathFinish();
            return;
        }

        if (wentAhead)
        {
            return;
        }

        if (curNode >= currentPath.getCurrentPathLength() || curNode <= 1)
        {
            return;
        }

        // Check some past nodes case we fell behind.
        final Vector3d curr = this.currentPath.getVectorFromIndex(this.entity, curNode - 1);
        final Vector3d next = this.currentPath.getVectorFromIndex(this.entity, curNode);

        if (entity.getPositionVec().distanceTo(curr) >= 2.0 && entity.getPositionVec().distanceTo(next) >= 2.0)
        {
            int currentIndex = curNode - 1;
            while (currentIndex > 0)
            {
                final Vector3d tempoPos = this.currentPath.getVectorFromIndex(this.entity, currentIndex);
                if (entity.getPositionVec().distanceTo(tempoPos) <= 1.0)
                {
                    this.currentPath.setCurrentPathIndex(currentIndex);
                }
                else
                {
                    // Mark nodes as unreached for debug path drawing
                    if (AbstractPathJob.lastDebugNodesPath != null)
                    {
                        final BlockPos pos = new BlockPos(tempoPos.x, tempoPos.y, tempoPos.z);
                        for (final Node node : AbstractPathJob.lastDebugNodesPath)
                        {
                            if (node.isReachedByWorker() && node.pos.equals(pos))
                            {
                                node.setReachedByWorker(false);
                                break;
                            }
                        }
                    }
                }
                currentIndex--;
            }
        }
    }

    /**
     * Called upon reaching the path end, reset values
     */
    private void onPathFinish()
    {
        clearPath();
    }

    public void updatePath() {}

    /**
     * Don't let vanilla rapidly discard paths, set a timeout before its allowed to use stuck.
     */
    @Override
    protected void checkForStuck(@NotNull final Vector3d positionVec3)
    {
        // Do nothing, unstuck is checked on tick, not just when we have a path
    }

    @Override
    public void clearPath()
    {
        if (pathResult != null)
        {
            pathResult.cancel();
            pathResult.setStatus(PathFindingStatus.CANCELLED);
            pathResult = null;
        }

        destination = null;
        super.clearPath();
    }

    @Nullable
    @Override
    public WaterPathResult moveToWater(final int range, final double speed, final List<Tuple<BlockPos, BlockPos>> ponds)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        return (WaterPathResult) setPathJob(
          new PathJobFindWater(CompatibilityUtils.getWorldFromEntity(ourEntity),
            start,
            ((AbstractEntityCitizen) ourEntity).getCitizenColonyHandler().getWorkBuilding().getPosition(),
            range,
            ponds,
            ourEntity), null, speed);
    }

    @Override
    public TreePathResult moveToTree(final BlockPos startRestriction, final BlockPos endRestriction, final double speed, final List<ItemStorage> excludedTrees, final IColony colony)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        final BlockPos buildingPos = ((AbstractEntityCitizen) entity).getCitizenColonyHandler().getWorkBuilding().getPosition();

        final BlockPos furthestRestriction = BlockPosUtil.getFurthestCorner(start, startRestriction, endRestriction);

        final PathJobFindTree job =
          new PathJobFindTree(CompatibilityUtils.getWorldFromEntity(entity), start, buildingPos, startRestriction, endRestriction, furthestRestriction, excludedTrees, colony, ourEntity);

        return (TreePathResult) setPathJob(job, null, speed);
    }

    @Override
    public TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> excludedTrees, final IColony colony)
    {
        @NotNull BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        final BlockPos buildingPos = ((AbstractEntityCitizen) entity).getCitizenColonyHandler().getWorkBuilding().getPosition();

        if (BlockPosUtil.getDistance2D(buildingPos, ((AbstractEntityCitizen) entity).getPosition()) > range * 4)
        {
            start = buildingPos;
        }

        return (TreePathResult) setPathJob(
          new PathJobFindTree(CompatibilityUtils.getWorldFromEntity(entity), start, buildingPos, range, excludedTrees, colony, ourEntity), null, speed);
    }

    @Nullable
    @Override
    public PathResult moveToLivingEntity(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.getPosX(), e.getPosY(), e.getPosZ(), speed);
    }

    @Nullable
    @Override
    public PathResult moveAwayFromLivingEntity(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(new BlockPos(e.getPositionVec()), distance, speed);
    }

    @Override
    public void setCanSwim(boolean canSwim)
    {
        super.setCanSwim(canSwim);
        getPathingOptions().setCanSwim(canSwim);
    }

    public BlockPos getDesiredPos()
    {
        return desiredPos;
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
}
