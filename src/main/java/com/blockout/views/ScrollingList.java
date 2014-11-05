package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.Screen;
import com.blockout.View;
import com.minecolonies.MineColonies;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

/*
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingList extends View
{
    public static interface DataProvider
    {
        int getElementCount();

        void updateElement(int index, Pane elementPane);
    }

    //  Params
    protected int scrollbarWidth          = 8;
    protected int scrollbarBackground     = 0xFF000000;
    protected int scrollbarColor          = 0xFFC0C0C0;
    protected int scrollbarColorHighlight = 0xFF808080;

    //  Runtime
    protected ScrollingListView scrollView;
    protected DataProvider      dataProvider;
    protected int               barClickY = 0;
    protected boolean           barClicked = false;

    public ScrollingList(){ super(); }

    public ScrollingList(ScrollingList other){ super(other); }

    /**
     * Constructs a ScrollingList from PaneParams
     *
     * @param params Params for the ScrollingList
     */
    public ScrollingList(PaneParams params)
    {
        super(params);
    }

    public void setDataProvider(DataProvider p)
    {
        dataProvider = p;
        scrollView.refreshElementPanes(dataProvider);
    }

    public void refreshElementPanes()
    {
        scrollView.refreshElementPanes(dataProvider);
    }

    /**
     * Override to change scroll bar width
     */
    public int getScrollbarWidth() { return scrollbarWidth; }

    @Override
    public void parseChildren(PaneParams params)
    {
        scrollView = new ScrollingListView(this);
        scrollView.setSize(getWidth() - getScrollbarWidth(), getHeight());
        scrollView.putInside(this);

        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null) return;

        //  Get the PaneParams for this child, because we'll need it in the future
        //  to create more nodes
        scrollView.setListNodeParams(childNodes.get(0));
    }

    @Override
    public void drawSelf(int mx, int my)
    {
        super.drawSelf(mx, my);

        barClicked = barClicked && Mouse.isButtonDown(0);
        if (barClicked)
        {
            //  Current relative position of the click position on the bar
            dragScroll(my - y);
        }

        drawScrollbar(mx, my);
    }

    public void drawScrollbar(int mx, int my)
    {
        if (scrollView.getContentHeight() < getHeight())
        {
            return;
        }

        //  Draw scroll bar
        int scrollBarBackX1 = x + scrollView.getWidth();
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
        if (mx >= scrollView.getWidth())
        {
            int barHeight = getBarHeight();

            int scrollBarStartY = getScrollBarYPos();
            int scrollBarEndY = scrollBarStartY + barHeight;

            if (my < scrollBarStartY)
            {
                scrollView.scrollBy(-(getHeight() - scrollView.getListElementHeight()));
            }
            else if (my > scrollBarEndY)
            {
                scrollView.scrollBy(getHeight() - scrollView.getListElementHeight());
            }
            else
            {
                barClickY = my - scrollBarStartY;
                barClicked = true;
            }
        }
    }

    public void dragScroll(int my)
    {
        int barClickYNow = getScrollBarYPos() + barClickY;
        int deltaFromClickPos = my - barClickYNow;

        if (deltaFromClickPos == 0)
        {
            return;
        }

        int scaledY = deltaFromClickPos * scrollView.getMaxScrollY() / getHeight();
        scrollView.scrollBy(scaledY);

        if (scrollView.getScrollY() == 0 || scrollView.getScrollY() == scrollView.getMaxScrollY())
        {
            barClickY = MathHelper.clamp_int(my - getScrollBarYPos(), 0, getBarHeight() - 1);
        }
    }

    public int getListElementIndexByPane(Pane pane)
    {
        while (pane != null && pane.getParent() != scrollView)
        {
            pane = pane.getParent();
        }

        if (pane == null)
        {
            return -1;
        }

        return scrollView.getChildren().indexOf(pane);
    }

    private int getContentHeightDiff() { return scrollView.getContentHeight() - getHeight(); }
    private int getBarHeight() { return Math.max(Math.min(20, getHeight() / 2), (getHeight() * getHeight()) / scrollView.getContentHeight()); }
    private int getScrollBarYPos() { return scrollView.getScrollY() * (getHeight() - getBarHeight()) / getContentHeightDiff(); }
}
