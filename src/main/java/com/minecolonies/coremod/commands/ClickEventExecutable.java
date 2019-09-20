package com.minecolonies.coremod.commands;

import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Small utility class to run an executable on a chat click event.
 */
public class ClickEventExecutable extends ClickEvent
{
    /**
     * The actions to run
     */
    private Runnable[] actions;

    public ClickEventExecutable(@NotNull final Runnable ... actions)
    {
        super(Action.RUN_COMMAND, "");
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
        return Action.RUN_COMMAND;
    }
}
