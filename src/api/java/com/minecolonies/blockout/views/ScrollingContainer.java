package com.minecolonies.blockout.views;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;

/**
 * Basic scrollable pane.
 */
public class ScrollingContainer extends View
{
    private static final int PERCENT_90   = 90;
    private static final int PERCENT_FULL = 100;

    protected ScrollingView owner;
    protected int scrollY       = 0;
    protected int contentHeight = 0;

    ScrollingContainer(final ScrollingView owner)
    {
        super();
        this.owner = owner;
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        super.parseChildren(params);
        computeContentHeight();
    }

    /**
     * Compute the height in pixels of the container.
     */
    public void computeContentHeight()
    {
        contentHeight = 0;

        for (@NotNull final Pane child : children)
        {
            if (child != null)
            {
                contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
            }
        }

        //  Recompute scroll
        setScrollY(scrollY);
    }

    /**
     * Compute the height in pixels of the container.
     */
    public void setContentHeight(final int size)
    {
        contentHeight = size;
        // Recompute scroll
        setScrollY(scrollY);
    }

    public int getMaxScrollY()
    {
        return Math.max(0, contentHeight - getHeight());
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        scissorsStart();

        //  Translate the scroll
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollY, 0);
        super.drawSelf(mx, my + scrollY);
        GlStateManager.popMatrix();

        scissorsEnd();
    }

    @Override
    public void click(final int mx, final int my)
    {
        //  Offset click by the scroll amounts; we'll adjust it back on clickSelf
        super.click(mx, my + scrollY);
    }

    @Override
    protected boolean childIsVisible(@NotNull final Pane child)
    {
        return child.getX() < getWidth()
                 && child.getY() < getHeight() + scrollY
                 && (child.getX() + child.getWidth()) >= 0
                 && (child.getY() + child.getHeight()) >= scrollY;
    }

    public int getScrollY()
    {
        return scrollY;
    }

    public void setScrollY(final int offset)
    {
        scrollY = offset;

        final int maxScrollY = getMaxScrollY();
        if (scrollY > maxScrollY)
        {
            scrollY = maxScrollY;
        }

        if (scrollY < 0)
        {
            scrollY = 0;
        }
    }

    public int getContentHeight()
    {
        return contentHeight;
    }

    public int getScrollPageSize()
    {
        return getHeight() * PERCENT_90 / PERCENT_FULL;
    }

    /**
     * Scroll down a certain amount of pixels.
     *
     * @param deltaY number of pixels to scroll.
     */
    public void scrollBy(final int deltaY)
    {
        setScrollY(scrollY + deltaY);
    }
}
