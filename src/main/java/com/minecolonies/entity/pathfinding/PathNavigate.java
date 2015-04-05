package com.minecolonies.entity.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
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
        ChunkCoordinates newDestination = new ChunkCoordinates(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z));

        if (destination.equals(newDestination))
        {
            return true;
        }

        clearPathEntity();

        destination = newDestination;

        if (future != null)
        {
            future.cancel(true);
            future = null;
        }

        this.speed = speed;

        future = Pathfinding.enqueue(
                new PathJob(theEntity, worldObj,
                        new ChunkCoordinates(MathHelper.floor_double(theEntity.posX), (int)theEntity.posY, MathHelper.floor_double(theEntity.posZ)),
                        destination));

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

        super.onUpdateNavigation();
    }

    @Override
    public void clearPathEntity()
    {
        destination = null;
        super.clearPathEntity();
    }
}
