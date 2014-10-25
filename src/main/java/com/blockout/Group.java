package com.blockout;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/*
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class Group extends View
{
    int padding = 0;

    public Group() { super(); }
    public Group(Group other) { super(other); }

    /**
     * Constructs a View from XML, and place it into the given Parent
     *
     * @param xml XML Node for the Pane
     */
    public Group(XMLNode xml)
    {
        super(xml);
        padding = xml.getIntegerAttribute("padding", padding);
    }

    @Override
    public void adjustChild(Pane child)
    {
        int childX = child.getX(),
            childY = 0;
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

        childY = 0;
        for (Pane c : children)
        {
            if (c == child) break;
            childY = c.getY() + c.getHeight() + padding;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
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
