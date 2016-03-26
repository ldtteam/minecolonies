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
            checkWater(world, water);
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
    private void checkWater(World world, ChunkCoordinates water)
    {
        if(!world.isAirBlock(location.posX, location.posY+1, location.posZ))
        {
            return;
        }
        int waterCount = 0;
        for(int x = -1; x <= 1; x++)
        {
            for(int z = -1; z <= 1; z++)
            {
                for(int y = -1; y <= 1; y++)
                {
                    if(world.getBlock(water.posX + x, water.posY + y, water.posZ + z).equals(Blocks.water))
                    {
                        waterCount++;
                        if(waterCount >= NUMBER_OF_CONNECTED_WATER)
                        {
                            isWater = true;
                            return;
                        }
                    }
                }
            }
        }
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
        if(!world.getBlock(x, y, z).equals(Blocks.water) || !world.isAirBlock(x,y+1,z))
        {
            return false;
        }

        ChunkCoordinates nextWaterBlock = null;
        int vectorX=0;
        int vectorZ=0;
        EntityPlayer mp;
        //TODO Check if there are at least 20 other waters connected to the water block
        //TODO Check in all 4 directions
        for(int dx = -1; dx <= 1; dx++)
        {
            for(int dz = -1; dz <= 1; dz++)
            {
                if (world.getBlock(x + dx, y, z + dz).equals(Blocks.water))
                {
                    nextWaterBlock = new ChunkCoordinates(x + dx, y, z + dz);
                    vectorX = dx;
                    vectorZ = dz;
                    break;
                }
            }
        }

        //TODO use vectors to calculate 6 blocks to front(isWater) and 3 blocks to each side of the middle block(isWater)
        //to confirm if the size is sufficient to count as a pond
        if(nextWaterBlock == null)
        {
            return false;
        }



        for(int dx = x; dx <= x+6; dx++)
        {
            world.getBlock(dx,y,z).equals(Blocks.water);
        }
        return false;
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