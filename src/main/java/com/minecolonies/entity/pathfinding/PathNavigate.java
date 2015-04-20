package com.minecolonies.entity.pathfinding;

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

import java.util.concurrent.Future;

public class PathNavigate extends net.minecraft.pathfinding.PathNavigate
{
    //  Parent class private members
    protected EntityLiving       theEntity;
    protected World              worldObj;
    protected double             speed;
    protected IAttributeInstance pathSearchRange;

    protected boolean canPassOpenWoodenDoors = true;
    protected boolean canSwim;
    protected boolean noSunPathfind;

    protected ChunkCoordinates  destination;
    protected Future<PathEntity> future;

    public PathNavigate(EntityLiving entity, World world)
    {
        super(entity, world);
        this.theEntity = entity;
        this.worldObj = world;
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
        int newX = MathHelper.floor_double(x);
        int newY = (int)y;
        int newZ = MathHelper.floor_double(z);

        if (!noPath() &&
                destination != null &&
                destination.posX == newX &&
                destination.posY == newY &&
                destination.posZ == newZ)
        {
            return true;
        }

        clearPathEntity();

        if (future != null)
        {
            future.cancel(true);
            future = null;
        }

        ChunkCoordinates start = PathJob.prepareStart(theEntity, worldObj);
        destination = new ChunkCoordinates(newX, newY, newZ);
        this.speed = speed;

        future = Pathfinding.enqueue(new PathJob(worldObj, start, destination, (int)getPathSearchRange()));

        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity e, double speed)
    {
        return tryMoveToXYZ(e.posX, e.posY, e.posZ, speed);
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
            }
            catch (Exception e) {}

            future = null;
        }

        int oldIndex = this.noPath() ? 0 : this.getPath().getCurrentPathIndex();
        super.onUpdateNavigation();

        //  Ladder Workaround
        if (!this.noPath())
        {
            try
            {
                PathPointExtended pEx = (PathPointExtended) this.getPath().getPathPointFromIndex(this.getPath().getCurrentPathIndex());

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
            }
<<<<<<< HEAD
            catch(Exception e)
            {
                //Just stop crashing!
            }

=======
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
>>>>>>> origin/Pathfinder
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
}
