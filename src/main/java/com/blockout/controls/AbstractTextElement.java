package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;

/**
 * Contains any code common to text controls.
 */
public abstract class AbstractTextElement extends Pane
{
    protected double    scale         = 1.0;
    protected Alignment textAlignment = Alignment.MiddleLeft;
    protected int       textColor     = 0xffffff;
    protected boolean   shadow        = false;

    protected static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    public AbstractTextElement()
    {
        //Required
    }

    /**
     * Create from xml.
     *
     * @param params xml parameters.
     */
    public AbstractTextElement(PaneParams params)
    {
        super(params);

        scale = params.getDoubleAttribute("textscale", scale);
        textAlignment = params.getEnumAttribute("textalign", Alignment.class, textAlignment);
        textColor = params.getColorAttribute("color", textColor);
        shadow = params.getBooleanAttribute("shadow", shadow);
    }

    public int getColor()
    {
        return textColor;
    }

    public void setColor(int c)
    {
        textColor = c;
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
}
