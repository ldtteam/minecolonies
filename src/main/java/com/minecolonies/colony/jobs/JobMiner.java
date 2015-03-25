package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
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

        //TODO save levels, and active node

//NOTE .getUnlocalizedName isn't the right string
//        compound.setString(TAG_FLOOR_BLOCK, floorBlock.getUnlocalizedName());
//        compound.setString(TAG_FENCE_BLOCK, fenceBlock.getUnlocalizedName());
        compound.setString(TAG_STAGE, stage.name());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
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
        stage = EntityAIWorkMiner.Stage.valueOf(compound.getString(TAG_STAGE));
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
