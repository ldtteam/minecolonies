package com.minecolonies.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.HorseModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

import javax.annotation.Nonnull;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;

public class CavalryOverlayLayer extends RenderLayer<Horse, HorseModel<Horse>> 
{

    public CavalryOverlayLayer(RenderLayerParent<Horse, HorseModel<Horse>> parent) 
    {
        super(parent);
    }

    /**
     * Renders the cavalry horse overlay layer, which decorates the horse
     * and indicates the horse's readiness for combat.
     *
     * @param pose    the pose stack
     * @param buffer  the multi buffer source
     * @param packedLight  the packed light
     * @param horse  the horse entity
     * @param limbSwing  the limb swing
     * @param limbSwingAmount  the limb swing amount
     * @param partialTicks  the partial ticks
     * @param ageInTicks  the age in ticks
     * @param netHeadYaw  the net head yaw
     * @param headPitch  the head pitch
     */
    @Override
    public void render(@Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int packedLight,
                       @Nonnull Horse horse, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) 
    {
        if (!(horse instanceof CavalryHorseEntity cavhorse)) return;

        // Compute readiness from cooldown
        float threshold = horse.getMaxHealth() * CavalryHorseEntity.COMBAT_READINESS_THRESHOLD;
        float cooldown  = Math.max(0f, cavhorse.getAnimalDataView() == null ? 0 : cavhorse.getAnimalDataView().getCombatCooldown());
        float readiness = net.minecraft.util.Mth.clamp(1.0f - (cooldown / Math.max(0.001f, threshold)), 0f, 1f);

        int segments = net.minecraft.util.Mth.clamp((int)Math.floor(readiness * 5f + 0.0001f), 0, 5);

        ResourceLocation OVERLAY_TEX = new ResourceLocation(Constants.MOD_ID, "textures/entity/horse/cavalry_overlay_layer" + segments + ".png");

        VertexConsumer vc = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(OVERLAY_TEX));
        this.getParentModel().renderToBuffer(pose, vc, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, .85f);
    }
}
