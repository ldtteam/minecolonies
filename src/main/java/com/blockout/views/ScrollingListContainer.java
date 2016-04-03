package com.blockout.views;

import com.blockout.Loader;
import com.blockout.Pane;
import com.blockout.PaneParams;

public class ScrollingListContainer extends ScrollingContainer
{
    private int            listElementHeight = 0;

    ScrollingListContainer(ScrollingList owner)
    {
        super(owner);
    }

    /**
     * Creates, deletes, and updates existing Panes for elements in the list based on the DataProvider.
     */
    public void refreshElementPanes(ScrollingList.DataProvider dataProvider, PaneParams listNodeParams)
    {
        int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        for (int i = 0; i < numElements; ++i)
        {
            Pane child;
            if (i < children.size())
            {
                child = children.get(i);
            }
            else
            {
                child = Loader.createFromPaneParams(listNodeParams, this);

                if (i == 0)
                {
                    //TODO: May be null
                    listElementHeight = child.getHeight();
                }
            }
            //TODO: May be null
            child.setPosition(0, i * listElementHeight);

            dataProvider.updateElement(i, child);
        }

        while (children.size() > numElements)
        {
            removeChild(children.get(numElements));
        }

        computeContentHeight();
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
     *
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
}
