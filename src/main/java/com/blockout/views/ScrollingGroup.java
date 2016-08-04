package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;

/**
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 * <p>
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingGroup extends ScrollingView
{
    /**
     * Required default constructor.
     */
    public ScrollingGroup()
    {
        super();
    }

    /**
     * Load from xml.
     *
     * @param params xml parameters.
     */
    public ScrollingGroup(PaneParams params)
    {
        super(params);
    }

    @Override
    public void adjustChild(Pane child)
    {
        int childY = 0;
        if (children.size() >= 2)
        {
            Pane lastChild = children.get(children.size() - 2);
            childY = lastChild.getY() + lastChild.getHeight();
        }

        child.setPosition(0, childY);
        child.setSize(getInteriorWidth(), child.getHeight());
    }

    @Override
    public void removeChild(Pane child)
    {
        super.removeChild(child);

        int formerChildY = child.getY();
        int formerChildHeight = child.getHeight();

        for (Pane c : children)
        {
            if (c.getY() > formerChildY)
            {
                c.moveBy(0, -formerChildHeight);
            }
        }
    }
}
