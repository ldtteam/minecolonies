package com.minecolonies.core.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.core.client.model.raiders.ModelAmazonChief;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief amazons.
 */
public class RendererChiefAmazon extends AbstractRendererAmazon<AbstractEntityAmazon, ModelAmazonChief>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies", "textures/entity/raiders/amazon_chief.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererChiefAmazon(final EntityRendererProvider.Context context)
    {
        super(context, new ModelAmazonChief(context.bakeLayer(ClientRegistryHandler.AMAZON_CHIEF)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityAmazon entity)
    {
        return TEXTURE;
    }
}
