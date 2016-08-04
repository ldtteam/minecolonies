package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.views.ScrollingContainer;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

public class Scrollbar extends Pane
{
    //  Params
    protected int scrollbarBackground = 0xFF000000;
    protected int scrollbarColor = 0xFFC0C0C0;
    protected int scrollbarColorHighlight = 0xFF808080;

    protected ScrollingContainer container;
    protected int barClickY = 0;
    protected boolean barClicked = false;

    public Scrollbar(ScrollingContainer container)
    {
        this.container = container;
    }

    public Scrollbar(ScrollingContainer container, PaneParams params)
    {
        this(container);
        //  TODO: Parse Scrollbar-specific Params
    }

    public void dragScroll(int my)
    {
        int barClickYNow = getScrollBarYPos() + barClickY;
        int deltaFromClickPos = my - barClickYNow;

        if (deltaFromClickPos == 0)
        {
            return;
        }

        int scaledY = deltaFromClickPos * container.getMaxScrollY() / getHeight();
        container.scrollBy(scaledY);

        if (container.getScrollY() == 0 || container.getScrollY() == container.getMaxScrollY())
        {
            barClickY = MathHelper.clamp_int(my - getScrollBarYPos(), 0, getBarHeight() - 1);
        }
    }

    @Override
    public void drawSelf(int mx, int my)
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

        int scrollBarBackX1 = x;
        int scrollBarBackX2 = scrollBarBackX1 + (getWidth() - 2);

        //  Scroll Area Back
        drawGradientRect(scrollBarBackX2, y + getHeight(), scrollBarBackX1, y,
                scrollbarBackground, scrollbarBackground);

        int scrollBarStartY = y + getScrollBarYPos();
        int scrollBarEndY = scrollBarStartY + getBarHeight();

        //  Scroll Bar (Bottom/Right Edge line) - Fill whole Scroll area
        drawGradientRect(scrollBarBackX2, scrollBarEndY, scrollBarBackX1, scrollBarStartY,
                scrollbarColorHighlight, scrollbarColorHighlight);

        //  Scroll Bar (Inset color)
        drawGradientRect(scrollBarBackX2 - 1, scrollBarEndY - 1, scrollBarBackX1, scrollBarStartY,
                scrollbarColor, scrollbarColor);
    }

    @Override
    public void handleClick(int mx, int my)
    {
        if (getContentHeightDiff() <= 0)
        {
            return;
        }

        int barHeight = getBarHeight();

        int scrollBarStartY = getScrollBarYPos();
        int scrollBarEndY = scrollBarStartY + barHeight;

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
