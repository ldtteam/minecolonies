package com.blockout.controls;

import com.blockout.PaneParams;
import org.lwjgl.opengl.GL11;

/**
 * BlockOut label pane. Used to render a piece of label.
 */
public class Label extends AbstractTextElement
{
    protected String label;

    protected int hoverColor = 0xffffff;

    public Label()
    {
        // Required default constructor.
    }

    /**
     * Create a label from xml.
     *
     * @param params xml parameters.
     */
    public Label(PaneParams params)
    {
        super(params);
        label = params.getLocalizedStringAttribute("label", label);

        //  match textColor by default
        hoverColor    = params.getColorAttribute("hovercolor", textColor);

        if (width == 0)
        {
            width = Math.min(mc.fontRendererObj.getStringWidth(label), params.getParentWidth());
        }
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String s)
    {
        label = s;
    }

    public int getHoverColor()
    {
        return hoverColor;
    }

    /**
     * Set the default and hover color for the label.
     * @param c default color.
     * @param h hover color.
     */
    public void setColor(int c, int h)
    {
        setColor(c);
        hoverColor = h;
    }

    public int getTextHeight()
    {
        return (int) (mc.fontRendererObj.FONT_HEIGHT * scale);
    }

    public int getStringWidth()
    {
        return (int) (mc.fontRendererObj.getStringWidth(label) * scale);
    }

    @Override
    public void drawSelf(int mx, int my)
    {
        int color = isPointInPane(mx, my) ? hoverColor : textColor;

        int offsetX = 0;
        int offsetY = 0;

        if (textAlignment.isRightAligned())
        {
            offsetX = getWidth() - getStringWidth();
        }
        else if (textAlignment.isHorizontalCentered())
        {
            offsetX = (getWidth() - getStringWidth()) / 2;
        }

        if (textAlignment.isBottomAligned())
        {
            offsetY = getHeight() - getTextHeight();
        }
        else if (textAlignment.isVerticalCentered())
        {
            offsetY = (getHeight() - getTextHeight()) / 2;
        }

        GL11.glPushMatrix();
        GL11.glTranslated((double) (getX() + offsetX), (double) (getY() + offsetY), 0);
        GL11.glScalef((float) scale, (float) scale, (float) scale);
        mc.renderEngine.bindTexture(TEXTURE);
        mc.fontRendererObj.drawString(label, 0, 0, color, shadow);
        GL11.glPopMatrix();
    }
}
