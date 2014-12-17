package com.minecolonies.entity.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.Colony;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class JobBuilder extends ColonyJob
{
    private Schematic schematic;

    private final String TAG_SCHEMATIC = "schematic";
    private final String TAG_NAME      = "name";
    private final String TAG_POSITION  = "position";
    private final String TAG_PROGRESS  = "progress";

    public JobBuilder(EntityCitizen entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "Builder"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.BUILDER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (hasSchematic())
        {
            NBTTagCompound schematicTag = new NBTTagCompound();
            schematicTag.setString(TAG_NAME, schematic.getName());
            ChunkCoordUtils.writeToNBT(schematicTag, TAG_POSITION, schematic.getPosition());
            ChunkCoordUtils.writeToNBT(schematicTag, TAG_PROGRESS, schematic.getLocalPosition());
            compound.setTag(TAG_SCHEMATIC, schematicTag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey(TAG_SCHEMATIC))
        {
            NBTTagCompound schematicTag = compound.getCompoundTag(TAG_SCHEMATIC);
            String name = schematicTag.getString(TAG_NAME);
            ChunkCoordinates pos = ChunkCoordUtils.readFromNBT(schematicTag, TAG_POSITION);
            ChunkCoordinates progress = ChunkCoordUtils.readFromNBT(schematicTag, TAG_PROGRESS);
            schematic = Schematic.loadSchematic(getCitizen().worldObj, name);
            if(schematic != null)
            {
                schematic.setPosition(pos);
                schematic.setLocalPosition(progress);
            }
            else
            {
                compound.removeTag(TAG_SCHEMATIC);
            }
        }
    }

    @Override
    public boolean isNeeded()
    {
        Colony colony = getCitizen().getColony();
        return colony != null &&
                getCitizen().getWorkBuilding() != null &&
                !colony.getBuildingUpgrades().isEmpty();
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkBuilder(this));
    }

    public boolean hasSchematic()
    {
        return schematic != null && schematic.hasSchematic();
    }

    public Schematic getSchematic()
    {
        return schematic;
    }

    public void setSchematic(Schematic schematic)
    {
        this.schematic = schematic;
    }

    public int getWorkInterval()
    {
        return 1;//Constants.BUILDERWORKINTERFALL - this.getLevel();//TODO
    }
}
