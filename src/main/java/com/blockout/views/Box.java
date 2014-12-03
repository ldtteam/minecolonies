package com.blockout.views;

import com.blockout.PaneParams;
import com.blockout.Render;
import com.blockout.View;

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
        Render.drawOutlineRect(x, y, x + getWidth(), y + getHeight(), lineWidth, color);

        super.drawSelf(mx, my);
    }
}
