package com.minecolonies.entity.ai;

import com.minecolonies.util.BlockPosUtil;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Pond
{
    private static final String TAG_LOCATION = "Location";

    private BlockPos location;

    private Pond(BlockPos water)
    {
        this.location = water;
    }

    /**
     * Creates a new Pond iff water is a valid water block
     * @param world The world the player is in
     * @param water the coordinates to check
     * @return a Pond object if the pond is valid, else null
     */
    public static Pond createWater(IBlockAccess world, BlockPos water)
    {
        if(checkWater(world,water))
        {
            return new Pond(water);
        }
        return null;
    }

    /** Checks if on position "water" really is water, if the water is connected to land and if the pond is big enough ( > 20)
     *
     * @param world The world the player is in
     * @param water The coordinate to check
     */
    private static boolean checkWater(IBlockAccess world, BlockPos water)
    {
        int x = water.getX();
        int y = water.getY();
        int z = water.getZ();

        if(!(world.getBlockState(water).getBlock() == Blocks.water) || !world.isAirBlock(water.up()))
        {
            return false;
        }

        //If not one direction contains a pool with length at least 6 and width 7
        return checkWaterPoolInDirectionXThenZ(world, x, y, z, 1) || checkWaterPoolInDirectionXThenZ(world, x, y, z, -1) ||
               checkWaterPoolInDirectionZThenX(world, x, y, z, 1) || checkWaterPoolInDirectionZThenX(world, x, y, z, -1);
    }

    /**
     * Checks if all blocks in direction X are Pond
     * @param world     World
     * @param x         posX
     * @param y         posY
     * @param z         posZ
     * @param vector    direction
     * @return true if all blocks are water, else false
     */
    private static boolean checkWaterPoolInDirectionX(IBlockAccess world, int x, int y, int z, int vector)
    {
        //Check 3 blocks in direction +/- x
        for (int dx = x + 3 * vector; dx <= x + 3 * vector; dx++)
        {
            if (!(world.getBlockState(new BlockPos(dx, y, z)).getBlock() == Blocks.water))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all blocks in direction Z are Pond
     * @param world     World
     * @param x         posX
     * @param y         posY
     * @param z         posZ
     * @param vector    direction
     * @return true if all blocks are water, else false
     */
    private static boolean checkWaterPoolInDirectionZ(IBlockAccess world, int x, int y, int z, int vector)
    {
        //Check 3 blocks in direction +/- z
        for (int dz = z + 3 * vector; dz <= z + 3 * vector; dz++)
        {
            if (!(world.getBlockState(new BlockPos(x, y, dz)).getBlock() == Blocks.water))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all blocks in direction X are water and if yes from the middle to both sides in
     * direction Z all blocks are also water.
     * @param world     World
     * @param x         posX
     * @param y         posY
     * @param z         posZ
     * @param vector    direction
     * @return true if all blocks are water, else false
     */
    private static boolean checkWaterPoolInDirectionXThenZ(IBlockAccess world, int x, int y, int z, int vector)
    {
        //Check 6 blocks in direction +/- x
        for (int dx = x + 6 * vector; dx <= x + 6 * vector; dx++)
        {
            if (!(world.getBlockState(new BlockPos(dx, y, z)).getBlock() == Blocks.water))
            {
                return false;
            }
        }
        //Takes the middle x block and searches 3 water blocks to both sides
        return checkWaterPoolInDirectionZ(world,x + 3 * vector, y, z, 1) && checkWaterPoolInDirectionZ(world,x + 3 * vector, y, z, -1);
    }

    /**
     * Checks if all blocks in direction Z are water and if yes from the middle to both sides in
     * direction X all blocks are also water.
     * @param world     World
     * @param x         posX
     * @param y         posY
     * @param z         posZ
     * @param vector    direction
     * @return true if all blocks are water, else false
     */
    private static boolean checkWaterPoolInDirectionZThenX(IBlockAccess world, int x, int y, int z, int vector)
    {
        //Check 6 blocks in direction +/- z
        for (int dz = z + 6 * vector; dz <= z + 6 * vector; dz++)
        {
            if (!(world.getBlockState(new BlockPos(x, y, dz)).getBlock() == Blocks.water))
            {
                return false;
            }
        }
        //Takes the middle z block and searches 3 water blocks to both sides
        return checkWaterPoolInDirectionX(world,x, y, z + 3 * vector, 1) && checkWaterPoolInDirectionX(world,x, y, z + 3 * vector, -1);
    }

    public BlockPos getLocation()
    {
        return location;
    }

    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Pond)){
            return false;
        }
        Pond wobj = (Pond)obj;
        return location.equals(wobj.getLocation());
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);
    }

    public static Pond readFromNBT(NBTTagCompound compound)
    {
        return new Pond(BlockPosUtil.readFromNBT(compound, TAG_LOCATION));
    }
}