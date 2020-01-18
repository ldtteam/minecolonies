package com.minecolonies.coremod.client.render;

import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Empty renderer for entities which should not actually be rendered.
 *
 * @param <T>
 */
public class RenderSitting<T extends Entity> extends EntityRenderer<T>
{
    public RenderSitting(final EntityRendererManager p_i46179_1_)
    {
        super(p_i46179_1_);
    }

    @Override
    public ResourceLocation getEntityTexture(final T t)
    {
        return null;
    }

    @Override
    public boolean shouldRender(T p_225626_1_, ClippingHelperImpl p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_)
    {
        return false;
    }
}