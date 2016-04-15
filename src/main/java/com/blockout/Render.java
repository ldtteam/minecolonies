package com.blockout;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public class Render
{
    public static void drawOutlineRect(int x1, int y1, int x2, int y2, int color)
    {
        drawOutlineRect(x1, y1, x2, y2, 1.0f, color);
    }

    public static void drawOutlineRect(int x1, int y1, int x2, int y2, float lineWidth, int color)
    {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.startDrawing(GL11.GL_LINE_LOOP);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(r, g, b, a);
        tessellator.addVertex(x1, y2, 0.0D);
        tessellator.addVertex(x2, y2, 0.0D);
        tessellator.addVertex(x2, y1, 0.0D);
        tessellator.addVertex(x1, y1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

    }
}
