package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Command to disable or enable barbarian events in a colony.
 */
public class DisableBarbarianSpawnsCommand extends AbstractSingleCommand
{

    public static final  String DESC                       = "barbarians";
    private static final String NO_ARGUMENTS               = "Please define a colony";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public DisableBarbarianSpawnsCommand(@NotNull final String... parents)
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
    public void execute(
                         @NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {

        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use this command"));
        }

        final int colonyId;

        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString(NO_ARGUMENTS));
        }

        colonyId = getIthArgument(args, 0, -1);

        final Colony colony = ColonyManager.getColony(colonyId);

        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(NO_COLONY_FOUND_MESSAGE_ID));
            return;
        }

        final boolean setBarbsTo = Boolean.parseBoolean(args[1]);

        colony.getBarbManager().setCanHaveBarbEvents(setBarbsTo);

        sender.sendMessage(new TextComponentString("Colony \" Can have Barbarian Events \" now set to: " + colony.isCanHaveBarbEvents()));
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
        return false;
    }
}
