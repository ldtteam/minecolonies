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
    protected static final ResourceLocation TEXTURE       = new ResourceLocation("textures/gui/widgets.png");
    protected              double           scale         = 1.0;
    protected              Alignment        textAlignment = Alignment.MIDDLE_LEFT;
    protected              int              textColor     = 0xffffff;
    protected              boolean          shadow        = false;

    public AbstractTextElement()
    {
        super();
        //Required
    }

    /**
     * Create from xml.
     *
     * @param params xml parameters.
     */
    public AbstractTextElement(final PaneParams params)
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

    public void setColor(final int c)
    {
        textColor = c;
    }

    public boolean getShadow()
    {
        return shadow;
    }

    public void setShadow(final boolean s)
    {
        shadow = s;
    }

    public Alignment getTextAlignment()
    {
        return textAlignment;
    }

    public void setTextAlignment(final Alignment align)
    {
        textAlignment = align;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(final float s)
    {
        scale = s;
    }
}
