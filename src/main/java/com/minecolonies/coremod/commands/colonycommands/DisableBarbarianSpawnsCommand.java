package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
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
public class DisableBarbarianSpawnsCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String DESC                       = "barbarians";
    private static final String NO_ARGUMENTS               = "Please define a colony";
    private static final String NO_COLONY_WITH_ID_FOUND_MESSAGE = "Colony with ID %d not found.";
    private static final String NO_COLONY_FOUND_MESSAGE = "Colony not found.";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public DisableBarbarianSpawnsCommand()
    {
        super();
    }

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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final IColony colony = actionMenuState.getColonyForArgument("colony");
        final boolean canHaveBarbEvents = !actionMenuState.getBooleanValueForArgument("disableSpawns", true);

        if (colony == null)
        {
            final String noColonyFoundMessage = String.format(NO_COLONY_FOUND_MESSAGE);
            sender.sendMessage(new TextComponentString(noColonyFoundMessage));
            return;
        }

        executeShared(server, sender, colony, canHaveBarbEvents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString(NO_ARGUMENTS));
        }

        final int colonyId;

        colonyId = getIthArgument(args, 0, -1);

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(0));

        if (colony == null)
        {
            final String noColonyFoundMessage = String.format(NO_COLONY_WITH_ID_FOUND_MESSAGE, colonyId);
            sender.sendMessage(new TextComponentString(noColonyFoundMessage));
            return;
        }

        final boolean canHaveBarbEvents = Boolean.parseBoolean(args[1]);

        executeShared(server, sender, colony, canHaveBarbEvents);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final IColony colony, final boolean canHaveBarbEvents)
            throws CommandException
    {

        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use this command"));
            return;
        }

        colony.getRaiderManager().setCanHaveRaiderEvents(canHaveBarbEvents);
        colony.markDirty();

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
