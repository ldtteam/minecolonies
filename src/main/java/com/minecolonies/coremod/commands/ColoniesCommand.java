package com.minecolonies.coremod.commands;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.coremod.commands.colonycommands.ListColoniesCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Minecolonies root command.
 * <p>
 * Manages all sub commands.
 */
public class ColoniesCommand extends AbstractSplitCommand
{
    public static final String DESC = "colonies";

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put("list", new ListColoniesCommand(MinecoloniesCommand.DESC, ColoniesCommand.DESC, "list"))
        .build();

    /**
     * Initialize this command with it's parents.
     *
     * @param parent the parent commands
     */
    public ColoniesCommand(@NotNull final String parent)
    {
        super(parent, DESC);
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }
}
