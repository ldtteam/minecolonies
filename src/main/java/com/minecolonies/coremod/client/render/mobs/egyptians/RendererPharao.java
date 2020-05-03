package com.minecolonies.coremod.client.render.mobs.egyptians;

import com.minecolonies.coremod.client.model.raiders.ModelPharaoh;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for the pharao.
 */
public class RendererPharao extends AbstractRendererEgyptian
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/pharao.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererPharao(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ModelPharaoh(), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        return TEXTURE;
    }
}
