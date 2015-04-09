package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.util.List;

public class JobMiner extends Job
{

    public List<ChunkCoordinates> vein;
    public int veinId=0;
    private EntityAIWorkMiner.Stage stage = EntityAIWorkMiner.Stage.WORKING;
    private static final String TAG_STAGE = "Stage";
    public ChunkCoordinates ladderLocation;
    public boolean foundLadder = false;
    private static final String TAG_LLOCATION = "ladderlocation";
    private static final String TAG_LADDER = "found_ladder";


    public JobMiner(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Miner"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.MINER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_STAGE, stage.name());
        compound.setBoolean(TAG_LADDER, foundLadder);
        if(ladderLocation!= null)
        {
            ChunkCoordUtils.writeToNBT(compound, TAG_LLOCATION, ladderLocation);
        }

    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        stage = EntityAIWorkMiner.Stage.valueOf(compound.getString(TAG_STAGE));
        ladderLocation = ChunkCoordUtils.readFromNBT(compound, TAG_LLOCATION);


        foundLadder = compound.getBoolean(TAG_LADDER);



    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkMiner(this));
    }
    public void setStage(EntityAIWorkMiner.Stage stage)
    {
        this.stage = stage;
    }
    public EntityAIWorkMiner.Stage getStage()
    {
        return stage;
    }


}
