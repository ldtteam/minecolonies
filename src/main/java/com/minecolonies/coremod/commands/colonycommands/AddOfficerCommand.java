package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.CommandConstants.*;
import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.ADDOFFICER;

/**
 * List all colonies.
 */
public class AddOfficerCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final String DESC = "addOfficer";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public AddOfficerCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AddOfficerCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <(Optional)Player>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final Colony colony = actionMenuState.getColonyForArgument("colony");
        final EntityPlayer player = actionMenuState.getPlayerForArgument("player");

        executeShared(server, sender, colony, player.getName());
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString(NO_COLONY_OR_PLAYER));
            return;
        }

        final Entity senderEntity = sender.getCommandSenderEntity();

        int colonyId = getIthArgument(args, 0, -1);
        if (colonyId == -1 && senderEntity instanceof EntityPlayer)
        {
            final IColony colony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), ((EntityPlayer) sender).getUniqueID());
            if (colony == null)
            {
                sender.sendMessage(new TextComponentString(COLONY_X_NULL));
                return;
            }
            colonyId = colony.getID();
        }

        final Colony colony = ColonyManager.getColony(colonyId);

        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(String.format(COLONY_X_NULL, colonyId)));
            return;
        }

        String playerName = null;
        if (args.length >= 2)
        {
            playerName = args[1];
        }

        if (playerName == null || playerName.isEmpty())
        {
            playerName = sender.getName();
        }

        executeShared(server, sender, colony, playerName);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final Colony colony, final String playerName)
            throws CommandException
    {
        final Entity senderEntity = sender.getCommandSenderEntity();
        if (senderEntity instanceof EntityPlayer)
        {
            final EntityPlayer senderPlayer = (EntityPlayer) sender;
            if (!canPlayerUseCommand(senderPlayer, ADDOFFICER, colony.getID()))
            {
                sender.sendMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        colony.getPermissions().addPlayer(playerName, Rank.OFFICER, colony.getWorld());
        sender.sendMessage(new TextComponentString(String.format(SUCCESS_MESSAGE_ADD_OFFICER, playerName, colony.getID())));
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
