package com.minecolonies.coremod.client.render;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Empty renderer for entities which should not actually be rendered.
 *
 * @param <T> the entity that shall sit.
 */
public class RenderSitting<T extends Entity> extends EntityRenderer<T>
{
    public RenderSitting(final EntityRendererManager p_i46179_1_)
    {
        super(p_i46179_1_);
    }

    @Override
    public ResourceLocation getTextureLocation(final T t)
    {
        return null;
    }

    @Override
    public boolean shouldRender(T entity, ClippingHelper clippingHelper, double x, double y, double z)
    {
        return false;
    }
}