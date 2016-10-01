package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Minecolonies async PathNavigate.
 */
public class PathNavigate extends net.minecraft.pathfinding.PathNavigateGround
{
    //  Parent class private members
    private EntityLiving entity;
    private double       walkSpeed;

    @Nullable
    private BlockPos           destination;
    @Nullable
    private Future<PathEntity> future;
    @Nullable
    private PathResult         pathResult;

    private boolean shouldAvoidWater = false;
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
    protected Vec3 getEntityPosition()
    {
        return this.entity.getPositionVector();
    }

    @Override
    protected boolean isDirectPathBetweenPoints(final Vec3 vec3, final Vec3 vec31, final int i, final int i1, final int i2)
    {
        //we don't use, so it doesn't matter
        return false;
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

    @Nullable
    @Override
    public PathEntity getPathToPos(BlockPos pos)
    {
        //Because this directly returns PathEntity we can't do it async.
        return null;
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

    @Override
    public boolean setPath(@NotNull PathEntity path, double speed)
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

            path = new PathEntity(newPoints);

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
                if(future.get() == null)
                {
                    future = null;
                    return;
                }

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
                Log.getLogger().catching(e);
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
                Vec3 vec3 = this.getPath().getPosition(this.entity);

                if (vec3.squareDistanceTo(new Vec3(entity.posX, vec3.yCoord, entity.posZ)) < 0.1)
                {
                    //This way he is less nervous and gets up the ladder
                    double newSpeed = 0.2;
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

                Vec3 vec3 = this.getPath().getPosition(this.entity);

                if (vec3.squareDistanceTo(new Vec3(entity.posX, vec3.yCoord, entity.posZ)) < 0.1 &&
                      Math.abs(entity.posY - vec3.yCoord) < 0.5)
                {
                    this.getPath().setCurrentPathIndex(this.getPath().getCurrentPathIndex() + 1);
                    if (this.noPath())
                    {
                        return;
                    }

                    vec3 = this.getPath().getPosition(this.entity);
                }

                this.entity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, walkSpeed);
            }
        }

        if (pathResult != null && noPath())
        {
            pathResult.setStatus(PathResult.Status.COMPLETE);
            pathResult = null;
        }
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
    private PathResult setPathJob(@NotNull AbstractPathJob job, BlockPos dest, double speed)
    {
        clearPathEntity();

        this.destination = dest;
        this.walkSpeed = speed;

        future = Pathfinding.enqueue(job);
        pathResult = job.getResult();
        return pathResult;
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

    public boolean isUnableToReachDestination()
    {
        return pathResult != null && pathResult.failedToReachDestination();
    }

    //We don't use any of these, but they need to be overriden.
    @Override
    public void setAvoidsWater(boolean avoidsWater)
    {
        this.shouldAvoidWater = avoidsWater;
    }

    @Override
    public boolean getAvoidsWater()
    {
        return shouldAvoidWater;
    }

    @Override
    public void setBreakDoors(boolean canBreakDoors)
    {
        this.canBreakDoors = canBreakDoors;
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
