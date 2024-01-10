package com.minecolonies.coremod.commands;

import net.minecraft.network.chat.ClickEvent;
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

    /**
     * Default constructor.
     *
     * @param actions the actions this event should execute.
     */
    public ClickEventWithExecutable(@NotNull final Runnable... actions)
    {
        super(ClickEvent.Action.RUN_COMMAND, "");
        this.actions = actions;
    }

    /**
     * Triggered when the chat component is clicked.
     */
    @Override
    @NotNull
    public Action getAction()
    {
        if (actions != null)
        {
            for (Runnable r : actions)
            {
                r.run();
            }
            actions = null;
        }
        return super.getAction();
    }
}
