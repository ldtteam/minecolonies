package com.blockout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class Window extends View
{
    protected Minecraft mc = Minecraft.getMinecraft();
    protected Screen screen;
    protected boolean hasLightbox = false;

    public Window(int w, int h)
    {
        /*root.*/ setSize(w, h);
        screen = new Screen(this);
    }

    public GuiScreen getScreen() { return screen; }

    public void onInitGui() {}

    public boolean hasLightbox() { return true; }

    protected boolean doesWindowPauseGame() { return true; }

    public void close()
    {
        this.mc.displayGuiScreen((GuiScreen) null);
        this.mc.setIngameFocus();
    }

    protected void drawBackground() {}

    @Override
    protected void drawSelf(int mx, int my)
    {
        drawBackground();
        super.drawSelf(mx, my);
    }

    public void onMouseClicked(int mx, int my)
    {
        Pane clickedPane = super.findPaneByCoord(mx, my);
        if (clickedPane != null)
        {
            clickedPane.onMouseClicked(mx, my);
            lastClickedPane = clickedPane;
        }
    }

    public void onMouseReleased(int mx, int my) {}

    @Override
    public boolean onKeyTyped(char ch, int key)
    {
        if (getFocus() != null && getFocus().onKeyTyped(ch, key))
        {
            return true;
        }

        onUnhandledKeyTyped(ch, key);

        return false;
    }

    public void onUnhandledKeyTyped(int ch, int key)
    {
        if (key == Keyboard.KEY_ESCAPE)
        {
            close();
        }
    }

    public void onClosed() {}
}
