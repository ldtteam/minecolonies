package com.minecolonies.coremod.client.render.mobs.barbarians;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererChiefBarbarian extends AbstractRendererBarbarian
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/barbarianchief1.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefBarbarian(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel(1.2F), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(final MobEntity entity)
    {
        return TEXTURE;
    }
}
