package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;

import java.util.ListIterator;

public class SwitchView extends View
{
    Pane currentView;

    public SwitchView() { super(); }

    public SwitchView(Group other) { super(other); }

    /**
     * Constructs a View from PaneParams
     *
     * @param params Params for the Pane
     */
    public SwitchView(PaneParams params)
    {
        super(params);
    }

    public void parseChildren(PaneParams params)
    {
        super.parseChildren(params);

        String defaultView = params.getStringAttribute("default", null);
        if (defaultView != null)
        {
            setView(defaultView);
        }
    }

    public Pane getCurrentView()
    {
        return currentView;
    }

    private void setCurrentView(Pane pane)
    {
        if (currentView != null)
        {
            currentView.setVisible(false);
        }
        currentView = pane;
        currentView.setVisible(true);
    }

    public void setView(String name)
    {
        //  Immediate children only
        for (Pane child : children)
        {
            if (child.getID().equals(name))
            {
                setCurrentView(child);
                return;
            }
        }
    }

    public void nextView()
    {
        if (children.isEmpty())
        {
            return;
        }

        int index = children.indexOf(currentView) + 1;
        if (index >= children.size())
        {
            index = 0;
        }

        setCurrentView(children.get(index));
    }

    public void previousView()
    {
        if (children.isEmpty())
        {
            return;
        }

        int index = children.indexOf(currentView) - 1;
        if (index < 0)
        {
            index = children.size();
        }

        setCurrentView(children.get(index));
    }

    @Override
    public void adjustChild(Pane child)
    {
        child.setPosition(0, 0);
        child.setSize(width, height);
    }

    @Override
    public void addChild(Pane child)
    {
        super.addChild(child);
        if (children.size() == 1)
        {
            currentView = child;
            child.setVisible(true);
        }
        else
        {
            child.setVisible(false);
        }
    }

    @Override
    public void removeChild(Pane child)
    {
        super.removeChild(child);
        if (child == currentView)
        {
            if (children.isEmpty())
            {
                currentView = null;
            }
            else
            {
                currentView = children.get(0);
                currentView.setVisible(true);
            }
        }
    }

    @Override
    protected boolean childIsVisible(Pane child)
    {
        return child == currentView && super.childIsVisible(child);
    }

    @Override
    public Pane findPaneForClick(int mx, int my)
    {
        if (currentView != null && currentView.canHandleClick(mx, my))
        {
            return currentView;
        }

        return null;
    }
}
