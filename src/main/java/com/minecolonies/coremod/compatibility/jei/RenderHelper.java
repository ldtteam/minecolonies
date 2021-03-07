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
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

// entity rendering code based on JustEnoughResources
public class RenderHelper
{
    public static void renderEntity(MatrixStack matrixStack, int x, int y, double scale, double yaw, double pitch, LivingEntity livingEntity) {
        if (livingEntity.world == null) livingEntity.world = Minecraft.getInstance().world;
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.getLast().getMatrix());
        RenderSystem.translatef(x, y, 50.0F);
        RenderSystem.scalef((float) -scale, (float) scale, (float) scale);
        final MatrixStack mobMatrix = new MatrixStack();
        mobMatrix.rotate(Vector3f.ZP.rotationDegrees(180.0F));
        RenderSystem.rotatef(((float) Math.atan((pitch / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        livingEntity.renderYawOffset = (float) Math.atan(yaw / 40.0F) * 20.0F;
        livingEntity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
        livingEntity.rotationPitch = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
        livingEntity.rotationYawHead = livingEntity.rotationYaw;
        livingEntity.prevRotationYawHead = livingEntity.rotationYaw;
        mobMatrix.translate(0.0F, livingEntity.getYOffset(), 0.0F);
        final EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        entityrenderermanager.setRenderShadow(false);
        final IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderermanager.renderEntityStatic(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, mobMatrix, renderTypeBuffer, 15728880);
        });
        renderTypeBuffer.finish();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }

    // although this one was given *to* JER rather than taken from it, since their version was broken
    public static void renderBlock(final MatrixStack matrixStack, final BlockState block, final float x, final float y, final float z, final float rotateX, final float rotateY, final float scale)
    {
        final Minecraft mc = Minecraft.getInstance();

        matrixStack.push();
        matrixStack.translate(x, y, z);
        matrixStack.scale(-scale, -scale, -scale);
        matrixStack.translate(-0.5F, -0.5F, 0);

        matrixStack.rotate(Vector3f.XP.rotationDegrees(rotateX));

        matrixStack.translate(0.5F, 0, -0.5F);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(rotateY));
        matrixStack.translate(-0.5F, 0, 0.5F);

        matrixStack.push();
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        matrixStack.translate(0, 0, -1);

        mc.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        final IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        mc.getBlockRendererDispatcher().renderBlock(block, matrixStack, buffers, 0x00F000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        buffers.finish();
        matrixStack.pop();

        matrixStack.pop();
    }

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

    public static void stopScissor()
    {
        RenderSystem.disableScissor();
    }

    public static double[] getGLTranslation(final MatrixStack matrixStack, final double scale)
    {
        final Matrix4f matrix = matrixStack.getLast().getMatrix();
        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        matrix.write(buf);
        // { x, y, z }
        return new double[] { buf.get(getIndexFloatBuffer(0,3)) * scale, buf.get(getIndexFloatBuffer(1, 3)) * scale, buf.get(getIndexFloatBuffer(2, 3)) * scale };
    }

    private static int getIndexFloatBuffer(final int x, final int y) {
        return y * 4 + x;
    }
}
