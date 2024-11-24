package com.minecolonies.api.util;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.ColonyConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
     * All directions.
     */
    public static final List<Direction> HORIZONTAL_DIRS = Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    /**
     * Selects a solid position with air above
     */
    public static final BiPredicate<BlockGetter, BlockPos> SOLID_AIR_POS_SELECTOR = (world, pos) -> {
        return (world.getBlockState(pos).isSolid() || world.getBlockState(pos).liquid()) && world.getBlockState(
          pos.above()).isAir() && world.getBlockState(pos.above(2)).isAir();
    };

    /**
     * Selects a double air position
     */
    public static final BiPredicate<BlockGetter, BlockPos> DOUBLE_AIR_POS_SELECTOR = (world, pos) -> {
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.above(1)).isAir();
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
    public static CompoundTag write(@NotNull final CompoundTag compound, final String name, @NotNull final BlockPos pos)
    {
        @NotNull final CompoundTag coordsCompound = new CompoundTag();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        compound.put(name, coordsCompound);
        return compound;
    }

    /**
     * Writes a chunk coordinate to a CompoundTag, but only if not null.
     *
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param value    Coordinates to write; if null, the tag is not written.
     * @return the resulting compound.
     */
    @NotNull
    public static CompoundTag writeOptional(
      @NotNull final CompoundTag compound, @NotNull final String name,
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
     * @return a tuple of two directions.
     */
    public static BlockPos getRandomPosAround(final BlockPos center, final int distance)
    {
        final Vec3 vec =
          new Vec3(ColonyConstants.rand.nextDouble() - 0.5, ColonyConstants.rand.nextDouble() - 0.5, ColonyConstants.rand.nextDouble() - 0.5).normalize()
            .multiply(distance, distance, distance);
        return new BlockPos((int) Math.round(center.getX() + vec.x), (int) Math.round(center.getY() + vec.y), (int) Math.round(center.getZ() + vec.z));
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
    public static BlockPos getRandomPosition(final Level world, final BlockPos currentPosition, final BlockPos def, final int minDist, final int maxDist)
    {
        int tries = 0;
        BlockPos pos = null;
        while (pos == null
                 || !WorldUtil.isEntityBlockLoaded(world, pos)
                 || world.getBlockState(pos).liquid()
                 || !BlockUtils.isAnySolid(world.getBlockState(pos.below()))
                 || (!world.isEmptyBlock(pos) || !world.isEmptyBlock(pos.above())))
        {
            pos = getRandomPosAround(currentPosition, ColonyConstants.rand.nextInt(maxDist) + minDist)
              .above(ColonyConstants.rand.nextInt(UP_DOWN_RANGE))
              .below(ColonyConstants.rand.nextInt(UP_DOWN_RANGE));

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
    public static BlockPos read(@NotNull final CompoundTag compound, final String name)
    {
        final CompoundTag coordsCompound = compound.getCompound(name);
        final int x = coordsCompound.getInt("x");
        final int y = coordsCompound.getInt("y");
        final int z = coordsCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Reads chunk coordinates from a CompoundTag, but returns null if zero or absent.
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound, or null if it was zero or absent.
     */
    @Nullable
    public static BlockPos readOrNull(@NotNull final CompoundTag compound, @NotNull final String name)
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
    public static void writeToListNBT(@NotNull final ListTag tagList, @NotNull final BlockPos pos)
    {
        @NotNull final CompoundTag coordsCompound = new CompoundTag();
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
    public static void writePosListToNBT(final CompoundTag compoundNBT, final String tagname, final List<BlockPos> positions)
    {
        ListTag listNBT = new ListTag();
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
    public static List<BlockPos> readPosListFromNBT(final CompoundTag compoundNBT, final String tagname)
    {
        final List<BlockPos> result = new ArrayList<>();
        ListTag listNBT = compoundNBT.getList(tagname, Tag.TAG_COMPOUND);
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
    public static BlockPos readFromListNBT(@NotNull final ListTag tagList, final int index)
    {
        final CompoundTag coordsCompound = tagList.getCompound(index);
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
     * this checks that you are not in the air or underground. If so it will look up and down for a good landing spot before TP.
     *
     * @param blockPos for the current block LOC.
     * @param world    the world to search in.
     * @return blockPos to be used for the TP.
     */
    public static BlockPos findLand(final BlockPos blockPos, final Level world)
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

        if (BlockUtils.isAnySolid(world.getBlockState(tempPos)))
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
    public static double getValidHeight(@NotNull final Vec3 position, @NotNull final Level world)
    {
        double returnHeight = position.y;
        if (position.y < world.getMinBuildHeight())
        {
            returnHeight = world.getMinBuildHeight();
        }

        while (returnHeight >= 1 && world.isEmptyBlock(new BlockPos(Mth.floor(position.x),
          (int) returnHeight,
          Mth.floor(position.z))))
        {
            returnHeight -= 1.0D;
        }

        while (!world.isEmptyBlock(
          new BlockPos(Mth.floor(position.x), (int) returnHeight, Mth.floor(position.z))))
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
     * Manhatten distance
     */
    public static int distManhattan(final BlockPos pos, final BlockPos pos2)
    {
        int xDist = Math.abs(pos.getX() - pos2.getX());
        int yDist = Math.abs(pos.getY() - pos2.getY());
        int zDist = Math.abs(pos.getZ() - pos2.getZ());
        return xDist + yDist + zDist;
    }

    /**
     * Manhatten distance
     */
    public static int distManhattan(final BlockPos pos, final int x2, final int y2, final int z2)
    {
        int xDist = Math.abs(pos.getX() - x2);
        int yDist = Math.abs(pos.getY() - y2);
        int zDist = Math.abs(pos.getZ() - z2);
        return xDist + yDist + zDist;
    }

    /**
     * Manhatten distance
     */
    public static int distManhattan(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        int xDist = Math.abs(x1 - x2);
        int yDist = Math.abs(y1 - y2);
        int zDist = Math.abs(z1 - z2);
        return xDist + yDist + zDist;
    }

    /**
     * Square distance
     */
    public static int distSqr(final BlockPos pos, final BlockPos pos2)
    {
        int xDist = pos.getX() - pos2.getX();
        int yDist = pos.getY() - pos2.getY();
        int zDist = pos.getZ() - pos2.getZ();
        return xDist * xDist + yDist * yDist + zDist * zDist;
    }

    /**
     * Square distance
     */
    public static int distSqr(final BlockPos pos, final int x2, final int y2, final int z2)
    {
        int xDist = pos.getX() - x2;
        int yDist = pos.getY() - y2;
        int zDist = pos.getZ() - z2;
        return xDist * xDist + yDist * yDist + zDist * zDist;
    }

    /**
     * Square distance
     */
    public static int distSqr(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        int xDist = x1 - x2;
        int yDist = y1 - y2;
        int zDist = z1 - z2;
        return xDist * xDist + yDist * yDist + zDist * zDist;
    }

    /**
     * Euclidean distance
     */
    public static double dist(final BlockPos pos, final int x2, final int y2, final int z2)
    {
        int xDist = pos.getX() - x2;
        int yDist = pos.getY() - y2;
        int zDist = pos.getZ() - z2;
        return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
    }

    /**
     * Euclidean distance
     */
    public static double dist(final BlockPos pos, final BlockPos pos2)
    {
        int xDist = pos.getX() - pos2.getX();
        int yDist = pos.getY() - pos2.getY();
        int zDist = pos.getZ() - pos2.getZ();
        return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
    }

    /**
     * Euclidean distance
     */
    public static double dist(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        int xDist = x1 - x2;
        int yDist = y1 - y2;
        int zDist = z1 - z2;
        return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
    }

    /**
     * Returns a radial bounding box aligned to chunk boundaries.  Note that the Y coordinate
     * is also aligned to chunk-like sizes; this does not return full world height.  (It also
     * might return Y coordinates outside the world limits, so clip before using if needed.)
     *
     * @param pos         A position inside the center chunk.
     * @param chunkRadius 0 for one chunk, 1 for nine chunks, etc.
     * @return The specified bounding box.
     */
    public static BoundingBox getChunkAlignedBB(final BlockPos pos, final int chunkRadius)
    {
        final int blockRadius = chunkRadius * 16;
        final int x1 = pos.getX() & ~15;
        final int y1 = pos.getY() & ~15;
        final int z1 = pos.getZ() & ~15;
        return new BoundingBox(x1 - blockRadius, y1 - blockRadius, z1 - blockRadius,
          x1 + blockRadius + 15, y1 + blockRadius + 15, z1 + blockRadius + 15);
    }

    /**
     * Returns the tile entity at a specific chunk coordinate.
     *
     * @param world World the tile entity is in.
     * @param pos   Coordinates of the tile entity.
     * @return Tile entity at the given coordinates.
     */
    public static BlockEntity getTileEntity(@NotNull final Level world, @NotNull final BlockPos pos)
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
    public static List<ItemStack> getBlockDrops(@NotNull final Level world, @NotNull final BlockPos coords, final int fortune, final ItemStack stack, final LivingEntity entity)
    {
        return world.getBlockState(coords).getDrops(new LootParams.Builder((ServerLevel) world)
          .withLuck(fortune)
          .withOptionalParameter(LootContextParams.BLOCK_ENTITY, world.getBlockEntity(coords))
          .withParameter(LootContextParams.ORIGIN, entity.position())
          .withParameter(LootContextParams.TOOL, stack));
    }

    /**
     * Returns the block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Block at the given coordinates.
     */
    public static Block getBlock(@NotNull final Level world, @NotNull final BlockPos coords)
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
    public static BlockState getBlockState(@NotNull final Level world, @NotNull final BlockPos coords)
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
    public static boolean setBlock(@NotNull final Level worldIn, @NotNull final BlockPos coords, final BlockState state, final int flag)
    {
        return worldIn.setBlock(coords, state, flag);
    }

    /**
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
     * @param living      A living entity.
     * @param destination chunk coordinates to check moving to.
     * @return True when XYZ is found, an set moving to, otherwise false.
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final Mob living, @NotNull final BlockPos destination)
    {
        return EntityUtils.tryMoveLivingToXYZ(living, destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     * Create a method for using a {@link BlockPos}.
     *
     * @param newPos The new position to set.
     */
    public static void set(@NotNull final BlockPos.MutableBlockPos pos, @NotNull final BlockPos newPos)
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
    public static boolean equals(@NotNull final BlockPos coords, final int x, final int y, final int z)
    {
        return coords.getX() == x && coords.getY() == y && coords.getZ() == z;
    }

    public static boolean equals(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }

    public static boolean equals(final BlockPos pos1, final BlockPos pos2)
    {
        return pos1.equals(pos2);
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
        return new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()));
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param world    the world the position is in.
     * @return returns BlockPos position with air above.
     */
    @NotNull
    public static BlockPos getFloor(@NotNull final BlockPos position, @NotNull final Level world)
    {
        final BlockPos floor = getFloor(new BlockPos.MutableBlockPos(position.getX(), position.getY(), position.getZ()), 0, world);
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
    public static BlockPos getFloor(@NotNull final BlockPos.MutableBlockPos position, final int depth, @NotNull final Level world)
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
        return directionFromDelta(vector.getX(), vector.getY(), vector.getZ());
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
        return directionFromDelta(vector.getX(), 0, vector.getZ());
    }

    /**
     * Get facing from x,y
     *
     * @param pos1X start-x
     * @param pos1Z start-z
     * @param pos2X end-x
     * @param pos2Z end-z
     * @return the direction.
     */
    public static Direction getXZFacing(final int pos1X, final int pos1Z, final int pos2X, final int pos2Z)
    {
        if (pos2X > pos1X)
        {
            return Direction.EAST;
        }
        else if (pos2X < pos1X)
        {
            return Direction.WEST;
        }
        if (pos2Z < pos1Z)
        {
            return Direction.NORTH;
        }
        else
        {
            return Direction.SOUTH;
        }
    }

    /**
     * Calculate a direction from a given delta.
     *
     * @param x the delta x.
     * @param y the delta y.
     * @param z the delta z.
     * @return the direction.
     */
    public static Direction directionFromDelta(int x, int y, int z)
    {
        if (x == 0)
        {
            if (y == 0)
            {
                if (z > 0)
                {
                    return Direction.SOUTH;
                }

                if (z < 0)
                {
                    return Direction.NORTH;
                }
            }
            else if (z == 0)
            {
                if (y > 0)
                {
                    return Direction.UP;
                }

                return Direction.DOWN;
            }
        }
        else if (y == 0 && z == 0)
        {
            if (x > 0)
            {
                return Direction.EAST;
            }

            return Direction.WEST;
        }

        return Direction.NORTH;
    }

    /**
     * Calculates the direction a position is from the building.
     *
     * @param building the building.
     * @param pos      the position.
     * @return a text component describing the direction.
     */
    public static DirectionResult calcDirection(@NotNull final BlockPos building, @NotNull final BlockPos pos)
    {
        DirectionResult direction = DirectionResult.SAME;

        // When the X and Z coordinates are identical to the building its position
        // then return a component saying that the position is either directly above or directly below
        // the building
        if (pos.getZ() == building.getZ() && pos.getX() == building.getX())
        {
            if (pos.getY() > building.getY())
            {
                direction = DirectionResult.UP;
            }
            else if (pos.getY() < building.getY())
            {
                direction = DirectionResult.DOWN;
            }
        }

        // If a building is greater or smaller in the Z direction, either return north or south
        if (pos.getZ() > building.getZ())
        {
            direction = DirectionResult.SOUTH;
        }
        else if (pos.getZ() < building.getZ())
        {
            direction = DirectionResult.NORTH;
        }

        // If a building is greater or smaller in the X direction, either return west or east
        // If previously already north or south was selected, create a compound direction (north/east etc)
        if (pos.getX() > building.getX())
        {
            direction = switch (direction)
                          {
                              case NORTH -> DirectionResult.NORTH_EAST;
                              case SOUTH -> DirectionResult.SOUTH_EAST;
                              default -> DirectionResult.EAST;
                          };
        }
        else if (pos.getX() < building.getX())
        {
            direction = switch (direction)
                          {
                              case NORTH -> DirectionResult.NORTH_WEST;
                              case SOUTH -> DirectionResult.SOUTH_WEST;
                              default -> DirectionResult.WEST;
                          };
        }

        // In case that none of the checks pass (XYZ fully identical to the building), return a component saying the positions are identical
        return direction;
    }

    /**
     * Direction result from {@link BlockPosUtil#calcDirection}.
     */
    public enum DirectionResult
    {
        NORTH(DIRECTION_NORTH, DIRECTION_NORTH_SHORT),
        SOUTH(DIRECTION_SOUTH, DIRECTION_SOUTH_SHORT),
        WEST(DIRECTION_WEST, DIRECTION_WEST_SHORT),
        EAST(DIRECTION_EAST, DIRECTION_EAST_SHORT),
        NORTH_WEST(List.of(DIRECTION_NORTH, DIRECTION_WEST), List.of(DIRECTION_NORTH_SHORT, DIRECTION_WEST_SHORT)),
        NORTH_EAST(List.of(DIRECTION_NORTH, DIRECTION_EAST), List.of(DIRECTION_NORTH_SHORT, DIRECTION_EAST_SHORT)),
        SOUTH_WEST(List.of(DIRECTION_SOUTH, DIRECTION_WEST), List.of(DIRECTION_SOUTH_SHORT, DIRECTION_WEST_SHORT)),
        SOUTH_EAST(List.of(DIRECTION_SOUTH, DIRECTION_EAST), List.of(DIRECTION_SOUTH_SHORT, DIRECTION_EAST_SHORT)),
        UP(DIRECTION_UP, DIRECTION_UP),
        DOWN(DIRECTION_DOWN, DIRECTION_DOWN),
        SAME(DIRECTION_EXACT, DIRECTION_EXACT);

        /**
         * The long display of this direction, for example "North".
         */
        private final Component longText;

        /**
         * The short display of this direction, for example "N".
         */
        private final Component shortText;

        /**
         * Single string constructor.
         */
        DirectionResult(final String longText, final String shortText)
        {
            this.longText = Component.translatable(longText);
            this.shortText = Component.translatable(shortText);
        }

        /**
         * Multi string constructor.
         */
        DirectionResult(final List<String> longText, final List<String> shortText)
        {
            this.longText = ComponentUtils.formatList(longText.stream().map(Component::translatable).toList(), Component.literal("/"));
            this.shortText = ComponentUtils.formatList(shortText.stream().map(Component::translatable).toList(), Component.literal("/"));
        }

        /**
         * Get the long display of this direction, for example "North".
         *
         * @return the component.
         */
        public Component getLongText()
        {
            return longText;
        }

        /**
         * Get the short display of this direction, for example "N".
         *
         * @return the component.
         */
        public Component getShortText()
        {
            return shortText;
        }
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
     * @param start           start position
     * @param horizontalRange horizontal search range
     * @param verticalRange   vertical search range
     * @param predicate       check predicate for the right block
     * @return position or null
     */
    public static BlockPos findAround(
      final Level world,
      final BlockPos start,
      final int verticalRange,
      final int horizontalRange,
      final BiPredicate<BlockGetter, BlockPos> predicate)
    {
        if (horizontalRange < 1 && verticalRange < 1)
        {
            return null;
        }

        if (predicate.test(world, start))
        {
            return start;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (predicate.test(world, start.relative(direction)))
            {
                return start.relative(direction);
            }
        }

        BlockPos temp;
        int y = 0;
        int y_offset = 1;

        for (int i = 0; i < verticalRange + 2; i++)
        {
            for (int steps = 1; steps <= horizontalRange; steps++)
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

            if (!WorldUtil.isInWorldHeight(start.getY() + y, world))
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
    public static BlockPos findSpawnPosAround(final Level worldReader, final BlockPos start)
    {
        return findAround(worldReader, start, 1, 1,
          (world, pos) -> world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir());
    }

    /**
     * Get the furthest corner from a pos.
     *
     * @param startPos the startpos.
     * @param boxStart the box start.
     * @param boxEnd   the box end.
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

    /**
     * Check if a location is within an area.
     *
     * @param cornerA  the first corner.
     * @param cornerB  the second corner.
     * @param location the location to check for.
     * @return true if so.
     */
    public static boolean isInArea(final BlockPos cornerA, final BlockPos cornerB, final BlockPos location)
    {
        int x1, x2, z1, z2, y1, y2;

        if (cornerA.getX() <= cornerB.getX())
        {
            x1 = cornerA.getX();
            x2 = cornerB.getX();
        }
        else
        {
            x2 = cornerA.getX();
            x1 = cornerB.getX();
        }

        if (cornerA.getZ() <= cornerB.getZ())
        {
            z1 = cornerA.getZ();
            z2 = cornerB.getZ();
        }
        else
        {
            z2 = cornerA.getZ();
            z1 = cornerB.getZ();
        }

        if (cornerA.getY() <= cornerB.getY())
        {
            y1 = cornerA.getY();
            y2 = cornerB.getY();
        }
        else
        {
            y2 = cornerA.getY();
            y1 = cornerB.getY();
        }

        return location.getX() >= x1 - 1 && location.getX() <= x2 + 1
                 && location.getY() >= y1 - 1 && location.getY() <= y2 + 1
                 && location.getZ() >= z1 - 1 && location.getZ() <= z2 + 1;
    }
}
