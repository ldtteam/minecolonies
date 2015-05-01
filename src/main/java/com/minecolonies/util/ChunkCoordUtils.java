package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.pathfinding.PathResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

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

    public static void writeToByteBuf(ByteBuf buf, ChunkCoordinates coords)
    {
        buf.writeInt(coords.posX);
        buf.writeInt(coords.posY);
        buf.writeInt(coords.posZ);
    }

    public static ChunkCoordinates readFromByteBuf(ByteBuf buf)
    {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
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

    public static List<ItemStack> getBlockDrops(World world, ChunkCoordinates coords, int fortune)
    {
        return getBlock(world, coords).getDrops(world, coords.posX, coords.posY, coords.posZ, getBlockMetadata(world, coords), fortune);
    }

    public static boolean setBlock(World world, ChunkCoordinates coords, Block block)
    {
        return world.setBlock(coords.posX, coords.posY, coords.posZ, block);
    }

    public static boolean setBlock(World world, ChunkCoordinates coords, Block block, int metadata, int flag)
    {
        return world.setBlock(coords.posX, coords.posY, coords.posZ, block, metadata, flag);
    }

    public static ChunkCoordinates scanForBlockNearPoint(World world, Block block, ChunkCoordinates pos, ChunkCoordinates radiusPos)
    {
        return Utils.scanForBlockNearPoint(world, block, pos.posX, pos.posY, pos.posZ, radiusPos.posX, radiusPos.posY, radiusPos.posZ);
    }

    public static boolean isPathingTo(EntityCitizen citizen, ChunkCoordinates pos)
    {
        return Utils.isPathingTo(citizen, pos.posX, pos.posZ);
    }

    public static boolean isWorkerAtSite(EntityCitizen worker, ChunkCoordinates site)
    {
        return Utils.isWorkerAtSite(worker, site.posX, site.posY, site.posZ);
    }

    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, ChunkCoordinates site)
    {
        return Utils.isWorkerAtSiteWithMove(worker, site.posX, site.posY, site.posZ);
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, ChunkCoordinates destination)
    {
        return Utils.tryMoveLivingToXYZ(living, destination.posX, destination.posY, destination.posZ);
    }

    public static PathResult moveLivingToXYZ(EntityCitizen citizen, ChunkCoordinates destination)
    {
        return citizen.getNavigator().moveToXYZ(destination.posX, destination.posY, destination.posZ, 1.0);
    }

    public static float distanceSqrd(ChunkCoordinates coords, int x, int y, int z)
    {
        return coords.getDistanceSquared(x, y, z);
    }

    public static float distanceSqrd(ChunkCoordinates coords1, Vec3 coords2)
    {
        return coords1.getDistanceSquared((int)coords2.xCoord, (int)coords2.yCoord, (int)coords2.zCoord);
    }

    public static boolean equals(ChunkCoordinates coords, int x, int y, int z)
    {
        return coords.posX == x && coords.posY == y && coords.posZ == z;
    }

    /**
     * @return coordinates with the result of {@code coords1} minus {@code coords2}
     */
    public static ChunkCoordinates subtract(ChunkCoordinates coords1, ChunkCoordinates coords2)
    {
        return new ChunkCoordinates(coords1.posX - coords2.posX, coords1.posY - coords2.posY, coords1.posZ - coords2.posZ);
    }

    /**
     * @return the result of {@code x},{@code y},{@code z} added to {@code coords}
     */
    public static ChunkCoordinates add(ChunkCoordinates coords, int x, int y, int z)
    {
        return new ChunkCoordinates(coords.posX + x, coords.posY + y, coords.posZ + z);
    }

    public static ChunkCoordinates fromEntity(Entity entity)
    {
        return new ChunkCoordinates(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
    }
}
