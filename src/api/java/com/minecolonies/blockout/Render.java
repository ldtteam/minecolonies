package com.ldtteam.blockout;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Render utility functions.
 */
public final class Render
{
    private static final int    ALPHA_SHIFT   = 24;
    private static final int    RED_SHIFT     = 16;
    private static final int    GREEN_SHIFT   = 8;
    private static final int    COLOR_MASK    = 255;
    private static final double COLOR_DIVISOR = 255.0;

    private Render()
    {
        // Hide default constructor
    }

    /**
     * {@link Render#drawOutlineRect(int, int, int, int, float, int)}.
     *
     * @param x1    lower x
     * @param y1    lower y
     * @param x2    upper x
     * @param y2    upper y
     * @param color color
     */
    public static void drawOutlineRect(final int x1, final int y1, final int x2, final int y2, final int color)
    {
        drawOutlineRect(x1, y1, x2, y2, 1.0F, color);
    }

    /**
     * Draw an outlined untextured rectangle.
     *
     * @param x1        lower x
     * @param y1        lower y
     * @param x2        upper x
     * @param y2        upper y
     * @param lineWidth line thickness, default of 1.0
     * @param color     color
     */
    public static void drawOutlineRect(final int x1, final int y1, final int x2, final int y2, final float lineWidth, final int color)
    {
        if (lineWidth <= 0.0F)
        {
            // If lineWidth is less than or equal to 0, a GL Error occurs
            return;
        }

        final float a = (float) (((color >> ALPHA_SHIFT) & COLOR_MASK) / COLOR_DIVISOR);
        final float r = (float) (((color >> RED_SHIFT) & COLOR_MASK) / COLOR_DIVISOR);
        final float g = (float) (((color >> GREEN_SHIFT) & COLOR_MASK) / COLOR_DIVISOR);
        final float b = (float) ((color & COLOR_MASK) / COLOR_DIVISOR);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();

        vertexBuffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(lineWidth);
        GlStateManager.color(r, g, b, a);

        //Since our points do not have any u,v this seems to be the correct code
        vertexBuffer.pos(x1, y2, 0.0D).endVertex();
        vertexBuffer.pos(x2, y2, 0.0D).endVertex();
        vertexBuffer.pos(x2, y1, 0.0D).endVertex();
        vertexBuffer.pos(x1, y1, 0.0D).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();
    }
}
