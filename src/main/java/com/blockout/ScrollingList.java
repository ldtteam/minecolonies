package com.blockout;

import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.ListIterator;

/*
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingList extends View
{
    ScrollView scrollView;
    XMLNode listNode;

    public ScrollingList() { super(); }
    public ScrollingList(ScrollingList other) { super(other); }

    /**
     * Constructs a View from XML, and place it into the given Parent
     *
     * @param xml XML Node for the Pane
     */
    public ScrollingList(XMLNode xml)
    {
        super(xml);

        scrollView = new ScrollView();
        scrollView.setSize(getWidth() - 16, getHeight());
        scrollView.putInside(this);
    }

    @Override
    public void parseChildren(XMLNode xml)
    {
        List<XMLNode> childNodes = xml.getChildren();
        if (childNodes == null) return;

        //  Get the XML node for this child, because we'll need it in the future
        //  to create more nodes
        listNode = childNodes.get(0);

        //  TEMP
        scrollView.parseChildren(xml);
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
