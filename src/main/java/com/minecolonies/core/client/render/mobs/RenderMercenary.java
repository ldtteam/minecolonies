package com.minecolonies.core.client.render.mobs;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for EntityMercenary.
 */
public class RenderMercenary extends MobRenderer<PathfinderMob, HumanoidModel<PathfinderMob>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/citizen/default/settlermale1_b.png");

    /**
     * Renders the mercenary mobs, with an held item and armorset.
     *
     * @param context RenderManager
     */
    public RenderMercenary(final EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), 0.5f);

        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
    }

    @Override
    public ResourceLocation getTextureLocation(final PathfinderMob entity)
    {
        return TEXTURE;
    }
}
