package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.BlockUtils;
import com.minecolonies.coremod.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Minecolonies async PathNavigate.
 */
public class PathNavigate extends PathNavigateGround
{
    public static final double MAX_PATHING_LENGTH          = 36.0;
    public static final double PATHING_INTERMEDIARY_LENGTH = 16.0;
    //  Parent class private members
    private final EntityLiving entity;
    private       double       walkSpeed;
    @Nullable
    private       BlockPos     destination;
    @Nullable
    private       BlockPos     originalDestination;
    @Nullable
    private       Future<Path> future;
    @Nullable
    private       PathResult   pathResult;

    /**
     * Instantiates the navigation of an entity.
     *
     * @param entity the entity.
     * @param world  the world it is in.
     */
    public PathNavigate(@NotNull final EntityLiving entity, final World world)
    {
        super(entity, world);
        this.entity = entity;

        this.nodeProcessor = new WalkNodeProcessor();
    }

    @Nullable
    @Override
    protected PathFinder getPathFinder()
    {
        return null;
    }

    @Override
    protected boolean canNavigate()
    {
        return true;
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return this.entity.getPositionVector();
    }

    /**
     * Get the destination from the path.
     * @return the destionation position.
     */
    public BlockPos getDestination()
    {
        return destination;
    }

    @Nullable
    @Override
    public Path getPathToPos(final BlockPos pos)
    {
        //Because this directly returns Path we can't do it async.
        return null;
    }

    //  Re-enable this if path shortcutting becomes a problem; then entities will move more rigidly along world grid
//    @Override
//    protected boolean isDirectPathBetweenPoints(final Vec3d Vec3d, final Vec3d Vec3d1, final int i, final int i1, final int i2)
//    {
//        //we don't use, so it doesn't matter
//        return false;
//    }

    public double getSpeed()
    {
        return walkSpeed;
    }

    @Override
    public void setSpeed(final double d)
    {
        walkSpeed = d;
    }

    @Override
    public boolean tryMoveToXYZ(final double x, final double y, final double z, final double speed)
    {
        moveToXYZ(x, y, z, speed);
        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(@NotNull final Entity e, final double speed)
    {
        return tryMoveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    /**
     * Try to move to a certain position.
     *
     * @param x     the x target.
     * @param y     the y target.
     * @param z     the z target.
     * @param speed the speed to walk.
     * @return the PathResult.
     */
    @Nullable
    public PathResult moveToXYZ(final double x, final double y, final double z, final double speed)
    {
        int newX = MathHelper.floor(x);
        int newY = (int) y;
        int newZ = MathHelper.floor(z);


        if ((destination != null
               && BlockPosUtil.isEqual(destination, newX, newY, newZ))
              || (originalDestination != null
                    && BlockPosUtil.isEqual(originalDestination, newX, newY, newZ)
                    && pathResult != null
                    && pathResult.isInProgress()))
        {
            return pathResult;
        }

        final Vec3d moveVector = getEntityPosition().subtractReverse(new Vec3d(newX, newY, newZ));
        final double moveLength = moveVector.lengthVector();
        if (moveLength >= MAX_PATHING_LENGTH && !this.isUnableToReachDestination())
        {
            final Vec3d newMove = moveVector.scale(PATHING_INTERMEDIARY_LENGTH / moveLength).add(getEntityPosition());
            originalDestination = new BlockPos(newX, newY, newZ);
            newX = MathHelper.floor(newMove.xCoord);
            newY = MathHelper.floor(newMove.yCoord);
            newZ = MathHelper.floor(newMove.zCoord);
        }

        @NotNull final BlockPos start = AbstractPathJob.prepareStart(entity);
        @NotNull final BlockPos dest = new BlockPos(newX, newY, newZ);

        return setPathJob(
          new PathJobMoveToLocation(entity.world, start, dest, (int) getPathSearchRange()),
          dest, speed);
    }

    public boolean isUnableToReachDestination()
    {
        return pathResult != null && pathResult.failedToReachDestination();
    }

    @Nullable
    private PathResult setPathJob(@NotNull final AbstractPathJob job, final BlockPos dest, final double speed)
    {
        clearPathEntity();

        this.destination = dest;
        this.walkSpeed = speed;

        future = Pathfinding.enqueue(job);
        pathResult = job.getResult();
        return pathResult;
    }

    @Override
    public boolean setPath(@NotNull Path path, final double speed)
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
                newPoints[i] = new PathPointExtended(new BlockPos(point.xCoord, point.yCoord, point.zCoord));
            }

            tempPath = new Path(newPoints);

            final PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.xCoord, finalPoint.yCoord, finalPoint.zCoord);
        }

        return super.setPath(tempPath == null ? path : tempPath, speed);
    }

    @Override
    public void onUpdateNavigation()
    {
        if (future != null)
        {
            if (!future.isDone())
            {
                return;
            }

            try
            {
                if (future.get() == null)
                {
                    future = null;
                    return;
                }

                setPath(future.get(), walkSpeed);

                pathResult.setPathLength(getPath().getCurrentPathLength());
                pathResult.setStatus(PathResult.Status.IN_PROGRESS_FOLLOWING);

                final PathPoint p = getPath().getFinalPathPoint();
                if (p != null && destination == null)
                {
                    destination = new BlockPos(p.xCoord, p.yCoord, p.zCoord);

                    //  AbstractPathJob with no destination, did reach it's destination
                    pathResult.setPathReachesDestination(true);
                }
            }
            catch (@NotNull InterruptedException | ExecutionException e)
            {
                Log.getLogger().catching(e);
            }

            future = null;
        }

        int oldIndex = this.noPath() ? 0 : this.getPath().getCurrentPathIndex();
        super.onUpdateNavigation();

        //  Ladder Workaround
        if (!this.noPath())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());

            if (pEx.isOnLadder())
            {
                final Vec3d vec3 = this.getPath().getPosition(this.entity);

                if (vec3.squareDistanceTo(entity.posX, vec3.yCoord, entity.posZ) < 0.1)
                {
                    //This way he is less nervous and gets up the ladder
                    double newSpeed = 0.05;
                    switch (pEx.getLadderFacing())
                    {
                        //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                        case NORTH:
                            vec3.addVector(0, 0, 1);
                            break;
                        case SOUTH:
                            vec3.addVector(0, 0, -1);
                            break;
                        case WEST:
                            vec3.addVector(1, 0, 0);
                            break;
                        case EAST:
                            vec3.addVector(-1, 0, 0);
                            break;
                        //  Any other value is going down, so lets not move at all
                        default:
                            newSpeed = 0;
                            break;
                    }

                    this.entity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, newSpeed);
                }
            }
            else if (entity.isInWater())
            {
                //  Prevent shortcuts when swimming
                final int curIndex = this.getPath().getCurrentPathIndex();
                if (curIndex > 0
                      && (curIndex + 1) < this.getPath().getCurrentPathLength()
                      && this.getPath().getPathPointFromIndex(curIndex - 1).yCoord != pEx.yCoord)
                {
                    //  Work around the initial 'spin back' when dropping into water
                    oldIndex = curIndex + 1;
                }

                this.getPath().setCurrentPathIndex(oldIndex);

                Vec3d Vec3d = this.getPath().getPosition(this.entity);

                if (Vec3d.squareDistanceTo(new Vec3d(entity.posX, Vec3d.yCoord, entity.posZ)) < 0.1
                      && Math.abs(entity.posY - Vec3d.yCoord) < 0.5)
                {
                    this.getPath().setCurrentPathIndex(this.getPath().getCurrentPathIndex() + 1);
                    if (this.noPath())
                    {
                        return;
                    }

                    Vec3d = this.getPath().getPosition(this.entity);
                }

                this.entity.getMoveHelper().setMoveTo(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord, walkSpeed);
            }
            else
            {
                if (BlockUtils.isPathBlock(world.getBlockState(entity.getPosition().down()).getBlock()))
                {
                    speed = 1.3;
                }
                else
                {
                    speed = 1.0;
                }
            }
        }

        if (pathResult != null && noPath())
        {
            pathResult.setStatus(PathResult.Status.COMPLETE);
            pathResult = null;
        }
    }

    @Override
    protected void pathFollow()
    {
        final int curNode = currentPath.getCurrentPathIndex();
        final int curNodeNext = curNode + 1;
        if (curNodeNext < currentPath.getCurrentPathLength())
        {
            final PathPointExtended pEx = (PathPointExtended) currentPath.getPathPointFromIndex(curNode);
            final PathPointExtended pExNext = (PathPointExtended) currentPath.getPathPointFromIndex(curNodeNext);

            //  If current node is bottom of a ladder, then stay on this node until
            //  the entity reaches the bottom, otherwise they will try to head out early
            if (pEx.isOnLadder() && pEx.getLadderFacing() == EnumFacing.DOWN
                  && !pExNext.isOnLadder())
            {
                final Vec3d vec3 = getEntityPosition();
                if ((vec3.yCoord - (double) pEx.yCoord) < 0.001)
                {
                    this.currentPath.setCurrentPathIndex(curNodeNext);
                }

                this.checkForStuck(vec3);
                return;
            }
        }

        super.pathFollow();
    }

    /**
     * If null path or reached the end.
     */
    @Override
    public boolean noPath()
    {
        return future == null && super.noPath();
    }

    @Override
    public void clearPathEntity()
    {
        if (future != null)
        {
            future.cancel(true);
            future = null;
        }

        if (pathResult != null)
        {
            pathResult.setStatus(PathResult.Status.CANCELLED);
            pathResult = null;
        }

        destination = null;
        super.clearPathEntity();
    }

    /**
     * Used to find a tree.
     *
     * @param range in the range.
     * @param speed walking speed.
     * @return the result of the search.
     */
    public PathJobFindTree.TreePathResult moveToTree(final int range, final double speed)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(entity);
        return (PathJobFindTree.TreePathResult) setPathJob(
          new PathJobFindTree(entity.world, start, ((EntityCitizen) entity).getWorkBuilding().getLocation(), range), null, speed);
    }

    /**
     * Used to find a water.
     *
     * @param range in the range.
     * @param speed walking speed.
     * @param ponds a list of ponds.
     * @return the result of the search.
     */
    @Nullable
    public PathJobFindWater.WaterPathResult moveToWater(final int range, final double speed, final List<BlockPos> ponds)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(entity);
        return (PathJobFindWater.WaterPathResult) setPathJob(
          new PathJobFindWater(entity.world, start, ((EntityCitizen) entity).getWorkBuilding().getLocation(), range, ponds), null, speed);
    }

    /**
     * Used to move a living entity with a speed.
     *
     * @param e     the entity.
     * @param speed the speed.
     * @return the result.
     */
    @Nullable
    public PathResult moveToEntityLiving(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    /**
     * Used to path away from a entity.
     *
     * @param e        the entity.
     * @param distance the distance to move to.
     * @param speed    the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable
    public PathResult moveAwayFromEntityLiving(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(e.getPosition(), distance, speed);
    }

    /**
     * Used to path away from a position.
     *
     * @param avoid the position to avoid.
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable
    public PathResult moveAwayFromXYZ(final BlockPos avoid, final double range, final double speed)
    {
        @NotNull final BlockPos start = AbstractPathJob.prepareStart(entity);

        return setPathJob(
          new PathJobMoveAwayFromLocation(entity.world, start, avoid, (int) range, (int) getPathSearchRange()),
          null, speed);
    }
}
