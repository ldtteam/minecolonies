package com.blockout;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class Screen extends GuiScreen
{
    Window window;
    int x = 0,
        y = 0;

    Screen(Window w)
    {
        window = w;
    }

    @Override
    public void drawScreen(int mx, int my, float f)
    {
        int scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();

        mx -= x;
        my -= y;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        window.draw(mx, my, scale);
        GL11.glPopMatrix();
    }

    @Override
    public void initGui()
    {
        x = (width - window.getWidth()) / 2;
        y = (height - window.getHeight()) / 2;

        window.createGui();
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
            window.onClick(mx - x, my - y);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mx, int my, int code) {
        if (code == 0)
        {
//            window.mouseReleased(mx, my);
        }
    }

}
