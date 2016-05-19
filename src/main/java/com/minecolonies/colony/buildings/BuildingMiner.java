package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutMiner;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.ai.citizen.miner.Level;
import com.minecolonies.entity.ai.citizen.miner.Node;
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

public class BuildingMiner extends BuildingWorker
{
    private static final    String              TAG_FLOOR_BLOCK         = "floorBlock";//TODO: is this something that needs to be saved? id say yea mw
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

    public                  Block               floorBlock              = Blocks.planks;
    public                  Block               fenceBlock              = Blocks.oak_fence; //todo this changed, mw, transition 1.8

    public                  Node                activeNode;
    /**
     * Here we can detect multiples of 5
     */
    public                  int                 startingLevelShaft      = 0;
    /**
     * The location of the topmost cobblestone the ladder starts at
     */
    public                  BlockPos            cobbleLocation;
    /**
     * True if shaft is at bottom limit
     */
    public                  boolean             clearedShaft            = false;
    public                  int                 startingLevelNode       = 0; //Save in hut
    public                  int                 active                  = 0;
    public                  int                 currentLevel            = 0;
    public                  BlockPos            shaftStart;
    /**
     * Ladder orientation in x
     */
    public                  int                vectorX                  = 1;
    /**
     * Ladder orientation in y
     */
    public                  int                 vectorZ                 = 1;
    /**
     * The location of the topmost ladder in the shaft
     */
    public                  BlockPos            ladderLocation;
    /**
     * True if a ladder is found
     */
    public                  boolean             foundLadder             = false;
    private                 List<Level>         levels                  = new ArrayList<>();     //Stores the levels of the miners mine. This could be a map<depth,level>

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
    public Job createJob(CitizenData citizen)
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
        getLevels().add(currentLevel);
    }

    /**
     * A list of all shaft levels that are cleared
     */
    public List<Level> getLevels()
    {
        return levels;
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
        buf.writeInt(getLevels().size());

        for(Level level : getLevels())
        {
            buf.writeInt(level.getNodes().size());
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
        for(Level level : getLevels())
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
            getLevels().add(level);
        }

        if(currentLevel >= 0 && currentLevel < getLevels().size() && active < getLevels().get(currentLevel).getNodes().size())
        {
            activeNode = getLevels().get(currentLevel).getNodes().get(active);
        }
    }

    public static class View extends BuildingWorker.View
    {
        public int[] levels;
        public int   current;

        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

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
