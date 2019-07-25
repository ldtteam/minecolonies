package com.minecolonies.coremod.entity.ai.citizen.fisherman;

import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a body of water used by the fisherman.
 */
public final class Pond
{
    private static final String TAG_LOCATION                  = "Location";
    private static final int    WATER_POOL_WIDTH_REQUIREMENT  = 6;
    private static final int    WATER_POOL_LENGTH_REQUIREMENT = 3;

    private final BlockPos location;

    private Pond(final BlockPos water)
    {
        this.location = water;
    }

    /**
     * Creates a new Pond iff water is a valid water block.
     *
     * @param world The world the player is in.
     * @param water the coordinates to check.
     * @return a Pond object if the pond is valid, else null.
     */
    public static Pond createWater(@NotNull final IBlockAccess world, @NotNull final BlockPos water)
    {
        if (checkWater(world, water, WATER_POOL_WIDTH_REQUIREMENT, WATER_POOL_LENGTH_REQUIREMENT))
        {
            return new Pond(water);
        }
        return null;
    }

    /**
     * Checks if on position "water" really is water, if the water is connected to land and if the pond is big enough ( > 20).
     *
     * @param world The world the player is in.
     * @param water The coordinate to check.
     * @param width which has to be water.
     * @param length which has to be water.
     */
    public static boolean checkWater(@NotNull final IBlockAccess world, @NotNull final BlockPos water, final int width, final int length)
    {
        if (world.getBlockState(water).getBlock() != Blocks.WATER || !world.isAirBlock(water.up()))
        {
            return false;
        }

        final int x = water.getX();
        final int y = water.getY();
        final int z = water.getZ();

        //If not one direction contains a pool with length at least 6 and width 7
        return checkWaterPoolInDirectionXThenZ(world, x, y, z, 1, width, length)
                 || checkWaterPoolInDirectionXThenZ(world, x, y, z, -1, width, length)
                 || checkWaterPoolInDirectionZThenX(world, x, y, z, 1, width, length)
                 || checkWaterPoolInDirectionZThenX(world, x, y, z, -1, width, length);
    }

    /**
     * Checks if all blocks in direction X are water and if yes from the middle to both sides in.
     * direction Z all blocks are also water.
     *
     * @param world  World.
     * @param x      posX.
     * @param y      posY.
     * @param z      posZ.
     * @param vector direction.
     * @param width which has to be water.
     * @param length length has to be water.
     * @return true if all blocks are water, else false.
     */
    private static boolean checkWaterPoolInDirectionXThenZ(
      @NotNull final IBlockAccess world,
      final int x,
      final int y,
      final int z,
      final int vector,
      final int width,
      final int length)
    {
        //Check 6 blocks in direction +/- x
        for (int dx = x + width * vector; dx <= x + width * vector; dx++)
        {
            if (world.getBlockState(new BlockPos(dx, y, z)).getBlock() != Blocks.WATER)
            {
                return false;
            }
        }
        //Takes the middle x block and searches 3 water blocks to both sides
        return checkWaterPoolInDirectionZ(world, x + length * vector, y, z, 1) && checkWaterPoolInDirectionZ(world, x + length
                                                                                                                                                 * vector, y, z, -1);
    }

    /**
     * Checks if all blocks in direction Z are water and if yes from the middle to both sides in
     * direction X all blocks are also water.
     *
     * @param world  World.
     * @param x      posX.
     * @param y      posY.
     * @param z      posZ.
     * @param vector direction.
     * @param width which has to be water.
     * @param length length has to be water.
     * @return true if all blocks are water, else false.
     */
    private static boolean checkWaterPoolInDirectionZThenX(
      @NotNull final IBlockAccess world,
      final int x,
      final int y,
      final int z,
      final int vector,
      final int width,
      final int length)
    {
        //Check 6 blocks in direction +/- z
        for (int dz = z + width * vector; dz <= z + width * vector; dz++)
        {
            if (world.getBlockState(new BlockPos(x, y, dz)).getBlock() != Blocks.WATER)
            {
                return false;
            }
        }
        //Takes the middle z block and searches 3 water blocks to both sides
        return checkWaterPoolInDirectionX(world, x, y, z + length * vector, 1) && checkWaterPoolInDirectionX(world, x, y, z + length
                                                                                                                                                       * vector, -1);
    }

    /**
     * Checks if all blocks in direction Z are Pond.
     *
     * @param world  World.
     * @param x      posX.
     * @param y      posY.
     * @param z      posZ.
     * @param vector direction.
     * @return true if all blocks are water, else false.
     */
    private static boolean checkWaterPoolInDirectionZ(@NotNull final IBlockAccess world, final int x, final int y, final int z, final int vector)
    {
        //Check 3 blocks in direction +/- z
        for (int dz = z + WATER_POOL_LENGTH_REQUIREMENT * vector; dz <= z + WATER_POOL_LENGTH_REQUIREMENT * vector; dz++)
        {
            if (world.getBlockState(new BlockPos(x, y, dz)).getBlock() != Blocks.WATER)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all blocks in direction X are Pond.
     *
     * @param world  World.
     * @param x      posX.
     * @param y      posY.
     * @param z      posZ.
     * @param vector direction.
     * @return true if all blocks are water, else false.
     */
    private static boolean checkWaterPoolInDirectionX(@NotNull final IBlockAccess world, final int x, final int y, final int z, final int vector)
    {
        //Check 3 blocks in direction +/- x
        for (int dx = x + WATER_POOL_LENGTH_REQUIREMENT * vector; dx <= x + WATER_POOL_LENGTH_REQUIREMENT * vector; dx++)
        {
            if (world.getBlockState(new BlockPos(dx, y, z)).getBlock() != Blocks.WATER)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a Pond object from NBT.
     *
     * @param compound NBT tag compound to read from.
     * @return new Pond instance.
     */
    @NotNull
    public static Pond readFromNBT(@NotNull final CompoundNBT compound)
    {
        return new Pond(BlockPosUtil.readFromNBT(compound, TAG_LOCATION));
    }

    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    @Override
    public boolean equals(@Nullable final Object obj)
    {
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }

        @NotNull final Pond wobj = (Pond) obj;
        return location.equals(wobj.getLocation());
    }

    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Serialize for NBT.
     *
     * @param compound nbt tag compound to write to.
     */
    public void write(@NotNull final CompoundNBT compound)
    {
        BlockPosUtil.write(compound, TAG_LOCATION, location);
    }
}
