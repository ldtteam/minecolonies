package com.minecolonies.util;

import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Vec3Utils
{
    public static void writeVecToNBT(NBTTagCompound compound, String name, Vec3 vec)
    {
        NBTTagCompound vecCompound = new NBTTagCompound();
        vecCompound.setInteger("x", (int) vec.xCoord);
        vecCompound.setInteger("y", (int) vec.yCoord);
        vecCompound.setInteger("z", (int) vec.zCoord);
        compound.setTag(name, vecCompound);
    }

    public static Vec3 readVecFromNBT(NBTTagCompound compound, String name)
    {
        NBTTagCompound vecCompound = compound.getCompoundTag(name);
        int x = vecCompound.getInteger("x");
        int y = vecCompound.getInteger("y");
        int z = vecCompound.getInteger("z");
        return Vec3.createVectorHelper(x, y, z);
    }

    public static TileEntity getTileEntityFromVec(World world, Vec3 vec)
    {
        return world.getTileEntity((int) vec.xCoord, (int) vec.yCoord, (int) vec.zCoord);
    }

    public static Block getBlockFromVec(World world, Vec3 vec)
    {
        return world.getBlock((int) vec.xCoord, (int) vec.yCoord, (int) vec.zCoord);
    }

    public static int getBlockMetadataFromVec(World world, Vec3 vec)
    {
        return world.getBlockMetadata((int) vec.xCoord, (int) vec.yCoord, (int) vec.zCoord);
    }

    public static int[] vecToInt(Vec3 vec)
    {
        return new int[]{(int) vec.xCoord, (int) vec.yCoord, (int) vec.zCoord};
    }

    /**
     * Method to find the closest townhall
     *
     * @param world world obj
     * @param pos   Vec3 coordinates to check from
     * @return closest TileEntityTownHall
     */
    public static TileEntityTownHall getClosestTownHall(World world, Vec3 pos)
    {
        double closestDist = Double.MAX_VALUE;
        TileEntityTownHall closestTownHall = null;

        if(world == null || world.loadedTileEntityList == null) return null;

        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
            {
                TileEntityTownHall townHall = (TileEntityTownHall) o;

                if(equals(pos, townHall.getPosition())) continue;

                double distanceSquared = townHall.getDistanceFrom(pos);
                if(closestDist > distanceSquared)
                {
                    closestTownHall = townHall;
                    closestDist = distanceSquared;
                }
            }
        return closestTownHall;
    }

    /**
     * find the distance to the closest townhall.
     *
     * @param world world townhall is in
     * @param pos   Vec3 coordinates to check from
     * @return distance to nearest townhall
     */
    public static double getDistanceToClosestTownHall(World world, Vec3 pos)
    {
        double closestDist = Double.MAX_VALUE;

        if(world == null || world.loadedTileEntityList == null) return -1;

        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
            {
                TileEntityTownHall townHall = (TileEntityTownHall) o;

                if(equals(pos, townHall.getPosition())) continue;

                double distanceSquared = townHall.getDistanceFrom(pos);
                if(closestDist > distanceSquared)
                {
                    closestDist = distanceSquared;
                }
            }
        return Math.sqrt(closestDist);
    }

    /**
     * Gives the distance to a given townhall
     *
     * @param pos        Vec3 coordinates to check from
     * @param tileEntity TileEntityTownhall to check to.
     * @return distance
     */
    public static double getDistanceToTileEntity(Vec3 pos, TileEntity tileEntity)
    {
        return Math.sqrt(tileEntity.getDistanceFrom((int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord));
    }

    public static Vec3 scanForBlockNearPoint(World world, Block block, Vec3 pos, Vec3 radiusPos)
    {
        Vec3 closestVec = null;
        double minDistance = Double.MAX_VALUE;

        for(int i = (int) (pos.xCoord - radiusPos.xCoord); i <= (int) (pos.xCoord + radiusPos.xCoord); i++)
        {
            for(int j = (int) (pos.yCoord - radiusPos.yCoord); j <= (int) (pos.yCoord + radiusPos.yCoord); j++)
            {
                for(int k = (int) (pos.zCoord - radiusPos.zCoord); k <= (int) (pos.zCoord + radiusPos.zCoord); k++)
                {
                    if(world.getBlock(i, j, k) == block)
                    {
                        Vec3 tempVec = Vec3.createVectorHelper(i, j, k);

                        if(closestVec == null || tempVec.distanceTo(pos) < minDistance)
                        {
                            closestVec = tempVec;
                            minDistance = closestVec.distanceTo(pos);
                        }
                    }
                }
            }
        }
        return closestVec;
    }

    public static boolean isWorkerAtSite(EntityWorker worker, Vec3 site)
    {
        if(worker.getPosition().squareDistanceTo(site) > 4)//Too far away
        {
            if(worker.getNavigator().noPath())//Not moving
            {
                if(!worker.getNavigator().tryMoveToXYZ(site.xCoord, site.yCoord, site.zCoord, 1.0F))
                {
                    worker.setStatus(EntityWorker.Status.PATHFINDING_ERROR);
                }
            }
            return false;
        }
        else
        {
            if(!worker.getNavigator().noPath())//within 2 blocks - can stop pathing //TODO may not need this check
            {
                worker.getNavigator().clearPathEntity();
            }
            return true;
        }
    }

    public static boolean equals(Vec3 vec1, Vec3 vec2)
    {
        return vec1.xCoord == vec2.xCoord && vec1.yCoord == vec2.yCoord && vec1.zCoord == vec2.zCoord;
    }
}
