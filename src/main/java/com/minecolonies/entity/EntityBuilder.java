package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.*;

public class EntityBuilder extends EntityCitizen
{
    private Schematic schematic;

    private List<ItemStack> itemsNeeded = new ArrayList<ItemStack>();

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
        //this.tasks.addTask(2, new EntityAISleep(this));
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
    public void setTexture()
    {
        texture = new ResourceLocation(Constants.MODID + ":textures/entity/EntityBuilder.png");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        if(hasSchematic())
        {
            NBTTagCompound schematicTag = new NBTTagCompound();
            schematicTag.setString("name", schematic.getName());
            writeVecToNBT(schematicTag, "position", schematic.getPosition());
            writeVecToNBT(schematicTag, "progress", schematic.getLocalPosition());
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
            Vec3 pos = readVecFromNBT(schematicTag, "position");
            Vec3 progress = readVecFromNBT(schematicTag, "progress");
            schematic = Schematic.loadSchematic(worldObj, name);
            schematic.setPosition(pos);
            schematic.setLocalPosition(progress);
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

    public boolean hasMaterials()
    {
        if(!hasSchematic()) return true;

        //TODO possibly find a better method
        for(ItemStack materialStack : this.getSchematic().getMaterials())
        {
            ItemStack materialStackCopy = materialStack.copy();
            for(int i = 0; i < this.getInventory().getSizeInventory(); i++)
            {
                ItemStack builderStack = this.getInventory().getStackInSlot(i);
                if(builderStack != null && builderStack.isItemEqual(materialStackCopy))
                {
                    materialStackCopy.stackSize -= builderStack.stackSize;
                }
            }
            if(materialStackCopy.stackSize > 0)
            {
                itemsNeeded.add(materialStackCopy);
            }
        }

        return itemsNeeded.isEmpty();
    }

    public int getWorkInterval()
    {
        return Constants.BUILDERWORKINTERFALL - this.level.getLevel();//TODO
    }

    public boolean isBuilderNeeded()
    {
        return this.getTownHall() != null && !this.getTownHall().getBuilderRequired().isEmpty();
    }
}
