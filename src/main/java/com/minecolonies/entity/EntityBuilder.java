package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.util.Schematic;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

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
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityMob.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(2, new EntityAIGoHome(this));
        this.tasks.addTask(3, new EntityAISleep(this));
        this.tasks.addTask(3, new EntityAIWorkBuilder(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));
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
            Vec3Utils.writeVecToNBT(schematicTag, "position", schematic.getPosition());
            Vec3Utils.writeVecToNBT(schematicTag, "progress", schematic.getLocalPosition());
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
            Vec3 pos = Vec3Utils.readVecFromNBT(schematicTag, "position");
            Vec3 progress = Vec3Utils.readVecFromNBT(schematicTag, "progress");
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
        return this.getTownHall() != null && !this.getTownHall().getBuilderRequired().isEmpty() && this.getWorkHut() != null;
    }
}
