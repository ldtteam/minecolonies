package com.minecolonies.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A command that has children.
 */
public abstract class SplitCommand implements ISubCommand
{

    private final String[] parents;

    public SplitCommand(@NotNull String[] parents)
    {
        this.parents = parents;
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        final Map<String, ISubCommand> childs = getSubCommands();
        final StringBuilder sb = new StringBuilder().append('/');
        for (String parent : parents)
        {
            sb.append(parent).append(' ');
        }
        sb.append('<');
        boolean first = true;
        for (String child : childs.keySet())
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args) throws CommandException
    {
        final Map<String, ISubCommand> childs = getSubCommands();
        if (args.length == 0
              || !childs.containsKey(args[0]))
        {
            //todo: check if WrongUsageException is better
            throw new CommandException(getCommandUsage(sender));
        }
        ISubCommand child = childs.get(args[0]);
        final String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        child.execute(server, sender, newArgs);
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {

        final Map<String, ISubCommand> childs = getSubCommands();
        if (args.length <= 1
              || !childs.containsKey(args[0]))
        {
            return new ArrayList<>(childs.keySet());
        }
        ISubCommand child = childs.get(args[0]);
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
        return child.isUsernameIndex(newArgs, index-1);
    }

    public abstract Map<String, ISubCommand> getSubCommands();
}
