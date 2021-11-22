package com.minecolonies.api.util;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Utility methods for BlockPos.
 */
public final class BlockPosUtil
{
    /**
     * Max depth of the floor check to avoid endless void searching (Stackoverflow).
     */
    private static final int MAX_DEPTH = 50;

    /**
     * Amount of string required to try to calculate a blockpos.
     */
    private static final int BLOCKPOS_LENGTH = 3;

    /**
     * Selects a solid position with air above
     */
    public static final BiPredicate<IBlockReader, BlockPos> SOLID_AIR_POS_SELECTOR = (world, pos) -> {
        return (world.getBlockState(pos).canOcclude() || world.getBlockState(pos).getMaterial().isLiquid()) && world.getBlockState(
          pos.above()).getMaterial() == Material.AIR && world.getBlockState(pos.above(2)).getMaterial() == Material.AIR;
    };

    /**
     * Selects a double air position
     */
    public static final BiPredicate<IBlockReader, BlockPos> DOUBLE_AIR_POS_SELECTOR = (world, pos) -> {
        return world.getBlockState(pos).getMaterial() == Material.AIR && world.getBlockState(pos.above(1)).getMaterial() == Material.AIR;
    };

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
     * @return the resulting compound.
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
     * Writes a chunk coordinate to a CompoundNBT, but only if not null.
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param value    Coordinates to write; if null, the tag is not written.
     * @return the resulting compound.
     */
    @NotNull
    public static CompoundNBT writeOptional(@NotNull final CompoundNBT compound, @NotNull final String name,
                                            @Nullable final BlockPos value)
    {
        if (value != null)
        {
            write(compound, name, value);
        }
        return compound;
    }

    /**
     * Searches a random direction.
     *
     * @param random a random object.
     * @return a tuple of two directions.
     */
    public static Tuple<Direction, Direction> getRandomDirectionTuple(final Random random)
    {
        return new Tuple<>(Direction.getRandom(random), Direction.getRandom(random));
    }

    /**
     * Gets a random position within a certain range for wandering around.
     *
     * @param world           the world.
     * @param currentPosition the current position.
     * @param def             the default position if none was found.
     * @param minDist         the minimum distance of the pos.
     * @param maxDist         the maximum distance.
     * @return the BlockPos.
     */
    public static BlockPos getRandomPosition(final World world, final BlockPos currentPosition, final BlockPos def, final int minDist, final int maxDist)
    {
        final Random random = world.random;

        int tries = 0;
        BlockPos pos = null;
        while (pos == null
                 || !WorldUtil.isEntityBlockLoaded(world, pos)
                 || world.getBlockState(pos).getMaterial().isLiquid()
                 || !world.getBlockState(pos.below()).getMaterial().isSolid()
                 || (!world.isEmptyBlock(pos) || !world.isEmptyBlock(pos.above())))
        {
            final Tuple<Direction, Direction> direction = getRandomDirectionTuple(random);
            pos =
              new BlockPos(currentPosition)
                .relative(direction.getA(), random.nextInt(maxDist) + minDist)
                .relative(direction.getB(), random.nextInt(maxDist) + minDist)
                .above(random.nextInt(UP_DOWN_RANGE))
                .below(random.nextInt(UP_DOWN_RANGE));

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
     * Reads chunk coordinates from a CompoundNBT, but returns null if zero or absent.
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound, or null if it was zero or absent.
     */
    @Nullable
    public static BlockPos readOrNull(@NotNull final CompoundNBT compound, @NotNull final String name)
    {
        final BlockPos result = read(compound, name);
        return result.equals(BlockPos.ZERO) ? null : result;
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
     * Write a list of positions to a compound
     *
     * @param compoundNBT compound to save the list on
     * @param tagname     key to save the list on
     * @param positions   block positions to write
     */
    public static void writePosListToNBT(final CompoundNBT compoundNBT, final String tagname, final List<BlockPos> positions)
    {
        ListNBT listNBT = new ListNBT();
        for (final BlockPos pos : positions)
        {
            writeToListNBT(listNBT, pos);
        }
        compoundNBT.put(tagname, listNBT);
    }

    /**
     * Reads a list of block positions from NBT
     *
     * @param compoundNBT compound the list is in
     * @param tagname     key of the list
     * @return list of block positions
     */
    public static List<BlockPos> readPosListFromNBT(final CompoundNBT compoundNBT, final String tagname)
    {
        final List<BlockPos> result = new ArrayList<>();
        ListNBT listNBT = compoundNBT.getList(tagname, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listNBT.size(); i++)
        {
            result.add(readFromListNBT(listNBT, i));
        }

        return result;
    }

    /**
     * Reads a Chunk Coordinate from a tag list.
     *
     * @param tagList Tag list to read compound with chunk coordinate from.
     * @param index   Index in the tag list where the required chunk coordinate is.
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
     * Returns a string representation of the block position for use in GUIs and chats.
     *
     * @param position The position of the string to be returned
     * @return The string representation of the block position
     */
    @NotNull
    public static String getString(@NotNull final BlockPos position)
    {
        return "{x=" + position.getX() + ", y=" + position.getY() + ", z=" + position.getZ() + "}";
    }

    /**
     * this checks that you are not in liquid.  Will check for all liquids, even those from other mods before TP
     *
     * @param sender   uses the player to get the world
     * @param blockPos for the current block LOC
     * @return isSafe true=safe false=water or lava
     */
    public static boolean isPositionSafe(@NotNull final World sender, final BlockPos blockPos)
    {
        return !(sender.getBlockState(blockPos).getBlock() instanceof AirBlock)
                 && !sender.getBlockState(blockPos).getMaterial().isLiquid()
                 && !sender.getBlockState(blockPos.below()).getMaterial().isLiquid()
                 && sender.getWorldBorder().isWithinBounds(blockPos);
    }

    /**
     * this checks that you are not in the air or underground. If so it will look up and down for a good landing spot before TP.
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
            final Block block = world.getBlockState(tempPos).getBlock();
            if (block instanceof AirBlock && world.canSeeSkyFromBelowWater(tempPos))
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
            return foundland.above();
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
    public static double getValidHeight(@NotNull final Vector3d position, @NotNull final World world)
    {
        double returnHeight = position.y;
        if (position.y < 0)
        {
            returnHeight = 0;
        }

        while (returnHeight >= 1 && world.isEmptyBlock(new BlockPos(MathHelper.floor(position.x),
          (int) returnHeight,
          MathHelper.floor(position.z))))
        {
            returnHeight -= 1.0D;
        }

        while (!world.isEmptyBlock(
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
     * Gets the actual distance in blocks between two positions
     *
     * @param pos1 Blockpos 1
     * @param pos2 Blockpos 2
     * @return distance in blocks.
     */
    public static double getDistance(final BlockPos pos1, final BlockPos pos2)
    {
        final long xDiff = pos1.getX() - pos2.getX();
        final long yDiff = pos1.getY() - pos2.getY();
        final long zDiff = pos1.getZ() - pos2.getZ();

        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
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
     * Returns a radial bounding box aligned to chunk boundaries.  Note that the Y coordinate
     * is also aligned to chunk-like sizes; this does not return full world height.  (It also
     * might return Y coordinates outside the world limits, so clip before using if needed.)
     * @param pos A position inside the center chunk.
     * @param chunkRadius 0 for one chunk, 1 for nine chunks, etc.
     * @return The specified bounding box.
     */
    public static MutableBoundingBox getChunkAlignedBB(final BlockPos pos, final int chunkRadius)
    {
        final int blockRadius = chunkRadius * 16;
        final int x1 = pos.getX() & ~15;
        final int y1 = pos.getY() & ~15;
        final int z1 = pos.getZ() & ~15;
        return new MutableBoundingBox(x1 - blockRadius, y1 - blockRadius, z1 - blockRadius,
                x1 + blockRadius + 15, y1 + blockRadius + 15, z1 + blockRadius + 15);
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
        return world.getBlockEntity(pos);
    }

    /**
     * Returns a list of drops possible mining a specific block with specific fortune level.
     *
     * @param world   World the block is in.
     * @param coords  Coordinates of the block.
     * @param fortune Level of fortune on the pickaxe.
     * @param stack   the tool.
     * @return List of {@link ItemStack} with possible drops.
     */
    public static List<ItemStack> getBlockDrops(@NotNull final World world, @NotNull final BlockPos coords, final int fortune, final ItemStack stack, final LivingEntity entity)
    {
        return world.getBlockState(coords).getDrops(new LootContext.Builder((ServerWorld) world)
                                                      .withLuck(fortune)
                                                      .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(coords))
                                                      .withParameter(LootParameters.ORIGIN, entity.position())
                                                      .withParameter(LootParameters.TOOL, stack));
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
        return worldIn.setBlock(coords, state, flag);
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
     * Create a method for using a {@link BlockPos} when using {@link net.minecraft.util.math.BlockPos.Mutable#set(int, int, int)}.
     *
     * @param pos    {@link net.minecraft.util.math.BlockPos.Mutable}.
     * @param newPos The new position to set.
     */
    public static void set(@NotNull final BlockPos.Mutable pos, @NotNull final BlockPos newPos)
    {
        pos.set(newPos.getX(), newPos.getY(), newPos.getZ());
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
        return new BlockPos(MathHelper.floor(entity.getX()), MathHelper.floor(entity.getY()), MathHelper.floor(entity.getZ()));
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
        final BlockPos floor = getFloor(new BlockPos.Mutable(position.getX(), position.getY(), position.getZ()), 0, world);
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
    public static BlockPos getFloor(@NotNull final BlockPos.Mutable position, final int depth, @NotNull final World world)
    {
        if (depth > MAX_DEPTH)
        {
            return null;
        }
        //If the position is floating in Air go downwards
        if (!EntityUtils.solidOrLiquid(world, position))
        {
            return getFloor(position.set(position.getX(), position.getY() - 1, position.getZ()), depth + 1, world);
        }
        //If there is no air above the block go upwards
        if (!EntityUtils.solidOrLiquid(world, position.set(position.getX(), position.getY() + 1, position.getZ())) &&
              !EntityUtils.solidOrLiquid(world, position.set(position.getX(), position.getY() + 2, position.getZ())))
        {
            return position.immutable();
        }
        return getFloor(position.set(position.getX(), position.getY() + 1, position.getZ()), depth + 1, world);
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
        return Direction.getNearest(vector.getX(), vector.getY(), vector.getZ());
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
        return Direction.getNearest(vector.getX(), 0, vector.getZ());
    }

    /**
     * Calculates the direction a position is from the building.
     *
     * @param building the building.
     * @param pos      the position.
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
            if (!dist.toString().isEmpty())
            {
                dist.append('/');
            }
            dist.append(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_EAST));
        }
        else if (pos.getX() < building.getX() - 1)
        {
            if (!dist.toString().isEmpty())
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

    /**
     * Returns the first air position near the given start. Advances vertically first then horizontally
     *
     * @param start     start position
     * @param vRange    vertical search range
     * @param hRange    horizontal search range
     * @param predicate check predicate for the right block
     * @return position or null
     */
    public static BlockPos findAround(final World world, final BlockPos start, final int vRange, final int hRange, final BiPredicate<IBlockReader, BlockPos> predicate)
    {
        if (vRange < 1 && hRange < 1)
        {
            return null;
        }

        BlockPos temp;
        int y = 0;
        int y_offset = 1;

        for (int i = 0; i < hRange + 2; i++)
        {
            for (int steps = 1; steps <= vRange; steps++)
            {
                // Start topleft of middle point
                temp = start.offset(-steps, y, -steps);

                // X ->
                for (int x = 0; x <= steps; x++)
                {
                    temp = temp.offset(1, 0, 0);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }

                // X
                // |
                // v
                for (int z = 0; z <= steps; z++)
                {
                    temp = temp.offset(0, 0, 1);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }

                // < - X
                for (int x = 0; x <= steps; x++)
                {
                    temp = temp.offset(-1, 0, 0);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }

                // ^
                // |
                // X
                for (int z = 0; z <= steps; z++)
                {
                    temp = temp.offset(0, 0, -1);
                    if (predicate.test(world, temp))
                    {
                        return temp;
                    }
                }
            }

            y += y_offset;
            y_offset = y_offset > 0 ? y_offset + 1 : y_offset - 1;
            y_offset *= -1;

            if (world.getHeight() <= start.getY() + y)
            {
                return null;
            }
        }

        return null;
    }

    /**
     * Finds a spawn pos around the given startpos with two air blocks
     *
     * @param worldReader blockreader
     * @param start       startpos
     * @return
     */
    public static BlockPos findSpawnPosAround(final World worldReader, final BlockPos start)
    {
        return findAround(worldReader, start, 1, 1,
          (world, pos) -> world.getBlockState(pos).getMaterial() == Material.AIR && world.getBlockState(pos.above()).getMaterial() == Material.AIR);
    }

    /**
     * Get the furthest corner from a pos.
     * @param startPos the startpos.
     * @param boxStart the box start.
     * @param boxEnd the box end.
     * @return the furthest corner.
     */
    public static BlockPos getFurthestCorner(final BlockPos startPos, final BlockPos boxStart, final BlockPos boxEnd)
    {
        final int minX = Math.min(boxStart.getX(), boxEnd.getX());
        final int minZ = Math.min(boxStart.getZ(), boxEnd.getZ());
        final int minY = Math.min(boxStart.getY(), boxEnd.getY());

        final int maxX = Math.max(boxStart.getX(), boxEnd.getX());
        final int maxZ = Math.max(boxStart.getZ(), boxEnd.getZ());
        final int maxY = Math.max(boxStart.getY(), boxEnd.getY());

        int cornerX = maxX;
        if (Math.abs(startPos.getX() - minX) > Math.abs(startPos.getX() - maxX))
        {
            cornerX = minX;
        }

        int cornerY = maxY;
        if (Math.abs(startPos.getY() - minY) > Math.abs(startPos.getY() - maxY))
        {
            cornerY = minY;
        }

        int cornerZ = maxZ;
        if (Math.abs(startPos.getZ() - minZ) > Math.abs(startPos.getZ() - maxZ))
        {
            cornerZ = minZ;
        }

        return new BlockPos(cornerX, cornerY, cornerZ);
    }
}
