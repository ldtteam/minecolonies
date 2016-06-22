package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * BlockOut label pane. Used to render a piece of textContent.
 */
public class Label extends Pane
{
    protected String text;
    protected double     scale         = 1.0;
    protected Alignment textAlignment = Alignment.MiddleLeft;
    protected int       textColor     = 0xffffff;
    protected int       hoverColor    = 0xffffff;
    protected boolean   shadow        = false;
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

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
        text = params.getLocalizedStringAttribute("textContent", text);
        scale         = params.getDoubleAttribute("textscale", scale);
        textAlignment = params.getEnumAttribute("textalign", Alignment.class, textAlignment);
        textColor     = params.getColorAttribute("color", textColor);

        //  match textColor by default
        hoverColor    = params.getColorAttribute("hovercolor", textColor);
        shadow        = params.getBooleanAttribute("shadow", shadow);

        if (width == 0)
        {
            width = Math.min(mc.fontRendererObj.getStringWidth(text), params.getParentWidth());
        }
    }

    public String getText()
    {
        return text;
    }

    public void setText(String s)
    {
        text = s;
    }

    public int getColor()
    {
        return textColor;
    }

    public int getHoverColor()
    {
        return hoverColor;
    }

    public void setColor(int c)
    {
        setColor(c, c);
    }

    /**
     * Set the default and hover color for the label.
     * @param c default color.
     * @param h hover color.
     */
    public void setColor(int c, int h)
    {
        textColor = c;
        hoverColor = h;
    }

    public boolean getShadow()
    {
        return shadow;
    }

    public void setShadow(boolean s)
    {
        shadow = s;
    }

    public Alignment getTextAlignment()
    {
        return textAlignment;
    }

    public void setTextAlignment(Alignment align)
    {
        textAlignment = align;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(float s)
    {
        scale = s;
    }

    public int getTextHeight()
    {
        return (int) (mc.fontRendererObj.FONT_HEIGHT * scale);
    }

    public int getStringWidth()
    {
        return (int) (mc.fontRendererObj.getStringWidth(text) * scale);
    }

    @Override
    public void drawSelf(int mx, int my)
    {
        int color = isPointInPane(mx, my) ? hoverColor : textColor;

        int offsetX = 0;
        int offsetY = 0;

        if (textAlignment.rightAligned)
        {
            offsetX = getWidth() - getStringWidth();
        }
        else if (textAlignment.horizontalCentered)
        {
            offsetX = (getWidth() - getStringWidth()) / 2;
        }

        if (textAlignment.bottomAligned)
        {
            offsetY = getHeight() - getTextHeight();
        }
        else if (textAlignment.verticalCentered)
        {
            offsetY = (getHeight() - getTextHeight()) / 2;
        }

        GL11.glPushMatrix();
        GL11.glTranslated((double) (getX() + offsetX), (double) (getY() + offsetY), 0);
        GL11.glScalef((float) scale, (float) scale, (float) scale);
        mc.renderEngine.bindTexture(TEXTURE);
        mc.fontRendererObj.drawString(text, 0, 0, color, shadow);
        GL11.glPopMatrix();
    }
}
