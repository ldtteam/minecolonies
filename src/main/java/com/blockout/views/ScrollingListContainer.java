package com.blockout.views;

import com.blockout.Loader;
import com.blockout.Pane;
import com.blockout.PaneParams;

/**
 * A Blockout pane that contains a scrolling line of other panes.
 */
public class ScrollingListContainer extends ScrollingContainer
{
    private int listElementHeight = 0;

    ScrollingListContainer(ScrollingList owner)
    {
        super(owner);
    }

    /**
     * Creates, deletes, and updates existing Panes for elements in the list based on the DataProvider.
     *
     * @param dataProvider   data provider object, shouldn't be null.
     * @param listNodeParams the xml parameters for this pane.
     */
    public void refreshElementPanes(ScrollingList.DataProvider dataProvider, PaneParams listNodeParams)
    {
        if (dataProvider != null)
        {
            for (int i = 0; i < dataProvider.getElementCount(); ++i)
            {
                Pane child;
                if (i < children.size())
                {
                    child = children.get(i);
                }
                else
                {
                    child = Loader.createFromPaneParams(listNodeParams, this);
                    if (child == null)
                    {
                        continue;
                    }

                    if (i == 0)
                    {
                        listElementHeight = child.getHeight();
                    }
                }
                child.setPosition(0, i * listElementHeight);

                dataProvider.updateElement(i, child);
            }
        }

        int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        while (children.size() > numElements)
        {
            removeChild(children.get(numElements));
        }

        computeContentHeight();
    }

    /**
     * Returns the element list index for the given pane.
     *
     * @param pane the pane to find the index of.
     * @return the index.
     */
    public int getListElementIndexByPane(Pane pane)
    {
        Pane parentPane = pane;
        while (parentPane != null && parentPane.getParent() != this)
        {
            parentPane = parentPane.getParent();
        }

        if (parentPane == null)
        {
            return -1;
        }

        return getChildren().indexOf(parentPane);
    }

    /**
     * This is an optimized version that relies on the fixed size and order of children to quickly determine
     *
     * @param mx Mouse X, relative to the top-left of this Pane
     * @param my Mouse Y, relative to the top-left of this Pane
     * @return a Pane that will handle a click action
     */
    @Override
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
