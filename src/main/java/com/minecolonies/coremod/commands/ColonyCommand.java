package com.minecolonies.coremod.commands;

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
        .put(KillCitizenCommand.DESC, new KillCitizenCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, KillCitizenCommand.DESC))
        .put(RespawnCitizenCommand.DESC, new RespawnCitizenCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, RespawnCitizenCommand.DESC))
        .put(ShowColonyInfoCommand.DESC, new ShowColonyInfoCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, ShowColonyInfoCommand.DESC))
        .put(DeleteColonyCommand.DESC, new DeleteColonyCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, DeleteColonyCommand.DESC))
        .put(AddOfficerCommand.DESC, new AddOfficerCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, AddOfficerCommand.DESC))
        .put(CitizenInfoCommand.DESC, new CitizenInfoCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, CitizenInfoCommand.DESC))
        .build();

    /**
     * Initialize this command with it's parents.
     *
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
