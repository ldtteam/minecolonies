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
import java.util.List;

/**
 * Custom class for Trees. Used by lumberjack
 */
public class Tree
{
    /**
     * Tag to save the location to NBT.
     */
    private static final String TAG_LOCATION = "Location";
    /**
     * Tag to save the log list to NBT.
     */
    private static final String TAG_LOGS = "Logs";
    /**
     * Tage to save the stump list to NBT.
     */
    private static final String TAG_STUMPS = "Stumps";
    /**
     * Number of leaves necessary for a tree to be recognized.
     */
    private static final int NUMBER_OF_LEAVES = 3;
    /**
     * The location of the tree stump.
     */
    private BlockPos location;
    /**
     * All wood blocks connected to the tree.
     */
    private LinkedList<BlockPos> woodBlocks;
    /**
     * Is the tree a tree?
     */
    private boolean isTree;
    /**
     * The locations of the stumps (Some trees are connected to dirt by 4 logs).
     */
    private ArrayList<BlockPos> stumpLocations;
    /**
     * The wood variant (Oak, jungle, dark oak...).
     */
    private BlockPlanks.EnumType variant;

    /**
     * Private constructor of the tree.
     * Used by the equals and createFromNBt method.
     */
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
        if(getStumpLocations().isEmpty())
        {
            fillTreeStumps(world,location.getY());
        }
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

    /**
     * Adds the baseLog of the tree
     */
    public void addBaseLog()
    {
        woodBlocks.add(new BlockPos(location));
    }

    /**
     * Adds a log and searches for further logs(Breadth first search)
     * @param world The world the log is in
     * @param log the log to add
     */
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

    /**
     * Checks if the found log is part of a tree
     * @param world The world the tree is in
     * @param topLog The most upper log of the tree
     */
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
    public List<BlockPos> getStumpLocations()
    {
        return new ArrayList<>(stumpLocations);
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

    /**
     * Overridden equals method checks if the location of the both trees are equal.
     * @param o the object to compare
     * @return true if equal or false if not
     */
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

    /**
     * Needed for the equals method.
     * @return the hash code of the location
     */
    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    /**
     * Writes the tree Object to NBT
     * @param compound the compound of the tree
     */
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

        NBTTagList stumps = new NBTTagList();
        for(BlockPos stump : stumpLocations)
        {
            BlockPosUtil.writeToNBTTagList(stumps, stump);
        }
        compound.setTag(TAG_STUMPS, stumps);


    }

    /**
     * Reads the tree object from NBT
     * @param compound the compound of the tree
     * @return a new tree object
     */
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

        tree.stumpLocations = new ArrayList<>();
        NBTTagList stumps = compound.getTagList(TAG_STUMPS, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < stumps.tagCount(); i++)
        {
            tree.stumpLocations.add(BlockPosUtil.readFromNBTTagList(stumps, i));
        }
        return tree;
    }
}