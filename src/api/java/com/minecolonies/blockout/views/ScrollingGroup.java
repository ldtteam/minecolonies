package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;

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
    public ScrollingGroup(final PaneParams params)
    {
        super(params);
    }

    @Override
    public void adjustChild(final Pane child)
    {
        int childY = 0;
        if (children.size() >= 2)
        {
            final Pane lastChild = children.get(children.size() - 2);
            childY = lastChild.getY() + lastChild.getHeight();
        }

        child.setPosition(0, childY);
        child.setSize(getInteriorWidth(), child.getHeight());
    }

    @Override
    public void removeChild(final Pane child)
    {
        super.removeChild(child);

        final int formerChildY = child.getY();
        final int formerChildHeight = child.getHeight();

        for (final Pane c : children)
        {
            if (c.getY() > formerChildY)
            {
                c.moveBy(0, -formerChildHeight);
            }
        }
    }
}
