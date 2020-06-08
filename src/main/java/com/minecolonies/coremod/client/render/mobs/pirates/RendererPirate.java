package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererPirate extends AbstractRendererPirate<AbstractEntityPirate, BipedModel<AbstractEntityPirate>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/pirate1.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererPirate(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel<>(1.0F), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityPirate entity)
    {
        1 + 2 + 3
        return TEXTURE;
    }
}
