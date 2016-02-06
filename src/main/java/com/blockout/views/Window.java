package com.blockout.views;

import com.blockout.Loader;
import com.blockout.PaneParams;
import com.blockout.Screen;
import com.blockout.View;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class Window extends View
{
    protected Minecraft mc = Minecraft.getMinecraft();
    protected Screen screen;

    protected boolean windowPausesGame = true;
    protected boolean hasLightbox      = true;

    public Window()
    {
        this(420, 240);
    }

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

    public Window(ResourceLocation resource)
    {
        this();
        Loader.createFromXMLFile(resource, this);
    }

    public Window(String resource)
    {
        this();
        Loader.createFromXMLFile(resource, this);
    }

    public void loadParams(PaneParams params)
    {
        String inherit = params.getStringAttribute("inherit", null);
        if (inherit != null)
        {
            Loader.createFromXMLFile(new ResourceLocation(inherit), this);
        }

        PaneParams.SizePair size = params.getSizePairAttribute("size", null, null);
        if (size != null)
        {
            setSize(size.x, size.y);
        }
        else
        {
            int w = params.getIntegerAttribute("width", width);
            int h = params.getIntegerAttribute("height", height);
            setSize(w, h);
        }

        hasLightbox = params.getBooleanAttribute("lightbox", hasLightbox);
        windowPausesGame = params.getBooleanAttribute("pause", windowPausesGame);
    }

    @Override
    public void parseChildren(PaneParams params) {}

    @Override
    protected void drawSelf(int mx, int my)
    {
        debugging = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) &&
                Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) &&
                Keyboard.isKeyDown(Keyboard.KEY_LMENU);

        super.drawSelf(mx, my);
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
    public boolean hasLightbox() { return hasLightbox; }

    /**
     * Return <tt>true</tt> if the game should be paused when the Window is displayed
     *
     * @return <tt>true</tt> if the game should be paused when the Window is displayed
     */
    public boolean doesWindowPauseGame() { return windowPausesGame; }

    /**
     * Open the window
     */
    public void open()
    {
        if(FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
        {
            FMLCommonHandler.instance().showGuiScreen(getScreen());
        }
    }

    /**
     * Close the Window
     */
    public void close()
    {
        this.mc.thePlayer.closeScreen();
        this.mc.setIngameFocus();
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
     * Called when the Window is displayed.
     */
    public void onOpened() {}

    /**
     * Called when the Window is closed.
     */
    public void onClosed() {}
}
