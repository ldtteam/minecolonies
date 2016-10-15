package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Minecolonies root command.
 * <p>
 * Manages all sub commands.
 */
public class ColonyCommand extends AbstractSplitCommand
{

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("killCitizen", new KillCitizen("mc", "colony", "kill"))
        .put("respawnCitizen", new RespawnCitizen("mc", "colony", "respawn"))
        .build();

    /**
     * Initialize this command with it's parents.
     */
    public ColonyCommand()
    {
        super("mc", "colonies");
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }
}
