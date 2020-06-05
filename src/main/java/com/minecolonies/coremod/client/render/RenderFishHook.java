package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how the fish hook is rendered.
 */
@OnlyIn(Dist.CLIENT)
public class RenderFishHook extends EntityRenderer<NewBobberEntity>
{
    /**
     * The resource location containing the particle textures (Spawned by the fishHook).
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/fishing_hook.png");

    /**
     * The render type of the hook.
     */
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);

    /**
     * Required constructor, sets the RenderManager.
     *
     * @param renderManagerIn RenderManager that we use.
     */
    public RenderFishHook(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    @Override
    public void render(NewBobberEntity entity, float ratio, float partialTicks, @NotNull MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int light)
    {
        EntityCitizen citizen = entity.getAngler();

        matrixStack.push();
        matrixStack.push();
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.rotate(this.renderManager.getCameraOrientation());
        matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));
        MatrixStack.Entry matrixstack$entry = matrixStack.getLast();
        Matrix4f matrix4f = matrixstack$entry.getPositionMatrix();
        Matrix3f matrix3f = matrixstack$entry.getNormalMatrix();
        IVertexBuilder ivertexbuilder = iRenderTypeBuffer.getBuffer(RENDER_TYPE);
        func_229106_a_(ivertexbuilder, matrix4f, matrix3f, light, 0.0F, 0, 0, 1);
        func_229106_a_(ivertexbuilder, matrix4f, matrix3f, light, 1.0F, 0, 1, 1);
        func_229106_a_(ivertexbuilder, matrix4f, matrix3f, light, 1.0F, 1, 1, 0);
        func_229106_a_(ivertexbuilder, matrix4f, matrix3f, light, 0.0F, 1, 0, 0);
        matrixStack.pop();
        int i = citizen.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
        ItemStack itemstack = citizen.getHeldItemMainhand();
        if (!(itemstack.getItem() instanceof net.minecraft.item.FishingRodItem))
        {
            i = -i;
        }

        float f = citizen.getSwingProgress(partialTicks);
        float f1 = MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI);
        float f2 = MathHelper.lerp(partialTicks, citizen.prevRenderYawOffset, citizen.renderYawOffset) * ((float) Math.PI / 180F);
        double d0 = (double) MathHelper.sin(f2);
        double d1 = (double) MathHelper.cos(f2);
        double d2 = (double) i * 0.35D;
        double d3 = 0.8D;
        double d4;
        double d5;
        double d6;
        float f3;
        if ((this.renderManager.options == null || this.renderManager.options.thirdPersonView <= 0))
        {
            double d7 = this.renderManager.options.fov;
            d7 = d7 / 100.0D;
            Vec3d vec3d = new Vec3d((double) i * -0.36D * d7, -0.045D * d7, 0.4D);
            vec3d = vec3d.rotatePitch(-MathHelper.lerp(partialTicks, citizen.prevRotationPitch, citizen.rotationPitch) * ((float) Math.PI / 180F));
            vec3d = vec3d.rotateYaw(-MathHelper.lerp(partialTicks, citizen.prevRotationYaw, citizen.rotationYaw) * ((float) Math.PI / 180F));
            vec3d = vec3d.rotateYaw(f1 * 0.5F);
            vec3d = vec3d.rotatePitch(-f1 * 0.7F);
            d4 = MathHelper.lerp((double) partialTicks, citizen.prevPosX, citizen.posX) + vec3d.x;
            d5 = MathHelper.lerp((double) partialTicks, citizen.prevPosY, citizen.posY) + vec3d.y;
            d6 = MathHelper.lerp((double) partialTicks, citizen.prevPosZ, citizen.posZ) + vec3d.z;
            f3 = citizen.getEyeHeight();
        }
        else
        {
            d4 = MathHelper.lerp((double) partialTicks, citizen.prevPosX, citizen.posX) - d1 * d2 - d0 * 0.8D;
            d5 = citizen.prevPosY + (double) citizen.getEyeHeight() + (citizen.posY - citizen.prevPosY) * (double) partialTicks - 0.45D;
            d6 = MathHelper.lerp((double) partialTicks, citizen.prevPosZ, citizen.posZ) - d0 * d2 + d1 * 0.8D;
            f3 = citizen.isCrouching() ? -0.1875F : 0.0F;
        }

        double d9 = MathHelper.lerp((double) partialTicks, entity.prevPosX, entity.posX);
        double d10 = MathHelper.lerp((double) partialTicks, entity.prevPosY, entity.posY) + 0.25D;
        double d8 = MathHelper.lerp((double) partialTicks, entity.prevPosZ, entity.posZ);
        float f4 = (float) (d4 - d9);
        float f5 = (float) (d5 - d10) + f3;
        float f6 = (float) (d6 - d8);
        IVertexBuilder ivertexbuilder1 = iRenderTypeBuffer.getBuffer(RenderType.lines());
        Matrix4f matrix4f1 = matrixStack.getLast().getPositionMatrix();
        int j = 16;

        for (int k = 0; k < 16; ++k)
        {
            func_229104_a_(f4, f5, f6, ivertexbuilder1, matrix4f1, func_229105_a_(k, 16));
            func_229104_a_(f4, f5, f6, ivertexbuilder1, matrix4f1, func_229105_a_(k + 1, 16));
        }

        matrixStack.pop();
        super.render(entity, ratio, partialTicks, matrixStack, iRenderTypeBuffer, light);
    }

    private static float func_229105_a_(int p_229105_0_, int p_229105_1_)
    {
        return (float) p_229105_0_ / (float) p_229105_1_;
    }

    private static void func_229106_a_(
      IVertexBuilder p_229106_0_,
      Matrix4f p_229106_1_,
      Matrix3f p_229106_2_,
      int p_229106_3_,
      float p_229106_4_,
      int p_229106_5_,
      int p_229106_6_,
      int p_229106_7_)
    {
        p_229106_0_.pos(p_229106_1_, p_229106_4_ - 0.5F, (float) p_229106_5_ - 0.5F, 0.0F).color(255, 255, 255, 255).tex((float) p_229106_6_, (float) p_229106_7_).overlay(
          OverlayTexture.DEFAULT_LIGHT).lightmap(p_229106_3_).normal(p_229106_2_, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private static void func_229104_a_(float p_229104_0_, float p_229104_1_, float p_229104_2_, IVertexBuilder p_229104_3_, Matrix4f p_229104_4_, float p_229104_5_)
    {
        p_229104_3_.pos(p_229104_4_, p_229104_0_ * p_229104_5_, p_229104_1_ * (p_229104_5_ * p_229104_5_ + p_229104_5_) * 0.5F + 0.25F, p_229104_2_ * p_229104_5_)
          .color(0, 0, 0, 255)
          .endVertex();
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     *
     * @param entity the entity to get the texture from
     * @return a resource location for the texture
     */
    @NotNull
    @Override
    public ResourceLocation getEntityTexture(@NotNull final NewBobberEntity entity)
    {
        return getTexture();
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @return the address of the resource
     */
    @NotNull
    private static ResourceLocation getTexture()
    {
        return TEXTURE;
    }
}
