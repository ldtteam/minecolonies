package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutMiner;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.ai.citizen.miner.Level;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class BuildingMiner extends AbstractBuildingWorker
{
    private static final    String              TAG_FLOOR_BLOCK         = "floorBlock";
    private static final    String              TAG_FENCE_BLOCK         = "fenceBlock";
    private static final    String              TAG_STARTING_LEVEL      = "startingLevelShaft";
    private static final    String              TAG_LEVELS              = "levels";
    private static final    String              TAG_CLEARED             = "clearedShaft";
    private static final    String              TAG_SLOCATION           = "shaftLocation";
    private static final    String              TAG_VECTORX             = "vectorx";
    private static final    String              TAG_VECTORZ             = "vectorz";
    private static final    String              TAG_CLOCATION           = "cobblelocation";
    private static final    String              TAG_ACTIVE              = "activeNodeint";
    private static final    String              TAG_CURRENT_LEVEL       = "currentLevel";
    private static final    String              TAG_SN                  = "StartingNode";
    private static final    String              TAG_LLOCATION           = "ladderlocation";
    private static final    String              TAG_LADDER              = "found_ladder";

    private Block floorBlock = Blocks.planks;
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
    public                  boolean             clearedShaft            = false;

    //Save in hut
    private int startingLevelNode = 0;
    private int active = 0;
    private int currentLevel = 0;
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

    //Stores the levels of the miners mine. This could be a map<depth,level>
    private List<Level> levels = new ArrayList<>();

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

    @Override
    public String getSchematicName()
    {
        return "Miner";
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public String getJobName()
    {
        return "Miner";
    }

    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobMiner(citizen);
    }

    /**
     * Adds a level to the levels list
     *
     * @param currentLevel      {@link Level}to add
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
     * @return              Current level
     */
    public Level getCurrentLevel()
    {
        if(currentLevel >= 0 && currentLevel < levels.size())
        {
            return levels.get(currentLevel);
        }
        return null;
    }

    @Override
    public void serializeToView(ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(currentLevel);
        buf.writeInt(levels.size());

        for(Level level : levels)
        {
            buf.writeInt(level.getNumberOfNodes());
        }
    }

    /**
     * Returns the depth limit
     * Limitted by building level
     *      - Level 1: 50
     *      - Level 2: 30
     *      - Level 3: 5
     *
     * @return              Depth limit
     */
    public int getDepthLimit()
    {
        if(this.getBuildingLevel() == 1)
        {
            return 50;
        }
        else if(this.getBuildingLevel() == 2)
        {
            return 30;
        }
        else if(this.getBuildingLevel() == 3)
        {
            return 5;
        }

        return 70;
    }

    public BlockPos getLadderLocation()
    {
        return ladderLocation;
    }

    public void setLadderLocation(BlockPos ladderLocation)
    {
        this.ladderLocation = ladderLocation;
    }

    public boolean hasFoundLadder()
    {
        return foundLadder;
    }

    public void setFoundLadder(boolean foundLadder)
    {
        this.foundLadder = foundLadder;
    }

    public int getVectorX()
    {
        return vectorX;
    }

    public int getVectorZ()
    {
        return vectorZ;
    }

    public void setVectorX(int vectorX)
    {
        this.vectorX = vectorX;
    }

    public void setVectorZ(int vectorZ)
    {
        this.vectorZ = vectorZ;
    }

    public BlockPos getCobbleLocation()
    {
        return cobbleLocation;
    }

    public void setCobbleLocation(BlockPos pos)
    {
        this.cobbleLocation = pos;
    }

    public void setShaftStart(BlockPos pos)
    {
        this.shaftStart = pos;
    }

    public int getStartingLevelShaft()
    {
        return startingLevelShaft;
    }

    public void resetStartingLevelShaft()
    {
        this.startingLevelShaft = 0;
    }

    public void increamentStartingLevelShaft()
    {
        this.startingLevelShaft++;
    }

    public void setCurrentLevel(int currentLevel)
    {
        this.currentLevel = currentLevel;
    }

    public Block getFloorBlock()
    {
        return floorBlock;
    }

    public Block getFenceBlock()
    {
        return fenceBlock;
    }

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

        if(shaftStart != null && cobbleLocation != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_SLOCATION, shaftStart);
            BlockPosUtil.writeToNBT(compound, TAG_CLOCATION, cobbleLocation);
        }

        if(ladderLocation != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_LLOCATION, ladderLocation);
        }

        NBTTagList levelTagList = new NBTTagList();
        for(Level level : levels)
        {
            NBTTagCompound levelCompound = new NBTTagCompound();
            level.writeToNBT(levelCompound);
            levelTagList.appendTag(levelCompound);
        }
        compound.setTag(TAG_LEVELS, levelTagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey(TAG_FLOOR_BLOCK))
        {
            floorBlock = Block.getBlockFromName(compound.getString(TAG_FLOOR_BLOCK));
        }
        if(compound.hasKey(TAG_FENCE_BLOCK))
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
        for(int i = 0; i < levelTagList.tagCount(); i++)
        {
            Level level = Level.createFromNBT(levelTagList.getCompoundTagAt(i));
            this.levels.add(level);
        }
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public int[] levels;
        public int   current;

        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        @Override
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutMiner(this);
        }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);
            current = buf.readInt();
            int size = buf.readInt();
            levels = new int[size];

            for(int i = 0; i < size; i++)
            {
                levels[i] = buf.readInt();
            }
        }
    }
}
