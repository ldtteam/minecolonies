package com.minecolonies.blockout.controls;

import com.minecolonies.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;

/**
 * BlockOut gradient pane. Used to render a gradient.
 */
public class Gradient extends AbstractTextElement
{
    /**
     * Default Gradients. Some transparent gray value.
     */
    private int gradientStart = -1072689136;
    private int gradientEnd = -804253680;

    /**
     * Standard constructor which instantiates a new label.
     */
    public Gradient()
    {
        super();
        // Required default constructor.
    }

    /**
     * Create a label from xml.
     *
     * @param params xml parameters.
     */
    public Gradient(final PaneParams params)
    {
        super(params);
        gradientStart = params.getIntegerAttribute("gradientstart", gradientStart);

        // match textColor by default
        gradientEnd = params.getColorAttribute("gradientend", gradientEnd);
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        GlStateManager.pushMatrix();
        this.drawGradientRect(getX(), getY(), getX() + width, getY() + height, gradientStart, gradientEnd);
        GlStateManager.popMatrix();
    }
}
