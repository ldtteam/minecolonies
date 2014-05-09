package com.minecolonies.client;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderBipedCitizen extends RenderBiped
{
    public RenderBipedCitizen(ModelBiped model, float label)
    {
        super(model, label);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        EntityCitizen entityCitizen = (EntityCitizen) entity;
        return entityCitizen.texture;
    }
}
