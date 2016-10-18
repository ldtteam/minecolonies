package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;
import net.minecraft.command.CommandException;
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
public class ColoniesCommand extends AbstractSplitCommand
{
    public static final String DESC = "colonies";

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("list", new ListColonies("mc", DESC, "list"))
        .build();

    /**
     * Initialize this command with it's parents.
     */
    public ColoniesCommand()
    {
        super();
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

    @NotNull
    @Override
    public List<String> getCommandAliases()
    {
        return Arrays.asList("c", DESC);
    }

    @NotNull
    @Override
    public String getCommandName()
    {
        return DESC;
    }
}
