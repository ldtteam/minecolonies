package com.minecolonies.entity.ai;

import com.minecolonies.util.BlockPosUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static net.java.games.input.Component.Identifier.Key.O;

/**
 * Custom class for Trees. Used by lumberjack
 */
public class Tree
{
    private static final    String                          TAG_LOCATION        = "Location";
    private static final    String                          TAG_LOGS            = "Logs";
    private static final    int                             NUMBER_OF_LEAVES    = 3;

    private BlockPos                    location;
    private LinkedList<BlockPos>        woodBlocks;
    private boolean                     isTree;
    private ArrayList<BlockPos>         stumpLocations;
    private BlockPlanks.EnumType        variant;
    private Tree()
    {
        isTree = true;
    }

    /**
     * Creates a new tree Object for the lumberjack
     * @param world The world where the tree is in
     * @param log the position of the found log.
     */
    public Tree(World world, BlockPos log)
    {
        Block block = BlockPosUtil.getBlock(world, log);
        if(block.isWood(world, log))
        {
            variant = world.getBlockState(log).getValue(BlockNewLog.VARIANT);
            location = getBaseLog(world, log);
            woodBlocks = new LinkedList<>();
            checkTree(world, getTopLog(world, log));
            stumpLocations = new ArrayList<>();
        }
    }

    /**
     * Searches all logs that belong to the tree.
     * @param world The world where the blocks are in
     */
    public void findLogs(World world)
    {
        addAndSearch(world, location);
        Collections.sort(woodBlocks, (c1, c2) -> (int) (c1.distanceSq(location) - c2.distanceSq(location)));
    }

    /**
     * Checks if the tree has been planted from more than 1 saplings.
     * Meaning that more than 1 log is on the lowest level.
     * @param world The world where the tree is in
     * @param yLevel The base y.
     */
    public void fillTreeStumps(World world, int yLevel)
    {
        for(BlockPos pos: woodBlocks)
        {
            if(pos.getY() == yLevel)
            {
                stumpLocations.add(getBaseLog(world,pos));
            }
        }
    }

    public void addBaseLog()
    {
        woodBlocks.add(new BlockPos(location));
    }

    private void addAndSearch(World world, BlockPos log)
    {
        woodBlocks.add(log);
        for(int y = -1; y <= 1; y++)
        {
            for(int x = -1; x <= 1; x++)
            {
                for(int z = -1; z <= 1; z++)
                {
                    BlockPos temp = BlockPosUtil.add(log, x, y, z);
                    if(BlockPosUtil.getBlock(world, temp).isWood(null,new BlockPos(0,0,0)) && !woodBlocks.contains(temp))//TODO reorder if more optimal
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

    private void checkTree(World world, BlockPos topLog)
    {
        if(!world.getBlockState(new BlockPos(location.getX(), location.getY()-1, location.getZ())).getBlock().getMaterial().isSolid())
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
                    if(world.getBlockState(new BlockPos(topLog.getX() + x, topLog.getY() + y, topLog.getZ() + z)).getBlock().getMaterial().equals(Material.leaves))
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
     * @param pos The coordinates
     * @return true if the log is part of a tree
     */
    public static boolean checkTree(IBlockAccess world, BlockPos pos)
    {
        //Is the first block a log?
        if(!world.getBlockState(pos).getBlock().isWood(world, pos))
        {
            return false;
        }

        //Get base log, should already be base log
        while(world.getBlockState(pos.down()).getBlock().isWood(world, pos))
        {
            pos = pos.down();
        }

        //Make sure tree is on solid ground and tree is not build above cobblestone
        if(!world.getBlockState(pos.down()).getBlock().getMaterial().isSolid() || world.getBlockState(pos.down()).getBlock() == Blocks.cobblestone)
        {
            return false;
        }

        //Get top log
        while(world.getBlockState(pos.up()).getBlock().isWood(world, pos))
        {
            pos = pos.up();
        }

        int leafCount = 0;
        for(int dx = -1; dx <= 1; dx++)
        {
            for(int dz = -1; dz <= 1; dz++)
            {
                for(int dy = -1; dy <= 1; dy++)
                {
                    if(world.getBlockState(pos.add(dx,dy,dz)).getBlock().getMaterial().equals(Material.leaves))
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

    /**
     * Get's the base log of the tree
     * @param world The entity world
     * @param pos The coordinates
     * @return the base log position
     */
    private BlockPos getBaseLog(World world, BlockPos pos)
    {
        while(world.getBlockState(pos.down()).getBlock().isWood(world, pos))
        {
            pos = pos.down();
        }
        return pos;
    }

    /**
     * Get's the top log of the tree
     * @param world The entity world
     * @param pos The coordinates
     * @return the top log position
     */
    private BlockPos getTopLog(World world, BlockPos pos)
    {
        while(world.getBlockState(pos.up()).getBlock().isWood(world,pos.down()))
        {
            pos = pos.up();
        }
        return pos;
    }

    /**
     * Returns the next log block
     * @return the position
     */
    public BlockPos pollNextLog()
    {
        return woodBlocks.poll();
    }

    /**
     * Looks up the next log block
     * @return the position
     */
    public BlockPos peekNextLog()
    {
        return woodBlocks.peek();
    }

    /**
     * Check if the found tree has any logs
     * @return true if size > 0
     */
    public boolean hasLogs()
    {
        return woodBlocks.size() > 0;
    }

    /**
     * Returns the trees location
     * @return the position
     */
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * All stump positions of a tree (A tree may have been planted with different saplings)
     * @return an Arraylist of the positions
     */
    public ArrayList<BlockPos> getStumpLocations()
    {
        return stumpLocations;
    }

    /**
     * Get's the variant of a tree.
     * A tree may only have 1 variant.
     * @return the EnumType variant
     */
    public BlockPlanks.EnumType getVariant()
    {
        return variant;
    }

    /**
     * Calculates the squareDistance to another Tree
     * @param other the other tree
     * @return the square distance in double
     */
    public double squareDistance(Tree other)
    {
        return this.getLocation().distanceSq(other.getLocation());
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

        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);

        NBTTagList logs = new NBTTagList();
        for(BlockPos log : woodBlocks)
        {
            BlockPosUtil.writeToNBTTagList(logs, log);
        }
        compound.setTag(TAG_LOGS, logs);
    }

    public static Tree readFromNBT(NBTTagCompound compound)
    {
        Tree tree = new Tree();
        tree.location = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);

        tree.woodBlocks = new LinkedList<>();
        NBTTagList logs = compound.getTagList(TAG_LOGS, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < logs.tagCount(); i++)
        {
            tree.woodBlocks.add(BlockPosUtil.readFromNBTTagList(logs, i));
        }
        return tree;
    }
}