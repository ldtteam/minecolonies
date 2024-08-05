package com.minecolonies.core.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.core.client.model.raiders.ModelAmazon;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for archer amazons.
 */
public class RendererAmazon extends AbstractRendererAmazon<AbstractEntityAmazon, ModelAmazon>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies", "textures/entity/raiders/amazon.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererAmazon(final EntityRendererProvider.Context context)
    {
        super(context, new ModelAmazon(context.bakeLayer(ClientRegistryHandler.AMAZON)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final AbstractEntityAmazon entity)
    {
        return TEXTURE;
    }
}
