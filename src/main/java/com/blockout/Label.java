package com.blockout;

import org.lwjgl.opengl.GL11;

public class Label extends Pane
{
    String label;
    Alignment textAlignment = Alignment.MiddleLeft;
    int textColor = 0xffffff;
    int hoverColor = 0xffffff;
    boolean shadow = false;

    public Label() {}
    public Label(Label other)
    {
        super(other);
        label = other.label;
        textAlignment = other.textAlignment;
        textColor = other.textColor;
        hoverColor = other.hoverColor;
        shadow = other.shadow;
    }

    public Label(XMLNode xml)
    {
        super(xml);
        label         = xml.getLocalizedStringAttribute("label", label);
        textAlignment = xml.getEnumAttribute("textalign", textAlignment);
        textColor     = xml.getColorAttribute("color", textColor);
        hoverColor    = xml.getColorAttribute("hovercolor", textColor /* match textcolor by default */);
        shadow        = xml.getBooleanAttribute("shadow", shadow);
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
        int offsetY = 2;

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
