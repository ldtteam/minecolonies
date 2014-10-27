package com.blockout;

/*
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class Group extends View
{
    int spacing = 0;

    public Group(){ super(); }

    public Group(Group other){ super(other); }

    /**
     * Constructs a View from PaneParams
     *
     * @param params Params for the Pane
     */
    public Group(PaneParams params)
    {
        super(params);
        spacing = params.getIntegerAttribute("spacing", spacing);
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
            //childX = 0;
            childWidth = getInteriorWidth();
            //childX += padding;
        }
        else if (child.alignment.rightAligned)
        {
            childX = (getInteriorWidth() - childWidth) - childX;// + padding;
        }
        else if (child.alignment.horizontalCentered)
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;// + padding;
        }
//        else
//        {
//            childX += padding;
//        }

        childY = spacing;
        for (Pane c : children)
        {
            if (c == child) break;
            childY = c.getY() + c.getHeight() + spacing;
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
