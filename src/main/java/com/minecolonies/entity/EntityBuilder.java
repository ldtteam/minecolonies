package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBuilder extends EntityCitizen
{
    public EntityBuilder(World world)
    {
        super(world);
        level = EnumCitizenLevel.CITIZENMALE;
    }

    @Override
    public void setTexture()
    {
        texture = new ResourceLocation(Constants.MODID + ":/textures/entity/EntityBuilder.png");
    }
}
