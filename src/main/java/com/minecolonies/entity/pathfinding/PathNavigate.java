package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.Future;

public class PathNavigate extends net.minecraft.pathfinding.PathNavigate
{
    //  Parent class private members
    protected EntityLiving       theEntity;
    protected double             speed;
    protected IAttributeInstance pathSearchRange;

    protected boolean canPassOpenWoodenDoors = true;
    protected boolean canSwim;
    protected boolean noSunPathfind;

    protected ChunkCoordinates   destination;
    protected Future<PathEntity> future;
    protected PathResult         pathResult;

    public PathNavigate(EntityLiving entity, World world)
    {
        super(entity, world);
        this.theEntity = entity;
        this.pathSearchRange = entity.getEntityAttribute(SharedMonsterAttributes.followRange);
    }

    public double getSpeed() { return speed; }
    @Override public void setSpeed(double d) { speed = d; super.setSpeed(d); }

    public boolean getAvoidSun() { return noSunPathfind; }
    @Override public void setAvoidSun(boolean b) { noSunPathfind = b; super.setAvoidSun(b); }

    public boolean getEnterDoors() { return canPassOpenWoodenDoors; }
    @Override public void setEnterDoors(boolean b) { canPassOpenWoodenDoors = b; super.setEnterDoors(b);}

    public boolean getCanSwim() { return canSwim; }
    @Override public void setCanSwim(boolean b) { canSwim = b; super.setCanSwim(b); }

    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, double speed)
    {
        moveToXYZ(x, y, z, speed);
        return true;
    }

    public PathResult moveToXYZ(double x, double y, double z, double speed)
    {
        int newX = MathHelper.floor_double(x);
        int newY = (int)y;
        int newZ = MathHelper.floor_double(z);

        if (!noPath() &&
                destination != null &&
                ChunkCoordUtils.equals(destination, newX, newY, newZ))
        {
            return pathResult;
        }

        ChunkCoordinates start = PathJob.prepareStart(theEntity);
        ChunkCoordinates dest = new ChunkCoordinates(newX, newY, newZ);

        return setPathJob(
                new PathJobMoveToLocation(theEntity.worldObj, start, dest, (int)getPathSearchRange()),
                dest, speed);
    }

    public PathResult moveAwayFromXYZ(double x, double y, double z, double range, double speed)
    {
        ChunkCoordinates start = PathJob.prepareStart(theEntity);
        ChunkCoordinates avoid = new ChunkCoordinates(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z));

        return setPathJob(
                new PathJobMoveAwayFromLocation(theEntity.worldObj, start, avoid, (int)range, (int)getPathSearchRange()),
                null, speed);
    }

    public PathJobFindTree.TreePathResult moveToTree(int range, double speed)
    {
        ChunkCoordinates start = PathJob.prepareStart(theEntity);
        return (PathJobFindTree.TreePathResult) setPathJob(new PathJobFindTree(theEntity.worldObj, start, ((EntityCitizen)theEntity).getWorkBuilding().getLocation(), range), null, speed);
    }

    public PathJobFindWater.WaterPathResult moveToWater(int range, double speed, List<ChunkCoordinates> ponds)
    {
        ChunkCoordinates start = PathJob.prepareStart(theEntity);
        return (PathJobFindWater.WaterPathResult) setPathJob(new PathJobFindWater(theEntity.worldObj, start, ((EntityCitizen)theEntity).getWorkBuilding().getLocation(), range, ponds), null, speed);
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity e, double speed)
    {
        return tryMoveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    public PathResult moveToEntityLiving(Entity e, double speed)
    {
        return moveToXYZ(e.posX, e.posY, e.posZ, speed);
    }

    public PathResult moveAwayFromEntityLiving(Entity e, double distance, double speed)
    {
        return moveAwayFromXYZ(e.posX, e.posY, e.posZ, distance, speed);
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
                setPath(future.get(), speed);

                pathResult.setPathLength(getPath().getCurrentPathLength());
                pathResult.setStatus(PathResult.Status.IN_PROGRESS_FOLLOWING);

                PathPoint p = getPath().getFinalPathPoint();
                if (p != null && destination == null)
                {
                    destination = new ChunkCoordinates(p.xCoord, p.yCoord, p.zCoord);
                    pathResult.setPathReachesDestination(true);    //  PathJob with no destination, did reach it's destination
                }
            }
            catch (Exception e) {}

            future = null;
        }

        int oldIndex = this.noPath() ? 0 : this.getPath().getCurrentPathIndex();
        super.onUpdateNavigation();

        //  Ladder Workaround
        if (!this.noPath())
        {
            PathPointExtended pEx = (PathPointExtended)this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());

            if (pEx.isOnLadder)
            {
                Vec3 vec3 = this.getPath().getPosition(this.theEntity);

                if (vec3.squareDistanceTo(theEntity.posX, vec3.yCoord, theEntity.posZ) < 0.1)
                {
                    double newSpeed = this.speed;

                    switch (pEx.ladderFacing)
                    {
                        //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                        case 2:
                            vec3.zCoord += 1;
                            break;
                        case 3:
                            vec3.zCoord -= 1;
                            break;
                        case 4:
                            vec3.xCoord += 1;
                            break;
                        case 5:
                            vec3.xCoord -= 1;
                            break;
                        //  Any other value is going down, so lets not move at all
                        default:
                            newSpeed = 0;
                            break;
                    }

                    this.theEntity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, newSpeed);
                }
            }
            else if (theEntity.isInWater())
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

                Vec3 vec3 = this.getPath().getPosition(this.theEntity);

                if (vec3.squareDistanceTo(theEntity.posX, vec3.yCoord, theEntity.posZ) < 0.1 &&
                        Math.abs(theEntity.posY - vec3.yCoord) < 0.5)
                {
                    this.getPath().setCurrentPathIndex(this.getPath().getCurrentPathIndex() + 1);
                    if (this.noPath())
                    {
                        return;
                    }

                    vec3 = this.getPath().getPosition(this.theEntity);
                }

                this.theEntity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, speed);
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

    @Override
    public boolean setPath(PathEntity path, double speed)
    {
        int pathLength = path.getCurrentPathLength();
        if (pathLength > 0 && !(path.getPathPointFromIndex(0) instanceof PathPointExtended))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            PathPointExtended newPoints[] = new PathPointExtended[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                PathPoint point = path.getPathPointFromIndex(i);
                newPoints[i] = new PathPointExtended(point.xCoord, point.yCoord, point.zCoord);
            }

            path = new PathEntity(newPoints);

            PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new ChunkCoordinates(finalPoint.xCoord, finalPoint.yCoord, finalPoint.zCoord);
        }

        return super.setPath(path, speed);
    }

    private PathResult setPathJob(PathJob job, ChunkCoordinates dest, double speed)
    {
        clearPathEntity();

        this.destination = dest;
        this.speed = speed;

        future = Pathfinding.enqueue(job);
        pathResult = job.getResult();
        return pathResult;
    }

    public boolean isUnableToReachDestination()
    {
        return pathResult != null && pathResult.failedToReachDestination();
    }
}
