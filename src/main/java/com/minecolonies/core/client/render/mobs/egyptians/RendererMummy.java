package com.minecolonies.core.client.render.mobs.egyptians;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.model.raiders.ModelMummy;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renderer used for mummies.
 */
public class RendererMummy extends AbstractRendererEgyptian<AbstractEntityMinecoloniesMonster, ModelMummy>
{
    /**
     * Texture of the entity.
     */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/mummy.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererMummy(final EntityRendererProvider.Context context)
    {
        super(context, new ModelMummy(context.bakeLayer(ClientRegistryHandler.MUMMY)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(RaiderRenderState entity)
    {
        return TEXTURE;
    }
}
