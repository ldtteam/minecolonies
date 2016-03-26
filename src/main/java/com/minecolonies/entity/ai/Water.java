package com.minecolonies.entity.ai;

import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

//TODO For future use!
public class Water
{
    private static final String TAG_LOCATION = "Location";
    private static final int NUMBER_OF_CONNECTED_WATER = 20;

    private ChunkCoordinates location;
    private boolean isWater = false;

    private Water()
    {
        isWater = true;
    }

    //TODO: What if that is an illegal pond of water?
    // I would suggest making the constructor private
    // and have some factory method check for water and return one if valid
    Water(World world, ChunkCoordinates water)
    {
        Block block = ChunkCoordUtils.getBlock(world, water);
        if(block.equals(Blocks.water))
        {
            if(checkWater(world, water))
            {
                isWater = true;
            }
        }
    }

    public boolean isWater()
    {
        return isWater;
    }

    /** Checks if on position "water" really is water, if the water is connected to land and if the pond is big enough ( > 20)
     *
     * @param world The world the player is in
     * @param water The coordinate to check
     */
    private boolean checkWater(World world, ChunkCoordinates water)
    {
        int x = water.posX;
        int y = water.posY;
        int z = water.posZ;

        if(!world.getBlock(x, y, z).equals(Blocks.water) || !world.isAirBlock(x, y + 1, z))
        {
            return false;
        }

        //If not one direction contains a pool with length at least 6 and width 7
        return !(!checkWaterPoolInDirectionX(world, x, y, z, 1) && !checkWaterPoolInDirectionX(world, x, y, z, -1) &&
                !checkWaterPoolInDirectionZ(world, x, y, z, 1) && !checkWaterPoolInDirectionZ(world, x, y, z, -1));
    }

    /**
     * For use in PathJobFindWater
     *
     * @param world the world
     * @param x log x coordinate
     * @param y log y coordinate
     * @param z log z coordinate
     * @return true if the water is part of a river, sea ...
     */
    public static boolean checkWater(IBlockAccess world, int x, int y, int z)
    {
        if(!world.getBlock(x, y, z).equals(Blocks.water) || !world.isAirBlock(x, y + 1, z))
        {
            return false;
        }

        //If not one direction contains a pool with length at least 6 and width 7
        return !(!checkWaterPoolInDirectionX(world, x, y, z, 1) && !checkWaterPoolInDirectionX(world, x, y, z, -1) &&
                !checkWaterPoolInDirectionZ(world, x, y, z, 1) && !checkWaterPoolInDirectionZ(world, x, y, z, -1));

    }

    private static boolean checkWaterPoolInDirectionX(IBlockAccess world, int x, int y, int z, int vector)
    {
        //Check 6 blocks in direction +/- x
        for (int dx = x + 6 * vector; dx <= x + 6 * vector; dx++)
        {
            if (!world.getBlock(dx, y, z).equals(Blocks.water))
            {
                return false;
            }
        }
        //Takes the middle x block and searches 3 water blocks to both sides
        return checkWaterPoolInDirectionZ(world,x + 3 * vector, y, z, 1) && checkWaterPoolInDirectionZ(world,x + 3 * vector, y, z, -1);
    }

    private static boolean checkWaterPoolInDirectionZ(IBlockAccess world, int x, int y, int z, int vector)
    {
        //Check 6 blocks in direction +/- z
        for (int dz = z + 6 * vector; dz <= z + 6 * vector; dz++)
        {
            if (!world.getBlock(x, y, dz).equals(Blocks.water))
            {
                return false;
            }
        }
        //Takes the middle z block and searches 3 water blocks to both sides
        return checkWaterPoolInDirectionX(world,x, y, z + 3 * vector, 1) && checkWaterPoolInDirectionX(world,x, y, z + 3 * vector, -1);
    }

    public ChunkCoordinates getLocation()
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
        if(!(obj instanceof Water)){
            return false;
        }
        Water wobj = (Water)obj;
        return location.equals(wobj.getLocation());
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        if(!isWater)
        {
            return;
        }
        ChunkCoordUtils.writeToNBT(compound, TAG_LOCATION, location);
    }

    public static Water readFromNBT(NBTTagCompound compound)
    {
        Water water = new Water();
        water.location = ChunkCoordUtils.readFromNBT(compound, TAG_LOCATION);
        return water;
    }
}