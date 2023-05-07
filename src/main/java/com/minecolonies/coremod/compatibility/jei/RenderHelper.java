package com.minecolonies.coremod.compatibility.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
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

        matrixStack.mulPose(Axis.XP.rotationDegrees(pitch));

        matrixStack.translate(0.5F, 0, -0.5F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(yaw));
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

    private static double[] getGLTranslation(final PoseStack poseStack, final double scale)
    {
        final Matrix4f matrix = poseStack.last().pose();
        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        matrix.set(buf);
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
