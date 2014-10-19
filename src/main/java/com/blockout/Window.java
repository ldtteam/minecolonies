package com.blockout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Window extends View
{
    protected Minecraft mc = Minecraft.getMinecraft();
    protected Screen screen;
    //protected View root = new View();

    public Window(int w, int h)
    {
        /*root.*/ setSize(w, h);
        screen = new Screen(this);
    }

    public GuiScreen getScreen() { return screen; }

    public void createGui() {}

    protected boolean doesWindowPauseGame() { return true; }

    //public View getRoot() { return root; }

    protected void drawBackground() {}

    @Override
    protected void drawSelf(int mx, int my, int scale)
    {
        drawBackground();
        super.drawSelf(mx, my, scale);
    }
}
