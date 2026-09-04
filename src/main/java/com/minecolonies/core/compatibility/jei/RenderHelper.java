package com.minecolonies.core.compatibility.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

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
    public static void renderBlock(final GuiGraphicsExtractor ctx,
                                   final BlockState block,
                                   final float x,
                                   final float y,
                                   final float z,
                                   final float pitch,
                                   final float yaw,
                                   final float scale)
    {
        ctx.item(new ItemStack(block.getBlock()), (int) x - 8, (int) y - 8);
    }
}
