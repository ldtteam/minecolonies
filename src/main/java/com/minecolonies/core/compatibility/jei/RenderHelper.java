package com.minecolonies.core.compatibility.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * JEI GUI rendering utility helpers
 */
public class RenderHelper
{
    /**
     * Render a block model on a GUI.
     *
     * @param matrixStack matrix
     * @param block the blockstate to render
     * @param x horizontal center position
     * @param y vertical bottom position
     * @param z distance from camera
     * @param pitch rotation forwards
     * @param yaw rotation sideways
     * @param scale scaling factor
     */
    public static void renderBlock(final PoseStack matrixStack, final BlockState block, final float x, final float y, final float z, final float pitch, final float yaw, final float scale)
    {
        final Minecraft mc = Minecraft.getInstance();

        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        matrixStack.scale(-scale, -scale, -scale);
        matrixStack.translate(-0.5F, -0.5F, 0);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(pitch));

        matrixStack.translate(0.5F, 0, -0.5F);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
        matrixStack.translate(-0.5F, 0, 0.5F);

        matrixStack.pushPose();
        matrixStack.translate(0, 0, -1);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        final MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        mc.getBlockRenderer().renderSingleBlock(block, matrixStack, buffers, 0x00F000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
        buffers.endBatch();
        matrixStack.popPose();

        matrixStack.popPose();
    }

    /**
     * Render an entity on a GUI.
     * @param poseStack matrix
     * @param x horizontal center position
     * @param y vertical bottom position
     * @param scale scaling factor
     * @param headYaw adjusts look rotation
     * @param yaw adjusts body rotation
     * @param pitch adjusts look rotation
     * @param livingEntity the entity to render
     */
    public static void renderEntity(final PoseStack poseStack, final int x, final int y, final double scale,
                                    final float headYaw, final float yaw, final float pitch, final LivingEntity livingEntity)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (livingEntity.level == null) livingEntity.level = mc.level;
        poseStack.pushPose();
        poseStack.translate((float) x, (float) y, 1050.0F);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternion pitchRotation = Vector3f.XP.rotationDegrees(pitch);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(pitchRotation);
        final float oldYawOffset = livingEntity.yBodyRot;
        final float oldYaw = livingEntity.getYRot();
        final float oldPitch = livingEntity.getXRot();
        final float oldPrevYawHead = livingEntity.yHeadRotO;
        final float oldYawHead = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + yaw;
        livingEntity.setYRot(180.0F + (float) headYaw);
        livingEntity.setXRot(-pitch);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        final EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        pitchRotation.conj();
        dispatcher.overrideCameraOrientation(pitchRotation);
        dispatcher.setRenderShadow(false);
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> dispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, buffers, 0x00F000F0));
        buffers.endBatch();
        dispatcher.setRenderShadow(true);
        livingEntity.yBodyRot = oldYawOffset;
        livingEntity.setYRot(oldYaw);
        livingEntity.setXRot(oldPitch);
        livingEntity.yHeadRotO = oldPrevYawHead;
        livingEntity.yHeadRot = oldYawHead;
        poseStack.popPose();
    }

    /**
     * Enable scissor (clipping) to GUI region (prevent drawing outside).
     *
     * @param poseStack matrix
     * @param x left position
     * @param y top position
     * @param w width
     * @param h height
     */
    public static void scissor(final PoseStack poseStack, int x, int y, int w, int h)
    {
        final double scale = Minecraft.getInstance().getWindow().getGuiScale();
        final double[] xyzTranslation = getGLTranslation(poseStack, scale);
        x *= scale;
        y *= scale;
        w *= scale;
        h *= scale;
        final int scissorX = Math.round(Math.round(xyzTranslation[0] + x));
        final int scissorY = Math.round(Math.round(Minecraft.getInstance().getWindow().getScreenHeight() - y - h - xyzTranslation[1]));
        final int scissorW = Math.round(w);
        final int scissorH = Math.round(h);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    }

    /**
     * Disable scissor.
     */
    public static void stopScissor()
    {
        RenderSystem.disableScissor();
    }

    private static double[] getGLTranslation(final PoseStack poseStack, final double scale)
    {
        final Matrix4f matrix = poseStack.last().pose();
        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        matrix.store(buf);
        // { x, y, z }
        return new double[]
        {
            buf.get(getIndexFloatBuffer(0,3)) * scale,
            buf.get(getIndexFloatBuffer(1, 3)) * scale,
            buf.get(getIndexFloatBuffer(2, 3)) * scale
        };
    }

    private static int getIndexFloatBuffer(final int x, final int y)
    {
        return y * 4 + x;
    }
}
