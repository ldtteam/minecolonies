package com.minecolonies.blockout.controls;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import com.minecolonies.blockout.views.ScrollingContainer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

/**
 * Class handling scrollbars in our GUIs.
 */
public class Scrollbar extends Pane
{
    /**
     * Max height of the scrollbar.
     */
    private static final int MAXIMUM_HEIGHT = 20;

    /**
     * Background of the scrollbar.
     */
    protected int scrollbarBackground = 0xFF000000;

    /**
     * Color of the scrollbar.
     */
    protected int scrollbarColor = 0xFFC0C0C0;

    /**
     * Color of the scrollbar when hovered.
     */
    protected int scrollbarColorHighlight = 0xFF808080;

    /**
     * Container containing the scrollbar.
     */
    protected ScrollingContainer container;

    /**
     * The height the bar is clicked.
     */
    protected int barClickY = 0;

    /**
     * True if the bar is clicked at the moment.
     */
    protected boolean barClicked = false;

    /**
     * Offsets
     */
    protected int offsetX = 0;
    protected int offsetY = 0;

    /**
     * Instantiates the scrollbar with certain parameters.
     *
     * @param container the container of the scrollbar.
     * @param params    the parameters.
     */
    public Scrollbar(final ScrollingContainer container, final PaneParams params)
    {
        this(container);
        //  TODO: Parse Scrollbar-specific Params
        
        final PaneParams.SizePair size = params.getSizePairAttribute("scrollbarOffset", null, null);
        if (size != null)
        {
            offsetX = size.getX();
            offsetY = size.getY();
        }
    }

    /**
     * Instantiates a simple scrollbar.
     *
     * @param container the container of the scrollbar.
     */
    public Scrollbar(final ScrollingContainer container)
    {
        super();
        this.container = container;
    }

    /**
     * Called when the scrollbar has been clicked.
     *
     * @param my the y it is clicked on.
     */
    public void dragScroll(final int my)
    {
        if(container.getContentHeight() == 0)
        {
            return;
        }

        final int barClickYNow = getScrollBarYPos() + barClickY;
        final int deltaFromClickPos = my - barClickYNow;

        if (deltaFromClickPos == 0)
        {
            return;
        }

        final int scaledY = deltaFromClickPos * container.getMaxScrollY() / getHeight();
        container.scrollBy(scaledY);

        if (container.getScrollY() == 0 || container.getScrollY() == container.getMaxScrollY())
        {
            barClickY = MathHelper.clamp(my - getScrollBarYPos(), 0, getBarHeight() - 1);
        }
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        barClicked = barClicked && Mouse.isButtonDown(0);
        if (barClicked)
        {
            //  Current relative position of the click position on the bar
            dragScroll(my - y);
        }

        if (getContentHeightDiff() <= 0)
        {
            return;
        }

        final int scrollBarBackX1 = x + offsetX;
        final int scrollBarBackX2 = scrollBarBackX1 + (getWidth() - 2);

        //  Scroll Area Back
        drawGradientRect(scrollBarBackX2, y + getHeight() + offsetY, scrollBarBackX1, y + offsetY,
          scrollbarBackground, scrollbarBackground);

        final int scrollBarStartY = y + getScrollBarYPos();
        final int scrollBarEndY = scrollBarStartY + getBarHeight();

        //  Scroll Bar (Bottom/Right Edge line) - Fill whole Scroll area
        drawGradientRect(scrollBarBackX2, scrollBarEndY, scrollBarBackX1, scrollBarStartY,
          scrollbarColorHighlight, scrollbarColorHighlight);

        //  Scroll Bar (Inset color)
        drawGradientRect(scrollBarBackX2 - 1, scrollBarEndY - 1, scrollBarBackX1, scrollBarStartY,
          scrollbarColor, scrollbarColor);
    }

    @Override
    public void handleClick(final int mx, final int my)
    {
        if (getContentHeightDiff() <= 0)
        {
            return;
        }

        final int barHeight = getBarHeight();

        final int scrollBarStartY = getScrollBarYPos();
        final int scrollBarEndY = scrollBarStartY + barHeight;

        if (my < scrollBarStartY)
        {
            container.scrollBy(-container.getScrollPageSize());
        }
        else if (my > scrollBarEndY)
        {
            container.scrollBy(container.getScrollPageSize());
        }
        else
        {
            barClickY = my - scrollBarStartY;
            barClicked = true;
        }
    }

    private int getContentHeightDiff()
    {
        return container.getContentHeight() - getHeight();
    }

    private int getBarHeight()
    {
        return Math.max(Math.min(MAXIMUM_HEIGHT, getHeight() / 2), (getHeight() * getHeight()) / container.getContentHeight());
    }

    private int getScrollBarYPos()
    {
        return container.getScrollY() * (getHeight() - getBarHeight()) / getContentHeightDiff();
    }

    public int getScrollOffsetX()
    {
        return offsetX;
    }
}
