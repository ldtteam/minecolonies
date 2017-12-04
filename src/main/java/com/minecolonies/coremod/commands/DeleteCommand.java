package com.minecolonies.coremod.commands;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.coremod.commands.killcommands.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Root minecolonies kill command.
 */
public class DeleteCommand extends AbstractSplitCommand
{

    public static final String DESC = "kill";

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put(BarbarianKillCommand.DESC, new BarbarianKillCommand())
        .put(AnimalKillCommand.DESC, new AnimalKillCommand())
        .put(MobKillCommand.DESC, new MobKillCommand())
        .put(ChickenKillCommand.DESC, new ChickenKillCommand())
        .put(CowKillCommand.DESC, new CowKillCommand())
        .put(PigKillCommand.DESC, new PigKillCommand())
        .put(SheepKillCommand.DESC, new SheepKillCommand())
        .build();

    /**
     * Initialize this command with it's parents.
     *
     * @param parent the parent of the command.
     */
    public DeleteCommand(@NotNull final String parent)
    {
        super(parent, DESC);
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }
}
