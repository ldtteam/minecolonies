package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import com.blockout.controls.Scrollbar;

public class ScrollingView extends View
{
    //  Params
    protected int scrollbarWidth          = 8;

    //  Runtime
    protected ScrollingContainer container;
    protected Scrollbar          scrollbar;

    public ScrollingView()
    {
        super();
        setup();
    }

    public ScrollingView(PaneParams params)
    {
        super(params);
        setup();
    }

    private void setup()
    {
        container = createScrollingContainer();
        container.setPosition(0, 0);
        container.setSize(getInteriorWidth() - scrollbarWidth, getInteriorHeight());
        container.putInside(this);

        scrollbar = new Scrollbar(container);
        scrollbar.setPosition(getInteriorWidth() - scrollbarWidth, 0);
        scrollbar.setSize(scrollbarWidth, getInteriorHeight());
        scrollbar.putInside(this);
    }

    protected ScrollingContainer createScrollingContainer()
    {
        return new ScrollingContainer(this);
    }

    public ScrollingContainer getContainer()
    {
        return container;
    }

    /**
     * Redirect all predefined children into our container
     *
     * @param params
     */
    @Override
    public void parseChildren(PaneParams params)
    {
        container.parseChildren(params);
    }

    /**
     * Optimized version of childIsVisible, because we only have two immediate children, which are guaranteed
     * to be visible: the ScrollingContainer and the Scrollbar
     */
    @Override
    protected boolean childIsVisible(Pane child)
    {
        return true;
    }

    public int getScrollY() { return container.getScrollY(); }
    public void setScrollY(int offset) { container.setScrollY(offset); }
}
