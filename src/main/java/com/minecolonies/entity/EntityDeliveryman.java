package com.minecolonies.entity;

import net.minecraft.world.World;

public class EntityDeliveryman extends EntityCitizen
{
    public EntityDeliveryman(World world)
    {
        super(world);
    }

    @Override
    protected String initJob()
    {
        return "Deliveryman";
    }

    @Override
    public int getTextureID()//TODO: add female texture (and more textures?)
    {
        return 1;
    }
}
