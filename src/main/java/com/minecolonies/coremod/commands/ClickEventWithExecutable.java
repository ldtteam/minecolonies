package com.minecolonies.coremod.commands;

import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Small utility class to run an executable on a chat click event.
 */
public class ClickEventWithExecutable extends ClickEvent
{
    /**
     * The actions to run
     */
    private Runnable[] actions;

    public ClickEventWithExecutable(final ClickEvent.Action action, final String actionString, @NotNull final Runnable... actions)
    {
        super(action, actionString);
        this.actions = actions;
    }

    /**
     * Triggered when the chat component is clicked.
     */
    @Override
    public Action getAction()
    {
        if (actions != null)
        {
            for (Runnable r: actions)
            {
                r.run();
            }
            actions = null;
        }
        return super.getAction();
    }
}
