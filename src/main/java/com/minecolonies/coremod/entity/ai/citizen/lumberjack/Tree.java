package com.minecolonies.coremod.entity.ai.citizen.lumberjack;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Tag to store if the Tree is a slime tree to NBT.
     */
    private static final String TAG_IS_SLIME_TREE = "isSlimeTree";

    /**
     * Number of leaves necessary for a tree to be recognized.
     */
    private static final int NUMBER_OF_LEAVES = 3;

    /**
     * Number of leaves in every direction from the middle of the tree.
     */
    private static final int LEAVES_WIDTH = 4;

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
     * All leaves of the tree.
     */
    private LinkedList<BlockPos> leaves;

    /**
     * Is the tree a tree?
     */
    private boolean isTree;

    /**
     * The locations of the stumps (Some trees are connected to dirt by 4 logs).
     */
    private ArrayList<BlockPos> stumpLocations;

    /**
     * The wood variant
     * Oak Sapling         :0
     * Spruce Sapling      :1
     * Birch Sapling       :2
     * Jungle Sapling      :3
     * Acacia Sapling      :4
     * Dark Oak Sapling    :5
     *
     * Blue Slime Sapling      :0
     * Purple Slime Sapling    :1
     * Magma Slime Sapling     :2
     */
    private int variantNumber;

    /**
     * If the Tree is a Slime Tree.
     */
    private boolean isSlimeTree = false;

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
        final BlockPos leaf = new BlockPos(log.getX()+1,log.getY()+5,log.getZ());
        if (block.isWood(world, log) || Compatibility.isSlimeBlock(block))
        {
            variantNumber = calcVariantNumber(block, log, leaf, world);
            woodBlocks = new LinkedList<>();
            leaves = new LinkedList<>();
            location = log;
            topLog = log;

            addAndSearch(world, log);
            addAndSearch(world);

            checkTree(world, topLog);
            stumpLocations = new ArrayList<>();
            woodBlocks.clear();
            final Block bottomBlock = world.getBlockState(location).getBlock();
            isSlimeTree = Compatibility.isSlimeBlock(bottomBlock);
        }
    }

    /**
     * Calculate the variant number of the tree.
     * @param block from the block.
     * @param log the position of it.
     * @param leaf the leaf position.
     * @param world the world access.
     * @return the variant representation.
     */
    private static int calcVariantNumber(final Block block, final BlockPos log, final BlockPos leaf, final IBlockAccess world)
    {
        if (block instanceof BlockOldLog)
        {
            return world.getBlockState(log).getValue(BlockOldLog.VARIANT).getMetadata();
        }

        if (block instanceof BlockNewLog)
        {
            return world.getBlockState(log).getValue(BlockNewLog.VARIANT).getMetadata();
        }

        if (Compatibility.isSlimeBlock(block))
        {
            return Compatibility.getLeafVariant(world.getBlockState(leaf));
        }

        return 0;
    }

    /**
     * For use in PathJobFindTree.
     *
     * @param world the world.
     * @param pos   The coordinates.
     * @param treesToCut the trees the lumberjack is supposed to cut.
     * @return true if the log is part of a tree.
     */
    public static boolean checkTree(@NotNull final IBlockAccess world, final BlockPos pos, final Map<ItemStorage, Boolean> treesToCut)
    {
        //Is the first block a log?
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (!block.isWood(world, pos) && !Compatibility.isSlimeBlock(block))
        {
            return false;
        }

        final Tuple<BlockPos, BlockPos> baseAndTOp = getBottomAndTopLog(world, pos, new LinkedList<>(), null, null);

        //Get base log, should already be base log.
        final BlockPos basePos = baseAndTOp.getFirst();

        //Make sure tree is on solid ground and tree is not build above cobblestone.
        return world.getBlockState(basePos.down()).getMaterial().isSolid()
                 && world.getBlockState(basePos.down()).getBlock() != Blocks.COBBLESTONE
                 && hasEnoughLeavesAndIsSupposedToCut(world, baseAndTOp.getSecond(), treesToCut);
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
                    final Block block = world.getBlockState(temp).getBlock();
                    if ((block.isWood(null, temp) || Compatibility.isSlimeBlock(block)) && !woodenBlocks.contains(temp))
                    {
                        return getBottomAndTopLog(world, temp, woodenBlocks, bottom, top);
                    }
                }
            }
        }

        return new Tuple<>(bottom, top);
    }

    /**
     * Check if the tree has enough leaves and the lj is supposed to cut them.
     * @param world the world it is in.
     * @param pos the position.
     * @param treesToCut the trees the lj is supposed to cut.
     * @return true if so.
     */
    private static boolean hasEnoughLeavesAndIsSupposedToCut(@NotNull final IBlockAccess world, final BlockPos pos, final Map<ItemStorage, Boolean> treesToCut)
    {
        boolean checkedLeaves = false;
        int leafCount = 0;
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                for (int dy = -1; dy <= 1; dy++)
                {
                    final BlockPos leafPos = pos.add(dx, dy, dz);
                    if (world.getBlockState(leafPos).getMaterial().equals(Material.LEAVES))
                    {
                        if(!checkedLeaves && !supposedToCut(world, pos, treesToCut, leafPos))
                        {
                            return false;
                        }
                        checkedLeaves = true;

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
     * Check if the Lj is supposed to cut a tree.
     * @param world the world it is in.
     * @param pos the position a leaf is at.
     * @param treesToCut the trees he is supposed to cut.
     * @param leafPos
     * @return false if not.
     */
    private static boolean supposedToCut(final IBlockAccess world, final BlockPos pos, final Map<ItemStorage, Boolean> treesToCut, final BlockPos leafPos)
    {
        final IBlockState state = world.getBlockState(pos);

        for(final ItemStorage stack: treesToCut.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey).collect(Collectors.toList()))
        {
            final int variantNumber = calcVariantNumber(state.getBlock(), pos, leafPos, world);

            if (Compatibility.isSlimeLeaf(world.getBlockState(leafPos).getBlock()))
            {
                if(Compatibility.isSlimeSapling(((ItemBlock) stack.getItem()).getBlock()) && variantNumber == stack.getItemStack().getMetadata())
                {
                    return false;
                }
            }
            else if(!Compatibility.isSlimeSapling(((ItemBlock) stack.getItem()).getBlock()) && variantNumber == stack.getItemStack().getMetadata())
            {
                return false;
            }
        }

        return true;
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

        tree.isSlimeTree = compound.getBoolean(TAG_IS_SLIME_TREE);

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
        woodBlocks.sort((c1, c2) -> (int) (c1.distanceSq(location) - c2.distanceSq(location)));
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
                    final Block block = BlockPosUtil.getBlock(world, temp);
                    if ((block.isWood(null, temp) || Compatibility.isSlimeBlock(block)) && !woodBlocks.contains(temp))
                    {
                        addAndSearch(world, temp);
                    }
                }
            }
        }
    }

    /**
     * Adds a leaf and searches for further leaves.
     *
     * @param world The world the leaf is in.
     */
    private void addAndSearch(@NotNull final World world)
    {
        int locXMin = location.getX() - LEAVES_WIDTH;
        int locXMax = location.getX() + LEAVES_WIDTH;
        final int locYMin = location.getY() + 2;
        int locZMin = location.getZ() - LEAVES_WIDTH;
        int locZMax = location.getZ() + LEAVES_WIDTH;
        int temp;
        if (locXMin > locXMax)
        {
            temp = locXMax;
            locXMax = locXMin;
            locXMin = temp;
        }
        if (locZMin > locZMax)
        {
            temp = locZMax;
            locZMax = locZMin;
            locZMin = temp;
        }
        for (int locX = locXMin; locX <= locXMax; locX++)
        {
            for (int locY = locYMin; locY <= MAX_TREE_SIZE; locY++)
            {
                for (int locZ = locZMin; locZ <= locZMax; locZ++)
                {
                    final BlockPos leaf = new BlockPos(locX, locY, locZ);
                    if (world.getBlockState(leaf).getMaterial() == Material.LEAVES)
                    {
                        leaves.add(leaf);
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
     * Returns the next leaf block.
     *
     * @return the position.
     */
    public BlockPos pollNextLeaf()
    {
        return leaves.pollLast();
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
     * Looks up the next leaf block.
     *
     * @return the position.
     */
    public BlockPos peekNextLeaf()
    {
        return leaves.peekLast();
    }

    /**
     * Check if the found tree has any leaves.
     *
     * @return true if there are leaves associated with the tree.
     */
    public boolean hasLeaves()
    {
        return !leaves.isEmpty();
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
     * @return if tree is slime tree.
     */
    public boolean isSlimeTree()
    {
        return isSlimeTree;
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
    public int getVariant()
    {
        return variantNumber;
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

        compound.setBoolean(TAG_IS_SLIME_TREE, isSlimeTree);
    }
}
