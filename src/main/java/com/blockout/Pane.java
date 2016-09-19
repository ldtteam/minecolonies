package com.blockout;

import com.blockout.views.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A Pane is the root of all UI objects
 */
public class Pane extends Gui
{
    protected static Pane lastClickedPane;
    protected static Pane focus;
    protected static boolean             debugging         = false;
    @NotNull
    private static   Deque<ScissorsInfo> scissorsInfoStack = new ConcurrentLinkedDeque<>();
    protected        Minecraft           mc                = Minecraft.getMinecraft();
    //  Attributes
    protected        String              id                = "";
    protected        int                 x                 = 0;
    protected        int                 y                 = 0;
    protected        int                 width             = 0;
    protected        int                 height            = 0;
    protected        Alignment           alignment         = Alignment.TopLeft;
    protected        boolean             visible           = true;
    protected        boolean             enabled           = true;
    //  Runtime
    protected Window window;
    protected View   parent;

    /**
     * Default constructor
     */
    public Pane()
    {
        //Required for panes.
    }

    /**
     * Constructs a Pane from PaneParams
     *
     * @param params Params for the Pane
     */
    public Pane(@NotNull PaneParams params)
    {
        id = params.getStringAttribute("id", id);

        @NotNull PaneParams.SizePair parentSizePair = new PaneParams.SizePair(params.getParentWidth(), params.getParentHeight());
        PaneParams.SizePair sizePair = params.getSizePairAttribute("size", null, parentSizePair);
        if (sizePair != null)
        {
            width = sizePair.getX();
            height = sizePair.getY();
        }
        else
        {
            width = params.getScalableIntegerAttribute("width", width, parentSizePair.getX());
            height = params.getScalableIntegerAttribute("height", height, parentSizePair.getY());
        }

        sizePair = params.getSizePairAttribute("pos", null, parentSizePair);
        if (sizePair != null)
        {
            x = sizePair.getX();
            y = sizePair.getY();
        }
        else
        {
            x = params.getScalableIntegerAttribute("x", x, parentSizePair.getX());
            y = params.getScalableIntegerAttribute("y", y, parentSizePair.getY());
        }

        alignment = params.getEnumAttribute("align", Alignment.class, alignment);
        visible = params.getBooleanAttribute("visible", visible);
        enabled = params.getBooleanAttribute("enabled", enabled);
    }

    /**
     * Returns the currently focused Pane
     *
     * @return the currently focused Pane
     */
    public static synchronized Pane getFocus()
    {
        return focus;
    }

    /**
     * Clear the currently focused Pane.
     */
    public static void clearFocus()
    {
        setFocus(null);
    }

    /**
     * Override to respond to the Pane losing focus
     */
    public void onFocusLost()
    {
        // Can be overloaded
    }

    /**
     * Override to respond to the Pane becoming the current focus
     */
    public void onFocus()
    {
        // Can be overloaded
    }

    public void parseChildren(PaneParams params)
    {
        // Can be overloaded
    }

    //  ID
    public final String getID()
    {
        return id;
    }

    public final void setID(String id)
    {
        this.id = id;
    }

    public void setSize(int w, int h)
    {
        width = w;
        height = h;
    }

    public void setPosition(int newX, int newY)
    {
        x = newX;
        y = newY;
    }

    public void moveBy(int dx, int dy)
    {
        x += dx;
        y += dy;
    }

    public Alignment getAlignment()
    {
        return alignment;
    }

    public void setAlignment(Alignment alignment)
    {
        this.alignment = alignment;
    }

    //  Visibility
    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean v)
    {
        visible = v;
    }

    public void show()
    {
        setVisible(true);
    }

    public void hide()
    {
        setVisible(false);
    }

    //  Enabling
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean e)
    {
        enabled = e;
    }

    public void enable()
    {
        setEnabled(true);
    }

    public void disable()
    {
        setEnabled(false);
    }


    //  Focus

    /**
     * Set Focus to this Pane.
     */
    public final void setFocus()
    {
        setFocus(this);
    }

    /**
     * Return <tt>true</tt> if this Pane is the current focus.
     *
     * @return <tt>true</tt> if this Pane is the current focus
     */
    public final synchronized boolean isFocus()
    {
        return focus == this;
    }

    /**
     * Set the currently focused Pane
     *
     * @param f Pane to focus, or nil
     */
    public static synchronized void setFocus(Pane f)
    {
        if (focus != null)
        {
            focus.onFocusLost();
        }

        focus = f;

        if (focus != null)
        {
            focus.onFocus();
        }
    }

    /**
     * Draw the current Pane if visible.
     *
     * @param mx mouse x
     * @param my mouse y
     */
    public final void draw(int mx, int my)
    {
        if (visible)
        {
            drawSelf(mx, my);
            if (debugging)
            {
                boolean isMouseOver = isPointInPane(mx, my);
                int color = isMouseOver ? 0xFF00FF00 : 0xFF0000FF;

                Render.drawOutlineRect(x, y, x + getWidth(), y + getHeight(), color);

                if (isMouseOver && !id.isEmpty())
                {
                    int stringWidth = mc.fontRendererObj.getStringWidth(id);
                    mc.fontRendererObj.drawString(id, x + getWidth() - stringWidth, y + getHeight() - mc.fontRendererObj.FONT_HEIGHT, color);
                }
            }
        }
    }

    /**
     * Draw self.  The graphics port is already relative to the appropriate location
     * <p>
     * Override this to actually draw.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    protected void drawSelf(int mx, int my)
    {
        // Can be overloaded
    }

    /**
     * Is a point relative to the parent's origin within the pane?
     *
     * @param mx point x
     * @param my point y
     * @return true if the point is in the pane
     */
    protected boolean isPointInPane(int mx, int my)
    {
        return mx >= x && mx < (x + width) &&
                 my >= y && my < (y + height);
    }

    //  Dimensions
    public int getWidth()
    {
        return width;
    }

    //  Drawing

    public int getHeight()
    {
        return height;
    }

    /**
     * Returns the first Pane (depth-first search) of a given ID,
     * if it matches the specified type.
     * Performs a depth-first search on the hierarchy of Panes and Views.
     *
     * @param id   ID of Pane to find
     * @param type Class of the desired Pane type
     * @param <T>  The type of pane returned
     * @return a Pane of the given ID, if it matches the specified type
     */
    public final <T extends Pane> T findPaneOfTypeByID(String id, @NotNull Class<T> type)
    {
        @Nullable Pane p = findPaneByID(id);
        try
        {
            return type.cast(p);
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException(String.format("No pane with id %s and type %s was found.", id, type), e);
        }
    }

    //  Subpanes

    /**
     * Returns the first Pane of a given ID
     * Performs a depth-first search on the hierarchy of Panes and Views
     *
     * @param id ID of Pane to find
     * @return a Pane of the given ID
     */
    @Nullable
    public Pane findPaneByID(String id)
    {
        return this.id.equals(id) ? this : null;
    }

    /**
     * Return the Pane that contains this one.
     *
     * @return the Pane that contains this one
     */
    public final View getParent()
    {
        return parent;
    }

    /**
     * Return the Window that this Pane ultimately belongs to.
     *
     * @return the Window that this Pane belongs to.
     */
    public final Window getWindow()
    {
        return window;
    }

    protected void setWindow(Window w)
    {
        window = w;
    }

    /**
     * Put this Pane inside a View.  Only Views and subclasses can contain Panes
     *
     * @param newParent the View to put this Pane into, or null to remove from Parents
     */
    public void putInside(View newParent)
    {
        if (parent != null)
        {
            parent.removeChild(this);
        }

        parent = newParent;

        if (parent != null)
        {
            parent.addChild(this);
        }
    }

    public boolean isClickable()
    {
        return visible && enabled;
    }

    //  Mouse

    /**
     * Process a mouse down on the Pane.
     * <p>
     * It is advised that only containers of other panes override this method
     *
     * @param mx mouse X coordinate, relative to parent's top-left
     * @param my mouse Y coordinate, relative to parent's top-left
     */
    public void click(int mx, int my)
    {
        setLastClickedPane(this);
        handleClick(mx - x, my - y);
    }

    private static synchronized void setLastClickedPane(Pane pane)
    {
        lastClickedPane = pane;
    }

    /**
     * Process a click on the Pane
     * <p>
     * Override this to process the actual click
     *
     * @param mx mouse X coordinate, relative to Pane's top-left
     * @param my mouse Y coordinate, relative to Pane's top-left
     */
    public void handleClick(int mx, int my)
    {
        // Can be overloaded
    }

    public boolean canHandleClick(int mx, int my)
    {
        return visible && enabled && isPointInPane(mx, my);
    }

    public boolean onKeyTyped(char ch, int key)
    {
        return false;
    }

    public void onUpdate()
    {
        // Can be overloaded
    }

    protected synchronized void scissorsStart()
    {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16 * 4);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);

        int scissorsX = (int) fb.get(12) + getX();
        int scissorsY = (int) fb.get(13) + getY();
        int h = getHeight();
        int w = getWidth();

        if (!scissorsInfoStack.isEmpty())
        {
            ScissorsInfo parentInfo = scissorsInfoStack.peek();
            int right = scissorsX + w;
            int bottom = scissorsY + h;
            int parentRight = parentInfo.x + parentInfo.width;
            int parentBottom = parentInfo.y + parentInfo.height;

            scissorsX = Math.max(scissorsX, parentInfo.x);
            scissorsY = Math.max(scissorsY, parentInfo.y);

            w = Math.max(0, Math.min(right, parentRight) - scissorsX);
            h = Math.max(0, Math.min(bottom, parentBottom) - scissorsY);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        @NotNull ScissorsInfo info = new ScissorsInfo(scissorsX, scissorsY, w, h);
        scissorsInfoStack.push(info);

        int scale = Screen.getScale();
        GL11.glScissor(info.x * scale, mc.displayHeight - ((info.y + info.height) * scale), info.width * scale, info.height * scale);
    }

    //  Position
    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    protected synchronized void scissorsEnd()
    {
        scissorsInfoStack.pop();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (!scissorsInfoStack.isEmpty())
        {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            ScissorsInfo info = scissorsInfoStack.peek();
            int scale = Screen.getScale();
            GL11.glScissor(info.x * scale, mc.displayHeight - ((info.y + info.height) * scale), info.width * scale, info.height * scale);
        }
    }

    private static class ScissorsInfo
    {
        private int x;
        private int y;
        private int width;
        private int height;

        ScissorsInfo(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
    }
}
