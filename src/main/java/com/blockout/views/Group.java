package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import org.jetbrains.annotations.NotNull;

/**
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 * <p>
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class Group extends View
{
    private int spacing = 0;

    /**
     * Required default constructor.
     */
    public Group()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams
     *
     * @param params Params for the Pane
     */
    public Group(@NotNull PaneParams params)
    {
        super(params);
        spacing = params.getIntegerAttribute("spacing", spacing);
    }

    @Override
    public void adjustChild(@NotNull Pane child)
    {
        int childX = child.getX();
        int childY = spacing;
        int childWidth = child.getWidth();
        int childHeight = child.getHeight();

        //  Adjust for horizontal size and alignment
        if (childWidth < 0)
        {
            childWidth = getInteriorWidth();
        }
        else if (child.getAlignment().isRightAligned())
        {
            childX = (getInteriorWidth() - childWidth) - childX;
        }
        else if (child.getAlignment().isHorizontalCentered())
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;
        }

        for (@NotNull Pane c : children)
        {
            if (c == child)
            {
                break;
            }
            childY = c.getY() + c.getHeight() + spacing;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
    }

    @Override
    public void removeChild(@NotNull Pane child)
    {
        super.removeChild(child);

        int formerChildY = child.getY();
        int formerChildHeight = child.getHeight();

        for (@NotNull Pane c : children)
        {
            if (c.getY() > formerChildY)
            {
                c.moveBy(0, -formerChildHeight);
            }
        }
    }
}
