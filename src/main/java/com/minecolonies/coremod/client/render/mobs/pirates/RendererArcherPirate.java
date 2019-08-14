package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

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
    public RendererArcherPirate(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel<AbstractEntityPirate>(1.0F), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(final MobEntity entity)
    {
        return TEXTURE;
    }
}
