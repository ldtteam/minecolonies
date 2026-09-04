package com.minecolonies.core.client.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Empty renderer for entities which should not actually be rendered.
 *
 * @param <T> the entity that shall sit.
 */
public class RenderSitting<T extends Entity> extends EntityRenderer<T, EntityRenderState>
{
    public RenderSitting(final EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState()
    {
        return new EntityRenderState();
    }

    @Override
    public boolean shouldRender(@NotNull T entity, @NotNull Frustum clippingHelper, double x, double y, double z)
    {
        return false;
    }
}
