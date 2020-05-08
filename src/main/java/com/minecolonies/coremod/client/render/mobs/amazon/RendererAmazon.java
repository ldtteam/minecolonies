package com.minecolonies.coremod.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.coremod.client.model.raiders.ModelAmazon;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for archer amazons.
 */
public class RendererAmazon extends AbstractRendererAmazon
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/amazon.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererAmazon(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ModelAmazon(), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        return TEXTURE;
    }
}
