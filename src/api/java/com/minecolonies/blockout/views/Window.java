package com.minecolonies.blockout.views;

import com.minecolonies.blockout.Loader;
import com.minecolonies.blockout.PaneParams;
import com.minecolonies.blockout.Screen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

/**
 * Blockout window, high level root pane.
 */
@SideOnly(Side.CLIENT)
public class Window extends View
{
    /**
     * The default width.
     */
    private static final int DEFAULT_WIDTH = 420;

    /**
     * The default height.
     */
    private static final int DEFAULT_HEIGHT = 240;

    /**
     * The screen of the window.
     */
    protected Screen screen;

    /**
     * Defines if the window should pause the game.
     */
    protected boolean windowPausesGame = true;

    /**
     * Defines if the window should have a lightbox.
     */
    protected boolean lightbox = true;

    /**
     * Create a window from an xml file.
     *
     * @param resource ResourceLocation to get file from.
     */
    public Window(final ResourceLocation resource)
    {
        this();
        Loader.createFromXMLFile(resource, this);
    }

    /**
     * Make default sized window.
     */
    public Window()
    {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Window constructor when there is a fixed Width and Height.
     *
     * @param w Width of the window, in pixels.
     * @param h Height of the window, in pixels.
     */
    public Window(final int w, final int h)
    {
        super();
        width = w;
        height = h;

        screen = new Screen(this);
        window = this;
    }

    /**
     * Create a window from an xml file.
     *
     * @param resource location to get file from.
     */
    public Window(final String resource)
    {
        this();
        Loader.createFromXMLFile(resource, this);
    }

    /**
     * Load the xml parameters.
     *
     * @param params xml parameters.
     */
    public void loadParams(@NotNull final PaneParams params)
    {
        final String inherit = params.getStringAttribute("inherit", null);
        if (inherit != null)
        {
            Loader.createFromXMLFile(new ResourceLocation(inherit), this);
        }

        final PaneParams.SizePair size = params.getSizePairAttribute("size", null, null);
        if (size == null)
        {
            final int w = params.getIntAttribute("width", width);
            final int h = params.getIntAttribute("height", height);
            setSize(w, h);
        }
        else
        {
            setSize(size.getX(), size.getY());
        }

        lightbox = params.getBooleanAttribute("lightbox", lightbox);
        windowPausesGame = params.getBooleanAttribute("pause", windowPausesGame);
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        // Can be overridden
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        updateDebugging();

        super.drawSelf(mx, my);
    }

    private static void updateDebugging()
    {
        debugging = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                      && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                      && Keyboard.isKeyDown(Keyboard.KEY_LMENU);
    }

    /**
     * Return <tt>true</tt> if the 'lightbox' (default dark background) should be displayed.
     *
     * @return <tt>true</tt> if the 'lightbox' should be displayed.
     */
    public boolean hasLightbox()
    {
        return lightbox;
    }

    /**
     * Return <tt>true</tt> if the game should be paused when the Window is displayed.
     *
     * @return <tt>true</tt> if the game should be paused when the Window is displayed.
     */
    public boolean doesWindowPauseGame()
    {
        return windowPausesGame;
    }

    /**
     * Open the window.
     */
    public void open()
    {
        if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
        {
            FMLCommonHandler.instance().showGuiScreen(getScreen());
        }
    }

    /**
     * Windows wrap a GuiScreen.
     *
     * @return The current GuiScreen.
     */
    public GuiScreen getScreen()
    {
        return screen;
    }

    /**
     * Mouse click released handler.
     * <p>
     * Currently does nothing.
     *
     * @param mx Mouse X position
     * @param my Mouse Y position
     */
    public void onMouseReleased(final int mx, final int my)
    {
        // Can be overridden
    }

    /**
     * Key input handler.  Directs keystrokes to focused Pane, or to onUnhandledKeyTyped() if no
     * Pane handles the keystroke.
     * <p>
     * It is advised not to override this method.
     *
     * @param ch  Character of key pressed.
     * @param key Keycode of key pressed.
     * @return <tt>true</tt> if the key was handled by a Pane.
     */
    @Override
    public boolean onKeyTyped(final char ch, final int key)
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
     * <p>
     * Override this to handle key input at the Window level.
     *
     * @param ch  Character of key pressed.
     * @param key Keycode of key pressed.
     */
    public void onUnhandledKeyTyped(final int ch, final int key)
    {
        if (key == Keyboard.KEY_ESCAPE)
        {
            close();
        }
    }

    /**
     * Close the Window.
     */
    public void close()
    {
        this.mc.player.closeScreen();
        this.mc.setIngameFocus();
    }

    /**
     * Called when the Window is displayed.
     */
    public void onOpened()
    {
        // Can be overridden
    }

    /**
     * Called when the Window is closed.
     */
    public void onClosed()
    {
        // Can be overridden
    }
}
