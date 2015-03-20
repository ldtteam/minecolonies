package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import com.minecolonies.entity.ai.Level;
import com.minecolonies.entity.ai.Node;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class JobMiner extends Job
{
    /**
     * Stores the levels of the miners mine. This could be a map<depth,level>
     */
    public List<Level> levels;
    public Node activeNode;


    public Block floorBlock = Blocks.planks; //save in hut
    public Block fenceBlock = Blocks.fence; //save in hut
    public int startingLevel = 0; //Save in hut

    private static final String TAG_FLOOR_BLOCK = "floorBlock";
    private static final String TAG_FENCE_BLOCK = "fenceBlock";
    private static final String TAG_STARTING_LEVEL = "startingLevel";

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
        compound.setInteger(TAG_STARTING_LEVEL, startingLevel);
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
        startingLevel = compound.getInteger(TAG_STARTING_LEVEL);
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkMiner(this));
    }
}
