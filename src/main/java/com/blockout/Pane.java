package com.blockout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

/*
 * A Pane is the root of all UI objects
 */
public class Pane extends Gui
{
    protected Minecraft mc = Minecraft.getMinecraft();

    //  Attributes
    protected String    id = "";
    protected int       x  = 0, y = 0;
    protected int       width = 0, height = 0;
    protected Alignment alignment = Alignment.TopLeft;
    protected boolean   visible   = true;
    //protected boolean active = true;
    protected boolean   enabled   = true;

    //  Runtime
    protected Window      window;
    protected View        parent;
    protected static Pane lastClickedPane;
    protected static Pane focus;

    /**
     * Default constructor
     */
    public Pane()
    {
    }

    /**
     * Constructs a Pane by copying (most) attributes of another Pane
     *
     * @param other Another Pane to copy the attributes from
     */
    public Pane(Pane other)
    {
        id = other.id;

        x = other.x;
        y = other.y;
        width = other.width;
        height = other.height;
        alignment = other.alignment;

        visible = other.visible;
        //active = other.active;
        enabled = other.enabled;
    }

    /**
     * Constructs a Pane from XML
     *
     * @param xml XML Node for the Pane
     */
    public Pane(XMLNode xml)
    {
        id        = xml.getStringAttribute("id", id);
        width     = xml.getIntegerAttribute("width", width);
        height    = xml.getIntegerAttribute("height", height);
        x         = xml.getIntegerAttribute("x", x);
        y         = xml.getIntegerAttribute("y", y);
        alignment = xml.getEnumAttribute("align", alignment);
        visible   = xml.getBooleanAttribute("visible", visible);
        enabled   = xml.getBooleanAttribute("enabled", enabled);
    }

    public void parseChildren(XMLNode xml) {}

    //  ID
    public final String getID() { return id; }
    public final void setID(String id) { this.id = id; }

    //  Dimensions
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setSize(int w, int h) { width = w; height = h; }

    //  Position
    public int getX() { return x; }
    public int getY() { return y; }
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

    public Alignment getAlignment() { return alignment; }
    public void setAlignment(Alignment alignment) { this.alignment = alignment; }

    //  Visibility
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { visible = v; }

    public void show() { setVisible(true); }
    public void hide() { setVisible(false); }

    //  Activation
//    public boolean isActive() { return active; }
//    public void setActive(boolean a) { active = a; }
//
//    public void activate() { setActive(true); }
//    public void deactive() { setActive(false); }

    //  Enabling
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }

    public void enable() { setEnabled(true); }
    public void disable() { setEnabled(false); }


    //  Focus

    /**
     * Returns the currently focused Pane
     * @return the currently focused Pane
     */
    public static Pane getFocus()
    {
        return focus;
    }

    /**
     * Set the currently focused Pane
     *
     * @param f Pane to focus, or nil
     */
    public static void setFocus(Pane f)
    {
        if (focus != null) focus.onFocusLost();
        focus = f;
        if (focus != null) focus.onFocus();
    }

    /**
     * Clear the currently focused Pane.
     */
    public static void clearFocus()
    {
        setFocus(null);
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
     * @return <tt>true</tt> if this Pane is the current focus
     */
    public final boolean isFocus() { return focus == this; }

    /**
     * Override to respond to the Pane losing focus
     */
    public void onFocusLost() {}

    /**
     * Override to respond to the Pane becoming the current focus
     */
    public void onFocus() {}


    //  Drawing

    /**
     * Draw the current Pane if visible.
     *
     * @param mx
     * @param my
     */
    public final void draw(int mx, int my)
    {
        if (visible)
        {
            drawSelf(mx, my);
        }
    }

    /**
     * Draw self.  The graphics port is already relative to the appropriate location
     *
     * Override this to actually draw.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    protected void drawSelf(int mx, int my) {}

    //  Subpanes

    /**
     * Returns the first Pane of a given ID
     * Performs a depth-first search on the hierarchy of Panes and Views
     *
     * @param id ID of Pane to find
     * @return a Pane of the given ID
     */
    public Pane findPaneByID(String id)
    {
        return this.id.equals(id) ? this : null;
    }

    /**
     * Returns the first Pane (depth-first search) of a given ID,
     * if it matches the specifiedtype
     * Performs a depth-first search on the hierarchy of Panes and Views
     *
     * @param id ID of Pane to find
     * @param type Class of the desired Pane type
     * @return a Pane of the given ID, if it matches the specified type
     */
    public final <T extends Pane> T findPaneOfTypeByID(String id, Class<T> type)
    {
        Pane p = findPaneByID(id);
        return type.isInstance(p) ? type.cast(p) : null;
    }

    /**
     * Return the Pane that contains this one.
     *
     * @return the Pane that contains this one
     */
    public final View getParent() { return parent; }

    /**
     * Return the Window that this Pane ultimately belongs to.
     *
     * @return the Window that this Pane belongs to.
     */
    public final Window getWindow() { return window; }
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

    //  Mouse

    /**
     * Is a point relative to the parent's origin within the pane?
     *
     * @param mx
     * @param my
     * @return
     */
    public boolean isPointInPane(int mx, int my)
    {
        return mx >= x && mx < (x + width) &&
               my >= y && my < (y + height);
    }

    public boolean isClickable() { return visible && enabled; }

    /**
     * Process a click on the Pane
     *
     * Override this to process the actual click
     *
     * @param mx mouse X coordinate, relative to Pane's top-left
     * @param my mouse Y coordinate, relative to Pane's top-left
     */
    public void handleClick(int mx, int my)
    {
    }

    /**
     * Process a mouse down on the Pane.
     *
     * It is advised that only containers of other panes override this method
     *
     * @param mx mouse X coordinate, relative to parent's top-left
     * @param my mouse Y coordinate, relative to parent's top-left
     */
    public void click(int mx, int my)
    {
        lastClickedPane = this;
        handleClick(mx - x, my - y);
    }
    
    public boolean canHandleClick(int mx, int my)
    {
        return visible && enabled && isPointInPane(mx, my);
    }

    public boolean onKeyTyped(char ch, int key) { return false; }

    public void onUpdate() {}
}
