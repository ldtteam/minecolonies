package com.blockout;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/*
 * A View is a Pane which can contain other Panes
 */
public class View extends Pane
{
    List<Pane> children = new ArrayList<Pane>();

    View() { super(); }
    View(View other) { super(other); }
    View(PaneInfo info, View parent) { super(info, parent); }
    View(PaneInfo info) { super(info); }

    public void expandChild(Pane child)
    {
        int childWidth = child.getWidth(),
            childHeight = child.getHeight();
        int childX = child.getX(),
            childY = child.getY();

        if (childWidth < 0)
        {
            childX = 0;
            childWidth = width;
        }

        if (childHeight < 0)
        {
            childY = 0;
            childHeight = height;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
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

        for (Pane child : children)
        {
            child.draw(mx, my);
        }

        GL11.glPopMatrix();
    }

    @Override
    public Pane findPaneByID(String id)
    {
        if (this.id.equals(id))
        {
            return this;
        }
        else
        {
            for (Pane child : children)
            {
                Pane found = child.findPaneByID(id);
                if (found != null)
                {
                    return found;
                }
            }
        }

        return null;
    }

    @Override
    public Pane findPaneByCoord(int mx, int my)
    {
        if (!isClickable() || super.findPaneByCoord(mx, my) == null)
        {
            return null;
        }

        //  Adjust coordinates to new origin
        mx -= x;
        my -= y;

        for (Pane child : children)
        {
            if (child.isClickable())
            {
                Pane found = child.findPaneByCoord(mx, my);
                if (found != null)
                {
                    return found;
                }
            }
        }

        return null;
    }


    public void addChild(Pane child)
    {
        children.add(child);
    }

    public void removeChild(Pane child)
    {
        children.remove(child);
    }

    public void removeAllChildren() { children.clear(); }

    //  Mouse
//    @Override
//    public boolean isClickable() { return false; }

//    @Override
//    public boolean onMouseClicked(int mx, int my)
//    {
//        for (Pane child : children)
//        {
//            if (child.isPointInPane(mx, my) && child.isClickable())
//            {
//                if (child.onMouseClicked(mx - child.getX(), my - child.getY()))
//                {
//                    child.setFocus();
//                    return true;
//                }
//                return false;
//            }
//        }
//
//        return false;
//    }

    public void onUpdate()
    {
        for (Pane child : children)
        {
            child.onUpdate();
        }
    }

}
