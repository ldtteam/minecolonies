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
    //protected int       gx = 0, gy = 0;
    protected int width = 0, height = 0;
    protected Alignment alignment = Alignment.TopLeft;
    protected boolean   visible   = true;
    //protected boolean active = true;
    protected boolean   enabled   = true;

    //  Runtime
    protected View parent;

    //  ---
    public static class PaneInfo
    {
        public String id = "";

        public int x = 0, y = 0;
        public int width = 0, height = 0;
        public Alignment alignment = Alignment.TopLeft;

        public boolean visible = true;
        public boolean enabled = true;

        public PaneInfo(){}

        public PaneInfo(PaneInfo other)
        {
            id = other.id;
            x = other.x;
            y = other.y;
            width = other.width;
            height = other.height;
            alignment = other.alignment;
            visible = other.visible;
            enabled = other.enabled;
        }
    }

    public Pane()
    {
    }

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

    public Pane(PaneInfo info, View parentView)
    {
        id = info.id;
        width = info.width;
        height = info.height;
        x = info.x;
        y = info.y;
        alignment = info.alignment;
        visible = info.visible;
        enabled = info.enabled;

        putInside(parentView);
    }

    public Pane(PaneInfo info)
    {
        this(info, null);
    }

    public String getID() { return id; }
    public void setID(String id) { this.id = id; }

    //  Dimensions
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setSize(int w, int h) { width = w; height = h; }

    //  Position
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int newX, int newY) { x = newX; y = newY; }

    public void moveBy(int dx, int dy)
    {
        x += dx;
        y += dy;
    }

    public Alignment getAlignment() { return alignment; }
    public void setAlignment(Alignment alignment) { this.alignment = alignment; }

//    public int getGlobalX() { return gx; }
//    public int getGlobalY() { return gy; }
//
//    public void finalize(int px, int py)
//    {
//        gx = px + x;
//        gy = py + y;
//    }

    //  Visibility
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { visible = v; }

    public void show() { setVisible(true); }
    public void hide() { setVisible(false); }

    //  Activation
//    public boolean isActive() { return (active == TriState.On); }
//    public TriState getActiveState() { return active; }
//    public void setActiveState(TriState state) { active = state; }
//
//    public void activate() {}
//    public void deactive() {}
//    public void setActive(boolean makeActive) {}

    //  Enabling
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }

    public void enable() { setEnabled(true); }
    public void disable() { setEnabled(false); }

    //  Drawing
    public final void draw(int mx, int my, int scale)
    {
        if (visible)
        {
            drawSelf(mx, my, scale);
        }
    }

    /**
     * Draw self, graphics port is already relative to the appropriate location
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    protected void drawSelf(int mx, int my, int scale) {}

    //  Subpanes
    public Pane findPaneByID(String other)
    {
        return id.equals(other) ? this : null;
    }

    public View getParent() { return parent; }

    public void putInside(View newParent)
    {
        if (parent != null)
        {
            parent.removeChild(this);
        }

        parent = newParent;

        if (parent != null)
        {
            //  Adjust for horizontal alignment
            if (alignment.rightAligned)
            {
                x = parent.getWidth() - width - x;
            }
            else if (alignment.horizontalCentered)
            {
                x = ((parent.getWidth() - getWidth()) / 2) + x;
            }

            //  Adjust for vertical alignment
            if (alignment.bottomAligned)
            {
                y = parent.getHeight() - height - y;
            }
            else if (alignment.verticalCentered)
            {
                y = ((parent.getHeight() - getHeight()) / 2) + y;
            }

            parent.addChild(this);

            if (width < 0 || height < 0)
            {
                parent.expandChild(this);
            }
        }
    }

//    public void putInside(Window window)
//    {
//        putInside(window.getRoot());
//    }

    //  Mouse

    /**
     * Is a locally relative point in the pane?
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

    public boolean isClickable() { return false; }
    public void onClick(int mx, int my) {}
}
