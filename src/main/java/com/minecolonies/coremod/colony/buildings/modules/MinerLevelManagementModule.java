package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderMiner;
import com.minecolonies.coremod.entity.ai.citizen.miner.MinerLevel;
import com.minecolonies.coremod.entity.ai.citizen.miner.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.BuildingConstants.*;
import static com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner.SHAFT_RADIUS;

/**
 * Module containing miner level management.
 */
public class MinerLevelManagementModule extends AbstractBuildingModule implements IPersistentModule
{
    /**
     * Stores the levels of the miners mine. This could be a map with (depth,level).
     */
    @NotNull
    private final List<MinerLevel> levels = new ArrayList<>();

    /**
     * The number of the current level.
     */
    private int currentLevel = 0;

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
     * The first y level to start the shaft at.
     */
    private int startingLevelShaft = 0;

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        startingLevelShaft = compound.getInt(TAG_STARTING_LEVEL);
        currentLevel = compound.getInt(TAG_CURRENT_LEVEL);
        final ListTag levelTagList = compound.getList(TAG_LEVELS, Tag.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.size(); i++)
        {
            this.levels.add(new MinerLevel(levelTagList.getCompound(i)));
        }

        if (compound.contains(TAG_ACTIVE))
        {
            activeNode = Node.createFromNBT(compound.getCompound(TAG_ACTIVE));
        }
        else if (compound.contains(TAG_OLD))
        {
            oldNode = Node.createFromNBT(compound.getCompound(TAG_OLD));
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        compound.putInt(TAG_STARTING_LEVEL, startingLevelShaft);
        compound.putInt(TAG_CURRENT_LEVEL, currentLevel);
        @NotNull final ListTag levelTagList = new ListTag();
        for (@NotNull final MinerLevel level : levels)
        {
            @NotNull final CompoundTag levelCompound = new CompoundTag();
            level.write(levelCompound);
            levelTagList.add(levelCompound);
        }
        compound.put(TAG_LEVELS, levelTagList);

        if (activeNode != null)
        {
            final CompoundTag nodeCompound = new CompoundTag();
            activeNode.write(nodeCompound);
            compound.put(TAG_ACTIVE, nodeCompound);
        }

        if (oldNode != null)
        {
            final CompoundTag nodeCompound = new CompoundTag();
            oldNode.write(nodeCompound);
            compound.put(TAG_OLD, nodeCompound);
        }
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buf)
    {
        buf.writeInt(currentLevel);
        buf.writeInt(levels.size());

        for (@NotNull final MinerLevel level : levels)
        {
            buf.writeInt(level.getNumberOfBuiltNodes());
            buf.writeInt(level.getDepth());
        }

        final List<WorkOrderMiner> list = building.getColony().getWorkManager().getOrderedList(WorkOrderMiner.class, building.getPosition());
        buf.writeInt(list.size());
        for (@NotNull final WorkOrderMiner wo : list)
        {
            wo.serializeViewNetworkData(buf);
        }
    }

    /**
     * Adds a level to the levels list.
     *
     * @param currentLevel {@link MinerLevel} to add.
     */
    public void addLevel(final MinerLevel currentLevel)
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
    public MinerLevel getCurrentLevel()
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
    public int getLevelId(final MinerLevel level)
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
     * Getter for the active node.
     *
     * @return the int id of the active node.
     */
    @Nullable
    public Node getActiveNode()
    {
        if (levels.isEmpty())
        {
            return null;
        }

        Node calcNode = activeNode;
        if (activeNode == null || activeNode.getStatus() == Node.NodeStatus.COMPLETED)
        {
            if (currentLevel >= levels.size())
            {
                currentLevel = levels.size() - 1;
            }
            calcNode = levels.get(currentLevel).getRandomNode(oldNode);
        }

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
    public void setActiveNode(@Nullable final Node activeNode)
    {
        this.activeNode = activeNode;
    }

    /**
     * Setter for the old node.
     *
     * @param oldNode the int id of the old node.
     */
    public void setOldNode(@Nullable final Node oldNode)
    {
        this.oldNode = oldNode;
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
     * Repair the level.
     * @param level the level to repair.
     */
    public void repairLevel(final int level)
    {
        if (building instanceof BuildingMiner)
        {
            final BlockPos ladderPos = ((BuildingMiner) building).getLadderLocation();
            final BlockPos vector = ladderPos.subtract(((BuildingMiner) building).getCobbleLocation());
            final int xOffset = SHAFT_RADIUS * vector.getX();
            final int zOffset = SHAFT_RADIUS * vector.getZ();

            BuildingMiner.initStructure(null,
              0,
              new BlockPos(ladderPos.getX() + xOffset, levels.get(level).getDepth(), ladderPos.getZ() + zOffset),
              (BuildingMiner) building,
              building.getColony().getWorld(),
              null);
        }
    }

    /**
     * Get the list of levels.
     * @return the list.
     */
    public List<MinerLevel> getLevels()
    {
        return levels;
    }
}
