package com.blockout;

import com.blockout.views.Window;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/*
 * A View is a Pane which can contain other Panes
 */
public class View extends Pane
{
    protected List<Pane> children = new ArrayList<Pane>();
    protected int padding = 0;

    public View() { super(); }
    public View(View other) { super(other); }

    /**
     * Constructs a View from PaneParams, and place it into the given Parent
     *
     * @param params Params for the View
     */
    public View(PaneParams params)
    {
        super(params);
        padding = params.getIntegerAttribute("padding", padding);
        //  TODO - Any attributes of our own?
    }

    public List<Pane> getChildren() { return children; }

    public int getInteriorWidth() { return width - (padding * 2); }
    public int getInteriorHeight() { return height - (padding * 2); }

    public void parseChildren(PaneParams params)
    {
        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null) return;

        for (PaneParams node : childNodes)
        {
            Loader.createFromPaneParams(node, this);
        }
    }

    protected void adjustChild(Pane child)
    {
        int childX = child.getX(),
            childY = child.getY();
        int childWidth = child.getWidth(),
            childHeight = child.getHeight();

        //  Adjust for horizontal size and alignment
        if (childWidth < 0)
        {
            //childX = 0;
            childWidth = getInteriorWidth();
            //childX += padding;
        }
        else if (child.alignment.rightAligned)
        {
            childX = (getInteriorWidth() - childWidth) - childX;// - padding;
        }
        else if (child.alignment.horizontalCentered)
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;// + padding;
        }
//        else
//        {
//            childX += padding;
//        }

        //  Adjust for vertical size and alignment
        if (childHeight < 0)
        {
            //childY = 0;
            childHeight = getInteriorHeight();
            //childY += padding;
        }
        else if (child.alignment.bottomAligned)
        {
            childY = (getInteriorHeight() - childHeight) - childY;// - padding;
        }
        else if (child.alignment.verticalCentered)
        {
            childY = ((getInteriorHeight() - childHeight) / 2) + childY;// + padding;
        }
//        else
//        {
//            childY += padding;
//        }

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

    @Override
    protected void drawSelf(int mx, int my)
    {
        //  Translate the drawing origin to our x,y
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x + padding, (float)y + padding, 0);

        //  Translate Mouse into the View
        mx -= x + padding;
        my -= y + padding;

        for (Pane child : children)
        {
            child.draw(mx, my);
        }

        GL11.glPopMatrix();
    }

    @Override
    public Pane findPaneByID(String id)
    {
        if (this.id.equals(id))
        {
            return this;
        }
        else
        {
            for (Pane child : children)
            {
                Pane found = child.findPaneByID(id);
                if (found != null)
                {
                    return found;
                }
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


    public void addChild(Pane child)
    {
        child.setWindow(getWindow());
        children.add(child);
        adjustChild(child);
    }

    public void removeChild(Pane child)
    {
        children.remove(child);
    }

    //public void removeAllChildren() { children.clear(); }

    //  Mouse
    @Override
    public void click(int mx, int my)
    {
        int childmx = mx - x - padding;
        int childmy = my - y - padding;
        Pane clickedPane = findPaneForClick(childmx, childmy);
        if (clickedPane != null)
        {
            clickedPane.click(childmx, childmy);
        }
        else
        {
            super.click(mx, my);
        }
    }

    @Override
    public void onUpdate()
    {
        for (Pane child : children)
        {
            child.onUpdate();
        }
    }
}
