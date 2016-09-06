package com.blockout;

import com.blockout.views.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window
 */
public class Screen extends GuiScreen
{
    protected Window window;
    protected int x = 0;
    protected int y = 0;

    protected static int scale = 0;

    /**
     * Create a GuiScreen from a BlockOut window.
     *
     * @param w blockout window.
     */
    public Screen(Window w)
    {
        window = w;
    }

    private static void setScale(Minecraft mc)
    {
        //Seems to work without the sides now
        scale = new ScaledResolution(mc).getScaleFactor();
    }

    public static int getScale()
    {
        return scale;
    }

    @Override
    public void drawScreen(int mx, int my, float f)
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
    public void initGui()
    {
        x = (width - window.getWidth()) / 2;
        y = (height - window.getHeight()) / 2;

        Keyboard.enableRepeatEvents(true);
        window.onOpened();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return window.doesWindowPauseGame();
    }

    @Override
    protected void mouseClicked(int mx, int my, int code)
    {
        if (code == 0)
        {
            //  Adjust coordinate to origin of window
            window.click(mx - x, my - y);
        }
    }

    @Override
    protected void mouseReleased(int mx, int my, int code)
    {
        if (code == 0)
        {
            //  Adjust coordinate to origin of window
            window.onMouseReleased(mx - x, my - y);
        }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int buttons, long timeElapsed)
    {
        // Can be overridden
    }

    @Override
    protected void keyTyped(char ch, int key)
    {
        window.onKeyTyped(ch, key);
    }

    @Override
    public void updateScreen()
    {
        window.onUpdate();

        if (!mc.thePlayer.isEntityAlive() || mc.thePlayer.isDead)
        {
            mc.thePlayer.closeScreen();
        }
    }

    @Override
    public void onGuiClosed()
    {
        window.onClosed();
        Window.clearFocus();
        Keyboard.enableRepeatEvents(false);
    }
}
