package com.minecolonies.coremod.client.render.projectile;

import com.minecolonies.api.entity.SpearEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.SpearModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Custom renderer for spears
 */
@OnlyIn(Dist.CLIENT)
public class RendererSpear extends EntityRenderer<SpearEntity>
{
    private final ResourceLocation texture = new ResourceLocation(Constants.MOD_ID, "textures/entity/spear.png");
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
    public void render(@NotNull  final SpearEntity entity, final float entityYaw, final float partialTicks, @NotNull final PoseStack stack, @NotNull final MultiBufferSource buffer, final int light)
    {
        super.render(entity, entityYaw, partialTicks, stack, buffer, light);

        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(this.getTextureLocation(entity)), false, entity.isFoil());
        model.renderToBuffer(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final SpearEntity spearEntity)
    {
        return texture;
    }
}
