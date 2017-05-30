package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
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

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.ADDOFFICER;

/**
 * List all colonies.
 */
public class AddOfficerCommand extends AbstractSingleCommand
{

    public static final  String       DESC            = "addOfficer";
    private static final String       SUCCESS_MESSAGE = "Succesfully added Player %s to colony %d";
    private static final String       COLONY_NULL     = "Couldn't find colony %d.";
    private static final String       NO_ARGUMENTS    = "Please define a colony or player";

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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if(args.length == 0)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
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
            sender.addChatMessage(new TextComponentString(String.format(COLONY_NULL, colonyId)));
            return;
        }

        if(sender instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) sender;
            if (!canPlayerUseCommand(player, ADDOFFICER, colonyId))
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

        colony.getPermissions().addPlayer(playerName, Rank.OFFICER, colony.getWorld());
        sender.addChatMessage(new TextComponentString(String.format(SUCCESS_MESSAGE, playerName, colonyId)));
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
