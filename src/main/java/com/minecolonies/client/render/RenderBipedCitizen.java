package com.minecolonies.client.render;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderBipedCitizen extends RenderBiped
{
    public RenderBipedCitizen(ModelBiped model)
    {
        super(model, 1F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        EntityCitizen entityCitizen = (EntityCitizen) entity;
        return entityCitizen.texture;
    }
}
