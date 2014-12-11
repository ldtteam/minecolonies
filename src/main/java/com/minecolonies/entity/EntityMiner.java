package com.minecolonies.entity;

import com.minecolonies.util.Schematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Class for the miner entity
 * Created: December 10, 2014
 *
 * @author Colton
 */
public class EntityMiner extends EntityWorker
{
    private Schematic schematic;

    public EntityMiner(World world)
    {
        super(world);
    }

    @Override
    protected void initTasks()
    {
        super.initTasks();
        //this.tasks.addTask(3, new EntityAIWorkMiner(this));
    }

    @Override
    protected String getJobName()
    {
        return "Miner";
    }

    @Override
    public int getTextureID()//TODO remove method once more textures are added
    {
        return 1;//TODO torches version
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
    }

    @Override
    public boolean isNeeded()
    {
        return getWorkBuilding() != null;//TODO
    }
}
