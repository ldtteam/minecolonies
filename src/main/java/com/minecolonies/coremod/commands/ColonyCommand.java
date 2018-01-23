package com.minecolonies.coremod.commands;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.coremod.commands.colonycommands.*;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.RSCommand;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.RSResetCommand;
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
        .put(ShowColonyInfoCommand.DESC, new ShowColonyInfoCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, ShowColonyInfoCommand.DESC))
        .put(DeleteColonyCommand.DESC, new DeleteColonyCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, DeleteColonyCommand.DESC))
        .put(DisableBarbarianSpawnsCommand.DESC, new DisableBarbarianSpawnsCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, DeleteColonyCommand.DESC))
        .put(AddOfficerCommand.DESC, new AddOfficerCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, AddOfficerCommand.DESC))
        .put(RefreshColonyCommand.DESC, new RefreshColonyCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, RefreshColonyCommand.DESC))
        .put(ChangeColonyOwnerCommand.DESC, new ChangeColonyOwnerCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, ChangeColonyOwnerCommand.DESC))
        .put(ColonyTeleportCommand.DESC, new ColonyTeleportCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, ColonyTeleportCommand.DESC))
        .put(MakeNotAutoDeletable.DESC, new MakeNotAutoDeletable(MinecoloniesCommand.DESC, ColonyCommand.DESC, MakeNotAutoDeletable.DESC))
        .put(DoRaidNowCommand.DESC, new DoRaidNowCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, DoRaidNowCommand.DESC))
        .put(DoRaidTonightCommand.DESC, new DoRaidTonightCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, DoRaidTonightCommand.DESC))
        .put(RSCommand.DESC, new RSCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, RSCommand.DESC))
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
