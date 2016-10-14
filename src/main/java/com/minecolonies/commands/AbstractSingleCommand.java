package com.minecolonies.commands;

import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A command that has children.
 */
public abstract class AbstractSingleCommand implements ISubCommand
{

    private final String[] parents;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AbstractSingleCommand(@NotNull String... parents)
    {
        this.parents = parents.clone();
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
