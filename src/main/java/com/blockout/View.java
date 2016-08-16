package com.blockout;

import com.blockout.views.Window;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A View is a Pane which can contain other Panes
 */
public class View extends Pane
{
    protected List<Pane> children = new ArrayList<>();
    protected int padding = 0;

    /**
     * Constructs a barebones View
     */
    public View()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams
     *
     * @param params Params for the View
     */
    public View(PaneParams params)
    {
        super(params);
        padding = params.getIntegerAttribute("padding", padding);
    }

    public List<Pane> getChildren()
    {
        return children;
    }

    public int getInteriorWidth()
    {
        return width - (padding * 2);
    }

    public int getInteriorHeight()
    {
        return height - (padding * 2);
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null)
        {
            return;
        }

        for (PaneParams node : childNodes)
        {
            Loader.createFromPaneParams(node, this);
        }
    }

    protected void adjustChild(Pane child)
    {
        int childX = child.getX();
        int childY = child.getY();
        int childWidth = child.getWidth();
        int childHeight = child.getHeight();

        //  Negative width = 100% of parents width minus abs(width)
        if (childWidth < 0)
        {
            childWidth = Math.max(0, getInteriorWidth() + childWidth);
        }

        //  Adjust for horizontal alignment
        if (child.alignment.isRightAligned())
        {
            childX = (getInteriorWidth() - childWidth) - childX;
        }
        else if (child.alignment.isHorizontalCentered())
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;
        }

        //  Negative height = 100% of parents height minus abs(height)
        if (childHeight < 0)
        {
            childHeight = Math.max(0, getInteriorHeight() + childHeight);
        }

        //  Adjust for vertical alignment
        if (child.alignment.isBottomAligned())
        {
            childY = (getInteriorHeight() - childHeight) - childY;
        }
        else if (child.alignment.isVerticalCentered())
        {
            childY = ((getInteriorHeight() - childHeight) / 2) + childY;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
    }

    @Override
    protected void setWindow(Window w)
    {
        super.setWindow(w);
        for (Pane child : children)
        {
            child.setWindow(w);
        }
    }

    protected boolean childIsVisible(Pane child)
    {
        return child.getX() < getInteriorWidth() &&
                child.getY() < getInteriorHeight() &&
                (child.getX() + child.getWidth()) >= 0 &&
                (child.getY() + child.getHeight()) >= 0;
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        //  Translate the drawing origin to our x,y
        GL11.glPushMatrix();

        int paddedX = x + padding;
        int paddedY = y + padding;

        GL11.glTranslatef((float) paddedX, (float) paddedY, 0);

        //  Translate Mouse into the View
        int drawX = mx - paddedX;
        int drawY = my - paddedY;

        children.stream().filter(this::childIsVisible).forEach(child -> child.draw(drawX, drawY));

        GL11.glPopMatrix();
    }

    @Override
    public Pane findPaneByID(String id)
    {
        if (this.id.equals(id))
        {
            return this;
        }

        for (Pane child : children)
        {
            Pane found = child.findPaneByID(id);
            if (found != null)
            {
                return found;
            }
        }

        return null;
    }

    /**
     * Return a Pane that will handle a click action at the specified mouse coordinates
     *
     * @param mx Mouse X, relative to the top-left of this Pane
     * @param my Mouse Y, relative to the top-left of this Pane
     * @return a Pane that will handle a click action
     */
    public Pane findPaneForClick(int mx, int my)
    {
        ListIterator<Pane> it = children.listIterator(children.size());

        //  Iterate in reverse, since Panes later in the list draw on top of earlier panes
        while (it.hasPrevious())
        {
            Pane child = it.previous();
            if (child.canHandleClick(mx, my))
            {
                return child;
            }
        }

        return null;
    }

    /**
     * Add child Pane to this view.
     *
     * @param child pane to add.
     */
    public void addChild(Pane child)
    {
        child.setWindow(getWindow());
        children.add(child);
        adjustChild(child);
    }

    /**
     * Remove pane from view.
     *
     * @param child pane to remove.
     */
    public void removeChild(Pane child)
    {
        children.remove(child);
    }

    //  Mouse
    @Override
    public void click(int mx, int my)
    {
        int mxChild = mx - x - padding;
        int myChild = my - y - padding;
        Pane clickedPane = findPaneForClick(mxChild, myChild);
        if (clickedPane != null)
        {
            clickedPane.click(mxChild, myChild);
        }
        else
        {
            super.click(mx, my);
        }
    }

    @Override
    public void onUpdate()
    {
        children.forEach(Pane::onUpdate);
    }
}
