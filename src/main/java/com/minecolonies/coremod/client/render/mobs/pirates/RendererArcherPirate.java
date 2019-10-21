package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererArcherPirate extends AbstractRendererPirate
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/pirate2.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererArcherPirate(final RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }


    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final AbstractEntityPirate entity)
    {
        return TEXTURE;
    }
}
