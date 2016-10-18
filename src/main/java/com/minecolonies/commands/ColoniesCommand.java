package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Minecolonies root command.
 * <p>
 * Manages all sub commands.
 */
public class ColoniesCommand extends AbstractSplitCommand
{

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("list", new ListColonies("mc", "colonies", "list"))
        .build();

    /**
     * Initialize this command with it's parents.
     */
    public ColoniesCommand()
    {
        super("mc", "colonies");
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    @Override
    public String getCommandName()
    {
        return "mc colonies list";
    }
}
