package com.minecolonies.core.client.render.projectile;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.SpearModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
/**
 * Custom renderer for spears
 */
@OnlyIn(Dist.CLIENT)
public class RendererSpear extends EntityRenderer<ThrownTrident, ThrownTridentRenderState>
{
    private final Identifier texture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/spear.png");
    private final SpearModel       model ;
    /**
     * Create a new spear renderer.
     * @param context the context.
     */
    public RendererSpear(final EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new SpearModel(context.bakeLayer(ModelLayers.TRIDENT));
    }
    @Override
    public void submit(@NotNull final ThrownTridentRenderState state,
                       @NotNull final PoseStack stack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        stack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 90.0F));
        collector.order(0).submitModel(
            this.model,
            Unit.INSTANCE,
            stack,
            this.texture,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor,
            null);
        if (state.isFoil)
        {
            collector.order(1).submitModel(
                this.model,
                Unit.INSTANCE,
                stack,
                RenderTypes.entityGlint(),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
        }
        stack.popPose();
        super.submit(state, stack, collector, camera);
    }
    @Override
    public ThrownTridentRenderState createRenderState()
    {
        return new ThrownTridentRenderState();
    }
    @Override
    public void extractRenderState(@NotNull final ThrownTrident spearEntity,
                                   @NotNull final ThrownTridentRenderState state,
                                   final float partialTicks)
    {
        super.extractRenderState(spearEntity, state, partialTicks);
        state.yRot = spearEntity.getYRot(partialTicks);
        state.xRot = spearEntity.getXRot(partialTicks);
        state.isFoil = spearEntity.isFoil();
    }
}
