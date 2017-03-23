package com.minecolonies.coremod.entity.ai.citizen.lumberjack;

import com.minecolonies.coremod.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
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
     * Tag to store the topLog to NBT.
     */
    private static final String TAG_TOP_LOG = "topLog";

    /**
     * Number of leaves necessary for a tree to be recognized.
     */
    private static final int NUMBER_OF_LEAVES = 3;

    /**
     * Max size a tree should have.
     */
    private static final int MAX_TREE_SIZE = 256;

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
     * Creates a new tree Object for the lumberjack.
     * Since the same type of variant of the block old log or new log do not match we have to separate them.
     *
     * @param world The world where the tree is in.
     * @param log   the position of the found log.
     */
    public Tree(@NotNull final World world, @NotNull final BlockPos log)
    {
        final Block block = BlockPosUtil.getBlock(world, log);
        if (block.isWood(world, log))
        {
            if (block instanceof BlockOldLog)
            {
                variant = world.getBlockState(log).getValue(BlockOldLog.VARIANT);
            }
            else if (block instanceof BlockNewLog)
            {
                variant = world.getBlockState(log).getValue(BlockNewLog.VARIANT);
            }
            else
            {
                variant = BlockPlanks.EnumType.OAK;
            }

            woodBlocks = new LinkedList<>();
            location = log;
            topLog = log;

            addAndSearch(world, log);

            checkTree(world, topLog);
            stumpLocations = new ArrayList<>();
            woodBlocks.clear();
        }
    }

    /**
     * For use in PathJobFindTree.
     *
     * @param world the world.
     * @param pos   The coordinates.
     * @return true if the log is part of a tree.
     */
    public static boolean checkTree(@NotNull final IBlockAccess world, final BlockPos pos)
    {
        //Is the first block a log?
        if (!world.getBlockState(pos).getBlock().isWood(world, pos))
        {
            return false;
        }

        final Tuple<BlockPos, BlockPos> baseAndTOp = getBottomAndTopLog(world, pos, new LinkedList<>(), null, null);

        //Get base log, should already be base log.
        final BlockPos basePos = baseAndTOp.getFirst();

        //Make sure tree is on solid ground and tree is not build above cobblestone.
        return world.getBlockState(basePos.down()).getMaterial().isSolid()
                 && world.getBlockState(basePos.down()).getBlock() != Blocks.COBBLESTONE
                 && hasEnoughLeaves(world, baseAndTOp.getSecond());
    }

    /**
     * Adds a log and searches for further logs(Breadth first search).
     *
     * @param world The world the log is in.
     * @param log   the log to add.
     * @return a tuple containing, first: bottom log and second: top log.
     */
    @NotNull
    private static Tuple<BlockPos, BlockPos> getBottomAndTopLog(
                                                                 @NotNull final IBlockAccess world,
                                                                 @NotNull final BlockPos log,
                                                                 @NotNull final LinkedList<BlockPos> woodenBlocks,
                                                                 final BlockPos bottomLog,
                                                                 final BlockPos topLog)
    {
        BlockPos bottom = bottomLog == null ? log : bottomLog;
        BlockPos top = topLog == null ? log : topLog;

        if (woodenBlocks.size() >= MAX_TREE_SIZE)
        {
            return new Tuple<>(bottom, top);
        }

        if (log.getY() < bottom.getY())
        {
            bottom = log;
        }

        if (log.getY() > top.getY())
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
                    final BlockPos temp = log.add(x, y, z);
                    if (world.getBlockState(temp).getBlock().isWood(null, temp) && !woodenBlocks.contains(temp))
                    {
                        return getBottomAndTopLog(world, temp, woodenBlocks, bottom, top);
                    }
                }
            }
        }

        return new Tuple<>(bottom, top);
    }

    private static boolean hasEnoughLeaves(@NotNull final IBlockAccess world, final BlockPos pos)
    {
        int leafCount = 0;
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                for (int dy = -1; dy <= 1; dy++)
                {
                    if (world.getBlockState(pos.add(dx, dy, dz)).getMaterial().equals(Material.LEAVES))
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
     * Reads the tree object from NBT.
     *
     * @param compound the compound of the tree.
     * @return a new tree object.
     */
    @NotNull
    public static Tree readFromNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final Tree tree = new Tree();
        tree.location = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);

        tree.woodBlocks = new LinkedList<>();
        final NBTTagList logs = compound.getTagList(TAG_LOGS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < logs.tagCount(); i++)
        {
            tree.woodBlocks.add(BlockPosUtil.readFromNBTTagList(logs, i));
        }

        tree.stumpLocations = new ArrayList<>();
        final NBTTagList stumps = compound.getTagList(TAG_STUMPS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < stumps.tagCount(); i++)
        {
            tree.stumpLocations.add(BlockPosUtil.readFromNBTTagList(stumps, i));
        }

        tree.topLog = BlockPosUtil.readFromNBT(compound, TAG_TOP_LOG);

        return tree;
    }

    /**
     * Checks if the found log is part of a tree.
     *
     * @param world  The world the tree is in.
     * @param topLog The most upper log of the tree.
     */
    private void checkTree(@NotNull final World world, @NotNull final BlockPos topLog)
    {
        if (!world.getBlockState(new BlockPos(location.getX(), location.getY() - 1, location.getZ())).getMaterial().isSolid())
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
                    if (world.getBlockState(new BlockPos(topLog.getX() + x, topLog.getY() + y, topLog.getZ() + z)).getMaterial().equals(Material.LEAVES))
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
     * Searches all logs that belong to the tree.
     *
     * @param world The world where the blocks are in.
     */
    public void findLogs(@NotNull final World world)
    {
        addAndSearch(world, location);
        Collections.sort(woodBlocks, (c1, c2) -> (int) (c1.distanceSq(location) - c2.distanceSq(location)));
        if (getStumpLocations().isEmpty())
        {
            fillTreeStumps(location.getY());
        }
    }

    /**
     * Checks if the tree has been planted from more than 1 saplings.
     * Meaning that more than 1 log is on the lowest level.
     *
     * @param yLevel The base y.
     */
    public void fillTreeStumps(final int yLevel)
    {
        for (@NotNull final BlockPos pos : woodBlocks)
        {
            if (pos.getY() == yLevel)
            {
                stumpLocations.add(pos);
            }
        }
    }

    /**
     * Adds a log and searches for further logs(Breadth first search).
     *
     * @param world The world the log is in.
     * @param log   the log to add.
     */
    private void addAndSearch(@NotNull final World world, @NotNull final BlockPos log)
    {
        if (woodBlocks.size() >= MAX_TREE_SIZE)
        {
            return;
        }

        if (log.getY() < location.getY())
        {
            location = log;
        }

        if (log.getY() > topLog.getY())
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
                    final BlockPos temp = log.add(x, y, z);
                    if (BlockPosUtil.getBlock(world, temp).isWood(null, temp) && !woodBlocks.contains(temp))
                    {
                        addAndSearch(world, temp);
                    }
                }
            }
        }
    }

    /**
     * Returns the next log block.
     *
     * @return the position.
     */
    public BlockPos pollNextLog()
    {
        return woodBlocks.pollLast();
    }

    /**
     * Looks up the next log block.
     *
     * @return the position.
     */
    public BlockPos peekNextLog()
    {
        return woodBlocks.peekLast();
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
     * All stump positions of a tree (A tree may have been planted with different saplings).
     *
     * @return an Arraylist of the positions.
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
    public void removeStump(final BlockPos pos)
    {
        stumpLocations.remove(pos);
    }

    /**
     * Get's the variant of a tree.
     * A tree may only have 1 variant.
     *
     * @return the EnumType variant.
     */
    public BlockPlanks.EnumType getVariant()
    {
        return variant;
    }

    /**
     * Calculates the squareDistance to another Tree.
     *
     * @param other the other tree.
     * @return the square distance in double.
     */
    public double squareDistance(@NotNull final Tree other)
    {
        return this.getLocation().distanceSq(other.getLocation());
    }

    /**
     * Returns the trees location.
     *
     * @return the position.
     */
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Needed for the equals method.
     *
     * @return the hash code of the location.
     */
    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    /**
     * Overridden equals method checks if the location of the both trees are equal.
     *
     * @param tree the object to compare.
     * @return true if equal or false if not.
     */
    @Override
    public boolean equals(@Nullable final Object tree)
    {
        return tree != null && tree.getClass() == this.getClass() && ((Tree) tree).getLocation().equals(location);
    }

    /**
     * Writes the tree Object to NBT.
     *
     * @param compound the compound of the tree.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        if (!isTree)
        {
            return;
        }

        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);

        @NotNull final NBTTagList logs = new NBTTagList();
        for (@NotNull final BlockPos log : woodBlocks)
        {
            BlockPosUtil.writeToNBTTagList(logs, log);
        }
        compound.setTag(TAG_LOGS, logs);

        @NotNull final NBTTagList stumps = new NBTTagList();
        for (@NotNull final BlockPos stump : stumpLocations)
        {
            BlockPosUtil.writeToNBTTagList(stumps, stump);
        }
        compound.setTag(TAG_STUMPS, stumps);

        BlockPosUtil.writeToNBT(compound, TAG_TOP_LOG, topLog);
    }
}
