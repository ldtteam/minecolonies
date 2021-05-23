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

        matrixStack.push();
        matrixStack.translate(x, y, z);
        matrixStack.scale(-scale, -scale, -scale);
        matrixStack.translate(-0.5F, -0.5F, 0);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(pitch));

        matrixStack.translate(0.5F, 0, -0.5F);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(yaw));
        matrixStack.translate(-0.5F, 0, 0.5F);

        matrixStack.push();
        matrixStack.translate(0, 0, -1);

        mc.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        final IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        mc.getBlockRendererDispatcher().renderBlock(block, matrixStack, buffers, 0x00F000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        buffers.finish();
        matrixStack.pop();

        matrixStack.pop();
    }

    /**
     * Render an entity on a GUI.
     * @param matrixStack matrix
     * @param x horizontal center position
     * @param y vertical bottom position
     * @param scale scaling factor
     * @param yaw adjusts look rotation
     * @param pitch adjusts look rotation
     * @param livingEntity the entity to render
     */
    public static void renderEntity(final MatrixStack matrixStack, final int x, final int y, final double scale,
                                    final double yaw, final double pitch, final LivingEntity livingEntity)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (livingEntity.world == null) livingEntity.world = mc.world;
        final float yawAngle = (float) Math.atan(yaw / 40.0F);
        final float pitchAngle = (float) Math.atan(pitch / 40.0F);
        matrixStack.push();
        matrixStack.translate((float) x, (float) y, 1050.0F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternion pitchRotation = Vector3f.XP.rotationDegrees(pitchAngle * 20.0F);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.rotate(pitchRotation);
        final float oldYawOffset = livingEntity.renderYawOffset;
        final float oldYaw = livingEntity.rotationYaw;
        final float oldPitch = livingEntity.rotationPitch;
        final float oldPrevYawHead = livingEntity.prevRotationYawHead;
        final float oldYawHead = livingEntity.rotationYawHead;
        livingEntity.renderYawOffset = 180.0F + yawAngle * 20.0F;
        livingEntity.rotationYaw = 180.0F + yawAngle * 40.0F;
        livingEntity.rotationPitch = -pitchAngle * 20.0F;
        livingEntity.rotationYawHead = livingEntity.rotationYaw;
        livingEntity.prevRotationYawHead = livingEntity.rotationYaw;
        final EntityRendererManager entityrenderermanager = mc.getRenderManager();
        pitchRotation.conjugate();
        entityrenderermanager.setCameraOrientation(pitchRotation);
        entityrenderermanager.setRenderShadow(false);
        final IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> entityrenderermanager.renderEntityStatic(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, buffers, 0x00F000F0));
        buffers.finish();
        entityrenderermanager.setRenderShadow(true);
        livingEntity.renderYawOffset = oldYawOffset;
        livingEntity.rotationYaw = oldYaw;
        livingEntity.rotationPitch = oldPitch;
        livingEntity.prevRotationYawHead = oldPrevYawHead;
        livingEntity.rotationYawHead = oldYawHead;
        matrixStack.pop();
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
        final double scale = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        final double[] xyzTranslation = getGLTranslation(matrixStack, scale);
        x *= scale;
        y *= scale;
        w *= scale;
        h *= scale;
        final int scissorX = Math.round(Math.round(xyzTranslation[0] + x));
        final int scissorY = Math.round(Math.round(Minecraft.getInstance().getMainWindow().getHeight() - y - h - xyzTranslation[1]));
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
        final Matrix4f matrix = matrixStack.getLast().getMatrix();
        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        matrix.write(buf);
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
