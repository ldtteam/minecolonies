package com.blockout;

import com.blockout.views.Window;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Screen extends GuiScreen
{
    protected Window window;
    protected int x = 0, y = 0;

    protected static int scale = 0;

    public Screen(Window w)
    {
        window = w;
    }

    public static int getScale(){ return scale; }

    @Override
    public void drawScreen(int mx, int my, float f)
    {
        if (window.hasLightbox())
        {
            super.drawDefaultBackground();
        }

        scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        window.draw(mx - x, my - y);
        GL11.glPopMatrix();
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
        try
        {
            if (code == 0)
            {
                //  Adjust coordinate to origin of window
                window.click(mx - x, my - y);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void mouseMovedOrUp(int mx, int my, int code)
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
    }

    @Override
    public void onGuiClosed()
    {
        window.onClosed();
        Window.clearFocus();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawDefaultBackground()
    {
        super.drawDefaultBackground();
    }
}
