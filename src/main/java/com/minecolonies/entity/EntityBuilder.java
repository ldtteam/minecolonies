package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Schematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class EntityBuilder extends EntityWorker
{
    private Schematic schematic;

    public EntityBuilder(World world)
    {
        super(world);
    }

    @Override
    protected void initTasks()
    {
        super.initTasks();
        this.tasks.addTask(3, new EntityAIWorkBuilder(this));
    }

    @Override
    protected String initJob()
    {
        return "Builder";
    }

    @Override
    public int getTextureID()//TODO remove method once more textures are added
    {
        return 1;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        if(hasSchematic())
        {
            NBTTagCompound schematicTag = new NBTTagCompound();
            schematicTag.setString("name", schematic.getName());
            ChunkCoordUtils.writeToNBT(schematicTag, "position", schematic.getPosition());
            ChunkCoordUtils.writeToNBT(schematicTag, "progress", schematic.getLocalPosition());
            compound.setTag("schematic", schematicTag);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        if(compound.hasKey("schematic"))
        {
            NBTTagCompound schematicTag = compound.getCompoundTag("schematic");
            String name = schematicTag.getString("name");
            ChunkCoordinates pos = ChunkCoordUtils.readFromNBT(schematicTag, "position");
            ChunkCoordinates progress = ChunkCoordUtils.readFromNBT(schematicTag, "progress");
            schematic = Schematic.loadSchematic(worldObj, name);
            if(schematic == null)
            {
                compound.removeTag("schematic");
            }
            else
            {
                schematic.setPosition(pos);
                schematic.setLocalPosition(progress);
            }
        }
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

    @Override
    public boolean isNeeded()
    {
        return /*this.getColony() != null &&*/ !getColony().getBuildingUpgrades().isEmpty() && getWorkBuilding() != null;
    }
}
