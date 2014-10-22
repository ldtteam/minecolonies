package com.blockout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class Window extends View
{
    protected Minecraft mc = Minecraft.getMinecraft();
    protected Screen screen;

    /**
     * Window constructor when there is a fixed Width and Height
     *
     * @param w Width of the window, in pixels
     * @param h Height of the window, in pixels
     */
    public Window(int w, int h)
    {
        setSize(w, h);
        screen = new Screen(this);
        window = this;
    }

    /**
     * Window constructor to create a Window that occupies the entirety of the screen
     */
//    public Window()
//    {
//        this(0, 0);
//    }

    /**
     * Windows wrap a GuiScreen
     *
     * @return The current GuiScreen
     */
    public GuiScreen getScreen() { return screen; }

    //public void onInitGui() {}

    /**
     * Return <tt>true</tt> if the 'lightbox' (default dark background) should be displayed
     *
     * @return <tt>true</tt> if the 'lightbox' should be displayed
     */
    public boolean hasLightbox() { return true; }

    /**
     * Return <tt>true</tt> if the game should be paused when the Window is displayed
     *
     * @return <tt>true</tt> if the game should be paused when the Window is displayed
     */
    protected boolean doesWindowPauseGame() { return true; }

    /**
     * Close the Window
     */
    public void close()
    {
        this.mc.displayGuiScreen((GuiScreen) null);
        this.mc.setIngameFocus();
    }

    /**
     * Draw a background
     */
    protected void drawBackground() {}

    /**
     * Default draw function.  Do not override this unless absolutely necessary.
     *
     * @param mx Mouse X position
     * @param my Mouse Y position
     */
    @Override
    protected void drawSelf(int mx, int my)
    {
        drawBackground();
        super.drawSelf(mx, my);
    }

    /**
     * Mouse click handler for the Window; finds target pane and calls onMouseClicked on it,
     * then sets the lastClickedPane.
     *
     * It is advised not to override this method.
     *
     * @param mx Mouse X position
     * @param my Mouse Y position
     */
    @Override
    public void onMouseClicked(int mx, int my)
    {
        Pane clickedPane = super.findPaneForClick(mx, my);
        if (clickedPane != null)
        {
            clickedPane.onMouseClicked(mx, my);
            lastClickedPane = clickedPane;
        }
    }

    /**
     * Mouse click released handler.
     *
     * Currently does nothing.
     *
     * @param mx Mouse X position
     * @param my Mouse Y position
     */
    public void onMouseReleased(int mx, int my) {}

    /**
     * Key input handler.  Directs keystrokes to focused Pane, or to onUnhandledKeyTyped() if no
     * Pane handles the keystroke.
     *
     * It is advised not to override this method.
     *
     * @param ch Character of key pressed
     * @param key Keycode of key pressed
     *
     * @return <tt>true</tt> if the key was handled by a Pane
     */
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

    /**
     * Key input handler when a focused pane did not handle the key.
     *
     * Override this to handle key input at the Window level.
     *
     * @param ch Character of key pressed
     * @param key Keycode of key pressed
     */
    public void onUnhandledKeyTyped(int ch, int key)
    {
        if (key == Keyboard.KEY_ESCAPE)
        {
            close();
        }
    }

    /**
     * Called when the Window is closed.
     */
    public void onClosed() {}
}
