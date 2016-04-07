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
    /**
     * Writes a Chunk Coordinate to an NBT compound, with a specific tag name
     *
     * @param compound Compound to write to
     * @param name     Name of the tag
     * @param coords   Coordinates to write to NBT
     */
    public static void writeToNBT(NBTTagCompound compound, String name, ChunkCoordinates coords)
    {
        NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", coords.posX);
        coordsCompound.setInteger("y", coords.posY);
        coordsCompound.setInteger("z", coords.posZ);
        compound.setTag(name, coordsCompound);
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name
     *
     * @param compound Compound to read data from
     * @param name     Tag name to read data from
     * @return Chunk coordinates read from the compound
     */
    public static ChunkCoordinates readFromNBT(NBTTagCompound compound, String name)
    {
        NBTTagCompound coordsCompound = compound.getCompoundTag(name);
        int            x              = coordsCompound.getInteger("x");
        int            y              = coordsCompound.getInteger("y");
        int            z              = coordsCompound.getInteger("z");
        return new ChunkCoordinates(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to
     * @param coords  Coordinate to write to the tag list
     */
    public static void writeToNBTTagList(NBTTagList tagList, ChunkCoordinates coords)
    {
        NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", coords.posX);
        coordsCompound.setInteger("y", coords.posY);
        coordsCompound.setInteger("z", coords.posZ);
        tagList.appendTag(coordsCompound);
    }

    /**
     * Reads a Chunk Coordinate from a tag list.
     *
     * @param tagList Tag list to read compound with chunk coordinate from
     * @param index   Index in the tag list where the required chunk coordinate is
     * @return Chunk coordinate read from the tag list
     */
    public static ChunkCoordinates readFromNBTTagList(NBTTagList tagList, int index)
    {
        NBTTagCompound coordsCompound = tagList.getCompoundTagAt(index);
        int            x              = coordsCompound.getInteger("x");
        int            y              = coordsCompound.getInteger("y");
        int            z              = coordsCompound.getInteger("z");
        return new ChunkCoordinates(x, y, z);
    }

    /**
     * Writes chunk coordinates to a {@link ByteBuf}
     *
     * @param buf    Buf to write to
     * @param coords Coordinate to write
     */
    public static void writeToByteBuf(ByteBuf buf, ChunkCoordinates coords)
    {
        buf.writeInt(coords.posX);
        buf.writeInt(coords.posY);
        buf.writeInt(coords.posZ);
    }

    /**
     * Read chunk coordinates from a {@link ByteBuf}
     *
     * @param buf Buf to read from
     * @return Chunk coordinate that was read
     */
    public static ChunkCoordinates readFromByteBuf(ByteBuf buf)
    {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        return new ChunkCoordinates(x, y, z);
    }

    /**
     * Returns if the {@link #distanceSqrd(ChunkCoordinates, Vec3)} from a coordinate to an cititzen is closer than 4.84
     *
     * @param coordinate Coordinate you want check distance of
     * @param citizen    Citizen you want check distance of
     * @return Whether or not the distance is less than 4.84
     */
    public static boolean isClose(ChunkCoordinates coordinate, EntityCitizen citizen)
    {
        return distanceSqrd(coordinate, citizen.getPosition()) < 4.84;
    }

    /**
     * Returns the squared distance to a Vec3
     *
     * @param coords1 Chunk coordinates   (point 1)
     * @param coords2 Vec3                (point 2)
     * @return Squared distance between points (float)
     */
    public static float distanceSqrd(ChunkCoordinates coords1, Vec3 coords2)
    {
        return coords1.getDistanceSquared((int) coords2.xCoord, (int) coords2.yCoord, (int) coords2.zCoord);
    }

    /**
     * Returns the tile entity at a specific chunk coordinate
     *
     * @param world  World the tile entity is in
     * @param coords Coordinates of the tile entity
     * @return Tile entity at the given coordinates
     */
    public static TileEntity getTileEntity(World world, ChunkCoordinates coords)
    {
        return world.getTileEntity(coords.posX, coords.posY, coords.posZ);
    }

    /**
     * Returns a list of drops possible mining a specific block with specific fortune level
     *
     * @param world   World the block is in
     * @param coords  Coordinates of the block
     * @param fortune Level of fortune on the pickaxe
     * @return List of {@link ItemStack} with possible drops
     */
    public static List<ItemStack> getBlockDrops(World world, ChunkCoordinates coords, int fortune)
    {
        return getBlock(world, coords).getDrops(world, coords.posX, coords.posY, coords.posZ, getBlockMetadata(world, coords), fortune);
    }

    /**
     * Returns the block at a specific chunk coordinate
     *
     * @param world  World the block is in
     * @param coords Coordinates of the block
     * @return Block at the given coordinates
     */
    public static Block getBlock(World world, ChunkCoordinates coords)
    {
        return world.getBlock(coords.posX, coords.posY, coords.posZ);
    }

    /**
     * Returns the metadata of a block at a specific chunk coordinate
     *
     * @param world  World the block is in
     * @param coords Coordinates of the block
     * @return Metadata of the block at the given coordinates
     */
    public static int getBlockMetadata(World world, ChunkCoordinates coords)
    {
        return world.getBlockMetadata(coords.posX, coords.posY, coords.posZ);
    }

    /**
     * Sets a block in the world
     *
     * @param world  World the block needs to be set in
     * @param coords Coordinate to place block
     * @param block  Block to place
     * @return True if block is placed, otherwise false
     */
    public static boolean setBlock(World world, ChunkCoordinates coords, Block block)
    {
        return world.setBlock(coords.posX, coords.posY, coords.posZ, block);
    }

    /**
     * Sets a block in the world, with specific metadata and flags
     *
     * @param world    World the block needs to be set in
     * @param coords   Coordinate to place block
     * @param block    Block to place
     * @param metadata Metadata to set
     * @param flag     Flag to set
     * @return True if block is placed, otherwise false
     */
    public static boolean setBlock(World world, ChunkCoordinates coords, Block block, int metadata, int flag)
    {
        return world.setBlock(coords.posX, coords.posY, coords.posZ, block, metadata, flag);
    }

    /**
     * Returns whether or not the citizen is heading to a specific location
     *
     * @param citizen Citizen you want to check
     * @param pos     Position you want to check
     * @return True if citizen heads to pos, otherwise false
     * @see {@link Utils#isPathingTo(EntityCitizen, int, int)}
     */
    public static boolean isPathingTo(EntityCitizen citizen, ChunkCoordinates pos)
    {
        return Utils.isPathingTo(citizen, pos.posX, pos.posZ);
    }

    /**
     * @param worker Worker to check
     * @param site   Chunk coordinates of site to check
     * @return True when worker is at site, otherwise false
     * @see {@link Utils#isWorkerAtSiteWithMove(EntityCitizen, int, int, int)}
     */
    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, ChunkCoordinates site)
    {
        return Utils.isWorkerAtSiteWithMove(worker, site.posX, site.posY, site.posZ);
    }

    /**
     * @param worker Worker to check
     * @param site   Chunk coordinates of site to check
     * @param range  Range to check in
     * @return True when within range, otherwise false
     * @see {@link Utils#isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}
     */
    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, ChunkCoordinates site, int range)
    {
        return Utils.isWorkerAtSiteWithMove(worker, site.posX, site.posY, site.posZ, range);
    }

    /**
     * @param living      A living entity
     * @param destination chunk coordinates to check moving to
     * @return True when XYZ is found, an set moving to, otherwise false
     * @see {@link Utils#tryMoveLivingToXYZ(EntityLiving, int, int, int)}
     */
    public static boolean tryMoveLivingToXYZ(EntityLiving living, ChunkCoordinates destination)
    {
        return Utils.tryMoveLivingToXYZ(living, destination.posX, destination.posY, destination.posZ);
    }

    /**
     * Attempt to move to XYZ.
     * True when found and destination is set
     *
     * @param citizen     Citizen to move to XYZ
     * @param destination Chunk coordinate of the distance
     * @return True when found, and destination is set, otherwise false
     */
    public static PathResult moveLivingToXYZ(EntityCitizen citizen, ChunkCoordinates destination)
    {
        return citizen.getNavigator().moveToXYZ(destination.posX, destination.posY, destination.posZ, 1.0);
    }

    /**
     * Returns whether a chunk coordinate is equals to (x, y, z)
     *
     * @param coords Chunk Coordinate    (point 1)
     * @param x      x-coordinate        (point 2)
     * @param y      y-coordinate        (point 2)
     * @param z      z-coordinate        (point 2)
     * @return True when coordinates are equal, otherwise false
     */
    public static boolean equals(ChunkCoordinates coords, int x, int y, int z)
    {
        return coords.posX == x && coords.posY == y && coords.posZ == z;
    }

    /**
     * Substract two coordinates, and returns the result
     *
     * @param coords1 Chunk Coordinate (point 1)
     * @param coords2 Chunk Coordinate (point 2)
     * @return coordinates with the result of {@code coords1} minus {@code coords2}
     */
    public static ChunkCoordinates subtract(ChunkCoordinates coords1, ChunkCoordinates coords2)
    {
        return new ChunkCoordinates(coords1.posX - coords2.posX, coords1.posY - coords2.posY, coords1.posZ - coords2.posZ);
    }

    /**
     * Add two coordinates, and returns the result
     *
     * @param coords Chunk Coordinate (point 1)
     * @param x      x-coordinate (point 2)
     * @param y      y-coordinate (point 2)
     * @param z      z-coordinate (point 2)
     * @return the result of {@code x},{@code y},{@code z} added to {@code coords}
     */
    public static ChunkCoordinates add(ChunkCoordinates coords, int x, int y, int z)
    {
        return new ChunkCoordinates(coords.posX + x, coords.posY + y, coords.posZ + z);
    }

    /**
     * Returns the Chunk Coordinate created from an entity
     *
     * @param entity Entity to create chunk coordinates from
     * @return Chunk Coordinates created from the entity
     */
    public static ChunkCoordinates fromEntity(Entity entity)
    {
        return new ChunkCoordinates(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
    }
}
