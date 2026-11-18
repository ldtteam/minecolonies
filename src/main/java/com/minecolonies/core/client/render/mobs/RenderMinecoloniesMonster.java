package com.minecolonies.core.client.render.mobs;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;

/**
 * Abstract for rendering our raiders.
 */
public abstract class RenderMinecoloniesMonster<T extends AbstractEntityMinecoloniesMonster, M extends HumanoidModel<T>> extends HumanoidMobRenderer<T, M>
{
    public RenderMinecoloniesMonster(final EntityRendererProvider.Context context, final M modelBipedIn, final float shadowSize)
    {
        super(context, modelBipedIn, shadowSize);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    public RenderMinecoloniesMonster(final EntityRendererProvider.Context context, final M modelBipedIn, final float shadowSize, final boolean armor)
    {
        super(context, modelBipedIn, shadowSize);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        if (armor)
        {
            // No need to add armor to entities that don't wear any, saves us rendering performance.
            this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
        }
    }

    @Override
    public void render(T raider, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_)
    {
        model.rightArmPose = RenderUtils.getArmPose(raider, InteractionHand.MAIN_HAND);
        model.leftArmPose = RenderUtils.getArmPose(raider, InteractionHand.OFF_HAND);

        float height = (float) raider.getHeight();
        p_115458_.scale(1, height, 1);
        super.render(raider, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
        p_115458_.scale(1, -height, 1);
    }
}