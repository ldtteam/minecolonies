package com.minecolonies.api.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Max depth of the floor check to avoid endless void searching (Stackoverflow).
     */
    private static final int MAX_DEPTH = 50;

    /**
     * Amount of string required to try to calculate a blockpos.
     */
    private static final int BLOCKPOS_LENGTH = 3;

    private BlockPosUtil()
    {
        //Hide default constructor.
    }

    /**
     * Writes a Chunk Coordinate to an NBT compound, with a specific tag name.
     *
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param pos      Coordinates to write to NBT.
     */
    public static void writeToNBT(@NotNull final NBTTagCompound compound, final String name, @NotNull final BlockPos pos)
    {
        @NotNull final NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", pos.getX());
        coordsCompound.setInteger("y", pos.getY());
        coordsCompound.setInteger("z", pos.getZ());
        compound.setTag(name, coordsCompound);
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name.
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound.
     */
    @NotNull
    public static BlockPos readFromNBT(@NotNull final NBTTagCompound compound, final String name)
    {
        final NBTTagCompound coordsCompound = compound.getCompoundTag(name);
        final int x = coordsCompound.getInteger("x");
        final int y = coordsCompound.getInteger("y");
        final int z = coordsCompound.getInteger("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to.
     * @param pos     Coordinate to write to the tag list.
     */
    public static void writeToNBTTagList(@NotNull final NBTTagList tagList, @NotNull final BlockPos pos)
    {
        @NotNull final NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", pos.getX());
        coordsCompound.setInteger("y", pos.getY());
        coordsCompound.setInteger("z", pos.getZ());
        tagList.appendTag(coordsCompound);
    }

    /**
     * Reads a Chunk Coordinate from a tag list.
     *
     * @param tagList Tag list to read compound with chunk coordinate from.
     * @param index   Index in the tag list where the required chunk coordinate is.
     * @return Chunk coordinate read from the tag list.
     */
    @NotNull
    public static BlockPos readFromNBTTagList(@NotNull final NBTTagList tagList, final int index)
    {
        final NBTTagCompound coordsCompound = tagList.getCompoundTagAt(index);
        final int x = coordsCompound.getInteger("x");
        final int y = coordsCompound.getInteger("y");
        final int z = coordsCompound.getInteger("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Writes chunk coordinates to a {@link ByteBuf}.
     *
     * @param buf Buf to write to.
     * @param pos Coordinate to write.
     */
    public static void writeToByteBuf(@NotNull final ByteBuf buf, @NotNull final BlockPos pos)
    {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    /**
     * Read chunk coordinates from a {@link ByteBuf}.
     *
     * @param buf Buf to read from.
     * @return Chunk coordinate that was read.
     */
    @NotNull
    public static BlockPos readFromByteBuf(@NotNull final ByteBuf buf)
    {
        final int x = buf.readInt();
        final int y = buf.readInt();
        final int z = buf.readInt();
        return new BlockPos(x, y, z);
    }

    /**
     * Try to parse a blockPos of an input string.
     * @param inputText the string to parse.
     * @return the blockPos if able to.
     */
    @Nullable
    public static BlockPos getBlockPosOfString(@NotNull final String inputText)
    {
        final String[] strings = inputText.split(" ");

        if(strings.length == BLOCKPOS_LENGTH)
        {
            try
            {
                final int x = Integer.parseInt(strings[0]);
                final int y = Integer.parseInt(strings[1]);
                final int z = Integer.parseInt(strings[2]);
                return new BlockPos(x, y, z);
            }
            catch (NumberFormatException e)
            {
                /**
                 * Empty for a purpose.
                 */
            }
        }
        return null;
    }

    /**
     * this checks that you are not in liquid.  Will check for all liquids, even those from other mods
     * before TP
     *
     * @param blockPos for the current block LOC
     * @param sender uses the player to get the world
     * @return isSafe true=safe false=water or lava
     */
    public static boolean isPositionSafe(@NotNull ICommandSender sender, BlockPos blockPos)
    {
        return sender.getEntityWorld().getBlockState(blockPos).getBlock() != Blocks.AIR
                && !sender.getEntityWorld().getBlockState(blockPos).getMaterial().isLiquid()
                && !sender.getEntityWorld().getBlockState(blockPos.up()).getMaterial().isLiquid();
    }

    /**
     * this checks that you are not in the air or underground.
     * If so it will look up and down for a good landing spot before TP.
     *
     * @param blockPos for the current block LOC.
     * @param world the world to search in.
     * @return blockPos to be used for the TP.
     */
    public static BlockPos findLand(final BlockPos blockPos, final World world)
    {
        int top = blockPos.getY();
        int bot = 0;
        int mid = blockPos.getY();

        BlockPos foundland = null;
        BlockPos tempPos = blockPos;
        //We are doing a binary search to limit the amount of checks (usually at most 9 this way)
        while (top >= bot)
        {
            tempPos = new BlockPos( tempPos.getX(),mid, tempPos.getZ());
            final Block blocks = world.getBlockState(tempPos).getBlock();
            if (blocks == Blocks.AIR && world.canSeeSky(tempPos))
            {
                top = mid - 1;
                foundland = tempPos;
            }
            else
            {
                bot = mid + 1;
                foundland = tempPos;
            }
            mid = (bot + top)/2;
        }

        return foundland;
    }

    /**
     * Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return squared distance.
     */
    public static long getDistanceSquared(@NotNull final BlockPos block1, @NotNull final BlockPos block2)
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
     * Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return squared distance.
     */
    public static long getDistance2D(@NotNull final BlockPos block1, @NotNull final BlockPos block2)
    {
        final long xDiff = Math.abs((long) block1.getX() - block2.getX());
        final long zDiff = Math.abs((long) block1.getZ() - block2.getZ());

        return Math.abs(xDiff + zDiff);
    }

    /**
     * 2D Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return 2D squared distance.
     */
    public static long getDistanceSquared2D(@NotNull final BlockPos block1, @NotNull final BlockPos block2)
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
     * Returns the tile entity at a specific chunk coordinate.
     *
     * @param world World the tile entity is in.
     * @param pos   Coordinates of the tile entity.
     * @return Tile entity at the given coordinates.
     */
    public static TileEntity getTileEntity(@NotNull final World world, @NotNull final BlockPos pos)
    {
        return world.getTileEntity(pos);
    }

    /**
     * Returns a list of drops possible mining a specific block with specific fortune level.
     *
     * @param world   World the block is in.
     * @param coords  Coordinates of the block.
     * @param fortune Level of fortune on the pickaxe.
     * @return List of {@link ItemStack} with possible drops.
     */
    public static List<ItemStack> getBlockDrops(@NotNull final World world, @NotNull final BlockPos coords, final int fortune)
    {
        return getBlock(world, coords).getDrops(world, new BlockPos(coords.getX(), coords.getY(), coords.getZ()), getBlockState(world, coords), fortune);
    }

    /**
     * Returns the block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Block at the given coordinates.
     */
    public static Block getBlock(@NotNull final World world, @NotNull final BlockPos coords)
    {
        return world.getBlockState(coords).getBlock();
    }

    /**
     * Returns the metadata of a block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Metadata of the block at the given coordinates.
     */
    public static IBlockState getBlockState(@NotNull final World world, @NotNull final BlockPos coords)
    {
        return world.getBlockState(coords);
    }

    /**
     * Sets a block in the world, with specific metadata and flags.
     *
     * @param worldIn World the block needs to be set in.
     * @param coords  Coordinate to place block.
     * @param state   BlockState to be placed.
     * @param flag    Flag to set.
     * @return True if block is placed, otherwise false.
     */
    public static boolean setBlock(@NotNull final World worldIn, @NotNull final BlockPos coords, final IBlockState state, final int flag)
    {
        return worldIn.setBlockState(coords, state, flag);
    }

    /**
     * {@link EntityUtils#tryMoveLivingToXYZ(EntityLiving, int, int, int)}.
     *
     * @param living      A living entity.
     * @param destination chunk coordinates to check moving to.
     * @return True when XYZ is found, an set moving to, otherwise false.
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final EntityLiving living, @NotNull final BlockPos destination)
    {
        return EntityUtils.tryMoveLivingToXYZ(living, destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     *
     * @param pos    {@link net.minecraft.util.math.BlockPos.MutableBlockPos}.
     * @param newPos The new position to set.
     */
    public static void set(@NotNull final BlockPos.MutableBlockPos pos, @NotNull final BlockPos newPos)
    {
        pos.setPos(newPos.getX(), newPos.getY(), newPos.getZ());
    }

    /**
     * Returns whether a chunk coordinate is equals to (x, y, z).
     *
     * @param coords Chunk Coordinate    (point 1).
     * @param x      x-coordinate        (point 2).
     * @param y      y-coordinate        (point 2).
     * @param z      z-coordinate        (point 2).
     * @return True when coordinates are equal, otherwise false.
     */
    public static boolean isEqual(@NotNull final BlockPos coords, final int x, final int y, final int z)
    {
        return coords.getX() == x && coords.getY() == y && coords.getZ() == z;
    }

    /**
     * Returns the Chunk Coordinate created from an entity.
     *
     * @param entity Entity to create chunk coordinates from.
     * @return Chunk Coordinates created from the entity.
     */
    @NotNull
    public static BlockPos fromEntity(@NotNull final Entity entity)
    {
        return new BlockPos(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param world    the world the position is in.
     * @return returns BlockPos position with air above.
     */
    @NotNull
    public static BlockPos getFloor(@NotNull BlockPos position, @NotNull final World world)
    {
        final BlockPos floor = getFloor(position, 0, world);
        if (floor == null)
        {
            return position;
        }
        return floor;
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param depth    the iteration depth.
     * @param world    the world the position is in.
     * @return returns BlockPos position with air above.
     */
    @Nullable
    private static BlockPos getFloor(@NotNull final BlockPos position, int depth, @NotNull final World world)
    {
        if (depth > MAX_DEPTH)
        {
            return null;
        }
        //If the position is floating in Air go downwards
        if (!EntityUtils.solidOrLiquid(world, position))
        {
            return getFloor(position.down(), depth + 1, world);
        }
        //If there is no air above the block go upwards
        if (!EntityUtils.solidOrLiquid(world, position.up()))
        {
            return position;
        }
        return getFloor(position.up(), depth + 1, world);
    }
}
