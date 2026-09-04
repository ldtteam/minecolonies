package com.minecolonies.core.commands;

import org.jetbrains.annotations.NotNull;

/**
 * Runs deferred actions when a client-side click handler needs to bridge an
 * event back to MineColonies-owned logic.
 */
public final class ClickEventWithExecutable
{
    private final Runnable[] actions;

    public ClickEventWithExecutable(@NotNull final Runnable... actions)
    {
        this.actions = actions.clone();
    }

    public void run()
    {
        for (final Runnable action : actions)
        {
            action.run();
        }
    }
}
