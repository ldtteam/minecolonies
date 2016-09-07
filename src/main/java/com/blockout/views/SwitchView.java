package com.blockout.views;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tabbed view.
 */
public class SwitchView extends View
{
    @Nullable
    private Pane currentView;

    /**
     * Required default constructor.
     */
    public SwitchView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams
     *
     * @param params Params for the Pane
     */
    public SwitchView(PaneParams params)
    {
        super(params);
    }

    @Override
    public void parseChildren(@NotNull PaneParams params)
    {
        super.parseChildren(params);

        String defaultView = params.getStringAttribute("default", null);
        if (defaultView != null)
        {
            setView(defaultView);
        }
    }

    public void setView(String name)
    {
        //  Immediate children only
        for (@NotNull Pane child : children)
        {
            if (child.getID().equals(name))
            {
                setCurrentView(child);
                return;
            }
        }
    }

    @Nullable
    @Override
    public Pane findPaneForClick(int mx, int my)
    {
        if (currentView != null && currentView.canHandleClick(mx, my))
        {
            return currentView;
        }

        return null;
    }

    @Override
    protected boolean childIsVisible(Pane child)
    {
        return child == currentView && super.childIsVisible(child);
    }

    @Override
    public void addChild(@NotNull Pane child)
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
    public void adjustChild(@NotNull Pane child)
    {
        if (child.getWidth() == 0 || child.getHeight() == 0)
        {
            child.setSize(width - child.getX(), height - child.getY());
        }

        super.adjustChild(child);
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

    @Nullable
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

    /**
     * Get the next tab/view.
     */
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

    /**
     * Get the last tab/view.
     */
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
}
