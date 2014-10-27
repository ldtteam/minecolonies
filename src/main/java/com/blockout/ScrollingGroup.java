package com.blockout;

import java.util.List;

/*
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingGroup extends View
{
    ScrollingGroupView scrollView;
    PaneParams         listNode;

    public ScrollingGroup(){ super(); }

    public ScrollingGroup(ScrollingGroup other){ super(other); }

    /**
     * Constructs a ScrollingList from PaneParams
     *
     * @param params Params for the ScrollingList
     */
    public ScrollingGroup(PaneParams params)
    {
        super(params);
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        scrollView = new ScrollingGroupView();
        scrollView.setSize(getWidth() - 16, getHeight());
        scrollView.putInside(this);

        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null) return;

        //  Get the PaneParams for this child, because we'll need it in the future
        //  to create more nodes
        listNode = childNodes.get(0);

        //  TEMP
        scrollView.parseChildren(params);
        scrollView.setListElementHeight(scrollView.children.get(0).getHeight());
    }

    @Override
    public void handleClick(int mx, int my)
    {
        if (scrollView.getScrollY() >= scrollView.getMaxScrollY())
        {
            scrollView.setScrollY(0);
        }
        else
        {
            scrollView.scrollBy(5);
        }
    }
}
