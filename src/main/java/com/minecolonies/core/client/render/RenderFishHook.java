package com.minecolonies.core.client.render;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.other.NewBobberEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderFishHook extends EntityRenderer<NewBobberEntity, FishingHookRenderState>
{
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutCull(TEXTURE);

    public RenderFishHook(final EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void submit(@NotNull final FishingHookRenderState state,
                       @NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
            vertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1);
            vertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1);
            vertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0);
            vertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0);
        });
        poseStack.popPose();

        final float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 0; i < 16; i++)
            {
                stringVertex(state.lineOriginOffset, buffer, pose, fraction(i, 16), fraction(i + 1, 16), width);
                stringVertex(state.lineOriginOffset, buffer, pose, fraction(i + 1, 16), fraction(i, 16), width);
            }
        });
    }

    @Override
    public FishingHookRenderState createRenderState()
    {
        return new FishingHookRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final NewBobberEntity entity,
                                   @NotNull final FishingHookRenderState state,
                                   final float partialTicks)
    {
        super.extractRenderState(entity, state, partialTicks);
        if (!(entity.getOwner() instanceof AbstractEntityCitizen citizen))
        {
            state.lineOriginOffset = Vec3.ZERO;
            return;
        }

        int side = citizen.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (!citizen.getMainHandItem().is(Items.FISHING_ROD))
        {
            side = -side;
        }

        final float bodyAngle = Mth.lerp(partialTicks, citizen.yBodyRotO, citizen.yBodyRot) * ((float) Math.PI / 180F);
        final double sideOffset = side * 0.35D;
        final double handX = Mth.lerp(partialTicks, citizen.xo, citizen.getX())
            - Mth.cos(bodyAngle) * sideOffset - Mth.sin(bodyAngle) * 0.8D;
        final double handY = citizen.yo + citizen.getEyeHeight()
            + (citizen.getY() - citizen.yo) * partialTicks - 0.45D;
        final double handZ = Mth.lerp(partialTicks, citizen.zo, citizen.getZ())
            - Mth.sin(bodyAngle) * sideOffset + Mth.cos(bodyAngle) * 0.8D;
        final double handLift = citizen.isCrouching() ? -0.1875D : 0.0D;

        final double bobberX = Mth.lerp(partialTicks, entity.xo, entity.getX());
        final double bobberY = Mth.lerp(partialTicks, entity.yo, entity.getY()) + 0.25D;
        final double bobberZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        state.lineOriginOffset = new Vec3(handX - bobberX, handY - bobberY + handLift, handZ - bobberZ);
    }

    private static float fraction(final int value, final int total)
    {
        return (float) value / total;
    }

    private static void vertex(
      final VertexConsumer consumer,
      final PoseStack.Pose pose,
      final int light,
      final float y,
      final int v,
      final int u,
      final int textureV)
    {
        consumer.addVertex(pose, y - 0.5F, v - 0.5F, 0.0F)
          .setColor(-1)
          .setUv(u, textureV)
          .setOverlay(OverlayTexture.NO_OVERLAY)
          .setLight(light)
          .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(
      final Vec3 offset,
      final VertexConsumer consumer,
      final PoseStack.Pose pose,
      final float first,
      final float second,
      final float width)
    {
        final float x = (float) offset.x * first;
        final float y = (float) offset.y * (first * first + first) * 0.5F + 0.25F;
        final float z = (float) offset.z * first;
        float normalX = (float) offset.x * second - x;
        float normalY = (float) offset.y * (second * second + second) * 0.5F + 0.25F - y;
        float normalZ = (float) offset.z * second - z;
        final float length = Mth.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        normalX /= length;
        normalY /= length;
        normalZ /= length;
        consumer.addVertex(pose, x, y, z)
          .setColor(-16777216)
          .setNormal(pose, normalX, normalY, normalZ)
          .setLineWidth(width);
    }
}
