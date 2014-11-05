package com.blockout.views;

import com.blockout.*;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ScrollingListView extends View
{
    private int scrollY           = 0;
    private int listElementHeight = 0;
    private int contentHeight     = 0;
    private PaneParams listNodeParams;
    private final ScrollingList list;

    public ScrollingListView(ScrollingList list)
    {
        super();
        this.list = list;
    }

    public int getScrollY(){ return scrollY; }

    public void setScrollY(int offset)
    {
        scrollY = offset;

        int maxScrollY = getMaxScrollY();
        if (scrollY > maxScrollY)
        {
            scrollY = maxScrollY;
        }

        if (scrollY < 0)
        {
            scrollY = 0;
        }
    }

    public int getContentHeight() { return contentHeight; }
    public int getMaxScrollY() { return contentHeight - getHeight(); }
    public int getListElementHeight() { return listElementHeight; }
//    public void setListElementHeight(int elementHeight)
//    {
//        listElementHeight = elementHeight;
//        computeContentHeight();
//    }

    public void setListNodeParams(PaneParams params)
    {
        listNodeParams = params;

        while (children.size() > 0)
        {
            removeChild(children.get(0));
        }
    }

    public int getVisibleListElementCount() { return getHeight() / listElementHeight; }

    private void computeContentHeight()
    {
        contentHeight = children.size() * listElementHeight;
        setScrollY(scrollY);
    }

    public void scrollBy(int delta)
    {
        setScrollY(scrollY + delta);
    }

    public void scrollToElement(int index)
    {
        setScrollY(MathHelper.clamp_int(index, 0, children.size()) * listElementHeight);
    }

    public void scrollByElementCount(int delta)
    {
        scrollBy(delta * listElementHeight);
    }

    public void scrollByPages(int delta)
    {
        scrollBy(delta * getHeight());
    }

    @Override
    public void adjustChild(Pane child)
    {
        //  Temporarily set child position to 0,0, force children to 100% interior width
        child.setPosition(0, 0);
        child.setSize(getInteriorWidth(), child.getHeight());
    }

    @Override
    protected boolean childIsVisible(Pane child)
    {
        return child.getX() < getWidth() &&
               child.getY() < getHeight() + scrollY &&
              (child.getX() + child.getWidth()) >= 0 &&
              (child.getY() + child.getHeight()) >= scrollY;
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        scissorsStart();

        //  Translate the scroll
        GL11.glPushMatrix();
        GL11.glTranslatef(0, -scrollY, 0);
        super.drawSelf(mx, my + scrollY);
        GL11.glPopMatrix();

        scissorsEnd();
    }

    @Override
    public void click(int mx, int my)
    {
        super.click(mx, my + scrollY);
    }

    /**
     * This is an optimized version that relies on the fixed size and predefined position of children
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
     *
     * @param dataProvider
     */
    protected void refreshElementPanes(ScrollingList.DataProvider dataProvider)
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
