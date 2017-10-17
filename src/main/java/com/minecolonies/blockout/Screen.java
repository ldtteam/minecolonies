package com.minecolonies.blockout;

import com.minecolonies.blockout.views.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window.
 */
public class Screen extends GuiScreen
{
    protected static int scale = 0;
    protected Window window;
    protected int x = 0;
    protected int y = 0;

    /**
     * Create a GuiScreen from a BlockOut window.
     *
     * @param w blockout window.
     */
    public Screen(final Window w)
    {
        super();
        window = w;
    }

    public static int getScale()
    {
        return scale;
    }

    private static void setScale(final Minecraft mc)
    {
        //Seems to work without the sides now
        scale = new ScaledResolution(mc).getScaleFactor();
    }

    @Override
    public void drawScreen(final int mx, final int my, final float f)
    {
        if (window.hasLightbox())
        {
            super.drawDefaultBackground();
        }

        setScale(mc);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        window.draw(mx - x, my - y);
        GlStateManager.popMatrix();
    }

    @Override
    protected void keyTyped(final char ch, final int key)
    {
        window.onKeyTyped(ch, key);
    }

    @Override
    protected void mouseClicked(final int mx, final int my, final int code)
    {
        if (code == 0)
        {
            //  Adjust coordinate to origin of window
            window.click(mx - x, my - y);
        }
    }

    @Override
    protected void mouseReleased(final int mx, final int my, final int code)
    {
        if (code == 0)
        {
            //  Adjust coordinate to origin of window
            window.onMouseReleased(mx - x, my - y);
        }
    }

    @Override
    protected void mouseClickMove(final int mx, final int my, final int buttons, final long timeElapsed)
    {
        // Can be overridden
    }

    @Override
    public void initGui()
    {
        x = (width - window.getWidth()) / 2;
        y = (height - window.getHeight()) / 2;

        Keyboard.enableRepeatEvents(true);
        window.onOpened();
    }

    @Override
    public void updateScreen()
    {
        window.onUpdate();

        if (!mc.player.isEntityAlive() || mc.player.isDead)
        {
            mc.player.closeScreen();
        }
    }

    @Override
    public void onGuiClosed()
    {
        window.onClosed();
        Window.clearFocus();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return window.doesWindowPauseGame();
    }
}
