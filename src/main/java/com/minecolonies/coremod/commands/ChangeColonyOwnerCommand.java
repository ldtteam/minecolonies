package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.permissions.Permissions;
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

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.CHANGE_COLONY_OWNER;

/**
 * List all colonies.
 */
public class ChangeColonyOwnerCommand extends AbstractSingleCommand
{

    public static final  String       DESC            = "ownerchange";
    private static final String       SUCCESS_MESSAGE = "Succesfully switched Owner %s to colony %d";
    private static final String       COLONY_NULL     = "Couldn't find colony %d.";
    private static final String       NO_ARGUMENTS    = "Please define a colony or player";
    private static final String       NO_PLAYER       = "Can't find player to add";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ChangeColonyOwnerCommand(@NotNull final String... parents)
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if(args.length == 0)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
            return;
        }

        if (!canCommandSenderUseCommand(CHANGE_COLONY_OWNER,sender))
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NOT_PERMITTED));
            return;
        }

        int colonyId = getIthArgument(args, 0, -1);
        if(colonyId == -1 && sender instanceof EntityPlayer)
        {
            final IColony colony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), ((EntityPlayer) sender).getUniqueID());
            if(colony == null)
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(COLONY_NULL));
                return;
            }
            colonyId = colony.getID();
        }

        final Colony colony = ColonyManager.getColony(colonyId);

        if (colony == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(COLONY_NULL, colonyId, colonyId)));
            return;
        }

        if(sender instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) sender;
            if (!colony.getPermissions().getRank(player).equals(Permissions.Rank.OWNER))
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        String playerName = null;
        if (args.length >= 2)
        {
            playerName = args[1];
        }

        if(playerName == null || playerName.isEmpty())
        {
            playerName = sender.getName();
        }

        if(playerName == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_PLAYER));
            return;
        }

        sender.addChatMessage(new TextComponentString(String.format(SUCCESS_MESSAGE, playerName, colonyId)));
    }

    /**
     * Will check the config file to see if players are allowed to use the command that is sent here
     * and will verify that they are of correct rank to do so
     * @param player the players/senders name
     * @param theCommand which command to check if the player can use it
     * @param colonyId the id of the colony.
     * @return boolean
     */
    @Override
    public boolean canPlayerUseCommand(final EntityPlayer player, final Commands theCommand, final int colonyId)
    {
        final Colony chkColony = ColonyManager.getColony(colonyId);
        if(chkColony == null)
        {
            return false;
        }
        return canCommandSenderUseCommand(theCommand, player)
                && (chkColony.getPermissions().getRank(player).equals(Permissions.Rank.OFFICER) || chkColony.getPermissions().getRank(player).equals(Permissions.Rank.OWNER));
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
