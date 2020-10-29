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
import net.minecraft.block.VineBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Minecolonies async PathNavigate.
 */
public class MinecoloniesAdvancedPathNavigate extends AbstractAdvancedPathNavigate
{
    private static final double ON_PATH_SPEED_MULTIPLIER = 1.3D;
    private static final double PIRATE_SWIM_BONUS        = 30;
    private static final double BARBARIAN_SWIM_BONUS     = 15;
    private static final double CITIZEN_SWIM_BONUS       = 10;
    public static final  double MIN_Y_DISTANCE           = 0.001;
    public static final  int    MAX_SPEED_ALLOWED        = 100;

    @Nullable
    private PathResult pathResult;

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
    public PathResult moveAwayFromXYZ(final BlockPos avoid, final double range, final double speed)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);

        return setPathJob(new PathJobMoveAwayFromLocation(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          avoid,
          (int) range,
          (int) ourEntity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speed);
    }

    @Nullable
    public RandomPathResult moveToRandomPos(final double range, final double speed)
    {
        if (pathResult instanceof RandomPathResult && pathResult.isComputing())
        {
            return (RandomPathResult) pathResult;
        }

        final int theRange = (int) (entity.getRNG().nextInt((int) range) + range / 2);
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);

        return (RandomPathResult) setPathJob(new PathJobRandomPos(CompatibilityUtils.getWorldFromEntity(ourEntity),
          start,
          theRange,
          (int) ourEntity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speed);
    }

    @Nullable
    public PathResult setPathJob(
      @NotNull final AbstractPathJob job,
      final BlockPos dest,
      final double speed)
    {
        if (dest != null && dest.equals(desiredPos) && calculationFuture != null && pathResult != null)
        {
            return pathResult;
        }

        clearPath();

        this.destination = dest;
        this.originalDestination = dest;
        if (dest != null)
        {
            desiredPos = dest;
            desiredPosTimeout = 50 * 20;
        }
        this.walkSpeed = speed;

        if (speed > MAX_SPEED_ALLOWED)
        {
            Log.getLogger().error("Tried to set a too high speed for entity:" + ourEntity, new Exception());
            return null;
        }

        job.setPathingOptions(getPathingOptions());
        calculationFuture = Pathfinding.enqueue(job);
        pathResult = job.getResult();
        return pathResult;
    }

    @Override
    public boolean noPath()
    {
        return calculationFuture == null && super.noPath();
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

        if (calculationFuture != null)
        {
            if (!calculationFuture.isDone())
            {
                return;
            }

            try
            {
                if (processCompletedCalculationResult())
                {
                    return;
                }
            }
            catch (@NotNull InterruptedException | ExecutionException e)
            {
                Log.getLogger().catching(e);
            }

            calculationFuture = null;
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
            return;
        }
        if (handleRails())
        {
            return;
        }
        super.tick();

        if (pathResult != null && noPath())
        {
            pathResult.setStatus(PathFindingStatus.COMPLETE);
            pathResult = null;
        }

        stuckHandler.checkStuck(this);
    }

    @Nullable
    public PathResult moveToXYZ(final double x, final double y, final double z, final double speed)
    {
        final int newX = MathHelper.floor(x);
        final int newY = (int) y;
        final int newZ = MathHelper.floor(z);

        if (pathResult != null &&
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
            (int) ourEntity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue(),
            ourEntity),
          desiredPos, speed);
    }

    @Override
    public boolean tryMoveToBlockPos(final BlockPos pos, final double speed)
    {
        moveToXYZ(pos.getX(), pos.getY(), pos.getZ(), speed);
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
            else if ((Math.abs(pEx.x - entity.posX) > 7 || Math.abs(pEx.z - entity.posZ) > 7) && ourEntity.ridingEntity != null)
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
    protected Vec3d getEntityPosition()
    {
        return this.ourEntity.getPositionVector();
    }

    @Override
    public Path getPathToPos(final BlockPos pos, final int p_179680_2_)
    {
        //Because this directly returns Path we can't do it async.
        return null;
    }

    @Override
    protected boolean isDirectPathBetweenPoints(final Vec3d start, final Vec3d end, final int sizeX, final int sizeY, final int sizeZ)
    {
        // TODO improve road walking. This is better in some situations, but still not great.
        return !WorkerUtil.isPathBlock(world.getBlockState(new BlockPos(start.x, start.y - 1, start.z)).getBlock())
                 && super.isDirectPathBetweenPoints(start, end, sizeX, sizeY, sizeZ);
    }

    public double getSpeed()
    {
        if (ourEntity instanceof AbstractEntityPirate && ourEntity.isInWater())
        {
            speed = walkSpeed * PIRATE_SWIM_BONUS;
            return speed;
        }
        else if (ourEntity instanceof AbstractEntityBarbarian && ourEntity.isInWater())
        {
            speed = walkSpeed * BARBARIAN_SWIM_BONUS;
            return speed;
        }
        else if (ourEntity instanceof AbstractEntityCitizen && ourEntity.isInWater())
        {
            speed = walkSpeed * CITIZEN_SWIM_BONUS;
            return speed;
        }

        speed = walkSpeed;
        return walkSpeed;
    }

    @Override
    public void setSpeed(final double d)
    {
        if (d > MAX_SPEED_ALLOWED)
        {
            Log.getLogger().error("Tried to set a too high speed for entity:" + ourEntity, new Exception());
            return;
        }
        walkSpeed = d;
    }

    /**
     * Deprecated - try to use BlockPos instead
     */
    @Override
    public boolean tryMoveToXYZ(final double x, final double y, final double z, final double speed)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return false;
        }

        moveToXYZ(x, y, z, speed);
        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(final Entity entityIn, final double speedIn)
    {
        return tryMoveToBlockPos(entityIn.getPosition(), speedIn);
    }

    // Removes stupid vanilla stuff, causing our pathpoints to occasionally be replaced by vanilla ones.
    @Override
    protected void trimPath() {}

    @Override
    public boolean setPath(@Nullable final Path path, final double speed)
    {
        if (path == null)
        {
            clearPath();
            return false;
        }
        pathStartTime = world.getGameTime();
        return super.setPath(convertPath(path), speed);
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

    private boolean processCompletedCalculationResult() throws InterruptedException, ExecutionException
    {
        if (calculationFuture.get() == null)
        {
            calculationFuture = null;
            return true;
        }

        setPath(calculationFuture.get(), getSpeed());

        pathResult.setPathLength(getPath().getCurrentPathLength());
        pathResult.setStatus(PathFindingStatus.IN_PROGRESS_FOLLOWING);

        final PathPoint p = getPath().getFinalPathPoint();
        if (p != null && destination == null)
        {
            destination = new BlockPos(p.x, p.y, p.z);

            //  AbstractPathJob with no destination, did reach it's destination
            pathResult.setPathReachesDestination(true);
        }
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
                    Vec3d motion = this.entity.getMotion();
                    double x = motion.x < -0.1 ? -0.1 : Math.min(motion.x, 0.1);
                    double z = motion.x < -0.1 ? -0.1 : Math.min(motion.z, 0.1);

                    this.ourEntity.setMotion(x, motion.y, z);
                    break;
                }
            }

            if (pEx.isOnLadder() && pExNext != null && (pEx.y != pExNext.y || entity.posY > pEx.y))
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
                    speed = ON_PATH_SPEED_MULTIPLIER * getSpeed();
                }
                else
                {
                    speed = getSpeed();
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
        int blockX = (int)Math.round(parEntity.posX);
        int blockY = MathHelper.floor(parEntity.posY-0.2D);
        int blockZ = (int)Math.round(parEntity.posZ);
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
                    minecart.setMotion(Vec3d.ZERO);
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
            final Vec3d motion = entity.ridingEntity.getMotion();
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
        Vec3d vec3 = this.getPath().getPosition(this.ourEntity);

        if (vec3.squareDistanceTo(ourEntity.posX, vec3.y, ourEntity.posZ) < Math.random() * 0.1)
        {
            //This way he is less nervous and gets up the ladder
            double newSpeed = 0.05;
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
                if (world.getBlockState(ourEntity.getPosition()).getBlock() instanceof VineBlock)
                {
                    this.ourEntity.setMotion(this.ourEntity.getMotion().add(0, 0.1D, 0));
                }
                this.ourEntity.getMoveHelper().setMoveTo(vec3.x, vec3.y, vec3.z, newSpeed);
            }
            else
            {
                if (world.getBlockState(ourEntity.getPosition().down()).isLadder(world, ourEntity.getPosition().down(), ourEntity))
                {
                    this.ourEntity.setMoveVertical(-0.5f);
                }
                else
                {
                    this.ourEntity.getNavigator().clearPath();
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

        Vec3d vec3d = this.getPath().getPosition(this.ourEntity);

        if (vec3d.squareDistanceTo(new Vec3d(ourEntity.posX, vec3d.y, ourEntity.posZ)) < 0.1
              && Math.abs(ourEntity.posY - vec3d.y) < 0.5)
        {
            this.getPath().incrementPathIndex();
            if (this.noPath())
            {
                return true;
            }

            vec3d = this.getPath().getPosition(this.ourEntity);
        }

        ourEntity.setAIMoveSpeed((float) getSpeed());
        this.ourEntity.getMoveHelper().setMoveTo(vec3d.x, vec3d.y, vec3d.z, getSpeed());
        return false;
    }

    @Override
    protected void pathFollow()
    {
        getSpeed();
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
                final Vec3d vec3 = getEntityPosition();
                if ((vec3.y - (double) pEx.y) < MIN_Y_DISTANCE)
                {
                    this.currentPath.setCurrentPathIndex(curNodeNext);
                }

                this.checkForStuck(vec3);
                return;
            }
        }

        Vec3d vec3d = this.getEntityPosition();
        this.maxDistanceToWaypoint = this.entity.getWidth() > 0.75F ? this.entity.getWidth() / 2.0F : 0.75F - this.entity.getWidth() / 2.0F;
        Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex());
        // Forge: fix MC-94054
        if (Math.abs(this.entity.getPosX() - vec3d1.x) < (double) this.maxDistanceToWaypoint
              && Math.abs(this.entity.getPosZ() - vec3d1.z) < (double) this.maxDistanceToWaypoint &&
              Math.abs(this.entity.getPosY() - vec3d1.y) < 1.0D)
        {
            this.currentPath.incrementPathIndex();
        }
        else
        {
            // Look ahead if we were too fast.
            for (int i = this.currentPath.getCurrentPathIndex(); i < Math.min(this.currentPath.getCurrentPathLength(), this.currentPath.getCurrentPathIndex() + 4); i++)
            {
                Vec3d vec3d2 = this.currentPath.getVectorFromIndex(this.entity, i);
                if (Math.abs(this.entity.getPosX() - vec3d2.x) < (double) this.maxDistanceToWaypoint
                      && Math.abs(this.entity.getPosZ() - vec3d2.z) < (double) this.maxDistanceToWaypoint &&
                      Math.abs(this.entity.getPosY() - vec3d2.y) < 1.0D)
                {
                    this.currentPath.setCurrentPathIndex(i);
                    break;
                }
            }
        }

        this.checkForStuck(vec3d);
    }

    public void updatePath() {}

    /**
     * Don't let vanilla rapidly discard paths, set a timeout before its allowed to use stuck.
     */
    @Override
    protected void checkForStuck(@NotNull final Vec3d positionVec3)
    {
        // Do nothing, unstuck is checked on tick, not just when we have a path
    }

    @Override
    public void clearPath()
    {
        if (calculationFuture != null)
        {
            calculationFuture.cancel(true);
            calculationFuture = null;
        }

        if (pathResult != null)
        {
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
    public TreePathResult moveToTree(final BlockPos startRestriction, final BlockPos endRestriction, final double speed, final List<ItemStorage> treesToCut, final IColony colony)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        final BlockPos buildingPos = ((AbstractEntityCitizen) entity).getCitizenColonyHandler().getWorkBuilding().getPosition();

        final PathJobFindTree job =
          new PathJobFindTree(CompatibilityUtils.getWorldFromEntity(entity), start, buildingPos, startRestriction, endRestriction, treesToCut, colony, ourEntity);

        return (TreePathResult) setPathJob(job, null, speed);
    }

    @Override
    public TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> treesToCut, final IColony colony)
    {
        @NotNull BlockPos start = AbstractPathJob.prepareStart(ourEntity);
        final BlockPos buildingPos = ((AbstractEntityCitizen) entity).getCitizenColonyHandler().getWorkBuilding().getPosition();

        if (BlockPosUtil.getDistance2D(buildingPos, entity.getPosition()) > range * 4)
        {
            start = buildingPos;
        }

        return (TreePathResult) setPathJob(
          new PathJobFindTree(CompatibilityUtils.getWorldFromEntity(entity), start, buildingPos, range, treesToCut, colony, ourEntity), null, speed);
    }

    @Nullable
    @Override
    public PathResult moveToLivingEntity(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    @Nullable
    @Override
    public PathResult moveAwayFromLivingEntity(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(e.getPosition(), distance, speed);
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
