package com.blockout;

import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ListIterator;

public class ScrollView extends View
{
    int scrollY = 0;
    int listElementHeight = 0;
    int contentHeight = 0;

    public ScrollView()
    {
        super();
    }
    public ScrollView(ScrollView other) { super(other); }

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
    public int getMaxScrollY() { return contentHeight - getHeight(); }

    public int getListElementHeight() { return listElementHeight; }
    public void setListElementHeight(int elementHeight)
    {
        listElementHeight = elementHeight;
        computeContentHeight();
    }

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
    public void addChild(Pane child)
    {
        super.addChild(child);
        computeContentHeight();
    }

    @Override
    public void removeChild(Pane child)
    {
        int index = children.indexOf(child);

        super.removeChild(child);
        computeContentHeight();
    }

    @Override
    public void adjustChild(Pane child)
    {
        //  Children in a ScrollView don't exist in normal locations...
        child.setPosition(0, 0);
        child.setSize(getWidth() - 16, child.getHeight());
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        //  Translate the drawing origin to our x,y
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, 0);

        //  Translate Mouse into the View
        mx -= x;
        my -= y;

        FloatBuffer fb = BufferUtils.createFloatBuffer(16 * 4);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);

        int scale = Screen.getScale();
        int scissorsX = (int)fb.get(12) * scale;
        int scissorsY = (int)(fb.get(13) + getHeight()) * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorsX, mc.displayHeight - scissorsY, getWidth() * scale, getHeight() * scale);

        int scrollIndex = scrollY / listElementHeight;
        int offsetY = (scrollIndex * listElementHeight) - scrollY;
        GL11.glTranslatef(0, offsetY, 0);
        ListIterator<Pane> it = children.listIterator(scrollIndex);
        while (it.hasNext())
        {
            Pane child = it.next();

            child.draw(mx, my - offsetY/*+ scrollY*/);

            offsetY += listElementHeight;

            if (offsetY >= getHeight())
            {
                break;
            }

            GL11.glTranslatef(0, listElementHeight, 0);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GL11.glPopMatrix();
    }

    @Override
    public void click(int mx, int my)
    {
        int scrollIndex = scrollY / listElementHeight;
        int offsetY = (scrollIndex * listElementHeight) - scrollY;
        ListIterator<Pane> it = children.listIterator(scrollIndex);
        while (it.hasNext())
        {
            Pane child = it.next();

            if (child.canHandleClick(mx, my - offsetY))
            {
                child.click(mx - x, my - y - offsetY /*+ scrollY - y*/);
                return;
            }

            offsetY += listElementHeight;

            if (offsetY >= getHeight())
            {
                break;
            }
        }

        //  no super.click(), we do not want to handleClick() or anything like that
    }

    /**
     * We use this rather than findPaneForClick, because we don't want findPaneForClick to
     * actually handle clicks on the pane
     *
     * @param mx
     * @param my
     * @return
     */
    public Pane findListElementForClick(int mx, int my)
    {
        int clickY = (my - y) + scrollY;

        int y = 0;
        int scrollIndex = scrollY / listElementHeight;
        ListIterator<Pane> it = children.listIterator(scrollIndex);
        while (it.hasNext())
        {
            Pane child = it.next();

            if (child.canHandleClick(mx, my))
            {
                return child;
            }

            my -= listElementHeight;
            y += listElementHeight;

            if (y >= getHeight())
            {
                break;
            }
        }

        return null;
    }
}
