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

public class Tree
{
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_LOGS = "Logs";

    private static final int NUMBER_OF_LEAVES = 3;

    private ChunkCoordinates location;
    private LinkedList<ChunkCoordinates> woodBlocks;
    private boolean isTree = false;

    private Tree()
    {
        isTree = true;
    }

    public Tree(World world, ChunkCoordinates log)
    {
        Block block = ChunkCoordUtils.getBlock(world, log);
        if(block.isWood(world, log.posX, log.posY, log.posZ))
        {
            location = getBaseLog(world, log.posX, log.posY, log.posZ);
            woodBlocks = new LinkedList<>();

            checkTree(world, getTopLog(world, log.posX, log.posY, log.posZ));
        }
    }

    public void findLogs(World world)
    {
        addAndSearch(world, location);
        Collections.sort(woodBlocks, (c1, c2) -> (int) (c1.getDistanceSquaredToChunkCoordinates(location) - c2.getDistanceSquaredToChunkCoordinates(location)));
    }

    public void addBaseLog()
    {
        woodBlocks.add(new ChunkCoordinates(location));
    }

    private void addAndSearch(World world, ChunkCoordinates log)
    {
        woodBlocks.add(log);
        for(int y = -1; y <= 1; y++)
        {
            for(int x = -1; x <= 1; x++)
            {
                for(int z = -1; z <= 1; z++)
                {
                    ChunkCoordinates temp = ChunkCoordUtils.add(log, x, y, z);
                    if(ChunkCoordUtils.getBlock(world, temp).isWood(null,0,0,0) && !woodBlocks.contains(temp))//TODO reorder if more optimal
                    {
                        addAndSearch(world, temp);
                    }
                }
            }
        }
    }

    public boolean isTree()
    {
        return isTree;
    }

    private void checkTree(World world, ChunkCoordinates topLog)
    {
        if(!world.getBlock(location.posX, location.posY-1, location.posZ).getMaterial().isSolid())
        {
            return;
        }
        int leafCount = 0;
        for(int x = -1; x <= 1; x++)
        {
            for(int z = -1; z <= 1; z++)
            {
                for(int y = -1; y <= 1; y++)
                {
                    if(world.getBlock(topLog.posX + x, topLog.posY + y, topLog.posZ + z).getMaterial().equals(Material.leaves))
                    {
                        leafCount++;
                        if(leafCount >= NUMBER_OF_LEAVES)
                        {
                            isTree = true;
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * For use in PathJobFindTree
     *
     * @param world the world
     * @param x log x coordinate
     * @param y log y coordinate
     * @param z log z coordinate
     * @return true if the log is part of a tree
     */
    public static boolean checkTree(IBlockAccess world, int x, int y, int z)
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
                        if(leafCount >= NUMBER_OF_LEAVES)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChunkCoordinates getBaseLog(World world, int x, int y, int z)
    {
        while(world.getBlock(x, y-1, z).isWood(world, x, y, z))
        {
            y--;
        }
        return new ChunkCoordinates(x, y, z);
    }

    private ChunkCoordinates getTopLog(World world, int x, int y, int z)
    {
        while(world.getBlock(x, y+1, z).isWood(world, x, y, z))
        {
            y++;
        }
        return new ChunkCoordinates(x, y, z);
    }

    public ChunkCoordinates pollNextLog()
    {
        return woodBlocks.poll();
    }

    public ChunkCoordinates peekNextLog()
    {
        return woodBlocks.peek();
    }

    public boolean hasLogs()
    {
        return woodBlocks.size() > 0;
    }

    public ChunkCoordinates getLocation()
    {
        return location;
    }

    public float squareDistance(Tree other)
    {
        return this.getLocation().getDistanceSquaredToChunkCoordinates(other.getLocation());
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Tree)
        {
            Tree tree = (Tree) o;
            return tree.getLocation().equals(location);
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
        if(!isTree)
        {
            return;
        }

        ChunkCoordUtils.writeToNBT(compound, TAG_LOCATION, location);

        NBTTagList logs = new NBTTagList();
        for(ChunkCoordinates log : woodBlocks)
        {
            ChunkCoordUtils.writeToNBTTagList(logs, log);
        }
        compound.setTag(TAG_LOGS, logs);
    }

    public static Tree readFromNBT(NBTTagCompound compound)
    {
        Tree tree = new Tree();
        tree.location = ChunkCoordUtils.readFromNBT(compound, TAG_LOCATION);

        tree.woodBlocks = new LinkedList<>();
        NBTTagList logs = compound.getTagList(TAG_LOGS, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < logs.tagCount(); i++)
        {
            tree.woodBlocks.add(ChunkCoordUtils.readFromNBTTagList(logs, i));
        }
        return tree;
    }
}