package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;
import net.minecraft.command.CommandBase;
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
public class MinecoloniesCommand extends AbstractSplitCommand
{

    public static final String DESC = "minecolonies";

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("colonies", new ColoniesCommand(DESC))
        .build();

    /**
     * Initialize this command with the shortest alias.
     */
    public MinecoloniesCommand()
    {
        super(DESC);
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }

}
