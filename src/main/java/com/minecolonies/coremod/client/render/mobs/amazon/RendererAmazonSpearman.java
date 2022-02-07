package com.minecolonies.coremod.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.coremod.client.model.raiders.ModelAmazonSpearman;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for spearman amazons.
 */
public class RendererAmazonSpearman extends AbstractRendererAmazon<AbstractEntityAmazon, ModelAmazonSpearman>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/amazon_spearman.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererAmazonSpearman(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ModelAmazonSpearman(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final AbstractEntityAmazon entity)
    {
        return TEXTURE;
    }
}
