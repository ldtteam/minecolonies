package com.minecolonies.core.client.render.mobs.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.core.client.model.raiders.ModelArcherNorsemen;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for archer amazons.
 */
public class RendererArcherNorsemen extends AbstractRendererNorsemen<AbstractEntityNorsemen, ModelArcherNorsemen>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies", "textures/entity/raiders/norsemen_archer1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("minecolonies", "textures/entity/raiders/norsemen_archer2.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererArcherNorsemen(final EntityRendererProvider.Context context)
    {
        super(context, new ModelArcherNorsemen(context.bakeLayer(ClientRegistryHandler.NORSEMEN_ARCHER)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityNorsemen entity)
    {
        if (entity.getTextureId() == 1)
        {
            return TEXTURE2;
        }
        return TEXTURE1;
    }
}
