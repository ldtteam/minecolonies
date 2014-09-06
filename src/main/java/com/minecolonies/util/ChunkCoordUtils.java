package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ChunkCoordUtils
{
    public static void writeToNBT(NBTTagCompound compound, String name, ChunkCoordinates coords)
    {
        NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", coords.posX);
        coordsCompound.setInteger("y", coords.posY);
        coordsCompound.setInteger("z", coords.posZ);
        compound.setTag(name, coordsCompound);
    }

    public static ChunkCoordinates readFromNBT(NBTTagCompound compound, String name)
    {
        NBTTagCompound coordsCompound = compound.getCompoundTag(name);
        int x = coordsCompound.getInteger("x");
        int y = coordsCompound.getInteger("y");
        int z = coordsCompound.getInteger("z");
        return new ChunkCoordinates(x, y, z);
    }

    public static void writeToNBTTagList(NBTTagList tagList, ChunkCoordinates coords)
    {
        NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", coords.posX);
        coordsCompound.setInteger("y", coords.posY);
        coordsCompound.setInteger("z", coords.posZ);
        tagList.appendTag(coordsCompound);
    }

    public static ChunkCoordinates readFromNBTTagList(NBTTagList tagList, int index)
    {
        NBTTagCompound coordsCompound = tagList.getCompoundTagAt(index);
        int x = coordsCompound.getInteger("x");
        int y = coordsCompound.getInteger("y");
        int z = coordsCompound.getInteger("z");
        return new ChunkCoordinates(x, y, z);
    }

    public static TileEntity getTileEntity(World world, ChunkCoordinates coords)
    {
        return world.getTileEntity(coords.posX, coords.posY, coords.posZ);
    }

    public static Block getBlock(World world, ChunkCoordinates coords)
    {
        return world.getBlock(coords.posX, coords.posY, coords.posZ);
    }

    public static int getBlockMetadata(World world, ChunkCoordinates coords)
    {
        return world.getBlockMetadata(coords.posX, coords.posY, coords.posZ);
    }

    /**
     * Method to find the closest townhall
     *
     * @param world world obj
     * @param pos   {@link ChunkCoordinates} to check from
     * @return closest TileEntityTownHall
     */
    public static TileEntityTownHall getClosestTownHall(World world, ChunkCoordinates pos)
    {
        return Utils.getClosestTownHall(world, pos.posX, pos.posY, pos.posZ);
    }

    /**
     * find the distance to the closest townhall.
     *
     * @param world world townhall is in
     * @param pos   {@link ChunkCoordinates} to check from
     * @return distance to nearest townhall
     */
    public static double getDistanceToClosestTownHall(World world, ChunkCoordinates pos)
    {
        return Utils.getDistanceToClosestTownHall(world, pos.posX, pos.posY, pos.posZ);
    }

    /**
     * Gives the distance to a given townhall
     *
     * @param pos        {@link ChunkCoordinates} to check from
     * @param tileEntity TileEntityTownhall to check to.
     * @return distance
     */
    public static double getDistanceToTileEntity(ChunkCoordinates pos, TileEntity tileEntity)
    {
        return Utils.getDistanceToTileEntity(pos.posX, pos.posY, pos.posZ, tileEntity);
    }

    public static ChunkCoordinates scanForBlockNearPoint(World world, Block block, ChunkCoordinates pos, ChunkCoordinates radiusPos)
    {
        return Utils.scanForBlockNearPoint(world, block, pos.posX, pos.posY, pos.posZ, radiusPos.posX, radiusPos.posY, radiusPos.posZ);
    }

    public static boolean isPathingTo(EntityCitizen citizen, ChunkCoordinates pos)
    {
        return Utils.isPathingTo(citizen, pos.posX, pos.posZ);
    }

    public static boolean isWorkerAtSite(EntityWorker worker, ChunkCoordinates site)
    {
        return Utils.isWorkerAtSite(worker, site.posX, site.posY, site.posZ);
    }

    public static boolean isWorkerAtSiteWithMove(EntityWorker worker, ChunkCoordinates site)
    {
        return Utils.isWorkerAtSiteWithMove(worker, site.posX, site.posY, site.posZ);
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, ChunkCoordinates destination)
    {
        return Utils.tryMoveLivingToXYZ(living, destination.posX, destination.posY, destination.posZ);
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, ChunkCoordinates destination, double speed)
    {
        return Utils.tryMoveLivingToXYZ(living, destination.posX, destination.posY, destination.posZ, speed);
    }

    public static float distanceTo(ChunkCoordinates coords, int x, int y, int z)
    {
        return distanceTo(coords, new ChunkCoordinates(x, y, z));
    }

    public static float distanceTo(ChunkCoordinates coords1, ChunkCoordinates coords2)
    {
        return MathHelper.sqrt_float(coords2.getDistanceSquaredToChunkCoordinates(coords1));
    }

    public static boolean equals(ChunkCoordinates coords, int x, int y, int z)
    {
        return coords.equals(new ChunkCoordinates(x, y, z));
    }

    /**
     * @return coordinates with the result of {@code coords2} minus {@code coords1}
     */
    public static ChunkCoordinates subtract(ChunkCoordinates coords1, ChunkCoordinates coords2)
    {
        return new ChunkCoordinates(coords2.posX - coords1.posX, coords2.posY - coords1.posY, coords2.posZ - coords1.posZ);
    }

    /**
     * @return the result of {@code x},{@code y},{@code z} added to {@code coords}
     */
    public static ChunkCoordinates add(ChunkCoordinates coords, int x, int y, int z)
    {
        return new ChunkCoordinates(coords.posX + x, coords.posY + y, coords.posZ + z);
    }
}
