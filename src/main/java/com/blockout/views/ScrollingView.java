package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import com.blockout.controls.Scrollbar;
import org.jetbrains.annotations.NotNull;

/**
 * Basic scrolling view.
 */
public class ScrollingView extends View
{
    private static final int DEFAULT_SCROLLBAR_WIDTH = 8;

    //  Params
    protected int scrollbarWidth = DEFAULT_SCROLLBAR_WIDTH;

    //  Runtime
    protected ScrollingContainer container;
    protected Scrollbar          scrollbar;

    /**
     * Required defualt constructor.
     */
    public ScrollingView()
    {
        super();
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

    @NotNull
    protected ScrollingContainer createScrollingContainer()
    {
        return new ScrollingContainer(this);
    }

    /**
     * Load from xml.
     *
     * @param params xml parameters.
     */
    public ScrollingView(PaneParams params)
    {
        super(params);
        setup();
    }

    public ScrollingContainer getContainer()
    {
        return container;
    }

    /**
     * Redirect all predefined children into our container
     *
     * @param params the xml parameters.
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

    public int getScrollY()
    {
        return container.getScrollY();
    }

    public void setScrollY(int offset)
    {
        container.setScrollY(offset);
    }
}
