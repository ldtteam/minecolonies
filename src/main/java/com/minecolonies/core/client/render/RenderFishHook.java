package com.minecolonies.core.client.render;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.other.NewBobberEntity;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");

    /**
     * The render type of the hook.
     */
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);

    /**
     * Required constructor, sets the RenderManager.
     *
     * @param context context that we use.
     */
    public RenderFishHook(final EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(@NotNull Entity entity, float p_114706_, float p_114707_, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int partialTicks)
    {
        if (!(entity instanceof NewBobberEntity))
        {
            return;
        }
        AbstractEntityCitizen citizen = ((NewBobberEntity) entity).getAngler();
        if (citizen != null)
        {
            poseStack.pushPose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            PoseStack.Pose posestack$pose = poseStack.last();
            Matrix4f matrix4f = posestack$pose.pose();
            Matrix3f matrix3f = posestack$pose.normal();
            VertexConsumer vertexconsumer = buffer.getBuffer(RENDER_TYPE);
            vertex(vertexconsumer, matrix4f, posestack$pose, partialTicks, 0.0F, 0, 0, 1);
            vertex(vertexconsumer, matrix4f, posestack$pose, partialTicks, 1.0F, 0, 1, 1);
            vertex(vertexconsumer, matrix4f, posestack$pose, partialTicks, 1.0F, 1, 1, 0);
            vertex(vertexconsumer, matrix4f, posestack$pose, partialTicks, 0.0F, 1, 0, 0);
            poseStack.popPose();
            int i = citizen.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemstack = citizen.getMainHandItem();
            if (!itemstack.is(Items.FISHING_ROD))
            {
                i = -i;
            }

            float f = citizen.getAttackAnim(p_114707_);
            float f2 = Mth.lerp(p_114707_, citizen.yBodyRotO, citizen.yBodyRot) * ((float) Math.PI / 180F);
            double d0 = (double) Mth.sin(f2);
            double d1 = (double) Mth.cos(f2);
            double d2 = (double) i * 0.35D;
            double d4;
            double d5;
            double d6;
            float f3;

            d4 = Mth.lerp(p_114707_, citizen.xo, citizen.getX()) - d1 * d2 - d0 * 0.8D;
            d5 = citizen.yo + (double) citizen.getEyeHeight() + (citizen.getY() - citizen.yo) * (double) p_114707_ - 0.45D;
            d6 = Mth.lerp(p_114707_, citizen.zo, citizen.getZ()) - d0 * d2 + d1 * 0.8D;
            f3 = citizen.isCrouching() ? -0.1875F : 0.0F;

            double d9 = Mth.lerp((double) p_114707_, entity.xo, entity.getX());
            double d10 = Mth.lerp((double) p_114707_, entity.yo, entity.getY()) + 0.25D;
            double d8 = Mth.lerp((double) p_114707_, entity.zo, entity.getZ());
            float f4 = (float) (d4 - d9);
            float f5 = (float) (d5 - d10) + f3;
            float f6 = (float) (d6 - d8);
            VertexConsumer vertexconsumer1 = buffer.getBuffer(RenderType.lineStrip());
            PoseStack.Pose posestack$pose1 = poseStack.last();
            int j = 16;

            for (int k = 0; k <= 16; ++k)
            {
                stringVertex(f4, f5, f6, vertexconsumer1, posestack$pose1, fraction(k, 16), fraction(k + 1, 16));
            }

            poseStack.popPose();
            super.render(entity, p_114706_, p_114707_, poseStack, buffer, partialTicks);
        }
    }

    private static float fraction(int first, int second)
    {
        return (float) first / (float) second;
    }

    private static void vertex(VertexConsumer consumer, Matrix4f p_114713_, PoseStack.Pose p_114714_, int p_114715_, float p_114716_, int p_114717_, int p_114718_, int p_114719_)
    {
        consumer.addVertex(p_114713_, p_114716_ - 0.5F, (float) p_114717_ - 0.5F, 0.0F)
          .setColor(255, 255, 255, 255)
          .setUv((float) p_114718_, (float) p_114719_)
          .setOverlay(OverlayTexture.NO_OVERLAY)
          .setLight(p_114715_)
          .setNormal(p_114714_, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(float p_174119_, float p_174120_, float p_174121_, VertexConsumer p_174122_, PoseStack.Pose p_174123_, float p_174124_, float p_174125_)
    {
        float f = p_174119_ * p_174124_;
        float f1 = p_174120_ * (p_174124_ * p_174124_ + p_174124_) * 0.5F + 0.25F;
        float f2 = p_174121_ * p_174124_;
        float f3 = p_174119_ * p_174125_ - f;
        float f4 = p_174120_ * (p_174125_ * p_174125_ + p_174125_) * 0.5F + 0.25F - f1;
        float f5 = p_174121_ * p_174125_ - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        p_174122_.addVertex(p_174123_, f, f1, f2).setColor(-16777216).setNormal(p_174123_, f3, f4, f5);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     *
     * @param entity the entity to get the texture from
     * @return a resource location for the texture
     */
    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final Entity entity)
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
