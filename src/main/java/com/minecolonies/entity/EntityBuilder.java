package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBuilder extends EntityCitizen
{
    private Schematic schematic;

    public EntityBuilder(World world)
    {
        super(world);
        // TODO: check if builder is male, OR create model for female builder
        level = EnumCitizenLevel.CITIZENMALE;
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
        if(!hasSchematic()) return false;

        schematic.getMaterials();
        this.getInventory();//TODO create contains method
        this.getWorkHut();//TODO create contains method
        return true;//TODO if builder has materials in inventory (or chest?)

    }

    public int getWorkInterval()
    {
        return Constants.BUILDERWORKINTERFALL - this.level.getLevel();//TODO
    }
}
