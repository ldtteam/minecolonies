package com.minecolonies.commands;

import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A command that has children. Is a single one-word command.
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

    /**
     * Get the ith argument (An Integer).
     * @param i the argument from the list you want.
     * @param args the list of arguments.
     * @param def the default value.
     * @return the argument.
     */
    public static int getIthArgument(String[] args, int i, int def)
    {
        if(args.length < i)
        {
            return def;
        }

        try
        {
            return Integer.parseInt(args[i]);
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }
}
