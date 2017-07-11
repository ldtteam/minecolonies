package com.minecolonies.blockout.controls;

import com.minecolonies.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * BlockOut implementation of a Vanilla Button.
 */
public class ButtonVanilla extends Button
{
    /**
     * Texture map that contains the button texture.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    private static final int DEFAULT_BUTTON_WIDTH  = 200;
    private static final int DEFAULT_BUTTON_HEIGHT = 20;

    private static final int ENABLED_COLOR  = 0xE0E0E0;
    private static final int HOVER_COLOR    = 0xFFFFA0;
    private static final int DISABLED_COLOR = 0xA0A0A0;

    private static final int ENABLED_TEXTURE_V  = 66;
    private static final int HOVER_TEXTURE_V    = 86;
    private static final int DISABLED_TEXTURE_V = 46;

    /**
     * Default constructor.
     */
    public ButtonVanilla()
    {
        super();
        width = DEFAULT_BUTTON_WIDTH;
        height = DEFAULT_BUTTON_HEIGHT;
    }

    /**
     * Constructor called when loaded from xml.
     *
     * @param params PaneParams from xml file.
     */
    public ButtonVanilla(final PaneParams params)
    {
        super(params);
        if (width == 0)
        {
            width = DEFAULT_BUTTON_WIDTH;
        }
        if (height == 0)
        {
            height = DEFAULT_BUTTON_HEIGHT;
        }
    }

    /**
     * Draws a vanilla button.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final int mx, final int my)
    {
        mc.renderEngine.bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        final boolean isMouseOver = isPointInPane(mx, my);

        final int u = 0;
        final int v;
        if (enabled)
        {
            if (isMouseOver)
            {
                v = HOVER_TEXTURE_V;
            }
            else
            {
                v = ENABLED_TEXTURE_V;
            }
        }
        else
        {
            v = DISABLED_TEXTURE_V;
        }


        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (width == DEFAULT_BUTTON_WIDTH && height == DEFAULT_BUTTON_HEIGHT)
        {
            //Full size button
            drawTexturedModalRect(x, y, u, v, width, height);
        }
        else
        {
            drawTexturedModalRect(x, y, u, v, width / 2, height / 2);
            drawTexturedModalRect(x + width / 2, y, u + DEFAULT_BUTTON_WIDTH - width / 2, v, width / 2, height / 2);
            drawTexturedModalRect(x, y + height / 2, u, v + DEFAULT_BUTTON_HEIGHT - height / 2, width / 2, height / 2);
            drawTexturedModalRect(x + width / 2, y + height / 2, u + DEFAULT_BUTTON_WIDTH - width / 2, v + DEFAULT_BUTTON_HEIGHT - height / 2, width / 2, height / 2);
        }

        final int textColor;
        if (enabled)
        {
            if (isMouseOver)
            {
                textColor = HOVER_COLOR;
            }
            else
            {
                textColor = ENABLED_COLOR;
            }
        }
        else
        {
            textColor = DISABLED_COLOR;
        }

        drawCenteredString(this.mc.fontRenderer, label, x + width / 2, y + (height - this.mc.fontRenderer.FONT_HEIGHT) / 2, textColor);

        GlStateManager.disableBlend();
    }
}
