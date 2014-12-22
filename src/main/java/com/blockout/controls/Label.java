package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.Pane;
import com.blockout.PaneParams;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class Label extends Pane
{
    protected String label;
    protected float     scale         = 1.0f;
    protected Alignment textAlignment = Alignment.MiddleLeft;
    protected int       textColor     = 0xffffff;
    protected int       hoverColor    = 0xffffff;
    protected boolean   shadow        = false;

    public Label(){}

    public Label(PaneParams params)
    {
        super(params);
        label         = params.getLocalizedStringAttribute("label", label);
        scale         = params.getFloatAttribute("textscale", scale);
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

    public int getColor() { return textColor; }
    public int getHoverColor() { return hoverColor; }
    public void setColor(int c) { setColor(c, c); }
    public void setColor(int c, int h)
    {
        textColor = c;
        hoverColor = h;
    }

    public boolean getShadow() { return shadow; }
    public void setShadow(boolean s) { shadow = s; }

    public Alignment getTextAlignment() { return textAlignment; }
    public void setTextAlignment(Alignment align) { textAlignment = align; }

    public float getScale() { return scale; }
    public void setScale(float s) { scale = s; }

    public int getTextHeight() { return (int)(mc.fontRenderer.FONT_HEIGHT * scale); }
    public int getStringWidth() { return (int)(mc.fontRenderer.getStringWidth(label) * scale); }

    @Override
    public void drawSelf(int mx, int my)
    {
        int color = isPointInPane(mx, my) ? hoverColor : textColor;

        int offsetX = 0;
        int offsetY = 0;

        if (textAlignment.rightAligned)
        {
            offsetX = (getWidth() - getStringWidth());
        }
        else if (textAlignment.horizontalCentered)
        {
            offsetX = (getWidth() - getStringWidth()) / 2;
        }

        if (textAlignment.bottomAligned)
        {
            offsetY = (getHeight() - getTextHeight());
        }
        else if (textAlignment.verticalCentered)
        {
            offsetY = (getHeight() - getTextHeight()) / 2;
        }

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        mc.fontRenderer.drawString(label, (int)((getX() + offsetX) / scale), (int)((getY() + offsetY) / scale), color, shadow);
        GL11.glPopMatrix();
    }
}
