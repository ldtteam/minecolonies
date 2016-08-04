package com.blockout.views;

import com.blockout.PaneParams;
import com.blockout.Render;
import com.blockout.View;

/**
 * Simple box element.
 */
public class Box extends View
{
    private float lineWidth = 1.0F;
    private int color = 0xff000000;

    /**
     * Required default constructor.
     */
    public Box()
    {
        super();
    }

    /**
     * Loads box from xml.
     *
     * @param params xml parameters.
     */
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
