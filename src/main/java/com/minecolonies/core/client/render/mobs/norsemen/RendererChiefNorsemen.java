package com.minecolonies.core.client.render.mobs.norsemen;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.model.raiders.ModelChiefNorsemen;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief norsemen.
 */
public class RendererChiefNorsemen extends AbstractRendererNorsemen<AbstractEntityMinecoloniesMonster, ModelChiefNorsemen>
{
    /**
     * Texture of the entity.
     */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/norsemen_chief.png");

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
    public Identifier getTextureLocation(RaiderRenderState entity)
    {
        return TEXTURE;
    }
}
