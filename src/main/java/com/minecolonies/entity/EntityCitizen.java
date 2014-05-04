package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityCitizen extends EntityAgeable
{
    public ResourceLocation texture;

    public EntityCitizen(World world)
    {
        super(world);
        setSize(.6f, 1.8f);
        setTexture();
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        //TODO ???
        return null;
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0d);
    }

    public void setTexture()
    {
        this.texture = new ResourceLocation(Constants.MODID + ":" + "/textures/entity/EntityCitizen.png");
    }
}
