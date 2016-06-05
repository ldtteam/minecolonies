package com.blockout.controls;

import com.blockout.PaneParams;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * BlockOut implementation of a Vanilla Button.
 */
public class ButtonVanilla extends Button
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    /**
     * Default constructor.
     */
    public ButtonVanilla()
    {
        setSize(200, 20);
    }

    /**
     * Constructor called when loaded from xml.
     *
     * @param params PaneParams from xml file.
     */
    public ButtonVanilla(PaneParams params)
    {
        super(params);
        if (getWidth() == 0)
        {
            width = 200;
        }
        if (getHeight() == 0)
        {
            height = 20;
        }
    }

    /**
     * Draws a vanilla button.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(int mx, int my)
    {
        mc.renderEngine.bindTexture(TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        boolean isMouseOver = isPointInPane(mx, my);

        int u = 0,
            v = enabled ? (isMouseOver ? 86 : 66) : 46; //  Base button

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (width == 200 && height == 20)
        {
            //Full size button
            drawTexturedModalRect(x, y, u, v, width, height);
        }
        else
        {
            drawTexturedModalRect(x, y, u, v, width/2, height/2);
            drawTexturedModalRect(x+width/2, y, u +200 - width /2, v, width/2, height/2);
            drawTexturedModalRect(x, y+height/2, u, v+20-height/2, width/2, height/2);
            drawTexturedModalRect(x+width/2, y+height/2, u + 200-width/2, v+20-height/2, width/2, height/2);
        }

        int textColor = enabled ? (isMouseOver ? 16777120 : 14737632) : 10526880;
        drawCenteredString(mc.fontRendererObj, label, x + width / 2, y + (height - 8) / 2, textColor);
    }
}
