package com.minecolonies.core.client.render.mobs.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.core.client.model.raiders.ModelChiefNorsemen;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief norsemen.
 */
public class RendererChiefNorsemen extends AbstractRendererNorsemen<AbstractEntityNorsemen, ModelChiefNorsemen>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_chief.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererChiefNorsemen(final EntityRendererProvider.Context context)
    {
        super(context, new ModelChiefNorsemen(context.bakeLayer(ClientRegistryHandler.NORSEMEN_CHIEF)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityNorsemen entity)
    {
        return TEXTURE;
    }
}
