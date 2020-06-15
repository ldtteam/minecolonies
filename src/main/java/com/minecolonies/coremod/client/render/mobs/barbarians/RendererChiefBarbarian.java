package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererChiefBarbarian extends AbstractRendererBarbarian<AbstractEntityBarbarian, BipedModel<AbstractEntityBarbarian>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/barbarianchief1.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefBarbarian(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel<>(0.0F), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityBarbarian entity)
    {
        return TEXTURE;
    }
}
