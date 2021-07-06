package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.huts.WindowHutMinerModule;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.buildings.moduleviews.SettingsModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.citizen.miner.Node;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.*;
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
    private static final String MINER = "miner";

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
     * The first y level to start the shaft at.
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
     * The location of the topmost ladder in the shaft.
     */
    private BlockPos ladderLocation;

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
        final ItemStack stackDirt = new ItemStack(Blocks.DIRT);

        keepX.put(stackLadder::sameItem, new Tuple<>(STACKSIZE, true));
        keepX.put(stackFence::sameItem, new Tuple<>(STACKSIZE, true));
        keepX.put(stackTorch::sameItem, new Tuple<>(STACKSIZE, true));
        keepX.put(stackCobble::sameItem, new Tuple<>(STACKSIZE, true));

        keepX.put(stack -> stack.getItem().is(ItemTags.SLABS), new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> stack.getItem().is(ItemTags.PLANKS), new Tuple<>(STACKSIZE, true));
        keepX.put(stackDirt::sameItem, new Tuple<>(STACKSIZE, true));
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
     * The Miner wants to get multiple nodes/levels worth of stuff when requesting.
     */
    @Override
    public int getResourceBatchMultiplier() 
    {
        //Ask for 10x the resources if possible
        return 10;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
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
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobMiner(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        startingLevelShaft = compound.getInt(TAG_STARTING_LEVEL);
        clearedShaft = compound.getBoolean(TAG_CLEARED);

        if (compound.getAllKeys().contains(TAG_ACTIVE))
        {
            activeNode = Node.createFromNBT(compound.getCompound(TAG_ACTIVE));
        }
        else if (compound.getAllKeys().contains(TAG_OLD))
        {
            oldNode = Node.createFromNBT(compound.getCompound(TAG_OLD));
        }

        currentLevel = compound.getInt(TAG_CURRENT_LEVEL);

        ladderLocation = BlockPosUtil.read(compound, TAG_LLOCATION);
        cobbleLocation = BlockPosUtil.read(compound, TAG_CLOCATION);

        startingLevelNode = compound.getInt(TAG_SN);

        final ListNBT levelTagList = compound.getList(TAG_LEVELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.size(); i++)
        {
            this.levels.add(new Level(levelTagList.getCompound(i)));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.putInt(TAG_STARTING_LEVEL, startingLevelShaft);
        compound.putBoolean(TAG_CLEARED, clearedShaft);
        if (activeNode != null)
        {
            final CompoundNBT nodeCompound = new CompoundNBT();
            activeNode.write(nodeCompound);
            compound.put(TAG_ACTIVE, nodeCompound);
        }

        if (oldNode != null)
        {
            final CompoundNBT nodeCompound = new CompoundNBT();
            oldNode.write(nodeCompound);
            compound.put(TAG_OLD, nodeCompound);
        }
        compound.putInt(TAG_CURRENT_LEVEL, currentLevel);
        compound.putInt(TAG_SN, startingLevelNode);

        if (cobbleLocation != null)
        {
            BlockPosUtil.write(compound, TAG_CLOCATION, cobbleLocation);
        }

        if (ladderLocation != null)
        {
            BlockPosUtil.write(compound, TAG_LLOCATION, ladderLocation);
        }

        @NotNull final ListNBT levelTagList = new ListNBT();
        for (@NotNull final Level level : levels)
        {
            @NotNull final CompoundNBT levelCompound = new CompoundNBT();
            level.write(levelCompound);
            levelTagList.add(levelCompound);
        }
        compound.put(TAG_LEVELS, levelTagList);
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

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Stamina;
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
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
     * @param level the level.
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
     * Returns the depth limit. Limitted by building level.
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
        if (ladderLocation == null)
        {
            loadLadderPos();
        }

        return ladderLocation;
    }

    /**
     * Getter of the cobbleLocation.
     *
     * @return the location.
     */
    public BlockPos getCobbleLocation()
    {
        if (cobbleLocation == null)
        {
            loadLadderPos();
        }

        return cobbleLocation;
    }

    @Override
    public void setTileEntity(final AbstractTileEntityColonyBuilding te)
    {
        super.setTileEntity(te);
        loadLadderPos();
    }

    private void loadLadderPos()
    {
        final Map<String, Set<BlockPos>> map = tileEntity.getWorldTagNamePosMap();
        final Set<BlockPos> cobblePos = map.getOrDefault("cobble", new HashSet<>());
        final Set<BlockPos> ladderPos = map.getOrDefault("ladder", new HashSet<>());
        if (cobblePos.isEmpty() || ladderPos.isEmpty())
        {
            return;
        }
        cobbleLocation = cobblePos.iterator().next();
        ladderLocation = ladderPos.iterator().next();
    }

    /**
     * Getter of the starting level of the shaft. (Y position).
     *
     * @return the start level.
     */
    public int getStartingLevelShaft()
    {
        if (levels.isEmpty())
        {
            return startingLevelShaft;
        }
        else
        {
            return levels.get(levels.size() - 1).getDepth() - 6;
        }
    }

    /**
     * Resets the starting level of the shaft to 0.
     *
     * @param level the level o set it to.
     */
    public void setStartingLevelShaft(final int level)
    {
        this.startingLevelShaft = level;
    }

    /**
     * Getter to check if the shaft has been cleared.
     *
     * @return true if so.
     */
    public boolean hasClearedShaft()
    {
        return clearedShaft;
    }

    /**
     * Setter if the shaft has been cleared.
     *
     * @param clearedShaft true if so.
     */
    public void setClearedShaft(final boolean clearedShaft)
    {
        this.clearedShaft = clearedShaft;
    }

    /**
     * Getter for the active node.
     *
     * @return the int id of the active node.
     */
    @NotNull
    public Node getActiveNode()
    {
        Node calcNode = activeNode == null || activeNode.getStatus() == Node.NodeStatus.COMPLETED ? levels.get(currentLevel).getRandomNode(oldNode) : activeNode;
        if (activeNode != calcNode)
        {
            activeNode = calcNode;
        }
        return activeNode;
    }

    /**
     * Setter for the active node.
     *
     * @param activeNode the int id of the active node.
     */
    public void setActiveNode(final Node activeNode)
    {
        this.activeNode = activeNode;
    }

    /**
     * Getter for the old node.
     *
     * @return the int id of the old node.
     */
    public Node getOldNode()
    {
        return oldNode;
    }

    /**
     * Setter for the old node.
     *
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
        public int                           current;

        /**
         * The number of guards assigned to this mine
         */
        public int assignedGuards;

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
            return new WindowHutMinerModule(this);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
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

        /**
         * Retrieve a list of guards working at buildings with the task to patrol the mine
         * @return list of guards
         */
        public List<ICitizenDataView> pullGuards()
        {
            assignedGuards = 0;
            final List<ICitizenDataView> guards = new ArrayList<>();
            final List<IBuildingView> buildings = this.getColony().getBuildings().stream().filter(entry -> entry instanceof AbstractBuildingGuards.View && entry.getModuleView(SettingsModuleView.class).getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.PATROL_MINE)).collect(Collectors.toList());
            for (final IBuildingView building : buildings)
            {
                final AbstractBuildingGuards.View guardbuilding = (AbstractBuildingGuards.View) building;
                if (guardbuilding.getMinePos() != null && guardbuilding.getMinePos().equals(this.getPosition()))
                {
                    assignedGuards++;
                }
                for (final Integer citizenId : guardbuilding.getGuards())
                {
                    guards.add(this.getColony().getCitizen(citizenId));
                }
            }
            return guards;
        }

        /**
         * Get the maximum of allowed guards for the mine
         * 1 guard for mine level 1 and 2
         * 2 guards for mine level 3 and 4
         * 3 guards for mine level 5
         * @return maximum number of guards
         */
        public int getMaxGuards()
        {
            switch (this.getBuildingLevel())
            {
                case 1:
                case 2:
                    return 1;
                case 3:
                case 4:
                    return 2;
                case 5:
                    return 3;
                default:
                    return 0;
            }
        }
    }
}
