package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutMiner;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The miners building.
 */
public class BuildingMiner extends AbstractBuildingWorker
{
    /**
     * Amount of items to be kept.
     */
    private static final int STACK_MAX_SIZE     = 64;
    /**
     * The NBT Tag to store the floorBlock.
     */
    private static final String TAG_FLOOR_BLOCK    = "floorBlock";
    /**
     * The NBT Tag to store the fenceBlock.
     */
    private static final String TAG_FENCE_BLOCK    = "fenceBlock";
    /**
     * The NBT Tag to store the starting level of the shaft.
     */
    private static final String TAG_STARTING_LEVEL = "startingLevelShaft";
    /**
     * The NBT Tag to store list of levels.
     */
    private static final String TAG_LEVELS         = "levels";
    /**
     * The NBT Tag to store if the shaft has been cleared.
     */
    private static final String TAG_CLEARED        = "clearedShaft";
    /**
     * The NBT Tag to store the location of the shaft.
     */
    private static final String TAG_SLOCATION      = "shaftLocation";
    /**
     * The NBT Tag to store the vector-x of the shaft.
     */
    private static final String TAG_VECTORX        = "vectorx";
    /**
     * The NBT Tag to store the vector-z of the shaft.
     */
    private static final String TAG_VECTORZ        = "vectorz";
    /**
     * The NBT Tag to store the location of the cobblestone at the shaft.
     */
    private static final String TAG_CLOCATION      = "cobblelocation";
    /**
     * The NBT Tag to store the active node the miner is working on.
     */
    private static final String TAG_ACTIVE         = "activeNodeint";
    /**
     * The NBT Tag to store the current level the miner is working in.
     */
    private static final String TAG_CURRENT_LEVEL  = "currentLevel";
    /**
     * The NBT Tag to store the starting node.
     */
    private static final String TAG_SN             = "StartingNode";
    /**
     * The NBT Tag to store the location of the ladder.
     */
    private static final String TAG_LLOCATION      = "ladderlocation";
    /**
     * The NBT Tag to store if a ladder has been found yet.
     */
    private static final String TAG_LADDER         = "found_ladder";

    private static final String TAG_SHAFT_BLOCK = "shaftBlock";

    /**
     * The maximum upgrade of the building.
     */
    private static final int         MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String      MINER              = "Miner";
    /**
     * Defines the material used for the floor of the shaft.
     */
    private static final IBlockState floorBlock         = Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);

    /**
     * Max depth the miner reaches at level 0.
     */
    private static final int MAX_DEPTH_LEVEL_0 = 70;

    /**
     * Max depth the miner reaches at level 1.
     */
    private static final int MAX_DEPTH_LEVEL_1 = 50;

    /**
     * Max depth the miner reaches at level 2.
     */
    private static final int MAX_DEPTH_LEVEL_2 = 30;

    /**
     * Max depth the miner reaches at level 3.
     */
    private static final int MAX_DEPTH_LEVEL_3 = 5;
    /**
     * Stores the levels of the miners mine. This could be a map with (depth,level).
     */
    @NotNull
    private final List<Level> levels      = new ArrayList<>();
    /**
     * True if shaft is at bottom limit.
     */
    public  boolean clearedShaft       = false;
    /**
     * Defines the material used for the structure of the horizontal shaft.
     */
    private Block   shaftBlock         = Blocks.PLANKS;
    /**
     * Defines the material used for the fence of the shaft.
     */
    private Block   fenceBlock         = Blocks.OAK_FENCE;
    /**
     * Here we can detect multiples of 5.
     */
    private int     startingLevelShaft = 0;
    /**
     * The location of the topmost cobblestone the ladder starts at.
     */
    private BlockPos cobbleLocation;
    /**
     * The starting level of the node.
     */
    private int startingLevelNode = 0;
    /**
     * The id of the active node.
     */
    private int active            = 0;
    /**
     * The number of the current level.
     */
    private int currentLevel      = 0;
    /**
     * The position of the start of the shaft.
     */
    private BlockPos shaftStart;
    /**
     * Ladder orientation in x.
     */
    private int vectorX = 1;
    /**
     * Ladder orientation in y.
     */
    private int vectorZ = 1;
    /**
     * The location of the topmost ladder in the shaft.
     */
    private BlockPos ladderLocation;
    /**
     * True if a ladder is found.
     */
    private       boolean     foundLadder = false;

    private final Map<ItemStorage, Integer> keepX = new HashMap<>();

    /**
     * Required constructor.
     *
     * @param c colony containing the building.
     * @param l location of the building.
     */
    public BuildingMiner(final Colony c, final BlockPos l)
    {
        super(c, l);

        final ItemStack stackLadder = new ItemStack(Blocks.LADDER);
        final ItemStack stackFence = new ItemStack(Blocks.OAK_FENCE);
        final ItemStack stackTorch = new ItemStack(Blocks.TORCH);
        final ItemStack stackCobble = new ItemStack(Blocks.COBBLESTONE);
        final ItemStack stackSlab = new ItemStack(Blocks.WOODEN_SLAB);
        final ItemStack stackPlanks = new ItemStack(Blocks.PLANKS);

        keepX.put(new ItemStorage(stackLadder, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackFence, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackTorch, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackCobble, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackSlab, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackPlanks, false), STACK_MAX_SIZE);
    }

    /**
     * Getter of the structure name.
     *
     * @return the structure name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return MINER;
    }

    /**
     * Getter of the max building level.
     *
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingMiner);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeMinerMax);
        }
    }

    /**
     * Getter of the job description.
     *
     * @return the description of the miners job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return MINER;
    }

    /**
     * Create the job for the miner.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobMiner(citizen);
    }

    @Override
    public boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return ItemStackUtils.hasToolLevel(stack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel())
            || ItemStackUtils.hasToolLevel(stack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel())
            || ItemStackUtils.hasToolLevel(stack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel());
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    public Map<ItemStorage, Integer> getRequiredItemsAndAmount()
    {
        return keepX;
    }

    /**
     * Reads the information from NBT from permanent storage.
     *
     * @param compound the compound key.
     */
    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_FENCE_BLOCK))
        {
            fenceBlock = Block.getBlockFromName(compound.getString(TAG_FENCE_BLOCK));
        }
        if (compound.hasKey(TAG_SHAFT_BLOCK))
        {
            shaftBlock = Block.getBlockFromName(compound.getString(TAG_SHAFT_BLOCK));
        }

        startingLevelShaft = compound.getInteger(TAG_STARTING_LEVEL);
        clearedShaft = compound.getBoolean(TAG_CLEARED);

        vectorX = compound.getInteger(TAG_VECTORX);
        vectorZ = compound.getInteger(TAG_VECTORZ);

        active = compound.getInteger(TAG_ACTIVE);
        currentLevel = compound.getInteger(TAG_CURRENT_LEVEL);

        ladderLocation = BlockPosUtil.readFromNBT(compound, TAG_LLOCATION);

        foundLadder = compound.getBoolean(TAG_LADDER);

        shaftStart = BlockPosUtil.readFromNBT(compound, TAG_SLOCATION);
        cobbleLocation = BlockPosUtil.readFromNBT(compound, TAG_CLOCATION);

        startingLevelNode = compound.getInteger(TAG_SN);

        final NBTTagList levelTagList = compound.getTagList(TAG_LEVELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.tagCount(); i++)
        {
            this.levels.add(new Level(levelTagList.getCompoundTagAt(i)));
        }
    }

    /**
     * Writes the information to NBT to store it permanently.
     *
     * @param compound the compound key.
     */
    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setString(TAG_FENCE_BLOCK, Block.REGISTRY.getNameForObject(fenceBlock).toString());
        compound.setString(TAG_FLOOR_BLOCK, Block.REGISTRY.getNameForObject(shaftBlock).toString());

        compound.setInteger(TAG_STARTING_LEVEL, startingLevelShaft);
        compound.setBoolean(TAG_CLEARED, clearedShaft);
        compound.setInteger(TAG_VECTORX, vectorX);
        compound.setInteger(TAG_VECTORZ, vectorZ);
        compound.setInteger(TAG_ACTIVE, active);
        compound.setInteger(TAG_CURRENT_LEVEL, currentLevel);
        compound.setBoolean(TAG_LADDER, foundLadder);
        compound.setInteger(TAG_SN, startingLevelNode);

        if (shaftStart != null && cobbleLocation != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_SLOCATION, shaftStart);
            BlockPosUtil.writeToNBT(compound, TAG_CLOCATION, cobbleLocation);
        }

        if (ladderLocation != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_LLOCATION, ladderLocation);
        }

        @NotNull final NBTTagList levelTagList = new NBTTagList();
        for (@NotNull final Level level : levels)
        {
            @NotNull final NBTTagCompound levelCompound = new NBTTagCompound();
            level.writeToNBT(levelCompound);
            levelTagList.appendTag(levelCompound);
        }
        compound.setTag(TAG_LEVELS, levelTagList);
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(currentLevel);
        buf.writeInt(levels.size());

        for (@NotNull final Level level : levels)
        {
            buf.writeInt(level.getNumberOfNodes());
        }
    }

    /**
     * Adds a level to the levels list.
     *
     * @param currentLevel {@link Level}to add.
     */
    public void addLevel(final Level currentLevel)
    {
        levels.add(currentLevel);
    }

    /**
     * The number of levels in the mine.
     *
     * @return levels size.
     */
    public int getNumberOfLevels()
    {
        return levels.size();
    }

    /**
     * Returns the current level.
     *
     * @return Current level.
     */
    @Nullable
    public Level getCurrentLevel()
    {
        if (currentLevel >= 0 && currentLevel < levels.size())
        {
            return levels.get(currentLevel);
        }
        return null;
    }

    /**
     * Sets the current level the miner is at.
     *
     * @param currentLevel the level to set.
     */
    public void setCurrentLevel(final int currentLevel)
    {
        this.currentLevel = currentLevel;
    }

    /**
     * Returns the depth limit.
     * Limitted by building level.
     * <pre>
     * - Level 1: 50
     * - Level 2: 30
     * - Level 3: 5
     * </pre>
     *
     * @return Depth limit.
     */
    public int getDepthLimit()
    {
        if (this.getBuildingLevel() == 1)
        {
            return MAX_DEPTH_LEVEL_1;
        }
        else if (this.getBuildingLevel() == 2)
        {
            return MAX_DEPTH_LEVEL_2;
        }
        else if (this.getBuildingLevel() >= 3)
        {
            return MAX_DEPTH_LEVEL_3;
        }

        return MAX_DEPTH_LEVEL_0;
    }

    /**
     * Getter of the ladderLocation.
     *
     * @return the ladder location.
     */
    public BlockPos getLadderLocation()
    {
        return ladderLocation;
    }

    /**
     * Setter of the ladder location.
     *
     * @param ladderLocation the new ladder location.
     */
    public void setLadderLocation(final BlockPos ladderLocation)
    {
        this.ladderLocation = ladderLocation;
    }

    /**
     * Checks if a ladder has been found already.
     *
     * @return true if so.
     */
    public boolean hasFoundLadder()
    {
        return foundLadder;
    }

    /**
     * Setter for the foundLadder.
     *
     * @param foundLadder the boolean.
     */
    public void setFoundLadder(final boolean foundLadder)
    {
        this.foundLadder = foundLadder;
    }

    /**
     * Getter of the X-vector.
     *
     * @return the vectorX.
     */
    public int getVectorX()
    {
        return vectorX;
    }

    /**
     * Setter of the X-vector.
     *
     * @param vectorX the vector to set +1 or -1.
     */
    public void setVectorX(final int vectorX)
    {
        this.vectorX = vectorX;
    }

    /**
     * Getter of the Z-vector.
     *
     * @return the vectorZ.
     */
    public int getVectorZ()
    {
        return vectorZ;
    }

    /**
     * Setter of the Z-vector.
     *
     * @param vectorZ the vector to set +1 or -1.
     */
    public void setVectorZ(final int vectorZ)
    {
        this.vectorZ = vectorZ;
    }

    /**
     * Getter of the cobbleLocation.
     *
     * @return the location.
     */
    public BlockPos getCobbleLocation()
    {
        return cobbleLocation;
    }

    /**
     * Setter for the cobbleLocation.
     *
     * @param pos the location to set.
     */
    public void setCobbleLocation(final BlockPos pos)
    {
        this.cobbleLocation = pos;
    }

    /**
     * Setter of the shaftStart.
     *
     * @param pos the location.
     */
    public void setShaftStart(final BlockPos pos)
    {
        this.shaftStart = pos;
    }

    /**
     * Getter of the starting level of the shaft.
     *
     * @return the start level.
     */
    public int getStartingLevelShaft()
    {
        return startingLevelShaft;
    }

    /**
     * Resets the starting level of the shaft to 0.
     */
    public void resetStartingLevelShaft()
    {
        this.startingLevelShaft = 0;
    }

    /**
     * Increments the starting level of the shaft by one.
     */
    public void incrementStartingLevelShaft()
    {
        this.startingLevelShaft++;
    }

    /**
     * Getter of the floor block.
     *
     * @return the material of the floor block.
     */
    public IBlockState getFloorBlock()
    {
        return floorBlock;
    }

    /**
     * Getter of the floor block.
     *
     * @return the material of the floor block.
     */
    public Block getShaftBlock()
    {
        return shaftBlock;
    }

    /**
     * Getter of the fence block.
     *
     * @return the material of the fence block.
     */
    public Block getFenceBlock()
    {
        return fenceBlock;
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The different miner levels the miner already has.
         */
        public int[] levels;
        /**
         * The level the miner currently works on.
         */
        public int   current;

        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutMiner(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            current = buf.readInt();
            final int size = buf.readInt();
            levels = new int[size];

            for (int i = 0; i < size; i++)
            {
                levels[i] = buf.readInt();
            }
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.STRENGTH;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.ENDURANCE;
        }
    }
}
