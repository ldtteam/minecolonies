package com.minecolonies.core.client.render.mobs.norsemen;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.model.raiders.ModelShieldmaiden;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for the shieldmaiden.
 */
public class RendererShieldmaidenNorsemen extends AbstractRendererNorsemen<AbstractEntityMinecoloniesMonster, ModelShieldmaiden>
{
    /**
     * Texture of the entity.
     */
    private static final Identifier TEXTURE1 = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/norsemen_shieldmaiden1.png");
    private static final Identifier TEXTURE2 = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/norsemen_shieldmaiden2.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererShieldmaidenNorsemen(final EntityRendererProvider.Context context)
    {
        super(context, new ModelShieldmaiden(context.bakeLayer(ClientRegistryHandler.SHIELD_MAIDEN)), 0.5F);
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(RaiderRenderState entity)
    {
        if (entity.getTextureId() == 1)
        {
            return TEXTURE2;
        }
        return TEXTURE1;
    }
}
