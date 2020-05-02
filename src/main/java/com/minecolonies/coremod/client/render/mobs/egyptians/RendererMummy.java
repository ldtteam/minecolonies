package com.minecolonies.coremod.client.render.mobs.egyptians;

import com.minecolonies.coremod.entity.mobs.egyptians.EntityMummy;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for mummies.
 */
public class RendererMummy extends AbstractRendererEgyptian
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
    public RendererMummy(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel<EntityMummy>(1.0F), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        return TEXTURE;
    }
}
