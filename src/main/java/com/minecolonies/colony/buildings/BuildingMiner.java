package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutMiner;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.ai.citizen.miner.Level;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.ServerUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * The miners building.
 */
public class BuildingMiner extends AbstractBuildingWorker
{
    /**
     * The NBT Tag to store the floorBlock
     */
    private static final    String              TAG_FLOOR_BLOCK         = "floorBlock";
    /**
     * The NBT Tag to store the fenceBlock
     */
    private static final    String              TAG_FENCE_BLOCK         = "fenceBlock";
    /**
     * The NBT Tag to store the starting level of the shaft.
     */
    private static final    String              TAG_STARTING_LEVEL      = "startingLevelShaft";
    /**
     * The NBT Tag to store list of levels.
     */
    private static final    String              TAG_LEVELS              = "levels";
    /**
     * The NBT Tag to store if the shaft has been cleared.
     */
    private static final    String              TAG_CLEARED             = "clearedShaft";
    /**
     * The NBT Tag to store the location of the shaft.
     */
    private static final    String              TAG_SLOCATION           = "shaftLocation";
    /**
     * The NBT Tag to store the vector-x of the shaft
     */
    private static final    String              TAG_VECTORX             = "vectorx";
    /**
     * The NBT Tag to store the vector-z of the shaft
     */
    private static final    String              TAG_VECTORZ             = "vectorz";
    /**
     * The NBT Tag to store the location of the cobblestone at the shaft.
     */
    private static final    String              TAG_CLOCATION           = "cobblelocation";
    /**
     * The NBT Tag to store the active node the miner is working on.
     */
    private static final    String              TAG_ACTIVE              = "activeNodeint";
    /**
     * The NBT Tag to store the current level the miner is working in.
     */
    private static final    String              TAG_CURRENT_LEVEL       = "currentLevel";
    /**
     * The NBT Tag to store the starting node.
     */
    private static final    String              TAG_SN                  = "StartingNode";
    /**
     * The NBT Tag to store the location of the ladder.
     */
    private static final    String              TAG_LLOCATION           = "ladderlocation";
    /**
     * The NBT Tag to store if a ladder has been found yet.
     */
    private static final    String              TAG_LADDER              = "found_ladder";

    /**
     * Defines the material used for the floor of the shaft.
     */
    private Block floorBlock = Blocks.planks;
    /**
     * Defines the material used for the fence of the shaft.
     */
    private Block fenceBlock = Blocks.oak_fence;

    /**
     * Here we can detect multiples of 5
     */
    private int startingLevelShaft = 0;

    /**
     * The location of the topmost cobblestone the ladder starts at
     */
    private BlockPos cobbleLocation;

    /**
     * True if shaft is at bottom limit
     */
    public boolean clearedShaft = false;

    /**
     * The starting level of the node.
     */
    private int startingLevelNode = 0;
    /**
     * The id of the active node.
     */
    private int active = 0;
    /**
     * The number of the current level.
     */
    private int currentLevel = 0;
    /**
     * The position of the start of the shaft.
     */
    private BlockPos shaftStart;

    /**
     * Ladder orientation in x
     */
    private int vectorX = 1;

    /**
     * Ladder orientation in y
     */
    private int vectorZ = 1;

    /**
     * The location of the topmost ladder in the shaft
     */
    private BlockPos ladderLocation;

    /**
     * True if a ladder is found
     */
    private boolean foundLadder = false;

    /**
     * Stores the levels of the miners mine. This could be a map<depth,level>
     */
    private List<Level> levels = new ArrayList<>();

    /**
     * The maximum upgrade of the building.
     */
    private static final int MAX_BUILDING_LEVEL = 3;
    /**
     * The job description.
     */
    private static final String MINER          = "Miner";

    /**
     * Required constructor.
     *
     * @param c colony containing the building.
     * @param l location of the building.
     */
    public BuildingMiner(Colony c, BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter of the schematic name.
     * @return the schematic name.
     */
    @Override
    public String getSchematicName()
    {
        return MINER;
    }

    /**
     * Getter of the max building level.
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * Getter of the job description.
     * @return the description of the miners job.
     */
    @Override
    public String getJobName()
    {
        return MINER;
    }

    /**
     * Create the job for the miner.
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobMiner(citizen);
    }

    /**
     * Adds a level to the levels list
     *
     * @param currentLevel {@link Level}to add
     */
    public void addLevel(Level currentLevel)
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
     * Returns the current level
     *
     * @return Current level
     */
    public Level getCurrentLevel()
    {
        if (currentLevel >= 0 && currentLevel < levels.size())
        {
            return levels.get(currentLevel);
        }
        return null;
    }

    /**
     * Method to serialize data to send it to the view.
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(currentLevel);
        buf.writeInt(levels.size());

        for (Level level : levels)
        {
            buf.writeInt(level.getNumberOfNodes());
        }
    }

    /**
     * Returns the depth limit Limitted by building level - Level 1: 50 - Level
     * 2: 30 - Level 3: 5
     *
     * @return Depth limit
     */
    public int getDepthLimit()
    {
        if (this.getBuildingLevel() == 1)
        {
            return 50;
        }
        else if (this.getBuildingLevel() == 2)
        {
            return 30;
        }
        else if (this.getBuildingLevel() == 3)
        {
            return 5;
        }

        return 70;
    }

    /**
     * Getter of the ladderLocation.
     * @return the ladder location.
     */
    public BlockPos getLadderLocation()
    {
        return ladderLocation;
    }

    /**
     * Setter of the ladder location.
     * @param ladderLocation the new ladder location.
     */
    public void setLadderLocation(BlockPos ladderLocation)
    {
        this.ladderLocation = ladderLocation;
    }

    /**
     * Checks if a ladder has been found already.
     * @return true if so.
     */
    public boolean hasFoundLadder()
    {
        return foundLadder;
    }

    /**
     * Setter for the foundLadder.
     * @param foundLadder the boolean.
     */
    public void setFoundLadder(boolean foundLadder)
    {
        this.foundLadder = foundLadder;
    }

    /**
     * Getter of the X-vector.
     * @return the vectorX.
     */
    public int getVectorX()
    {
        return vectorX;
    }

    /**
     * Getter of the Z-vector.
     * @return the vectorZ.
     */
    public int getVectorZ()
    {
        return vectorZ;
    }

    /**
     * Setter of the X-vector.
     * @param vectorX the vector to set +1 or -1.
     */
    public void setVectorX(int vectorX)
    {
        this.vectorX = vectorX;
    }

    /**
     * Setter of the Z-vector.
     * @param vectorZ the vector to set +1 or -1.
     */
    public void setVectorZ(int vectorZ)
    {
        this.vectorZ = vectorZ;
    }

    /**
     * Getter of the cobbleLocation.
     * @return the location.
     */
    public BlockPos getCobbleLocation()
    {
        return cobbleLocation;
    }

    /**
     * Setter for the cobbleLocation.
     * @param pos the location to set.
     */
    public void setCobbleLocation(BlockPos pos)
    {
        this.cobbleLocation = pos;
    }

    /**
     * Setter of the shaftStart.
     * @param pos the location.
     */
    public void setShaftStart(BlockPos pos)
    {
        this.shaftStart = pos;
    }

    /**
     * Getter of the starting level of the shaft.
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
     * Sets the current level the miner is at.
     * @param currentLevel the level to set.
     */
    public void setCurrentLevel(int currentLevel)
    {
        this.currentLevel = currentLevel;
    }

    /**
     * Getter of the floor block.
     * @return the material of the floor block.
     */
    public Block getFloorBlock()
    {
        return floorBlock;
    }

    /**
     * Getter of the fence block.
     * @return the material of the fence block.
     */
    public Block getFenceBlock()
    {
        return fenceBlock;
    }

    /**
     * Writes the information to NBT to store it permanently.
     * @param compound the compound key.
     */
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setString(TAG_FLOOR_BLOCK, Block.blockRegistry.getNameForObject(floorBlock).toString());
        compound.setString(TAG_FENCE_BLOCK, Block.blockRegistry.getNameForObject(fenceBlock).toString());
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

        NBTTagList levelTagList = new NBTTagList();
        for (Level level : levels)
        {
            NBTTagCompound levelCompound = new NBTTagCompound();
            level.writeToNBT(levelCompound);
            levelTagList.appendTag(levelCompound);
        }
        compound.setTag(TAG_LEVELS, levelTagList);
    }

    /**
     * Reads the information from NBT from permanent storage.
     * @param compound the compound key.
     */
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_FLOOR_BLOCK))
        {
            floorBlock = Block.getBlockFromName(compound.getString(TAG_FLOOR_BLOCK));
        }
        if (compound.hasKey(TAG_FENCE_BLOCK))
        {
            fenceBlock = Block.getBlockFromName(compound.getString(TAG_FENCE_BLOCK));
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

        NBTTagList levelTagList = compound.getTagList(TAG_LEVELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.tagCount(); i++)
        {
            Level level = Level.createFromNBT(levelTagList.getCompoundTagAt(i));
            this.levels.add(level);
        }
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        final EntityPlayer owner = ServerUtils.getPlayerFromUUID(getColony().getPermissions().getOwner());

        if (newLevel == 1)
        {
            owner.triggerAchievement(ModAchievements.achievementBuildingMiner);
        }
        else if (newLevel >= this.getMaxBuildingLevel())
        {
            owner.triggerAchievement(ModAchievements.achievementUpgradeMinerMax);
        }
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        public int[] levels;
        public int   current;

        /**
         * Public constructor of the view, creates an instance of it.
         * @param c the colony.
         * @param l the position.
         */
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        /**
         * Gets the blockOut Window.
         * @return the window of the lumberjack building.
         */
        @Override
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutMiner(this);
        }

        /**
         * Deserializes the information the building class sent to store it in the view.
         * @param buf the buffer to read from.
         */
        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);
            current = buf.readInt();
            int size = buf.readInt();
            levels = new int[size];

            for (int i = 0; i < size; i++)
            {
                levels[i] = buf.readInt();
            }
        }
    }
}
