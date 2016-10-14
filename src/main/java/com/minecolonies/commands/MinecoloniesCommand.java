package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Minecolonies root command.
 * <p>
 * Manages all sub commands.
 */
public class MinecoloniesCommand extends SplitCommand implements ICommand
{
    private ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("colonies", new ColoniesCommand())
        .build();

    public MinecoloniesCommand()
    {
        super("mc");
    }

    @NotNull
    @Override
    public String getCommandName()
    {
        return "minecolonies";
    }

    @NotNull
    @Override
    public List<String> getCommandAliases()
    {
        return Arrays.asList("mc", "col", "mcol", "mcolonies", "minecol", "minecolonies");
    }

    @Override
    public boolean checkPermission(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender)
    {
        return true;
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    @Override
    public int compareTo(@NotNull final ICommand o)
    {
        return 0;
    }
}
