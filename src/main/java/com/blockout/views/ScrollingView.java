package com.blockout.views;

import com.blockout.Pane;
import com.blockout.Screen;
import com.blockout.View;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class ScrollingView extends View
{
    int scrollX = 0;
    int scrollY = 0;
    int contentWidth = 0;
    int contentHeight = 0;

    public ScrollingView()
    {
        super();
    }
    public ScrollingView(ScrollingView other) { super(other); }

    public int getScrollX() { return scrollX; }
    public int getScrollY() { return scrollY; }
    public void setScroll(int newScrollX, int newScrollY)
    {
        scrollX = MathHelper.clamp_int(newScrollX, 0, getMaxScrollX());
        scrollY = MathHelper.clamp_int(newScrollY, 0, getMaxScrollY());
    }

    public int getContentWidth() { return contentWidth; }
    public int getMaxScrollX() { return Math.max(0, contentWidth - getInteriorHeight()); }

    public int getContentHeight() { return contentHeight; }
    public int getMaxScrollY() { return Math.max(0, contentHeight - getInteriorHeight()); }

    private void computeContentHeight()
    {
        contentWidth = 0;
        contentHeight = 0;

        for (Pane child : children)
        {
            contentWidth = Math.max(contentWidth, child.getX() + child.getWidth());
            contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
        }

        //  Recompute scroll
        setScroll(scrollX, scrollY);
    }

    public void scrollBy(int deltaX, int deltaY)
    {
        setScroll(scrollX + deltaX, scrollY + deltaY);
    }

    public void scrollByPages(int deltaX, int deltaY)
    {
        scrollBy(deltaX * getInteriorHeight(), deltaY * getInteriorHeight());
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
    protected void drawSelf(int mx, int my)
    {
        //  Translate the drawing origin to our x,y
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x + padding, (float)y - padding, 0);

        //  Translate Mouse into the View
        mx -= x;
        my -= y;

        //  TODO - Scissor Stack
        FloatBuffer fb = BufferUtils.createFloatBuffer(16 * 4);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);

        int scale = Screen.getScale();
        int scissorsX = (int)fb.get(12) * scale;
        int scissorsY = (int)(fb.get(13) + getHeight()) * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorsX, mc.displayHeight - scissorsY, getInteriorWidth() * scale, getInteriorHeight() * scale);

        //  Translate to parent view...
        GL11.glPushMatrix();
        GL11.glTranslatef(-scrollX, -scrollY, 0);
        for (Pane child : children)
        {
            child.draw(mx, my);
        }
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //  TODO: Draw the scroll bars

        GL11.glPopMatrix();
    }

    @Override
    public void click(int mx, int my)
    {
        //  Offset click by the scroll amounts; we'll adjust it back on clickSelf
        super.click(mx + scrollX, my + scrollY);
    }

    @Override
    public void handleClick(int mx, int my)
    {
        //  Adjust the scroll amounts back
        mx -= scrollX;
        my -= scrollY;

        //  TODO - handle scrollbar click
    }
}
