package com.blockout.controls;

import com.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;

/**
 * BlockOut label pane. Used to render a piece of text.
 */
public class Label extends AbstractTextElement
{
    protected String labelText;

    protected int hoverColor = 0xffffff;

    public Label()
    {
        super();
        // Required default constructor.
    }

    /**
     * Create a label from xml.
     *
     * @param params xml parameters.
     */
    public Label(final PaneParams params)
    {
        super(params);
        labelText = params.getLocalizedStringAttribute("label", labelText);

        //  match textColor by default
        hoverColor = params.getColorAttribute("hovercolor", textColor);

        if (width == 0)
        {
            width = Math.min(mc.fontRendererObj.getStringWidth(labelText), params.getParentWidth());
        }
    }

    public String getLabelText()
    {
        return labelText;
    }

    public void setLabelText(final String s)
    {
        labelText = s;
    }

    public int getHoverColor()
    {
        return hoverColor;
    }

    /**
     * Set the default and hover color for the label.
     *
     * @param c default color.
     * @param h hover color.
     */
    public void setColor(final int c, final int h)
    {
        setColor(c);
        hoverColor = h;
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        final int color = isPointInPane(mx, my) ? hoverColor : textColor;

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

        GlStateManager.pushMatrix();
        GlStateManager.translate((double) (getX() + offsetX), (double) (getY() + offsetY), 0);
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        mc.renderEngine.bindTexture(TEXTURE);
        mc.fontRendererObj.drawString(labelText, 0, 0, color, shadow);
        GlStateManager.popMatrix();
    }

    public int getStringWidth()
    {
        return (int) (mc.fontRendererObj.getStringWidth(labelText) * scale);
    }

    public int getTextHeight()
    {
        return (int) (mc.fontRendererObj.FONT_HEIGHT * scale);
    }
}
