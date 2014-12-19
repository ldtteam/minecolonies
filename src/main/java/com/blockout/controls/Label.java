package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.Pane;
import com.blockout.PaneParams;
import org.lwjgl.input.Mouse;

public class Label extends Pane
{
    protected String label;
    protected Alignment textAlignment = Alignment.MiddleLeft;
    protected int       textColor     = 0xffffff;
    protected int       hoverColor    = 0xffffff;
    protected boolean   shadow        = false;

    public Label(){}

    public Label(PaneParams params)
    {
        super(params);
        label         = params.getLocalizedStringAttribute("label", label);
        textAlignment = params.getEnumAttribute("textalign", textAlignment);
        textColor     = params.getColorAttribute("color", textColor);
        hoverColor    = params.getColorAttribute("hovercolor", textColor); //  match textcolor by default
        shadow        = params.getBooleanAttribute("shadow", shadow);

        if (width == 0)
        {
            width = Math.min(mc.fontRenderer.getStringWidth(label), params.getParentWidth());
        }
    }

    public String getLabel() { return label; }
    public void setLabel(String s) { label = s; }

    public void setColor(int c) { setColor(c, c); }
    public void setColor(int c, int h)
    {
        textColor = c;
        hoverColor = h;
    }
    public int getColor() { return textColor; }
    public int getHoverColor() { return hoverColor; }

    public void setShadow(boolean s) { shadow = s; }
    public boolean getShadow() { return shadow; }

    public Alignment getTextAlignment() { return textAlignment; }
    public void setTextAlignment(Alignment align) { textAlignment = align; }

    @Override
    public void drawSelf(int mx, int my)
    {
        int color = isPointInPane(mx, my) ? hoverColor : textColor;

        int offsetX = 0;
        int offsetY = 0;

        if (textAlignment.rightAligned)
        {
            offsetX = (getWidth() - mc.fontRenderer.getStringWidth(label));
        }
        else if (textAlignment.horizontalCentered)
        {
            offsetX = (getWidth() - mc.fontRenderer.getStringWidth(label)) / 2;
        }

        if (textAlignment.bottomAligned)
        {
            offsetY = (getHeight() - mc.fontRenderer.FONT_HEIGHT);
        }
        else if (textAlignment.verticalCentered)
        {
            offsetY = (getHeight() - mc.fontRenderer.FONT_HEIGHT) / 2;
        }

        mc.fontRenderer.drawString(label, getX() + offsetX, getY() + offsetY, color, shadow);
    }
}
