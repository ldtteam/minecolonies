package com.minecolonies.entity.ai.citizen.lumberjack;

import com.minecolonies.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Max size a tree should have.
     */
    private static final int MAX_TREE_SIZE    = 128;

    /**
     * The location of the tree stump.
     */
    private BlockPos location;

    /**
     * The location of the tree stump.
     */
    private BlockPos topLog;

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
     *
     * @param world The world where the tree is in
     * @param log   the position of the found log.
     */
    public Tree(@NotNull World world, @NotNull BlockPos log)
    {
        Block block = BlockPosUtil.getBlock(world, log);
        if (block.isWood(world, log))
        {
            variant = world.getBlockState(log).getValue(BlockNewLog.VARIANT);
            location = getBaseLog(world, log);
            topLog = getTopLog(world, log);

            woodBlocks = new LinkedList<>();
            addAndSearch(world, location);

            checkTree(world, getTopLog(world, log));
            stumpLocations = new ArrayList<>();
            woodBlocks = new LinkedList<>();
        }
    }

    /**
     * Get's the base log of the tree
     *
     * @param world The entity world
     * @param pos   The coordinates
     * @return the base log position
     */
    private static BlockPos getBaseLog(@NotNull IBlockAccess world, BlockPos pos)
    {
        BlockPos basePos = pos;
        while (world.getBlockState(basePos.down()).getBlock().isWood(world, basePos))
        {
            basePos = basePos.down();
        }
        return basePos;
    }

    /**
     * Checks if the found log is part of a tree
     *
     * @param world  The world the tree is in
     * @param topLog The most upper log of the tree
     */
    private void checkTree(@NotNull World world, @NotNull BlockPos topLog)
    {
        if (!world.getBlockState(new BlockPos(location.getX(), location.getY() - 1, location.getZ())).getBlock().getMaterial().isSolid())
        {
            return;
        }
        int leafCount = 0;
        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    if (world.getBlockState(new BlockPos(topLog.getX() + x, topLog.getY() + y, topLog.getZ() + z)).getBlock().getMaterial().equals(Material.leaves))
                    {
                        leafCount++;
                        if (leafCount >= NUMBER_OF_LEAVES)
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
     * Get's the top log of the tree
     *
     * @param world The entity world
     * @param pos   The coordinates
     * @return the top log position
     */
    private static BlockPos getTopLog(@NotNull IBlockAccess world, BlockPos pos)
    {
        BlockPos topPos = pos;
        while (world.getBlockState(topPos.up()).getBlock().isWood(world, topPos.down()))
        {
            topPos = topPos.up();
        }
        return topPos;
    }

    /**
     * For use in PathJobFindTree
     *
     * @param world the world
     * @param pos   The coordinates
     * @return true if the log is part of a tree
     */
    public static boolean checkTree(@NotNull IBlockAccess world, BlockPos pos)
    {
        //Is the first block a log?
        if (!world.getBlockState(pos).getBlock().isWood(world, pos))
        {
            return false;
        }

        BlockPos[] baseAndTOp = getBottomAndTopLog(world, pos, new LinkedList<>(), null, null);

        if(baseAndTOp == null || baseAndTOp.length < 2)
        {
            return false;
        }

        //Get base log, should already be base log
        BlockPos basePos = baseAndTOp[0];

        //Make sure tree is on solid ground and tree is not build above cobblestone
        return world.getBlockState(basePos.down()).getMaterial().isSolid()
                 && world.getBlockState(basePos.down()).getBlock() != Blocks.COBBLESTONE
                 && hasEnoughLeaves(world, baseAndTOp[1]);
    }

    /**
     * Adds a log and searches for further logs(Breadth first search)
     *
     * @param world The world the log is in
     * @param log   the log to add
     */
    private static BlockPos[] getBottomAndTopLog(@NotNull IBlockAccess world, @NotNull BlockPos log, @NotNull LinkedList<BlockPos> woodenBlocks, BlockPos bottomLog,
            BlockPos topLog)
    {
        if(woodenBlocks.size() >= MAX_TREE_SIZE)
        {
            return null;
        }

        BlockPos bottom = bottomLog == null ? log  : bottomLog;
        BlockPos top = topLog == null ? log : topLog;

        if(log.getY() < bottom.getY())
        {
            bottom = log;
        }

        if(log.getY() > top.getY())
        {
            top = log;
        }

        woodenBlocks.add(log);
        for (int y = -1; y <= 1; y++)
        {
            for (int x = -1; x <= 1; x++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    BlockPos temp = log.add(x, y, z);
                    if (world.getBlockState(temp).getBlock().isWood(null, new BlockPos(0, 0, 0)) && !woodenBlocks.contains(temp))
                    {
                        getBottomAndTopLog(world, temp, woodenBlocks, bottom, top);
                    }
                }
            }
        }

        return new BlockPos[]{bottomLog, topLog};
    }

    private static boolean hasEnoughLeaves(@NotNull IBlockAccess world, BlockPos pos)
    {
        //Get top log
        BlockPos topPos = getTopLog(world, pos);

        int leafCount = 0;
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                for (int dy = -1; dy <= 1; dy++)
                {
                    if (world.getBlockState(topPos.add(dx, dy, dz)).getBlock().getMaterial().equals(Material.leaves))
                    {
                        leafCount++;
                        if (leafCount >= NUMBER_OF_LEAVES)
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
     * Reads the tree object from NBT
     *
     * @param compound the compound of the tree
     * @return a new tree object
     */
    @NotNull
    public static Tree readFromNBT(@NotNull NBTTagCompound compound)
    {
        @NotNull Tree tree = new Tree();
        tree.location = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);

        tree.woodBlocks = new LinkedList<>();
        NBTTagList logs = compound.getTagList(TAG_LOGS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < logs.tagCount(); i++)
        {
            tree.woodBlocks.add(BlockPosUtil.readFromNBTTagList(logs, i));
        }

        tree.stumpLocations = new ArrayList<>();
        NBTTagList stumps = compound.getTagList(TAG_STUMPS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < stumps.tagCount(); i++)
        {
            tree.stumpLocations.add(BlockPosUtil.readFromNBTTagList(stumps, i));
        }
        return tree;
    }

    /**
     * Searches all logs that belong to the tree.
     *
     * @param world The world where the blocks are in
     */
    public void findLogs(@NotNull World world)
    {
        addAndSearch(world, location);
        Collections.sort(woodBlocks, (c1, c2) -> (int) (c1.distanceSq(location) - c2.distanceSq(location)));
        if (getStumpLocations().isEmpty())
        {
            fillTreeStumps(world, location.getY());
        }
    }

    /**
     * Checks if the tree has been planted from more than 1 saplings.
     * Meaning that more than 1 log is on the lowest level.
     *
     * @param world  The world where the tree is in
     * @param yLevel The base y.
     */
    public void fillTreeStumps(@NotNull World world, int yLevel)
    {
        for (@NotNull BlockPos pos : woodBlocks)
        {
            if (pos.getY() == yLevel)
            {
                stumpLocations.add(getBaseLog(world, pos));
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
     *
     * @param world The world the log is in
     * @param log   the log to add
     */
    private void addAndSearch(@NotNull World world, @NotNull BlockPos log)
    {
        if(woodBlocks.size() >= MAX_TREE_SIZE)
        {
            return;
        }

        if(log.getY() < location.getY())
        {
            location = log;
        }

        if(log.getY() > topLog.getY())
        {
            topLog = log;
        }

        woodBlocks.add(log);
        for (int y = -1; y <= 1; y++)
        {
            for (int x = -1; x <= 1; x++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    BlockPos temp = log.add(x, y, z);
                    if (BlockPosUtil.getBlock(world, temp).isWood(null, new BlockPos(0, 0, 0)) && !woodBlocks.contains(temp))
                    {
                        addAndSearch(world, temp);
                    }
                }
            }
        }
    }

    /**
     * Returns the next log block
     *
     * @return the position
     */
    public BlockPos pollNextLog()
    {
        return woodBlocks.poll();
    }

    /**
     * Looks up the next log block
     *
     * @return the position
     */
    public BlockPos peekNextLog()
    {
        return woodBlocks.peek();
    }

    /**
     * Check if the found tree has any logs.
     *
     * @return true if there are wood blocks associated with the tree.
     */
    public boolean hasLogs()
    {
        return !woodBlocks.isEmpty();
    }

    /**
     * All stump positions of a tree (A tree may have been planted with different saplings)
     *
     * @return an Arraylist of the positions
     */
    @NotNull
    public List<BlockPos> getStumpLocations()
    {
        return new ArrayList<>(stumpLocations);
    }

    /**
     * Removes a stump from the stump list.
     *
     * @param pos the position of the stump.
     */
    public void removeStump(BlockPos pos)
    {
        stumpLocations.remove(pos);
    }

    /**
     * Get's the variant of a tree.
     * A tree may only have 1 variant.
     *
     * @return the EnumType variant
     */
    public BlockPlanks.EnumType getVariant()
    {
        return variant;
    }

    /**
     * Calculates the squareDistance to another Tree
     *
     * @param other the other tree
     * @return the square distance in double
     */
    public double squareDistance(@NotNull Tree other)
    {
        return this.getLocation().distanceSq(other.getLocation());
    }

    /**
     * Returns the trees location
     *
     * @return the position
     */
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Needed for the equals method.
     *
     * @return the hash code of the location
     */
    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    /**
     * Overridden equals method checks if the location of the both trees are equal.
     *
     * @param tree the object to compare
     * @return true if equal or false if not
     */
    @Override
    public boolean equals(@Nullable Object tree)
    {
        return tree != null && tree.getClass() == this.getClass() && ((Tree) tree).getLocation().equals(location);
    }

    /**
     * Writes the tree Object to NBT
     *
     * @param compound the compound of the tree
     */
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        if (!isTree)
        {
            return;
        }

        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);

        @NotNull NBTTagList logs = new NBTTagList();
        for (@NotNull BlockPos log : woodBlocks)
        {
            BlockPosUtil.writeToNBTTagList(logs, log);
        }
        compound.setTag(TAG_LOGS, logs);

        @NotNull NBTTagList stumps = new NBTTagList();
        for (@NotNull BlockPos stump : stumpLocations)
        {
            BlockPosUtil.writeToNBTTagList(stumps, stump);
        }
        compound.setTag(TAG_STUMPS, stumps);
    }
}
