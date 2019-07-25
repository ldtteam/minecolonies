package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutMiner;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.citizen.miner.Node;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.BuildingConstants.*;
import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The miners building.
 */
public class BuildingMiner extends AbstractBuildingStructureBuilder
{
    /**
     * The job description.
     */
    private static final String MINER = "Miner";

    /**
     * Stores the levels of the miners mine. This could be a map with (depth,level).
     */
    @NotNull
    private final List<Level> levels = new ArrayList<>();

    /**
     * True if shaft is at bottom limit.
     */
    private boolean clearedShaft = false;

    /**
     * Here we can detect multiples of 5.
     */
    private int startingLevelShaft = 0;

    /**
     * The location of the topmost cobblestone the ladder starts at.
     */
    private BlockPos cobbleLocation;

    /**
     * The starting level of the node.
     */
    private int startingLevelNode = 0;

    /**
     * The number of the current level.
     */
    private int currentLevel = 0;

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
    private boolean foundLadder = false;

    /**
     * The id of the activeNode node.
     */
    @Nullable
    private Node activeNode = null;

    /**
     * The id of the old node.
     */
    @Nullable
    private Node oldNode = null;

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
        final ItemStack stackDirt = new ItemStack(Blocks.DIRT);

        keepX.put(stackLadder::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(stackFence::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(stackTorch::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(stackCobble::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(stackSlab::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(stackPlanks::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(stackDirt::isItemEqual, new Tuple<>(STACKSIZE, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
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

        if (newLevel == NUM_ACHIEVEMENT_FIRST)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingMiner);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeMinerMax);
        }
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

    /**
     * Reads the information from NBT from permanent storage.
     *
     * @param compound the compound key.
     */
    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);

        startingLevelShaft = compound.getInt(TAG_STARTING_LEVEL);
        clearedShaft = compound.getBoolean(TAG_CLEARED);

        vectorX = compound.getInt(TAG_VECTORX);
        vectorZ = compound.getInt(TAG_VECTORZ);

        if (compound.keySet().contains(TAG_ACTIVE))
        {
            activeNode = Node.createFromNBT(compound.getCompound(TAG_ACTIVE));
        }
        else if(compound.keySet().contains(TAG_OLD))
        {
            oldNode = Node.createFromNBT(compound.getCompound(TAG_OLD));
        }

        currentLevel = compound.getInt(TAG_CURRENT_LEVEL);

        ladderLocation = BlockPosUtil.readFromNBT(compound, TAG_LLOCATION);

        foundLadder = compound.getBoolean(TAG_LADDER);

        shaftStart = BlockPosUtil.readFromNBT(compound, TAG_SLOCATION);
        cobbleLocation = BlockPosUtil.readFromNBT(compound, TAG_CLOCATION);

        startingLevelNode = compound.getInt(TAG_SN);

        final ListNBT levelTagList = compound.getList(TAG_LEVELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.size(); i++)
        {
            this.levels.add(new Level(levelTagList.getCompound(i)));
        }
    }

    /**
     * Writes the information to NBT to store it permanently.
     *
     * @param compound the compound key.
     */
    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        super.writeToNBT(compound);

        compound.putInt(TAG_STARTING_LEVEL, startingLevelShaft);
        compound.putBoolean(TAG_CLEARED, clearedShaft);
        compound.putInt(TAG_VECTORX, vectorX);
        compound.putInt(TAG_VECTORZ, vectorZ);
        if (activeNode != null)
        {
            final CompoundNBT nodeCompound = new CompoundNBT();
            activeNode.writeToNBT(nodeCompound);
            compound.put(TAG_ACTIVE, nodeCompound);
        }

        if (oldNode != null)
        {
            final CompoundNBT nodeCompound = new CompoundNBT();
            oldNode.writeToNBT(new CompoundNBT());
            compound.put(TAG_OLD, nodeCompound);
        }
        compound.putInt(TAG_CURRENT_LEVEL, currentLevel);
        compound.putBoolean(TAG_LADDER, foundLadder);
        compound.putInt(TAG_SN, startingLevelNode);

        if (shaftStart != null && cobbleLocation != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_SLOCATION, shaftStart);
            BlockPosUtil.writeToNBT(compound, TAG_CLOCATION, cobbleLocation);
        }

        if (ladderLocation != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_LLOCATION, ladderLocation);
        }

        @NotNull final ListNBT levelTagList = new ListNBT();
        for (@NotNull final Level level : levels)
        {
            @NotNull final CompoundNBT levelCompound = new CompoundNBT();
            level.writeToNBT(levelCompound);
            levelTagList.add(levelCompound);
        }
        compound.put(TAG_LEVELS, levelTagList);
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
            buf.writeInt(level.getNumberOfBuiltNodes());
            buf.writeInt(level.getDepth());
        }
    }

    /**
     * Adds a level to the levels list.
     *
     * @param currentLevel {@link Level} to add.
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
     * Find given level in the levels array.
     *
     * @return position in the levels array.
     */
    public int getLevelId(final Level level)
    {
        return levels.indexOf(level);
    }

    /**
     * Sets the current level the miner is at.
     *
     * @param currentLevel the level to set.
     */
    public void setCurrentLevel(final int currentLevel)
    {
        this.currentLevel = currentLevel;
        this.activeNode = null;
        this.oldNode = null;
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
        this.startingLevelShaft = 1;
    }

    /**
     * Increments the starting level of the shaft by one.
     */
    public void incrementStartingLevelShaft()
    {
        this.startingLevelShaft++;
    }

    /**
     * Getter to check if the shaft has been cleared.
     * @return true if so.
     */
    public boolean hasClearedShaft()
    {
        return clearedShaft;
    }

    /**
     * Setter if the shaft has been cleared.
     * @param clearedShaft true if so.
     */
    public void setClearedShaft(final boolean clearedShaft)
    {
        this.clearedShaft = clearedShaft;
    }

    /**
     * Getter for the active node.
     * @return the int id of the active node.
     */
    @NotNull
    public Node getActiveNode()
    {
        return activeNode == null || activeNode.getStatus() == Node.NodeStatus.COMPLETED ? levels.get(currentLevel).getRandomNode(oldNode) : activeNode;
    }

    /**
     * Setter for the active node.
     * @param activeNode the int id of the active node.
     */
    public void setActiveNode(final Node activeNode)
    {
        this.activeNode = activeNode;
    }

    /**
     * Getter for the old node.
     * @return the int id of the old node.
     */
    public Node getOldNode()
    {
        return oldNode;
    }

    /**
     * Setter for the old node.
     * @param oldNode the int id of the old node.
     */
    public void setOldNode(final Node oldNode)
    {
        this.oldNode = oldNode;
    }

    @Override
    public void searchWorkOrder()
    {
        final CitizenData citizen = getMainCitizen();
        if (citizen == null)
        {
            return;
        }

        final List<WorkOrderBuildMiner> list = getColony().getWorkManager().getOrderedList(WorkOrderBuildMiner.class, getLocation());

        for (final WorkOrderBuildMiner wo : list)
        {
            if (this.getID().equals(wo.getMinerBuilding()))
            {
                citizen.getJob(JobMiner.class).setWorkOrder(wo);
                wo.setClaimedBy(citizen);
                return;
            }
        }
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingBuilderView
    {
        /**
         * The tuple of number of nodes and y depth per all levels.
         */
        public List<Tuple<Integer, Integer>> levelsInfo;
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

            levelsInfo = new ArrayList<Tuple<Integer, Integer>>(size);
            for (int i = 0; i < size; i++)
            {
                levelsInfo.add(i, new Tuple<>(buf.readInt(), buf.readInt()));
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
