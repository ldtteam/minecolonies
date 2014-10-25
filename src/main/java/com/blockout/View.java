package com.blockout;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/*
 * A View is a Pane which can contain other Panes
 */
public class View extends Pane
{
    List<Pane> children = new ArrayList<Pane>();

    public View() { super(); }
    public View(View other) { super(other); }

    /**
     * Constructs a View from XML, and place it into the given Parent
     *
     * @param xml XML Node for the Pane
     */
    public View(XMLNode xml)
    {
        super(xml);
        //  TODO - Any attributes of our own?
    }

    int getInteriorWidth() { return width; }
    int getInteriorHeight() { return height; }

    public void parseChildren(XMLNode xml)
    {
        List<XMLNode> childNodes = xml.getChildren();
        if (childNodes == null) return;

        for (XMLNode node : childNodes)
        {
            Loader.createFromXML(node, this);
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
            childX = 0;
            childWidth = getInteriorWidth();
        }
        else if (child.alignment.rightAligned)
        {
            childX = (getInteriorWidth() - childWidth) - childX;
        }
        else if (child.alignment.horizontalCentered)
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;
        }

        //  Adjust for vertical size and alignment
        if (childHeight < 0)
        {
            childY = 0;
            childHeight = getInteriorHeight();
        }
        else if (child.alignment.bottomAligned)
        {
            childY = (getInteriorHeight() - childHeight) - childY;
        }
        else if (child.alignment.verticalCentered)
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

    @Override
    protected void drawSelf(int mx, int my)
    {
        //  Translate the drawing origin to our x,y
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, 0);

        //  Translate Mouse into the View
        mx -= x;
        my -= y;

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
        Pane clickedPane = findPaneForClick(mx - x, my - y);
        if (clickedPane != null)
        {
            clickedPane.click(mx - x, my - y);
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
