package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererBarbarian extends AbstractRendererBarbarian
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/barbarian1.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererBarbarian(final RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final AbstractEntityBarbarian entity)
    {
        return TEXTURE;
    }
}
