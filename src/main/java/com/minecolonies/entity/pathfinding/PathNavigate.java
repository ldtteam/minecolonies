package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
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
    //  Parent class private members
    private EntityLiving entity;
    private double       walkSpeed;

    @Nullable
    private BlockPos     destination;
    @Nullable
    private Future<Path> future;
    @Nullable
    private PathResult   pathResult;

    private boolean canEnterDoors    = false;
    private boolean canBreakDoors    = false;
    private boolean canSwim          = false;

    public PathNavigate(@NotNull EntityLiving entity, World world)
    {
        super(entity, world);
        this.entity = entity;
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

    @Nullable
    @Override
    public Path getPathToPos(BlockPos pos)
    {
        //Because this directly returns Path we can't do it async.
        return null;
    }

    @Override
    protected boolean isDirectPathBetweenPoints(final Vec3d Vec3d, final Vec3d Vec3d1, final int i, final int i1, final int i2)
    {
        //we don't use, so it doesn't matter
        return false;
    }

    @Override
    public void setBreakDoors(boolean canBreakDoors)
    {
        this.canBreakDoors = canBreakDoors;
    }

    public double getSpeed()
    {
        return walkSpeed;
    }

    @Override
    public void setSpeed(double d)
    {
        walkSpeed = d;
    }

    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, double speed)
    {
        moveToXYZ(x, y, z, speed);
        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(@NotNull Entity e, double speed)
    {
        return tryMoveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    @Nullable
    public PathResult moveToXYZ(double x, double y, double z, double speed)
    {
        int newX = MathHelper.floor_double(x);
        int newY = (int) y;
        int newZ = MathHelper.floor_double(z);

        if (!noPath() &&
              destination != null &&
              BlockPosUtil.isEqual(destination, newX, newY, newZ))
        {
            return pathResult;
        }

        @NotNull BlockPos start = AbstractPathJob.prepareStart(entity);
        @NotNull BlockPos dest = new BlockPos(newX, newY, newZ);

        return setPathJob(
          new PathJobMoveToLocation(entity.worldObj, start, dest, (int) getPathSearchRange()),
          dest, speed);
    }

    @Nullable
    private PathResult setPathJob(@NotNull AbstractPathJob job, BlockPos dest, double speed)
    {
        clearPathEntity();

        this.destination = dest;
        this.walkSpeed = speed;

        future = Pathfinding.enqueue(job);
        pathResult = job.getResult();
        return pathResult;
    }

    @Override
    public boolean setPath(@NotNull Path path, double speed)
    {
        int pathLength = path.getCurrentPathLength();
        if (pathLength > 0 && !(path.getPathPointFromIndex(0) instanceof PathPointExtended))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            @NotNull PathPointExtended[] newPoints = new PathPointExtended[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                PathPoint point = path.getPathPointFromIndex(i);
                newPoints[i] = new PathPointExtended(new BlockPos(point.xCoord, point.yCoord, point.zCoord));
            }

            path = new Path(newPoints);

            PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.xCoord, finalPoint.yCoord, finalPoint.zCoord);
        }

        return super.setPath(path, speed);
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
                setPath(future.get(), walkSpeed);

                pathResult.setPathLength(getPath().getCurrentPathLength());
                pathResult.setStatus(PathResult.Status.IN_PROGRESS_FOLLOWING);

                PathPoint p = getPath().getFinalPathPoint();
                if (p != null && destination == null)
                {
                    destination = new BlockPos(p.xCoord, p.yCoord, p.zCoord);

                    //  AbstractPathJob with no destination, did reach it's destination
                    pathResult.setPathReachesDestination(true);
                }
            }
            catch (@NotNull InterruptedException | ExecutionException e)
            {
                Log.logger.catching(e);
            }

            future = null;
        }

        int oldIndex = this.noPath() ? 0 : this.getPath().getCurrentPathIndex();
        super.onUpdateNavigation();

        //  Ladder Workaround
        if (!this.noPath())
        {
            @NotNull PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());

            if (pEx.isOnLadder)
            {
                Vec3d vec3 = this.getPath().getPosition(this.entity);

                if (vec3.squareDistanceTo(new Vec3d(entity.posX, vec3.yCoord, entity.posZ)) < 0.1)
                {
                    //This way he is less nervous and gets up the ladder
                    double newSpeed = 0.05;
                    switch (pEx.ladderFacing)
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
                int curIndex = this.getPath().getCurrentPathIndex();
                if (curIndex > 0 &&
                      (curIndex + 1) < this.getPath().getCurrentPathLength() &&
                      this.getPath().getPathPointFromIndex(curIndex - 1).yCoord != pEx.yCoord)
                {
                    //  Work around the initial 'spin back' when dropping into water
                    oldIndex = curIndex + 1;
                }

                this.getPath().setCurrentPathIndex(oldIndex);

                Vec3d Vec3d = this.getPath().getPosition(this.entity);

                if (Vec3d.squareDistanceTo(new Vec3d(entity.posX, Vec3d.yCoord, entity.posZ)) < 0.1 &&
                      Math.abs(entity.posY - Vec3d.yCoord) < 0.5)
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
        int curNode = currentPath.getCurrentPathIndex();
        int curNodeNext = curNode + 1;
        if (curNodeNext < currentPath.getCurrentPathLength())
        {
            PathPointExtended pEx = (PathPointExtended)currentPath.getPathPointFromIndex(curNode);
            PathPointExtended pExNext = (PathPointExtended)currentPath.getPathPointFromIndex(curNodeNext);

            //  If current node is bottom of a ladder, then stay on this node until
            //  the entity reaches the bottom, otherwise they will try to head out early
            if (pEx.isOnLadder && pEx.ladderFacing == EnumFacing.DOWN &&
                    !pExNext.isOnLadder)
            {
                Vec3d vec3 = getEntityPosition();
                if ((vec3.yCoord - (double)pEx.yCoord) < 0.001)
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
     * If null path or reached the end
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

    @NotNull
    public PathJobFindTree.TreePathResult moveToTree(int range, double speed)
    {
        @NotNull BlockPos start = AbstractPathJob.prepareStart(entity);
        return (PathJobFindTree.TreePathResult) setPathJob(
          new PathJobFindTree(entity.worldObj, start, ((EntityCitizen) entity).getWorkBuilding().getLocation(), range), null, speed);
    }

    @Nullable
    public PathJobFindWater.WaterPathResult moveToWater(int range, double speed, List<BlockPos> ponds)
    {
        @NotNull BlockPos start = AbstractPathJob.prepareStart(entity);
        return (PathJobFindWater.WaterPathResult) setPathJob(
          new PathJobFindWater(entity.worldObj, start, ((EntityCitizen) entity).getWorkBuilding().getLocation(), range, ponds), null, speed);
    }

    @Nullable
    public PathResult moveToEntityLiving(@NotNull Entity e, double speed)
    {
        return moveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    @Nullable
    public PathResult moveAwayFromEntityLiving(@NotNull Entity e, double distance, double speed)
    {
        return moveAwayFromXYZ(e.getPosition(), distance, speed);
    }

    @Nullable
    public PathResult moveAwayFromXYZ(BlockPos avoid, double range, double speed)
    {
        @NotNull BlockPos start = AbstractPathJob.prepareStart(entity);

        return setPathJob(
          new PathJobMoveAwayFromLocation(entity.worldObj, start, avoid, (int) range, (int) getPathSearchRange()),
          null, speed);
    }

    //We don't use any of these, but they need to be overriden.
    /*@Override
    public void setAvoidsWater(boolean avoidsWater)
    {
        this.shouldAvoidWater = avoidsWater;
    }

    @Override
    public boolean getAvoidsWater()
    {
        return shouldAvoidWater;
    }*/

    public boolean isUnableToReachDestination()
    {
        return pathResult != null && pathResult.failedToReachDestination();
    }

    @Override
    public void setEnterDoors(boolean canEnterDoors)
    {
        this.canEnterDoors = canEnterDoors;
    }

    @Override
    public boolean getEnterDoors()
    {
        return canEnterDoors;
    }

    @Override
    public void setCanSwim(boolean canSwim)
    {
        this.canSwim = canSwim;
    }

    @Override
    public boolean getCanSwim()
    {
        return canSwim;
    }
}
