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
public class ColoniesCommand extends SplitCommand
{

    public ColoniesCommand()
    {
        super(new String[] {"mc", "colonies"});
    }

    private ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("list", new ListColonies(new String[] {"mc", "colonies", "list"}))
        .build();

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }

}
