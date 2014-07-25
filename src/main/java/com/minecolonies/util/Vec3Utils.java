package com.minecolonies.util;

import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
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

    /**
     * Method to find the closest townhall
     *
     * @param world world obj
     * @param pos   Vec3 coordinates to check from
     * @return closest TileEntityTownHall
     */
    public static TileEntityTownHall getClosestTownHall(World world, Vec3 pos)
    {
        return Utils.getClosestTownHall(world, (int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord);
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
        return Utils.getDistanceToClosestTownHall(world, (int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord);
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
        return Utils.getDistanceToTileEntity((int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord, tileEntity);
    }

    public static Vec3 scanForBlockNearPoint(World world, Block block, Vec3 pos, Vec3 radiusPos)
    {
        return Utils.scanForBlockNearPoint(world, block, (int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord, (int) radiusPos.xCoord, (int) radiusPos.yCoord, (int) radiusPos.zCoord);
    }

    public static boolean isWorkerAtSite(EntityWorker worker, Vec3 site)
    {
        return Utils.isWorkerAtSite(worker, (int) site.xCoord, (int) site.yCoord, (int) site.zCoord);
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, Vec3 destination)
    {
        return Utils.tryMoveLivingToXYZ(living, (int) destination.xCoord, (int) destination.yCoord, (int) destination.zCoord);
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, Vec3 destination, double speed)
    {
        return Utils.tryMoveLivingToXYZ(living, (int) destination.xCoord, (int) destination.yCoord, (int) destination.zCoord, speed);
    }

    public static double distanceTo(Vec3 vec, int x, int y, int z)
    {
        return vec.distanceTo(Vec3.createVectorHelper(x, y, z));
    }

    public static boolean equals(Vec3 vec, int x, int y, int z)
    {
        return equals(vec, Vec3.createVectorHelper(x, y, z));
    }

    public static boolean equals(Vec3 vec1, Vec3 vec2)
    {
        return vec1.xCoord == vec2.xCoord && vec1.yCoord == vec2.yCoord && vec1.zCoord == vec2.zCoord;
    }
}
