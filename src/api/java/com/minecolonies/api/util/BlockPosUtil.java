package com.minecolonies.api.util;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Utility methods for BlockPos.
 */
public final class BlockPosUtil
{
    /**
     * Max depth of the floor check to avoid endless void searching
     * (Stackoverflow).
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
    public static CompoundNBT write(@NotNull final CompoundNBT compound, final String name, @NotNull final BlockPos pos)
    {
        @NotNull final CompoundNBT coordsCompound = new CompoundNBT();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        compound.put(name, coordsCompound);
        return compound;
    }

    /**
     * Searches a random direction.
     *
     * @param random a random object.
     * @return a tuple of two directions.
     */
    private static Tuple<Direction, Direction> getRandomDirectionTuple(final Random random)
    {
        return new Tuple<>(Direction.random(random), Direction.random(random));
    }

    /**
     * Gets a random position within a certain range for wandering around.
     *
     * @param world           the world.
     * @param currentPosition the current position.
     * @param def             the default position if none was found.
     * @return the BlockPos.
     */
    public static BlockPos getRandomPosition(final World world, final BlockPos currentPosition, final BlockPos def)
    {
        final Random random = new Random();

        int tries = 0;
        BlockPos pos = null;
        while (pos == null
                 || world.getBlockState(pos).getMaterial().isLiquid()
                 || !world.getBlockState(pos.down()).getMaterial().isSolid()
                 || (!world.isAirBlock(pos) && !world.isAirBlock(pos.up())))
        {
            final Tuple<Direction, Direction> direction = getRandomDirectionTuple(random);
            pos =
              new BlockPos(currentPosition)
                .offset(direction.getA(), random.nextInt(LENGTH_RANGE))
                .offset(direction.getB(), random.nextInt(LENGTH_RANGE))
                .up(random.nextInt(UP_DOWN_RANGE))
                .down(random.nextInt(UP_DOWN_RANGE));

            if (tries >= MAX_TRIES)
            {
                return def;
            }

            tries++;
        }

        return pos;
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name.
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound.
     */
    @NotNull
    public static BlockPos read(@NotNull final CompoundNBT compound, final String name)
    {
        final CompoundNBT coordsCompound = compound.getCompound(name);
        final int x = coordsCompound.getInt("x");
        final int y = coordsCompound.getInt("y");
        final int z = coordsCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to.
     * @param pos     Coordinate to write to the tag list.
     */
    public static void writeToListNBT(@NotNull final ListNBT tagList, @NotNull final BlockPos pos)
    {
        @NotNull final CompoundNBT coordsCompound = new CompoundNBT();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        tagList.add(coordsCompound);
    }

    /**
     * Reads a Chunk Coordinate from a tag list.
     *
     * @param tagList Tag list to read compound with chunk coordinate from.
     * @param index   Index in the tag list where the required chunk coordinate
     *                is.
     * @return Chunk coordinate read from the tag list.
     */
    @NotNull
    public static BlockPos readFromListNBT(@NotNull final ListNBT tagList, final int index)
    {
        final CompoundNBT coordsCompound = tagList.getCompound(index);
        final int x = coordsCompound.getInt("x");
        final int y = coordsCompound.getInt("y");
        final int z = coordsCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Try to parse a blockPos of an input string.
     *
     * @param inputText the string to parse.
     * @return the blockPos if able to.
     */
    @Nullable
    public static BlockPos getBlockPosOfString(@NotNull final String inputText)
    {
        final String[] strings = inputText.split(" ");

        if (strings.length == BLOCKPOS_LENGTH)
        {
            try
            {
                final int x = Integer.parseInt(strings[0]);
                final int y = Integer.parseInt(strings[1]);
                final int z = Integer.parseInt(strings[2]);
                return new BlockPos(x, y, z);
            }
            catch (final NumberFormatException e)
            {
                /*
                 * Empty for a purpose.
                 */
            }
        }
        return null;
    }

    /**
     * this checks that you are not in liquid.  Will check for all liquids, even
     * those from other mods before TP
     *
     * @param sender   uses the player to get the world
     * @param blockPos for the current block LOC
     * @return isSafe true=safe false=water or lava
     */
    public static boolean isPositionSafe(@NotNull final World sender, final BlockPos blockPos)
    {
        return sender.getBlockState(blockPos).getBlock() != Blocks.AIR
                 && !sender.getBlockState(blockPos).getMaterial().isLiquid()
                 && !sender.getBlockState(blockPos.down()).getMaterial().isLiquid()
          && sender.getWorldBorder().contains(blockPos);
    }

    /**
     * this checks that you are not in the air or underground.
     * If so it will look up and down for a good landing spot before TP.
     *
     * @param blockPos for the current block LOC.
     * @param world    the world to search in.
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
            tempPos = new BlockPos(tempPos.getX(), mid, tempPos.getZ());
            final Block blocks = world.getBlockState(tempPos).getBlock();
            if (blocks == Blocks.AIR && world.canBlockSeeSky(tempPos))
            {
                top = mid - 1;
                foundland = tempPos;
            }
            else
            {
                bot = mid + 1;
                foundland = tempPos;
            }
            mid = (bot + top) / 2;
        }

        if (world.getBlockState(tempPos).getMaterial().isSolid())
        {
            return foundland.up();
        }

        return foundland;
    }

    /**
     * Returns the right height for the given position (ground block).
     *
     * @param position Current position of the entity.
     * @param world    the world object.
     * @return Ground level at (position.x, position.z).
     */
    public static double getValidHeight(@NotNull final Vec3d position, @NotNull final World world)
    {
        double returnHeight = position.y;
        if (position.y < 0)
        {
            returnHeight = 0;
        }

        while (returnHeight >= 1 && world.isAirBlock(new BlockPos(MathHelper.floor(position.x),
                (int) returnHeight,
                MathHelper.floor(position.z))))
        {
            returnHeight -= 1.0D;
        }

        while (!world.isAirBlock(
                new BlockPos(MathHelper.floor(position.x), (int) returnHeight, MathHelper.floor(position.z))))
        {
            returnHeight += 1.0D;
        }
        return returnHeight;
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
     * X+Z Distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return X+Z distance
     */
    public static long getDistance2D(@NotNull final BlockPos block1, @NotNull final BlockPos block2)
    {
        final long xDiff = Math.abs((long) block1.getX() - block2.getX());
        final long zDiff = Math.abs((long) block1.getZ() - block2.getZ());

        return Math.abs(xDiff + zDiff);
    }

    /**
     * Maximum of x/z distance between two blocks.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return X or Z distance
     */
    public static int getMaxDistance2D(@NotNull final BlockPos block1, @NotNull final BlockPos block2)
    {
        final int xDif = Math.abs(block1.getX() - block2.getX());
        final int zDif = Math.abs(block1.getZ() - block2.getZ());

        return Math.max(xDif, zDif);
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
     * Returns a list of drops possible mining a specific block with specific
     * fortune level.
     *
     * @param world   World the block is in.
     * @param coords  Coordinates of the block.
     * @param fortune Level of fortune on the pickaxe.
     * @return List of {@link ItemStack} with possible drops.
     */
    public static List<ItemStack> getBlockDrops(@NotNull final World world, @NotNull final BlockPos coords, final int fortune, final ItemStack stack)
    {
        return world.getBlockState(coords).getDrops(new LootContext.Builder((ServerWorld) world)
                                                      .withLuck(fortune)
                                                      .withParameter(LootParameters.TOOL, stack)
                                                      .withParameter(LootParameters.POSITION, coords));
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
    public static BlockState getBlockState(@NotNull final World world, @NotNull final BlockPos coords)
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
    public static boolean setBlock(@NotNull final World worldIn, @NotNull final BlockPos coords, final BlockState state, final int flag)
    {
        return worldIn.setBlockState(coords, state, flag);
    }

    /**
     * {@link EntityUtils#tryMoveLivingToXYZ(net.minecraft.entity.MobEntity, int, int, int)}.
     *
     * @param living      A living entity.
     * @param destination chunk coordinates to check moving to.
     * @return True when XYZ is found, an set moving to, otherwise false.
     */
    public static boolean tryMoveBaseCitizenEntityToXYZ(@NotNull final AbstractEntityCitizen living, @NotNull final BlockPos destination)
    {
        if (!(living instanceof LivingEntity))
        {
            return false;
        }

        return EntityUtils.tryMoveLivingToXYZ(living, destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     * {@link EntityUtils#tryMoveLivingToXYZ(net.minecraft.entity.MobEntity, int, int, int)}.
     *
     * @param living      A living entity.
     * @param destination chunk coordinates to check moving to.
     * @return True when XYZ is found, an set moving to, otherwise false.
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final MobEntity living, @NotNull final BlockPos destination)
    {
        return EntityUtils.tryMoveLivingToXYZ(living, destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     * Create a method for using a {@link BlockPos} when using {@link
     * net.minecraft.util.math.BlockPos.Mutable#setPos(int, int, int)}.
     *
     * @param pos    {@link net.minecraft.util.math.BlockPos.Mutable}.
     * @param newPos The new position to set.
     */
    public static void set(@NotNull final BlockPos.Mutable pos, @NotNull final BlockPos newPos)
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
        return new BlockPos(MathHelper.floor(entity.posX), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ));
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param world    the world the position is in.
     * @return returns BlockPos position with air above.
     */
    @NotNull
    public static BlockPos getFloor(@NotNull final BlockPos position, @NotNull final World world)
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
    public static BlockPos getFloor(@NotNull final BlockPos position, final int depth, @NotNull final World world)
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

    /**
     * Calculate in which direction a pos is facing.
     *
     * @param pos      the pos.
     * @param neighbor the block its facing.
     * @return the directions its facing.
     */
    public static Direction getFacing(final BlockPos pos, final BlockPos neighbor)
    {
        final BlockPos vector = neighbor.subtract(pos);
        return Direction.getFacingFromVector(vector.getX(), vector.getY(), -vector.getZ());
    }

    /**
     * Calculate in which direction a pos is facing. Ignoring y.
     *
     * @param pos      the pos.
     * @param neighbor the block its facing.
     * @return the directions its facing.
     */
    public static Direction getXZFacing(final BlockPos pos, final BlockPos neighbor)
    {
        final BlockPos vector = neighbor.subtract(pos);
        return Direction.getFacingFromVector(vector.getX(), 0, vector.getZ());
    }

    /**
     * Calculates the direction a position is from the building.
     *
     * @param building the building.
     * @param pos    the position.
     * @return a string describing the direction.
     */
    public static String calcDirection(@NotNull final BlockPos building, @NotNull final BlockPos pos)
    {
        final StringBuilder dist = new StringBuilder();

        if (pos.getZ() > building.getZ() + 1)
        {
            dist.append(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_SOUTH));
        }
        else if (pos.getZ() < building.getZ() - 1)
        {
            dist.append(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_NORTH));
        }

        if (pos.getX() > building.getX() + 1)
        {
            if(!dist.toString().isEmpty())
            {
                dist.append('/');
            }
            dist.append(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_EAST));
        }
        else if (pos.getX() < building.getX() - 1)
        {
            if(!dist.toString().isEmpty())
            {
                dist.append('/');
            }
            dist.append(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_WEST));
        }

        return dist.toString();
    }

    /**
     * Get the rotation enum value from the amount of rotations.
     *
     * @param rotations the amount of rotations.
     * @return the enum Rotation.
     */
    public static Rotation getRotationFromRotations(final int rotations)
    {
        switch (rotations)
        {
            case ROTATE_ONCE:
                return Rotation.CLOCKWISE_90;
            case ROTATE_TWICE:
                return Rotation.CLOCKWISE_180;
            case ROTATE_THREE_TIMES:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }
}
