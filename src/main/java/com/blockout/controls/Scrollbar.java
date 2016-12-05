package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.views.ScrollingContainer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class Scrollbar extends Pane
{
    //  Params
    protected int scrollbarBackground     = 0xFF000000;
    protected int scrollbarColor          = 0xFFC0C0C0;
    protected int scrollbarColorHighlight = 0xFF808080;

    protected ScrollingContainer container;
    protected int     barClickY  = 0;
    protected boolean barClicked = false;

    public Scrollbar(final ScrollingContainer container, final PaneParams params)
    {
        this(container);
        //  TODO: Parse Scrollbar-specific Params
    }

    public Scrollbar(final ScrollingContainer container)
    {
        super();
        this.container = container;
    }

    public void dragScroll(final int my)
    {
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
            barClickY = MathHelper.clamp_int(my - getScrollBarYPos(), 0, getBarHeight() - 1);
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

        final int scrollBarBackX1 = x;
        final int scrollBarBackX2 = scrollBarBackX1 + (getWidth() - 2);

        //  Scroll Area Back
        drawGradientRect(scrollBarBackX2, y + getHeight(), scrollBarBackX1, y,
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
        return Math.max(Math.min(20, getHeight() / 2), (getHeight() * getHeight()) / container.getContentHeight());
    }

    private int getScrollBarYPos()
    {
        return container.getScrollY() * (getHeight() - getBarHeight()) / getContentHeightDiff();
    }
}
