package com.minecolonies.entity.ai;

import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.Queue;

public class Tree
{
    private static final int NUMBER_OF_LEAVES = 3;

    private ChunkCoordinates location;
    private Queue<ChunkCoordinates> woodBlocks;
    private boolean isTree = false;

    public Tree(World world, ChunkCoordinates log)
    {
        Block block = ChunkCoordUtils.getBlock(world, log);
        if(block instanceof BlockLog)
        {
            ChunkCoordinates baseLog = getBaseLog(world, log);
            location = new ChunkCoordinates(baseLog);
            woodBlocks = new LinkedList<ChunkCoordinates>();

            //simple pillar
            while(ChunkCoordUtils.getBlock(world, baseLog) instanceof BlockLog)
            {
                woodBlocks.add(new ChunkCoordinates(baseLog));
                baseLog.posY++;
            }
            baseLog.posY--;//Make baseLog the topLog

            checkTree(world, baseLog);

            //TODO add the rest of the logs
        }
        //isTree = false
    }

    public boolean isTree()
    {
        return isTree;
    }

    private void checkTree(World world, ChunkCoordinates topLog)
    {
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
        //isTree = false
    }

    private ChunkCoordinates getBaseLog(World world, ChunkCoordinates log)
    {
        while(ChunkCoordUtils.getBlock(world, log) instanceof BlockLog)
        {
            log.posY--;
        }
        log.posY++;
        return log;
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
}