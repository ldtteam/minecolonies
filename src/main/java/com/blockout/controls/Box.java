package com.blockout.controls;

import com.blockout.PaneParams;
import com.blockout.View;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public class Box extends View
{
    float lineWidth = 1.0f;
    int color = 0xff000000;

    public Box() { super(); }
    public Box(Box img) { super(img); }
    public Box(PaneParams params)
    {
        super(params);
        lineWidth = params.getFloatAttribute("linewidth", lineWidth);
        color = params.getColorAttribute("color", color);
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        double x1 = x, x2 = x + getWidth();
        double y1 = y, y2 = y + getHeight();
        Tessellator tessellator = Tessellator.instance;
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

        super.drawSelf(mx, my);
    }
}
