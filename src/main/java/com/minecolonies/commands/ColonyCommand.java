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
        .put(KillCitizen.DESC, new KillCitizen(MinecoloniesCommand.DESC, ColonyCommand.DESC, KillCitizen.DESC))
        .put(RespawnCitizen.DESC, new RespawnCitizen(MinecoloniesCommand.DESC, ColonyCommand.DESC, RespawnCitizen.DESC))
        .put(ColonyInfo.DESC, new ColonyInfo(MinecoloniesCommand.DESC, ColonyCommand.DESC, ColonyInfo.DESC))
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
