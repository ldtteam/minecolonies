package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;

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
     * Initialize this command with the canon alias.
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
