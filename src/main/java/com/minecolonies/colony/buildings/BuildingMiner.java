package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.ai.Level;
import com.minecolonies.entity.ai.Node;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BuildingMiner extends BuildingWorker {

    public List<Level> levels = new ArrayList<Level>();     //Stores the levels of the miners mine. This could be a map<depth,level>
    public Node activeNode;

    public Block floorBlock = Blocks.planks;
    public Block fenceBlock = Blocks.fence;

    public int startingLevelShaft = 0;



    public ChunkCoordinates getLocation = new ChunkCoordinates(1,1,1);
    public ChunkCoordinates ladderLocation;
    public boolean foundLadder = false;
    public boolean clearedShaft = false;

    public boolean clearedNode = false;
    public int startingLevelNode = 0; //Save in hut

    public ChunkCoordinates shaftStart;

    private static final String TAG_FLOOR_BLOCK = "floorBlock";
    private static final String TAG_FENCE_BLOCK = "fenceBlock";
    private static final String TAG_STARTING_LEVEL = "startingLevelShaft";
    private static final String TAG_LEVELS = "levels";
    private static final String TAG_NODE = "nodes";
    private static final String TAG_CLEARED = "clearedShaft";
    private static final String TAG_GET_LOCATION = "getLocation";
    private static final String TAG_LLOCATION = "ladderlocation";
    private static final String TAG_SLOCATION = "shaftLocation";
    private static final String TAG_LADDER = "found_ladder";
    private static Logger logger = LogManager.getLogger("Miner");



    public BuildingMiner(Colony c, ChunkCoordinates l) {
        super(c, l);
    }

    @Override
    public String getSchematicName() {
        return "Miner";
    }

    @Override
    public int getMaxBuildingLevel() {
        return 3;
    }

    @Override
    public String getJobName() {
        return "Miner";
    }

    @Override
    public Job createJob(CitizenData citizen) {
        return new JobMiner(citizen);
    }

    public static class View extends BuildingWorker.View {
        public View(ColonyView c, ChunkCoordinates l) {
            super(c, l);
        }

        public com.blockout.views.Window getWindow(int guiId) {
            return new WindowHutWorkerPlaceholder<BuildingMiner.View>(this, "minerHut");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString(TAG_FLOOR_BLOCK, GameRegistry.findUniqueIdentifierFor(floorBlock).toString());
        compound.setString(TAG_FENCE_BLOCK, GameRegistry.findUniqueIdentifierFor(fenceBlock).toString());
        compound.setInteger(TAG_STARTING_LEVEL, startingLevelShaft);
        compound.setBoolean(TAG_CLEARED, clearedShaft);
        compound.setBoolean(TAG_LADDER, foundLadder);

        if(ladderLocation!= null && getLocation!=null && shaftStart !=null)
        {
            ChunkCoordUtils.writeToNBT(compound, TAG_LLOCATION, ladderLocation);
            ChunkCoordUtils.writeToNBT(compound, TAG_SLOCATION, shaftStart);
            ChunkCoordUtils.writeToNBT(compound, TAG_GET_LOCATION, getLocation);
        }

        NBTTagList levelTagList = new NBTTagList();
        for (Level level : levels) {
            NBTTagCompound levelCompound = new NBTTagCompound();
            level.writeToNBT(levelCompound);
            levelTagList.appendTag(levelCompound);
        }
        compound.setTag(TAG_LEVELS, levelTagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
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
        foundLadder = compound.getBoolean(TAG_LADDER);

        getLocation = ChunkCoordUtils.readFromNBT(compound, TAG_GET_LOCATION);
        ladderLocation = ChunkCoordUtils.readFromNBT(compound, TAG_LLOCATION);
        shaftStart = ChunkCoordUtils.readFromNBT(compound, TAG_SLOCATION);

        NBTTagList levelTagList = compound.getTagList(TAG_LEVELS, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < levelTagList.tagCount(); i++)
        {
            levels.add(Level.createFromNBT(levelTagList.getCompoundTagAt(i)));
        }

        logger.info("Finished loading Building");
    }
}
