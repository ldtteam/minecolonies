package com.minecolonies.commands;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Minecolonies root command.
 * <p>
 * Manages all sub commands.
 */
public class ColonyCommand extends AbstractSplitCommand
{

    public static final String DESC = "colony";

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("kill", new KillCitizen(MinecoloniesCommand.DESC, ColonyCommand.DESC, "kill"))
        .put("respawn", new RespawnCitizen(MinecoloniesCommand.DESC, ColonyCommand.DESC, "respawn"))
        .build();

    /**
     * Initialize this command with it's parents.
     * @param parent the parent of the command.
     */
    public ColonyCommand(@NotNull final String parent)
    {
        super(parent, ColonyCommand.DESC);
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }
}
