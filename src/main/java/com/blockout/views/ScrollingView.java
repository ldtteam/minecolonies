package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ScrollingView extends View
{
    //  Params
    protected int scrollbarWidth          = 8;
    protected int scrollbarBackground     = 0xFF000000;
    protected int scrollbarColor          = 0xFFC0C0C0;
    protected int scrollbarColorHighlight = 0xFF808080;

    //  Runtime
    protected int     barClickY     = 0;
    protected boolean barClicked    = false;
    protected int     scrollY       = 0;
    protected int     contentHeight = 0;

    public ScrollingView()
    {
        super();
    }

    public ScrollingView(ScrollingView other){ super(other); }

    public ScrollingView(PaneParams params)
    {
        super(params);
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        super.parseChildren(params);
        computeContentHeight();
    }

    @Override
    public int getInteriorWidth() { return width - (padding * 2) - getScrollbarWidth(); }

    public int getScrollbarWidth() { return scrollbarWidth; }

    public int getScrollY() { return scrollY; }
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
    public int getMaxScrollY() { return Math.max(0, contentHeight - getInteriorHeight()); }
    public int getScrollPageSize() { return getInteriorHeight() * 90 / 100; }

    public void computeContentHeight()
    {
        contentHeight = 0;

        for (Pane child : children)
        {
            contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
        }

        //  Recompute scroll
        setScrollY(scrollY);
    }

    public void scrollBy(int deltaY)
    {
        setScrollY(scrollY + deltaY);
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
    public void drawSelf(int mx, int my)
    {
        scissorsStart();

        //  Translate the scroll
        GL11.glPushMatrix();
        GL11.glTranslatef(0, -scrollY, 0);
        super.drawSelf(mx, my + scrollY);
        GL11.glPopMatrix();

        scissorsEnd();

        barClicked = barClicked && Mouse.isButtonDown(0);
        if (barClicked)
        {
            //  Current relative position of the click position on the bar
            dragScrollY(my - y);
        }

        drawVerticalScrollbar();
    }

    public void drawVerticalScrollbar()
    {
        if (getContentHeight() < getInteriorHeight())
        {
            return;
        }

        int scrollBarBackX1 = x + getInteriorWidth();
        int scrollBarBackX2 = scrollBarBackX1 + (getScrollbarWidth() - 2);

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
        my -= scrollY;
        if (mx >= getWidth() - getScrollbarWidth())
        {
            int barHeight = getBarHeight();

            int scrollBarStartY = getScrollBarYPos();
            int scrollBarEndY = scrollBarStartY + barHeight;

            if (my < scrollBarStartY)
            {
                scrollBy(-getScrollPageSize());
            }
            else if (my > scrollBarEndY)
            {
                scrollBy(getScrollPageSize());
            }
            else
            {
                barClickY = my - scrollBarStartY;
                barClicked = true;
            }
        }
    }

    @Override
    public void click(int mx, int my)
    {
        //  Offset click by the scroll amounts; we'll adjust it back on clickSelf
        super.click(mx, my + scrollY);
    }

    public void dragScrollY(int my)
    {
        int barClickYNow = getScrollBarYPos() + barClickY;
        int deltaFromClickPos = my - barClickYNow;

        if (deltaFromClickPos == 0)
        {
            return;
        }

        int scaledY = deltaFromClickPos * getMaxScrollY() / getHeight();
        scrollBy(scaledY);

        if (getScrollY() == 0 || getScrollY() == getMaxScrollY())
        {
            barClickY = MathHelper.clamp_int(my - getScrollBarYPos(), 0, getBarHeight() - 1);
        }
    }

    private int getContentHeightDiff() { return getContentHeight() - getHeight(); }
    private int getBarHeight() { return Math.max(Math.min(20, getHeight() / 2), (getHeight() * getHeight()) / getContentHeight()); }
    private int getScrollBarYPos() { return getScrollY() * (getHeight() - getBarHeight()) / getContentHeightDiff(); }
}
