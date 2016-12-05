package com.minecolonies.coremod.commands;

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
        .put(ColoniesCommand.DESC, new ColoniesCommand(DESC))
        .put(ColonyCommand.DESC, new ColonyCommand(DESC))
        .put(CitizensCommand.DESC, new CitizensCommand(DESC))
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
