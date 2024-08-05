package com.minecolonies.core.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.core.client.model.raiders.ModelAmazonSpearman;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for spearman amazons.
 */
public class RendererAmazonSpearman extends AbstractRendererAmazon<AbstractEntityAmazon, ModelAmazonSpearman>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies", "textures/entity/raiders/amazon_spearman.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererAmazonSpearman(final EntityRendererProvider.Context context)
    {
        super(context, new ModelAmazonSpearman(context.bakeLayer(ClientRegistryHandler.AMAZON_SPEARMAN)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final AbstractEntityAmazon entity)
    {
        return TEXTURE;
    }
}
