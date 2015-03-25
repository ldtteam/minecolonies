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
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.io.*;
import java.util.List;

public class BuildingMiner extends BuildingWorker {
    public List<Level> levels;     //Stores the levels of the miners mine. This could be a map<depth,level>



    public Node activeNode;

    public Block floorBlock = Blocks.planks;
    public Block fenceBlock = Blocks.fence;

    public int startingLevel = 0; //Save in hut

    public ChunkCoordinates getLocation = new ChunkCoordinates(1,1,1);

    public boolean cleared = false;

    private static final String TAG_FLOOR_BLOCK = "floorBlock";
    private static final String TAG_FENCE_BLOCK = "fenceBlock";
    private static final String TAG_STARTING_LEVEL = "startingLevel";
    private static final String TAG_LEVELS = "levels";
    private static final String TAG_NODE = "nodes";
    private static final String TAG_CLEARED = "cleared";
    private static final String TAG_LOCATION = "location";
    NBTTagCompound workManagerCompound = new NBTTagCompound();


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

        //TODO save levels, and active node

        //NOTE .getUnlocalizedName isn't the right string
        //compound.setString(TAG_FLOOR_BLOCK, floorBlock.getUnlocalizedName());
        //compound.setString(TAG_FENCE_BLOCK, fenceBlock.getUnlocalizedName());
        compound.setInteger(TAG_STARTING_LEVEL, startingLevel);
        compound.setBoolean(TAG_CLEARED,cleared);


        /*compound.setByteArray(TAG_LEVELS, WriteToByteStream(levels));
        compound.setByteArray(TAG_NODE,WriteToByteStream(activeNode));*/

        ChunkCoordUtils.writeToNBT(workManagerCompound,TAG_LOCATION,getLocation);






    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        //TODO load levels, and active node
//        if(compound.hasKey(TAG_FLOOR_BLOCK))
//        {
//            floorBlock = Block.getBlockFromName(compound.getString(TAG_FLOOR_BLOCK));
//        }
//        if(compound.hasKey(TAG_FENCE_BLOCK))
//        {
//            fenceBlock = Block.getBlockFromName(compound.getString(TAG_FENCE_BLOCK));
//        }
        startingLevel = compound.getInteger(TAG_STARTING_LEVEL);
        cleared = compound.getBoolean(TAG_CLEARED);
        getLocation = ChunkCoordUtils.readFromNBT(workManagerCompound, TAG_LOCATION);

        /*levels = (List<Level>)ReadFromByteStream(compound.getByteArray(TAG_LEVELS));


        activeNode = (Node)ReadFromByteStream(compound.getByteArray(TAG_NODE));
        */

    }




}
