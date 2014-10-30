package com.blockout.views;

import com.blockout.Loader;
import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import net.minecraft.util.MathHelper;

import java.util.List;

/*
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingListFixed extends View
{
    public static interface DataProvider
    {
        int getElementCount();

        int updateElement(int index, Pane elementPane);
    }

    //    View container;
    int visibleListElements = 0;    //  Number of total elements
    int listElementHeight   = 0;
    int scrollOffset        = 0;    //  Scroll offset (in elements)
    DataProvider provider;

    public ScrollingListFixed(){ super(); }

    public ScrollingListFixed(ScrollingListFixed other){ super(other); }

    public ScrollingListFixed(PaneParams params)
    {
        super(params);
    }

    void setDataProvider(DataProvider p)
    {
        provider = p;

    }

    public int getScroll()
    {
        return scrollOffset;
    }

    public int getMaxScroll()
    {
        return Math.max(0, children.size() - visibleListElements);
    }

    public void setScroll(int offset)
    {
        int newScrollOffset = MathHelper.clamp_int(offset, 0, getMaxScroll());

        if (newScrollOffset != scrollOffset)
        {
            scrollOffset = newScrollOffset;
            refreshElementPanes();
        }

    }

    public void scrollBy(int delta)
    {
        setScroll(scrollOffset + delta);
    }

    public void clampScroll()
    {
        setScroll(scrollOffset);
    }

    protected void refreshElementPanes()
    {
        int numElements = (provider != null) ? provider.getElementCount() : 0;
        int lastValidVisibleElement = Math.min(visibleListElements, numElements);
        for (int i = 0; i < lastValidVisibleElement; ++i)
        {
            Pane child = children.get(i);
            if (child != null)
            {
                if (provider != null)
                {
                    provider.updateElement(scrollOffset + i, child);
                    child.setVisible(true);
                    child.setEnabled(true);
                }
                else
                {
                    child.setVisible(false);
                    child.setEnabled(false);
                }
            }
        }

        for (int i = lastValidVisibleElement; i < visibleListElements; ++i)
        {
            Pane child = children.get(i);
            if (child != null)
            {
                child.setVisible(false);
                child.setEnabled(false);
            }
        }
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null) return;

//        container = new View();
//        container.setSize(0, getWidth() - 16);
//        container.putInside(this);

        PaneParams paneNode = childNodes.get(0);

        Pane child = Loader.createFromPaneParams(paneNode, /*container*/ this);
        child.setPosition(0, 0);
        child.setVisible(false);
        child.setEnabled(false);

        listElementHeight = child.getHeight();
        visibleListElements = Math.max(1, getHeight() / listElementHeight);

        for (int i = 1; i < visibleListElements; ++i)
        {
            child = Loader.createFromPaneParams(paneNode, /*container*/ this);
            child.setPosition(0, i * listElementHeight);
            child.setVisible(false);
            child.setEnabled(false);
        }
    }

    @Override
    public void adjustChild(Pane child)
    {
        //  Children in a ScrollView don't exist in normal locations...
        child.setPosition(0, children.indexOf(child) * listElementHeight);
        child.setSize(getInteriorHeight() - 16, child.getHeight());
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        super.drawSelf(mx, my);
    }

    @Override
    public void handleClick(int mx, int my)
    {
        if (scrollOffset >= getMaxScroll())
        {
            setScroll(0);
        }
        else
        {
            scrollBy(1);
        }
    }

    public int getElementForPane(Pane pane)
    {
        while (pane != null && pane.getParent() != /*container*/ this)
        {
            pane = pane.getParent();
        }

        if (pane == null)
        {
            return -1;
        }

        return pane.getY() / listElementHeight;
    }
}
