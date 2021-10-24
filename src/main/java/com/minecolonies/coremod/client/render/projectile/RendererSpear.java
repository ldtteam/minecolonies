package com.minecolonies.coremod.client.render.projectile;

import com.minecolonies.api.entity.SpearEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelSpear;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Custom renderer for spears
 */
@OnlyIn(Dist.CLIENT)
public class RendererSpear extends EntityRenderer<SpearEntity>
{
    private final ResourceLocation texture;
    private final ModelSpear model = new ModelSpear();

    public RendererSpear(EntityRendererManager entityRendererManager) {
        this(entityRendererManager, new ResourceLocation(Constants.MOD_ID, "textures/entity/spear.png"));
    }

    public RendererSpear(final EntityRendererManager entityRendererManager, final ResourceLocation texture)
    {
        super(entityRendererManager);
        this.texture = texture;
    }

    @Override
    public void render(
      @NotNull final SpearEntity entity, final float entityYaw, final float partialTicks, @NotNull final MatrixStack stack, @NotNull final IRenderTypeBuffer buffer, final int light)
    {
        super.render(entity, entityYaw, partialTicks, stack, buffer, light);
        stack.pushPose();
        stack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        stack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot) + 90.0F));
        IVertexBuilder ivertexbuilder = net.minecraft.client.renderer.ItemRenderer.getFoilBuffer(buffer, this.model.renderType(this.getTextureLocation(entity)), false, entity.isInWater());
        model.renderToBuffer(stack, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final SpearEntity spearEntity)
    {
        return texture;
    }
}
