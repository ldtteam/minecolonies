package com.minecolonies.commands;

import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A command that has children.
 */
public abstract class SingleCommand implements ISubCommand
{

    private final String[] parents;

    public SingleCommand(@NotNull String[] parents)
    {
        this.parents = parents;
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        final StringBuilder sb = new StringBuilder().append('/');
        for (final String parent : parents)
        {
            sb.append(parent).append(' ');
        }
        return sb.toString();
    }
}
