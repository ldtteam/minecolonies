package com.minecolonies.core.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.model.HumanoidModel;

/**
 * Abstract for rendering amazons.
 */
public abstract class AbstractRendererAmazon<T extends AbstractEntityAmazon, M extends HumanoidModel<T>> extends HumanoidMobRenderer<T, M>
{
    public AbstractRendererAmazon(final EntityRendererProvider.Context context, final M modelBipedIn, final float shadowSize)
    {
        super(context, modelBipedIn, shadowSize);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }
}