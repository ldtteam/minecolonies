package com.minecolonies.entity.ai;

import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

//TODO For future use!
public class Water
{
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_LAKES = "Lakes";

    private static final int NUMBER_OF_CONNECTED_WATER = 20;

    private ChunkCoordinates location;
    private LinkedList<ChunkCoordinates> waterBlocks;
    private boolean isWater = false;

    private Water()
    {
        isWater = true;
    }

    public Water(World world, ChunkCoordinates water)
    {
        Block block = ChunkCoordUtils.getBlock(world, water);
        if(block.equals(Blocks.water))
        {
            //TODO First check if block is connected to land if not search closest land
            /*location = getWaterConnectedToLand(world, water.posX, water.posY, water.posZ);
            waterBlocks = new LinkedList<ChunkCoordinates>();

            checkWater(world, getTopLog(world, water.posX, water.posY, water.posZ));*/
        }
    }

    public void findWater(World world)
    {
        //System.out.println("Starting findLogs recursive search.");
        long startTime = System.nanoTime();

        addAndSearch(world, location);
        //System.out.println("Search time taken(ms): " + (System.nanoTime()-startTime)/1000000D);

        //Sorts the waterBlocks list depending on the distance to location
        Collections.sort(waterBlocks, new Comparator<ChunkCoordinates>() {
            @Override
            public int compare(ChunkCoordinates c1, ChunkCoordinates c2) {
                return (int) (c1.getDistanceSquaredToChunkCoordinates(location) - c2.getDistanceSquaredToChunkCoordinates(location));
            }
        });
        //System.out.println("Time including sort(ms): " + (System.nanoTime()-startTime)/1000000D);
    }

    private void addAndSearch(World world, ChunkCoordinates log)
    {
        waterBlocks.add(log);
        for(int y = -1; y <= 1; y++)
        {
            for(int x = -1; x <= 1; x++)
            {
                for(int z = -1; z <= 1; z++)
                {
                    ChunkCoordinates temp = ChunkCoordUtils.add(log, x, y, z);
                    if(ChunkCoordUtils.getBlock(world, temp).isWood(null,0,0,0) && !waterBlocks.contains(temp))//TODO reorder if more optimal
                    {
                        addAndSearch(world, temp);
                    }
                }
            }
        }
    }

    public boolean isWater()
    {
        return isWater;
    }

    //TODO checks if it is really water
    private void checkWater(World world, ChunkCoordinates topWater)
    {
        if(!world.getBlock(location.posX, location.posY-1, location.posZ).getMaterial().isLiquid())
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
                    if(world.getBlock(topWater.posX + x, topWater.posY + y, topWater.posZ + z).getMaterial().equals(Blocks.water))
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

    //TODO checks if it is really water
    /**
     * For use in PathJobFindTree
     *
     * @param world the world
     * @param x log x coordinate
     * @param y log y coordinate
     * @param z log z coordinate
     * @return true if the log is part of a tree
     */
    public static boolean checkWater(IBlockAccess world, int x, int y, int z)
    {
        //Is the first block a log?
        if(!world.getBlock(x, y, z).isWood(world, x, y, z))
        {
            return false;
        }

        //Get base log, should already be base log
        while(world.getBlock(x, y-1, z).isWood(world, x, y, z))
        {
            y--;
        }

        //Make sure tree is on solid ground and tree is not build above cobblestone
        if(!world.getBlock(x, y-1, z).getMaterial().isSolid() || world.getBlock(x, y-1, z) == Blocks.cobblestone)
        {
            return false;
        }

        //Get top log
        while(world.getBlock(x, y+1, z).isWood(world, x, y, z))
        {
            y++;
        }

        int leafCount = 0;
        for(int dx = -1; dx <= 1; dx++)
        {
            for(int dz = -1; dz <= 1; dz++)
            {
                for(int dy = -1; dy <= 1; dy++)
                {
                    if(world.getBlock(x + dx, y + dy, z + dz).getMaterial().equals(Material.leaves))
                    {
                        leafCount++;
                        if(leafCount >= NUMBER_OF_CONNECTED_WATER)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public ChunkCoordinates pollNextWater()
    {
        return waterBlocks.poll();
    }

    public ChunkCoordinates peekNextWater()
    {
        return waterBlocks.peek();
    }

    public boolean hasLakes()
    {
        return waterBlocks.size() > 0;
    }

    public ChunkCoordinates getLocation()
    {
        return location;
    }

    public float squareDistance(Water other)
    {
        return this.getLocation().getDistanceSquaredToChunkCoordinates(other.getLocation());
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Water)
        {
            Water water = (Water) o;
            return water.getLocation().equals(location);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        if(!isWater)
        {
            return;
        }

        ChunkCoordUtils.writeToNBT(compound, TAG_LOCATION, location);

        NBTTagList lakes = new NBTTagList();
        for(ChunkCoordinates water : waterBlocks)
        {
            ChunkCoordUtils.writeToNBTTagList(lakes, water);
        }
        compound.setTag(TAG_LAKES, lakes);
    }

    public static Water readFromNBT(NBTTagCompound compound)
    {
        Water water = new Water();
        water.location = ChunkCoordUtils.readFromNBT(compound, TAG_LOCATION);

        water.waterBlocks = new LinkedList<ChunkCoordinates>();
        NBTTagList logs = compound.getTagList(TAG_LAKES, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < logs.tagCount(); i++)
        {
            water.waterBlocks.add(ChunkCoordUtils.readFromNBTTagList(logs, i));
        }
        return water;
    }
}