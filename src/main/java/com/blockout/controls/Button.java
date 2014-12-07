package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;

public class Button extends Pane
{
    public interface Handler {
        void onButtonClicked(Button button);
    }

    protected Handler handler;

    public Button() {}
    public Button(PaneParams params)
    {
        super(params);
    }

    public void setHandler(Handler h)
    {
        handler = h;
    }

    @Override
    public void handleClick(int mx, int my)
    {
        Handler delegatedHandler = handler;

        if (delegatedHandler == null)
        {
            //  If we do not have a designated handler, find the closest ancestor that is a Handler
            for (Pane p = parent; p != null; p = p.getParent())
            {
                if (p instanceof Handler)
                {
                    delegatedHandler = (Handler)p;
                    break;
                }
            }
        }

        if (delegatedHandler != null)
        {
            delegatedHandler.onButtonClicked(this);
        }
    }
}
