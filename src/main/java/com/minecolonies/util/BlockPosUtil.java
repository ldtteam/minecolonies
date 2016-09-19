package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.pathfinding.PathResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility methods for BlockPos.
 */
public final class BlockPosUtil
{
    /**
     * Min distance to availate two positions as close.
     */
    private static final double CLOSE_DISTANCE = 4.84;

    private BlockPosUtil()
    {
        //Hide default constructor.
    }

    /**
     * Writes a Chunk Coordinate to an NBT compound, with a specific tag name
     *
     * @param compound Compound to write to
     * @param name     Name of the tag
     * @param pos      Coordinates to write to NBT
     */
    public static void writeToNBT(@NotNull NBTTagCompound compound, String name, @NotNull BlockPos pos)
    {
        @NotNull NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", pos.getX());
        coordsCompound.setInteger("y", pos.getY());
        coordsCompound.setInteger("z", pos.getZ());
        compound.setTag(name, coordsCompound);
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name
     *
     * @param compound Compound to read data from
     * @param name     Tag name to read data from
     * @return Chunk coordinates read from the compound
     */
    @NotNull
    public static BlockPos readFromNBT(@NotNull NBTTagCompound compound, String name)
    {
        NBTTagCompound coordsCompound = compound.getCompoundTag(name);
        int x = coordsCompound.getInteger("x");
        int y = coordsCompound.getInteger("y");
        int z = coordsCompound.getInteger("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to
     * @param pos     Coordinate to write to the tag list
     */
    public static void writeToNBTTagList(@NotNull NBTTagList tagList, @NotNull BlockPos pos)
    {
        @NotNull NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", pos.getX());
        coordsCompound.setInteger("y", pos.getY());
        coordsCompound.setInteger("z", pos.getZ());
        tagList.appendTag(coordsCompound);
    }

    /**
     * Reads a Chunk Coordinate from a tag list.
     *
     * @param tagList Tag list to read compound with chunk coordinate from
     * @param index   Index in the tag list where the required chunk coordinate is
     * @return Chunk coordinate read from the tag list
     */
    @NotNull
    public static BlockPos readFromNBTTagList(@NotNull NBTTagList tagList, int index)
    {
        NBTTagCompound coordsCompound = tagList.getCompoundTagAt(index);
        int x = coordsCompound.getInteger("x");
        int y = coordsCompound.getInteger("y");
        int z = coordsCompound.getInteger("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Writes chunk coordinates to a {@link ByteBuf}
     *
     * @param buf Buf to write to
     * @param pos Coordinate to write
     */
    public static void writeToByteBuf(@NotNull ByteBuf buf, @NotNull BlockPos pos)
    {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    /**
     * Read chunk coordinates from a {@link ByteBuf}
     *
     * @param buf Buf to read from
     * @return Chunk coordinate that was read
     */
    @NotNull
    public static BlockPos readFromByteBuf(@NotNull ByteBuf buf)
    {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        return new BlockPos(x, y, z);
    }

    /**
     * Returns if the {@link #getDistanceSquared(BlockPos, BlockPos)} from a coordinate to an citizen is closer than 4.84
     *
     * @param coordinate Coordinate you want check distance of
     * @param citizen    Citizen you want check distance of
     * @return Whether or not the distance is less than 4.84
     */
    public static boolean isClose(@NotNull BlockPos coordinate, @NotNull EntityCitizen citizen)
    {
        return getDistanceSquared(coordinate, citizen.getPosition()) < CLOSE_DISTANCE;
    }

    /**
     * Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return squared distance.
     */
    public static long getDistanceSquared(@NotNull BlockPos block1, @NotNull BlockPos block2)
    {
        final long xDiff = (long) block1.getX() - block2.getX();
        final long yDiff = (long) block1.getY() - block2.getY();
        final long zDiff = (long) block1.getZ() - block2.getZ();

        final long result = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
        if (result < 0)
        {
            throw new IllegalStateException("max-sqrt is to high! Failure to catch overflow with "
                    + xDiff + " | " + yDiff + " | " + zDiff);
        }
        return result;
    }

    /**
     * 2D Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return 2D squared distance.
     */
    public static long getDistanceSquared2D(@NotNull BlockPos block1, @NotNull BlockPos block2)
    {
        final long xDiff = (long) block1.getX() - block2.getX();
        final long zDiff = (long) block1.getZ() - block2.getZ();

        final long result = xDiff * xDiff + zDiff * zDiff;
        if (result < 0)
        {
            throw new IllegalStateException("max-sqrt is to high! Failure to catch overflow with "
                    + xDiff + " | " + zDiff);
        }
        return result;
    }

    /**
     * Returns the tile entity at a specific chunk coordinate
     *
     * @param world World the tile entity is in
     * @param pos   Coordinates of the tile entity
     * @return Tile entity at the given coordinates
     */
    public static TileEntity getTileEntity(@NotNull World world, @NotNull BlockPos pos)
    {
        return world.getTileEntity(pos);
    }

    /**
     * Returns a list of drops possible mining a specific block with specific fortune level
     *
     * @param world   World the block is in
     * @param coords  Coordinates of the block
     * @param fortune Level of fortune on the pickaxe
     * @return List of {@link ItemStack} with possible drops
     */
    public static List<ItemStack> getBlockDrops(@NotNull World world, @NotNull BlockPos coords, int fortune)
    {
        return getBlock(world, coords).getDrops(world, new BlockPos(coords.getX(), coords.getY(), coords.getZ()), getBlockState(world, coords), fortune);
    }

    /**
     * Returns the block at a specific chunk coordinate
     *
     * @param world  World the block is in
     * @param coords Coordinates of the block
     * @return Block at the given coordinates
     */
    public static Block getBlock(@NotNull World world, @NotNull BlockPos coords)
    {
        return world.getBlockState(coords).getBlock();
    }

    /**
     * Returns the metadata of a block at a specific chunk coordinate
     *
     * @param world  World the block is in
     * @param coords Coordinates of the block
     * @return Metadata of the block at the given coordinates
     */
    public static IBlockState getBlockState(@NotNull World world, @NotNull BlockPos coords)
    {
        return world.getBlockState(coords);
    }

    /**
     * Sets a block in the world
     *
     * @param world  World the block needs to be set in
     * @param coords Coordinate to place block
     * @param block  Block to place
     * @return True if block is placed, otherwise false
     */
    public static boolean setBlock(@NotNull World world, BlockPos coords, @NotNull Block block)
    {
        return world.setBlockState(coords, block.getDefaultState());
    }

    /**
     * Sets a block in the world, with specific metadata and flags
     *
     * @param worldIn World the block needs to be set in
     * @param coords  Coordinate to place block
     * @param state   BlockState to be placed
     * @param flag    Flag to set
     * @return True if block is placed, otherwise false
     */
    public static boolean setBlock(@NotNull World worldIn, @NotNull BlockPos coords, IBlockState state, int flag)
    {
        return worldIn.setBlockState(coords, state, flag);
    }

    /**
     * Returns whether or not the citizen is heading to a specific location.
     * {@link EntityUtils#isPathingTo(EntityCitizen, int, int)}
     *
     * @param citizen Citizen you want to check
     * @param pos     Position you want to check
     * @return True if citizen heads to pos, otherwise false
     */
    public static boolean isPathingTo(@NotNull EntityCitizen citizen, @NotNull BlockPos pos)
    {
        return EntityUtils.isPathingTo(citizen, pos.getX(), pos.getZ());
    }

    /**
     * {@link EntityUtils#isWorkerAtSiteWithMove(EntityCitizen, int, int, int)}.
     *
     * @param worker Worker to check
     * @param site   Chunk coordinates of site to check
     * @return True when worker is at site, otherwise false
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull EntityCitizen worker, @NotNull BlockPos site)
    {
        return EntityUtils.isWorkerAtSiteWithMove(worker, site.getX(), site.getY(), site.getZ());
    }

    /**
     * {@link EntityUtils#isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}.
     *
     * @param worker Worker to check
     * @param site   Chunk coordinates of site to check
     * @param range  Range to check in
     * @return True when within range, otherwise false
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull EntityCitizen worker, @NotNull BlockPos site, int range)
    {
        return EntityUtils.isWorkerAtSiteWithMove(worker, site.getX(), site.getY(), site.getZ(), range);
    }

    /**
     * {@link EntityUtils#tryMoveLivingToXYZ(EntityLiving, int, int, int)}.
     *
     * @param living      A living entity
     * @param destination chunk coordinates to check moving to
     * @return True when XYZ is found, an set moving to, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(@NotNull EntityLiving living, @NotNull BlockPos destination)
    {
        return EntityUtils.tryMoveLivingToXYZ(living, destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     * Attempt to move to XYZ.
     * True when found and destination is set
     *
     * @param citizen     Citizen to move to XYZ
     * @param destination Chunk coordinate of the distance
     * @return True when found, and destination is set, otherwise false
     */
    public static PathResult moveLivingToXYZ(@NotNull EntityCitizen citizen, @NotNull BlockPos destination)
    {
        return citizen.getNavigator().moveToXYZ(destination.getX(), destination.getY(), destination.getZ(), 1.0);
    }

    /**
     * Create a method for using a {@link BlockPos} when using {@link net.minecraft.util.math.BlockPos.MutableBlockPos#setPos(int, int, int)}.
     *
     * @param pos    {@link net.minecraft.util.math.BlockPos.MutableBlockPos}.
     * @param newPos The new position to set.
     */
    public static void set(@NotNull BlockPos.MutableBlockPos pos, @NotNull BlockPos newPos)
    {
        pos.setPos(newPos.getX(), newPos.getY(), newPos.getZ());
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
    public static boolean isEqual(@NotNull BlockPos coords, int x, int y, int z)
    {
        return coords.getX() == x && coords.getY() == y && coords.getZ() == z;
    }

    /**
     * Returns the Chunk Coordinate created from an entity
     *
     * @param entity Entity to create chunk coordinates from
     * @return Chunk Coordinates created from the entity
     */
    @NotNull
    public static BlockPos fromEntity(@NotNull Entity entity)
    {
        return new BlockPos(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
    }
}
