package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.commands.AbstractSingleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Command to detect abandoned colonies.
 */
public class FindAbandonedColoniesCommand extends AbstractSingleCommand
{

    public static final  String DESC                       = "delete";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public FindAbandonedColoniesCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {

    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
            @NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args, @Nullable final BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
