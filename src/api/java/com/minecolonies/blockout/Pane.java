package com.minecolonies.blockout;

import com.minecolonies.blockout.views.View;
import com.minecolonies.blockout.views.Window;
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
 * A Pane is the root of all UI objects.
 */
public class Pane extends Gui
{
    @NotNull
    private static final Deque<ScissorsInfo> scissorsInfoStack = new ConcurrentLinkedDeque<>();
    private static final int SCISSOR_X_INDEX = 12;
    private static final int SCISSOR_Y_INDEX = 13;
    protected static Pane lastClickedPane;
    protected static Pane focus;
    protected        Pane onHover;
    protected static boolean   debugging = false;
    protected        Minecraft mc        = Minecraft.getMinecraft();
    //  Attributes
    protected        String    id        = "";
    protected        int       x         = 0;
    protected        int       y         = 0;
    protected        int       width     = 0;
    protected        int       height    = 0;
    protected        Alignment alignment = Alignment.TOP_LEFT;
    protected        boolean   visible   = true;
    protected        boolean   enabled   = true;
    protected        String    onHoverId = "";
    //  Runtime
    protected Window window;
    protected View   parent;

    /**
     * Default constructor.
     */
    public Pane()
    {
        super();
        //Required for panes.
    }

    /**
     * Constructs a Pane from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public Pane(@NotNull final PaneParams params)
    {
        super();
        id = params.getStringAttribute("id", id);

        @NotNull final PaneParams.SizePair parentSizePair = new PaneParams.SizePair(params.getParentWidth(), params.getParentHeight());
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
        onHoverId = params.getStringAttribute("onHoverId");
    }

    /**
     * Returns the currently focused Pane.
     *
     * @return the currently focused Pane.
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
     * Override to respond to the Pane losing focus.
     */
    public void onFocusLost()
    {
        // Can be overloaded
    }

    /**
     * Override to respond to the Pane becoming the current focus.
     */
    public void onFocus()
    {
        // Can be overloaded
    }

    /**
     * Parse the children of the pane.
     *
     * @param params the parameter.
     */
    public void parseChildren(final PaneParams params)
    {
        // Can be overloaded
    }

    //  ID
    public final String getID()
    {
        return id;
    }

    public final void setID(final String id)
    {
        this.id = id;
    }

    /**
     * Set the size of a pane.
     *
     * @param w the width.
     * @param h the height.
     */
    public void setSize(final int w, final int h)
    {
        width = w;
        height = h;
    }

    /**
     * Set the position of the pane.
     *
     * @param newX the new x.
     * @param newY the new y.
     */
    public void setPosition(final int newX, final int newY)
    {
        x = newX;
        y = newY;
    }

    /**
     * Move the pane by x and y to a place.
     *
     * @param dx the x.
     * @param dy the y.
     */
    public void moveBy(final int dx, final int dy)
    {
        x += dx;
        y += dy;
    }

    public Alignment getAlignment()
    {
        return alignment;
    }

    public void setAlignment(final Alignment alignment)
    {
        this.alignment = alignment;
    }

    //  Visibility
    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(final boolean v)
    {
        visible = v;
    }

    /**
     * Show this pane.
     */
    public void show()
    {
        setVisible(true);
    }

    /**
     * Hide this pane.
     */
    public void hide()
    {
        setVisible(false);
    }

    //  Enabling
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean e)
    {
        enabled = e;
    }

    /**
     * Enable this pane.
     */
    public void enable()
    {
        setEnabled(true);
    }

    /**
     * Disable this pane.
     */
    public void disable()
    {
        setEnabled(false);
    }

    /**
     * Is pane visible and enabled
     */
    public boolean isOn()
    {
        return enabled && visible;
    }

    /**
     * Enable and show this pane.
     */
    public void on()
    {
        setEnabled(true);
        setVisible(true);
    }

    /**
     * Disable and hide this pane.
     */
    public void off()
    {
        setEnabled(false);
        setVisible(false);
    }

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
     * @return <tt>true</tt> if this Pane is the current focus.
     */
    public final synchronized boolean isFocus()
    {
        return focus == this;
    }

    /**
     * Set the currently focused Pane.
     *
     * @param f Pane to focus, or nil.
     */
    public static synchronized void setFocus(final Pane f)
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
     * @param mx mouse x.
     * @param my mouse y.
     */
    public final void draw(final int mx, final int my)
    {
        if (visible)
        {
            drawSelf(mx, my);
            if (debugging)
            {
                final boolean isMouseOver = isPointInPane(mx, my);
                final int color = isMouseOver ? 0xFF00FF00 : 0xFF0000FF;

                Render.drawOutlineRect(x, y, x + getWidth(), y + getHeight(), color);

                if (isMouseOver && !id.isEmpty())
                {
                    final int stringWidth = mc.fontRenderer.getStringWidth(id);
                    mc.fontRenderer.drawString(id, x + getWidth() - stringWidth, y + getHeight() - mc.fontRenderer.FONT_HEIGHT, color);
                }
            }
        }
    }

    /**
     * Draw self.  The graphics port is already relative to the appropriate
     * location.
     * <p>
     * Override this to actually draw.
     *
     * @param mx Mouse x (relative to parent).
     * @param my Mouse y (relative to parent).
     */
    public void drawSelf(final int mx, final int my)
    {
        // Can be overloaded
    }

    /**
     * Is a point relative to the parent's origin within the pane?.
     *
     * @param mx point x.
     * @param my point y.
     * @return true if the point is in the pane.
     */
    protected boolean isPointInPane(final int mx, final int my)
    {
        return mx >= x && mx < (x + width)
                 && my >= y && my < (y + height);
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
     * Returns the first Pane (depth-first search) of a given ID.
     * if it matches the specified type.
     * Performs a depth-first search on the hierarchy of Panes and Views.
     *
     * @param id   ID of Pane to find.
     * @param type Class of the desired Pane type.
     * @param <T>  The type of pane returned.
     * @return a Pane of the given ID, if it matches the specified type.
     */
    public final <T extends Pane> T findPaneOfTypeByID(final String id, @NotNull final Class<T> type)
    {
        @Nullable final Pane p = findPaneByID(id);
        try
        {
            return type.cast(p);
        }
        catch (final ClassCastException e)
        {
            throw new IllegalArgumentException(String.format("No pane with id %s and type %s was found.", id, type), e);
        }
    }

    //----------Subpanes-------------//

    /**
     * Returns the first Pane of a given ID.
     * Performs a depth-first search on the hierarchy of Panes and Views.
     *
     * @param id ID of Pane to find.
     * @return a Pane of the given ID.
     */
    @Nullable
    public Pane findPaneByID(final String id)
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

    public void setWindow(final Window w)
    {
        window = w;
    }

    /**
     * Put this Pane inside a View.  Only Views and subclasses can contain
     * Panes.
     *
     * @param newParent the View to put this Pane into, or null to remove from
     *                  Parents.
     */
    public void putInside(final View newParent)
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

    //----------Mouse-------------//

    /**
     * Process a mouse down on the Pane.
     * <p>
     * It is advised that only containers of other panes override this method.
     *
     * @param mx mouse X coordinate, relative to parent's top-left.
     * @param my mouse Y coordinate, relative to parent's top-left.
     */
    public void click(final int mx, final int my)
    {
        setLastClickedPane(this);
        handleClick(mx - x, my - y);
    }

    /**
     * Process a rightclick mouse down on the Pane.
     * <p>
     * It is advised that only containers of other panes override this method.
     *
     * @param mx mouse X coordinate, relative to parent's top-left.
     * @param my mouse Y coordinate, relative to parent's top-left.
     */
    public void rightClick(final int mx, final int my)
    {
        setLastClickedPane(this);
        handleRightClick(mx - x, my - y);
    }

    /**
     * Set a pane as the last clicked pane.
     *
     * @param pane pane to set.
     */
    private static synchronized void setLastClickedPane(final Pane pane)
    {
        lastClickedPane = pane;
    }

    /**
     * Process a click on the Pane.
     * <p>
     * Override this to process the actual click.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left.
     * @param my mouse Y coordinate, relative to Pane's top-left.
     */
    public void handleClick(final int mx, final int my)
    {
        // Can be overloaded
    }

    /**
     * Process a right click on the Pane.
     * <p>
     * Override this to process the actual click.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left.
     * @param my mouse Y coordinate, relative to Pane's top-left.
     */
    public void handleRightClick(final int mx, final int my)
    {
        // Can be overloaded
    }

    /**
     * Check if a pane can handle clicks.
     *
     * @param mx int x position.
     * @param my int y position.
     * @return true if so.
     */
    public boolean canHandleClick(final int mx, final int my)
    {
        return visible && enabled && isPointInPane(mx, my);
    }

    /**
     * Called when a key is pressed.
     *
     * @param ch  the character.
     * @param key the key.
     * @return false at all times - do nothing.
     */
    public boolean onKeyTyped(final char ch, final int key)
    {
        return false;
    }

    /**
     * On update. Can be overloaded.
     */
    public void onUpdate()
    {
        // Can be overloaded
    }

    protected synchronized void scissorsStart()
    {
        final FloatBuffer fb = BufferUtils.createFloatBuffer(16 * 4);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);

        int scissorsX = (int) fb.get(SCISSOR_X_INDEX) + getX();
        int scissorsY = (int) fb.get(SCISSOR_Y_INDEX) + getY();
        int h = getHeight();
        int w = getWidth();

        if (!scissorsInfoStack.isEmpty())
        {
            final ScissorsInfo parentInfo = scissorsInfoStack.peek();
            final int right = scissorsX + w;
            final int bottom = scissorsY + h;
            final int parentRight = parentInfo.x + parentInfo.width;
            final int parentBottom = parentInfo.y + parentInfo.height;

            scissorsX = Math.max(scissorsX, parentInfo.x);
            scissorsY = Math.max(scissorsY, parentInfo.y);

            w = Math.max(0, Math.min(right, parentRight) - scissorsX);
            h = Math.max(0, Math.min(bottom, parentBottom) - scissorsY);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        @NotNull final ScissorsInfo info = new ScissorsInfo(scissorsX, scissorsY, w, h);
        scissorsInfoStack.push(info);

        final int scale = Screen.getScale();
        GL11.glScissor(info.x * scale, mc.displayHeight - ((info.y + info.height) * scale), info.width * scale, info.height * scale);
    }

    /**
     * X position.
     *
     * @return the int x.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Y position.
     *
     * @return the int y.
     */
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

            final ScissorsInfo info = scissorsInfoStack.peek();
            final int scale = Screen.getScale();
            GL11.glScissor(info.x * scale, mc.displayHeight - ((info.y + info.height) * scale), info.width * scale, info.height * scale);
        }
    }

    /**
     * Wheel input.
     * @param wheel minus for down, plus for up.
     */
    public void scrollInput(final int wheel)
    {
        /**
         * Can be overwritten by child classes
         */
    }

    /**
     * Set the parent of the child.
     * @param view the parent view.
     */
    public void setParentView(final View view)
    {
        this.parent = view;
    }

    private static class ScissorsInfo
    {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        ScissorsInfo(final int x, final int y, final int w, final int h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
    }

    /**
     * Handle onHover element, element must be visible
     * TODO: bug: must have pos set from xml (or be not in a group)
     */
    public void handleHover(final int mx, final int my)
    {
        if (onHover == null)
        {
            if (!onHoverId.isEmpty())
            {
                onHover = window.findPaneByID(onHoverId);
            }
            else
            {
                return;
            }
        }
        if (!this.isVisible())
        {
            if (onHover.isVisible())
            {
                onHover.off();
            }
            return;
        }
        if (onHover.isPointInPane(mx, my) && onHover.isVisible())
        {
            return;
        }
        if (this.isPointInPane(mx, my) && !onHover.isVisible())
        {
            onHover.on();
            return;
        }
        if (!this.isPointInPane(mx, my) && onHover.isVisible())
        {
            onHover.off();
        }
    }
}
