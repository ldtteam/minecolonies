package com.minecolonies.core.client.render.mobs.amazon;

import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.render.RenderUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;

import net.minecraft.world.InteractionHand;

/**
 * Abstract for rendering amazons.
 */
public abstract class AbstractRendererAmazon<T extends AbstractEntityMinecoloniesMonster, M extends HumanoidModel<RaiderRenderState>> extends HumanoidMobRenderer<T, RaiderRenderState, M>
{
    public AbstractRendererAmazon(final EntityRendererProvider.Context context, final M modelBipedIn, final float shadowSize)
    {
        super(context, modelBipedIn, shadowSize);
        final ArmorModelSet<HumanoidModel<RaiderRenderState>> armorModels = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new
        );
        this.addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
    }

    @Override
    public RaiderRenderState createRenderState()
    {
        return new RaiderRenderState();
    }

    @Override
    public void extractRenderState(final T raider, final RaiderRenderState state, final float partialTicks)
    {
        super.extractRenderState(raider, state, partialTicks);
        state.setRaider(raider);
        state.rightArmPose = RenderUtils.getArmPose(raider, InteractionHand.MAIN_HAND);
        state.leftArmPose = RenderUtils.getArmPose(raider, InteractionHand.OFF_HAND);
    }
}
