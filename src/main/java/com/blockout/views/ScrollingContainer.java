package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import org.lwjgl.opengl.GL11;

public class ScrollingContainer extends View
{
    protected ScrollingView owner;
    protected int           scrollY       = 0;
    protected int           contentHeight = 0;

    ScrollingContainer(ScrollingView owner)
    {
        this.owner = owner;
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        super.parseChildren(params);
        computeContentHeight();
    }

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
    public int getMaxScrollY() { return Math.max(0, contentHeight - getHeight()); }
    public int getScrollPageSize() { return getHeight() * 90 / 100; }

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
    }

    @Override
    public void click(int mx, int my)
    {
        //  Offset click by the scroll amounts; we'll adjust it back on clickSelf
        super.click(mx, my + scrollY);
    }
}
