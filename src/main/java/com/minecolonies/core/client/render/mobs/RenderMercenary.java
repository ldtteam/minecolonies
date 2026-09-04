package com.minecolonies.core.client.render.mobs;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.MercenaryModel;
import com.minecolonies.core.event.ClientRegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.PathfinderMob;
import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;

/**
 * Renderer for EntityMercenary.
 */
public class RenderMercenary extends HumanoidMobRenderer<PathfinderMob, RaiderRenderState, MercenaryModel>
{
    /**
     * Texture of the entity.
     */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/citizen/default/settlermale1_b.png");

    /**
     * Renders the mercenary mobs, with an held item and armorset.
     *
     * @param context RenderManager
     */
    public RenderMercenary(final EntityRendererProvider.Context context)
    {
        super(context, new MercenaryModel(context.bakeLayer(ClientRegistryHandler.MERCENARY)), 0.5f);

    }

    @Override
    public RaiderRenderState createRenderState()
    {
        return new RaiderRenderState();
    }

    @Override
    public Identifier getTextureLocation(RaiderRenderState entity)
    {
        return TEXTURE;
    }
}
