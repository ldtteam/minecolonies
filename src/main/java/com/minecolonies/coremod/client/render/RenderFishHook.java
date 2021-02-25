package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how the fish hook is rendered.
 */
@OnlyIn(Dist.CLIENT)
public class RenderFishHook extends EntityRenderer<Entity>
{
    /**
     * The resource location containing the particle textures (Spawned by the fishHook).
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/fishing_hook.png");

    /**
     * The render type of the hook.
     */
    private static final RenderType RENDER_TYPE = RenderType.getEntityCutout(TEXTURE);

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
    public void render(final Entity entityIn, final float entityYaw, final float partialTicks, @NotNull final MatrixStack matrixStackIn, @NotNull final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        NewBobberEntity bobber = (NewBobberEntity) entityIn.getEntity();
        if (bobber != null && bobber.getAngler() != null)
        {
            final EntityCitizen citizen = bobber.getAngler();
            matrixStackIn.push();
            matrixStackIn.push();
            matrixStackIn.scale(0.5F, 0.5F, 0.5F);
            matrixStackIn.rotate(this.renderManager.getCameraOrientation());
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
            final MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
            final Matrix4f matrix4f = matrixstack$entry.getMatrix();
            final Matrix3f matrix3f = matrixstack$entry.getNormal();
            final IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RENDER_TYPE);
            func_229106_a_(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 0.0F, 0, 0, 1);
            func_229106_a_(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 1.0F, 0, 1, 1);
            func_229106_a_(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 1.0F, 1, 1, 0);
            func_229106_a_(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 0.0F, 1, 0, 0);
            matrixStackIn.pop();
            int i = citizen.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
            final ItemStack itemstack = citizen.getHeldItemMainhand();
            if (!(itemstack.getItem() instanceof net.minecraft.item.FishingRodItem))
            {
                i = -i;
            }

            final float f2 = MathHelper.lerp(partialTicks, citizen.prevRenderYawOffset, citizen.renderYawOffset) * ((float) Math.PI / 180F);
            final double d0 = MathHelper.sin(f2);
            final double d1 = MathHelper.cos(f2);
            final double d2 = (double) i * 0.35D;

            double d4 = MathHelper.lerp(partialTicks, citizen.prevPosX, citizen.getPosX()) - d1 * d2 - d0 * 0.8D;
            double d5 = citizen.prevPosY + (double) citizen.getEyeHeight() + (citizen.getPosY() - citizen.prevPosY) * (double) partialTicks - 0.45D;
            double d6 = MathHelper.lerp(partialTicks, citizen.prevPosZ, citizen.getPosZ()) - d0 * d2 + d1 * 0.8D;
            float f3 = citizen.isCrouching() ? -0.1875F : 0.0F;

            double d9 = MathHelper.lerp(partialTicks, entityIn.prevPosX, entityIn.getPosX());
            double d10 = MathHelper.lerp(partialTicks, entityIn.prevPosY, entityIn.getPosY()) + 0.25D;
            double d8 = MathHelper.lerp(partialTicks, entityIn.prevPosZ, entityIn.getPosZ());
            float f4 = (float) (d4 - d9);
            float f5 = (float) (d5 - d10) + f3;
            float f6 = (float) (d6 - d8);
            final IVertexBuilder ivertexbuilder1 = bufferIn.getBuffer(RenderType.getLines());
            final Matrix4f matrix4f1 = matrixStackIn.getLast().getMatrix();

            for (int k = 0; k < 16; ++k)
            {
                func_229104_a_(f4, f5, f6, ivertexbuilder1, matrix4f1, func_229105_a_(k, 16));
                func_229104_a_(f4, f5, f6, ivertexbuilder1, matrix4f1, func_229105_a_(k + 1, 16));
            }

            matrixStackIn.pop();
            super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
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
          OverlayTexture.NO_OVERLAY).lightmap(p_229106_3_).normal(p_229106_2_, 0.0F, 1.0F, 0.0F).endVertex();
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
    public ResourceLocation getEntityTexture(@NotNull final Entity entity)
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
