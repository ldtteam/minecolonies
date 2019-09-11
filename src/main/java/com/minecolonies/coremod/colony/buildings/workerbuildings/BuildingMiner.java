package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutMiner;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.citizen.miner.Node;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
    public BuildingMiner(final IColony c, final BlockPos l)
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

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.miner;
    }

    /**
     * Create the job for the miner.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobMiner(citizen);
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

        startingLevelShaft = compound.getInteger(TAG_STARTING_LEVEL);
        clearedShaft = compound.getBoolean(TAG_CLEARED);

        vectorX = compound.getInteger(TAG_VECTORX);
        vectorZ = compound.getInteger(TAG_VECTORZ);

        if (compound.hasKey(TAG_ACTIVE))
        {
            activeNode = Node.createFromNBT(compound.getCompoundTag(TAG_ACTIVE));
        }
        else if(compound.hasKey(TAG_OLD))
        {
            oldNode = Node.createFromNBT(compound.getCompoundTag(TAG_OLD));
        }

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

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        compound.setInteger(TAG_STARTING_LEVEL, startingLevelShaft);
        compound.setBoolean(TAG_CLEARED, clearedShaft);
        compound.setInteger(TAG_VECTORX, vectorX);
        compound.setInteger(TAG_VECTORZ, vectorZ);
        if (activeNode != null)
        {
            final NBTTagCompound nodeCompound = new NBTTagCompound();
            activeNode.writeToNBT(nodeCompound);
            compound.setTag(TAG_ACTIVE, nodeCompound);
        }

        if (oldNode != null)
        {
            final NBTTagCompound nodeCompound = new NBTTagCompound();
            oldNode.writeToNBT(new NBTTagCompound());
            compound.setTag(TAG_OLD, nodeCompound);
        }
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
        return compound;
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
        final ICitizenData citizen = getMainCitizen();
        if (citizen == null)
        {
            return;
        }

        final List<WorkOrderBuildMiner> list = getColony().getWorkManager().getOrderedList(WorkOrderBuildMiner.class, getPosition());

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
        public View(final IColonyView c, final BlockPos l)
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
