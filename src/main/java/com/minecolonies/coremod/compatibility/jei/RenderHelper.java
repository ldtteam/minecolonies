package com.minecolonies.coremod.compatibility.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

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
}
