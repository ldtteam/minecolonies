package com.blockout.views;

import com.blockout.Loader;
import com.blockout.Pane;
import com.blockout.PaneParams;

import java.util.List;

/*
 * A ScrollingList is a View which can contain 0 or more children of a specific Pane or View type
 * and are ordered sequentially
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingList extends ScrollingView
{
    public static interface DataProvider
    {
        int getElementCount();

        void updateElement(int index, Pane elementPane);
    }

    //  Runtime
    protected DataProvider dataProvider;
    private PaneParams     listNodeParams;
    private int            listElementHeight = 0;

    public ScrollingList(){ super(); }

    public ScrollingList(ScrollingList other){ super(other); }

    /**
     * Constructs a ScrollingList from PaneParams
     *
     * @param params Params for the ScrollingList
     */
    public ScrollingList(PaneParams params)
    {
        super(params);
    }

    public void setDataProvider(DataProvider p)
    {
        dataProvider = p;
        refreshElementPanes();
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null) return;

        //  Get the PaneParams for this child, because we'll need it in the future
        //  to create more nodes
        listNodeParams = childNodes.get(0);
    }

    public int getListElementIndexByPane(Pane pane)
    {
        while (pane != null && pane.getParent() != this)
        {
            pane = pane.getParent();
        }

        if (pane == null)
        {
            return -1;
        }

        return getChildren().indexOf(pane);
    }

    /**
     * This is an optimized version that relies on the fixed size and order of children to quickly determine
     * the clicked child
     *
     * @param mx Mouse X, relative to the top-left of this Pane
     * @param my Mouse Y, relative to the top-left of this Pane
     * @return a Pane that will handle a click action
     */
    public Pane findPaneForClick(int mx, int my)
    {
        if (children.isEmpty() || listElementHeight == 0)
        {
            return null;
        }

        int listElement = my / listElementHeight;
        if (listElement < children.size())
        {
            Pane child = children.get(listElement);
            if (child.canHandleClick(mx, my))
            {
                return child;
            }
        }

        return null;
    }

    /**
     * Creates, deletes, and updates existing Panes for elements in the list based on the DataProvider.
     */
    public void refreshElementPanes()
    {
        int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        for (int i = 0; i < numElements; ++i)
        {
            Pane child = null;
            if (i < children.size())
            {
                child = children.get(i);
            }
            else
            {
                child = Loader.createFromPaneParams(listNodeParams, this);

                if (i == 0)
                {
                    listElementHeight = child.getHeight();
                }
            }

            child.setPosition(0, i * listElementHeight);
            dataProvider.updateElement(i, child);
        }

        while (children.size() > numElements)
        {
            removeChild(children.get(numElements));
        }

        computeContentHeight();
    }
}
