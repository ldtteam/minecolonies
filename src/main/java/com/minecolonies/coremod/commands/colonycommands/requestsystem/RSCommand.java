package com.minecolonies.coremod.commands.colonycommands.requestsystem;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.coremod.commands.AbstractSplitCommand;
import com.minecolonies.coremod.commands.ColonyCommand;
import com.minecolonies.coremod.commands.ISubCommand;
import com.minecolonies.coremod.commands.MinecoloniesCommand;
import com.minecolonies.coremod.commands.colonycommands.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RSCommand extends AbstractSplitCommand
{
    public static final String DESC = "rs";

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put(RSResetCommand.DESC, new RSResetCommand(MinecoloniesCommand.DESC, ColonyCommand.DESC, RSCommand.DESC, RSResetCommand.DESC))
        .build();

    public RSCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }
}
