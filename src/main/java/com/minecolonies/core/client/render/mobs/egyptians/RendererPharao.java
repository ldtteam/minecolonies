package com.minecolonies.core.client.render.mobs.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.core.client.model.raiders.ModelPharaoh;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer used for the pharao.
 */
public class RendererPharao extends AbstractRendererEgyptian<AbstractEntityEgyptian, ModelPharaoh>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/pharao.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererPharao(final EntityRendererProvider.Context context)
    {
        super(context, new ModelPharaoh(context.bakeLayer(ClientRegistryHandler.PHARAO)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityEgyptian entity)
    {
        return TEXTURE;
    }
}
