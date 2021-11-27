package com.minecolonies.coremod.compatibility.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
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
    public static void renderBlock(final MatrixStack matrixStack, final BlockState block, final float x, final float y, final float z, final float pitch, final float yaw, final float scale)
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

        mc.getTextureManager().bind(PlayerContainer.BLOCK_ATLAS);
        final IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        mc.getBlockRenderer().renderBlock(block, matrixStack, buffers, 0x00F000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        buffers.endBatch();
        matrixStack.popPose();

        matrixStack.popPose();
    }

    /**
     * Render an entity on a GUI.
     * @param matrixStack matrix
     * @param x horizontal center position
     * @param y vertical bottom position
     * @param scale scaling factor
     * @param headYaw adjusts look rotation
     * @param yaw adjusts body rotation
     * @param pitch adjusts look rotation
     * @param livingEntity the entity to render
     */
    public static void renderEntity(final MatrixStack matrixStack, final int x, final int y, final double scale,
                                    final float headYaw, final float yaw, final float pitch, final LivingEntity livingEntity)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (livingEntity.level == null) livingEntity.level = mc.level;
        matrixStack.pushPose();
        matrixStack.translate((float) x, (float) y, 1050.0F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternion pitchRotation = Vector3f.XP.rotationDegrees(pitch);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.mulPose(pitchRotation);
        final float oldYawOffset = livingEntity.yBodyRot;
        final float oldYaw = livingEntity.yRot;
        final float oldPitch = livingEntity.xRot;
        final float oldPrevYawHead = livingEntity.yHeadRotO;
        final float oldYawHead = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + yaw;
        livingEntity.yRot = 180.0F + (float) headYaw;
        livingEntity.xRot = -pitch;
        livingEntity.yHeadRot = livingEntity.yRot;
        livingEntity.yHeadRotO = livingEntity.yRot;
        final EntityRendererManager entityrenderermanager = mc.getEntityRenderDispatcher();
        pitchRotation.conj();
        entityrenderermanager.overrideCameraOrientation(pitchRotation);
        entityrenderermanager.setRenderShadow(false);
        final IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderermanager.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, buffers, 0x00F000F0));
        buffers.endBatch();
        entityrenderermanager.setRenderShadow(true);
        livingEntity.yBodyRot = oldYawOffset;
        livingEntity.yRot = oldYaw;
        livingEntity.xRot = oldPitch;
        livingEntity.yHeadRotO = oldPrevYawHead;
        livingEntity.yHeadRot = oldYawHead;
        matrixStack.popPose();
    }

    /**
     * Enable scissor (clipping) to GUI region (prevent drawing outside).
     *
     * @param matrixStack matrix
     * @param x left position
     * @param y top position
     * @param w width
     * @param h height
     */
    public static void scissor(final MatrixStack matrixStack, int x, int y, int w, int h)
    {
        final double scale = Minecraft.getInstance().getWindow().getGuiScale();
        final double[] xyzTranslation = getGLTranslation(matrixStack, scale);
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

    private static double[] getGLTranslation(final MatrixStack matrixStack, final double scale)
    {
        final Matrix4f matrix = matrixStack.last().pose();
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
