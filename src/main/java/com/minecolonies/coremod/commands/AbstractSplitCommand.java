package com.minecolonies.coremod.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A command that has children. Split-command with various parts.
 */
public abstract class AbstractSplitCommand implements ISubCommand
{

    private final String[] parents;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AbstractSplitCommand(@NotNull final String... parents)
    {
        super();
        this.parents = parents;
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final CommandSource sender)
    {
        final Map<String, ISubCommand> childs = getSubCommands();
        final StringBuilder sb = new StringBuilder().append('/');
        for (final String parent : parents)
        {
            sb.append(parent).append(' ');
        }
        sb.append('<');
        boolean first = true;
        for (final String child : childs.keySet())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append('|');
            }
            sb.append(child);
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String... args) throws CommandException
    {
        final Map<String, ISubCommand> childs = getSubCommands();
        if (args.length == 0
              || !childs.containsKey(args[0]))
        {
            //todo: check if WrongUsageException is better
            throw new CommandException(getCommandUsage(sender));
        }
        final ISubCommand child = childs.get(args[0]);
        final String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        child.execute(server, sender, newArgs);
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final CommandSource sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {

        final Map<String, ISubCommand> childs = getSubCommands();
        if (args.length <= 1
              || !childs.containsKey(args[0]))
        {
            return childs.keySet().stream().filter(k -> k.startsWith(args[0])).collect(Collectors.toList());
        }
        final ISubCommand child = childs.get(args[0]);
        final String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return child.getTabCompletionOptions(server, sender, newArgs, pos);
    }


    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        final Map<String, ISubCommand> childs = getSubCommands();
        if (index == 0 || args.length == 0
              || !childs.containsKey(args[0]))
        {
            return false;
        }
        final ISubCommand child = childs.get(args[0]);
        final String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return child.isUsernameIndex(newArgs, index - 1);
    }

    /**
     * Get all sub-commands that can be reached.
     *
     * @return a mapping from command text to ISubCommand
     */
    public abstract Map<String, ISubCommand> getSubCommands();
}
