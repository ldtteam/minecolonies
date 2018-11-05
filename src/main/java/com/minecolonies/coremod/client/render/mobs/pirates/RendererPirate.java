package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.coremod.entity.ai.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererPirate extends AbstractRendererPirate
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/pirate1.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererPirate(final RenderManager renderManagerIn)
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
