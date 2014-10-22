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

    public View() { super(); }
    public View(View other) { super(other); }

    /**
     * Constructs a View from XML, and place it into the given Parent
     *
     * @param xml XML Node for the Pane
     */
    public View(XMLNode xml)
    {
        super(xml);
        //  TODO - Any attributes of our own?
    }

    public void parseChildren(XMLNode xml)
    {
        List<XMLNode> childNodes = xml.getChildren();
        if (childNodes == null) return;

        for (XMLNode node : childNodes)
        {
            Loader.createFromXML(node, this);
        }
    }

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
    protected void setWindow(Window w)
    {
        super.setWindow(w);
        for (Pane child : children)
        {
            child.setWindow(w);
        }
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

    public <T extends Pane> T findPaneOfTypeByID(String id, Class<T> type)
    {
        Pane p = findPaneByID(id);
        return type.isInstance(p) ? type.cast(p) : null;
    }

    @Override
    public Pane findPaneForClick(int mx, int my)
    {
        if (!isClickable() || super.findPaneForClick(mx, my) == null)
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
                Pane found = child.findPaneForClick(mx, my);
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
