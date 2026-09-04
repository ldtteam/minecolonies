package com.minecolonies.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.Identifier;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;

public class CavalryOverlayLayer extends RenderLayer<HorseRenderState, HorseModel>
{

    public CavalryOverlayLayer(RenderLayerParent<HorseRenderState, HorseModel> parent)
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
    public void submit(@Nonnull PoseStack pose,
                    @Nonnull SubmitNodeCollector submitNodeCollector,
                    int packedLight,
                    @Nonnull HorseRenderState state,
                    float yRot,
                    float xRot)
    {
        float readiness = 1.0F;

        int segments = net.minecraft.util.Mth.clamp((int) Math.floor(readiness * 5f + 0.0001f), 0, 5);

        Identifier OVERLAY_TEX = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/horse/cavalry_overlay_layer" + segments + ".png");

        int alpha = (int)(0.85f * 255.0f);
        int color = net.minecraft.util.ARGB.color(alpha, 255, 255, 255);

        submitNodeCollector.order(1).submitModel(this.getParentModel(), state, pose,
            RenderTypes.entityTranslucent(OVERLAY_TEX), packedLight, LivingEntityRenderer.getOverlayCoords(state, 0.0F), color, null);
    }

}
