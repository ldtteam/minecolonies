package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MakeNotAutoDeletable extends AbstractSingleCommand
{
    public static final  String DESC                       = "markUndeletable";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";
    private static final String MARKED                     = "Marking succesful!";
    private static final String NOT_ENOUGH_ARGUMENTS       = "You must have 2 Arguments: <ColonyId> <true|false> ";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public MakeNotAutoDeletable(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <true|false>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length < 2)
        {
            sender.addChatMessage(new TextComponentString(NOT_ENOUGH_ARGUMENTS));
            return;
        }

        sender.addChatMessage(new TextComponentString(Arrays.toString(args)));

        int colonyId;
        colonyId = Integer.parseInt(args[1]);
        Colony colony = ColonyManager.getColony(colonyId);

        boolean canBeDeleted;
        canBeDeleted = Boolean.parseBoolean(args[2]);

        final Entity senderEntity = sender.getCommandSenderEntity();

        if (colony == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE_ID, colonyId)));
            return;
        }

        sender.addChatMessage(new TextComponentString(MARKED));
        colony.setCanBeAutoDeleted(canBeDeleted);
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return index == 0
                 && args.length > 0
                 && !args[0].isEmpty()
                 && getIthArgument(args, 0, Integer.MAX_VALUE) == Integer.MAX_VALUE;
    }
}
