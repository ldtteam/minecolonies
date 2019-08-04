package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;

/**
 * Contains any code common to text controls.
 */
public abstract class AbstractTextElement extends Pane
{
    /**
     * Texture of the abstractTextElement.
     */
    protected static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    /**
     * The scale of the element.
     */
    protected double scale = 1.0;

    /**
     * How the text aligns in it.
     */
    protected Alignment textAlignment = Alignment.MIDDLE_LEFT;

    /**
     * The standard text color.
     */
    protected int textColor = 0xffffff;

    /**
     * The default state for shadows.
     */
    protected boolean shadow = false;

    /**
     * Creates an instance of the abstractTextElement.
     */
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

    public boolean hasShadow()
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
