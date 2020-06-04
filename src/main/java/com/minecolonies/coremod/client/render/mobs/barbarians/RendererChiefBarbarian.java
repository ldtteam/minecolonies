package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererChiefBarbarian extends AbstractRendererBarbarian<EntityChiefBarbarian, BipedModel<EntityChiefBarbarian>>
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
        super(renderManagerIn, new BipedModel<>(1.2F), 0.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityChiefBarbarian entity)
    {
        return TEXTURE;
    }
}
