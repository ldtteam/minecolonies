package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.mojang.authlib.GameProfile;
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
import java.util.UUID;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.SHOWCOLONYINFO;

/**
 * List all colonies.
 */
public class DeleteColonyCommand extends AbstractSingleCommand
{

    public static final  String DESC                       = "delete";
    private static final String NO_COLONY_FOUND_MESSAGE    = "Colony with mayor %s not found.";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public DeleteColonyCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId|OwnerName>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        /* check if sender is permitted to do this :: OFFICER or MAYOR */
        boolean chkPlayer = canCommandSenderUseCommand(SHOWCOLONYINFO);
            /* this checks config to see if player is allowed to use the command and if they are mayor or office of the Colony */
        if (!chkPlayer)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("Not happenin bro!!, You are not permitted to do that!"));
            return;
        }
                /* here we see if they have colony rank to do this command */
        EntityPlayer player = (EntityPlayer)sender;
        int colonyId = -1;
        colonyId = GetColonyAndCitizen.getColonyId(sender.getCommandSenderEntity().getUniqueID(), sender.getEntityWorld(), args);
        Colony chkColony = ColonyManager.getColony(colonyId);
        if (!chkColony.getPermissions().getRank(player).equals(Permissions.Rank.OWNER))
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("Not happenin bro!!, You are not permitted to do that!"));
            return;
        }
        UUID mayorID = null;
        if (args.length != 0)
        {
            try
            {
                colonyId = Integer.parseInt(args[0]);
            }
            catch (final NumberFormatException e)
            {
                mayorID = getUUIDFromName(sender, args);
            }
        }

        final IColony colony;
        if (colonyId == -1)
        {
            colony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID);
        }
        else
        {
            colony = ColonyManager.getColony(colonyId);
        }

        if (colony == null)
        {
            if (colonyId == -1)
            {
                sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE, args[0])));
            }
            else
            {
                sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE_ID, colonyId)));
            }
            return;
        }
        server.addScheduledTask(() -> ColonyManager.deleteColony(colony.getID()));
    }

    @NotNull
    private static UUID getUUIDFromName(@NotNull final ICommandSender sender, @NotNull final String... args)
    {
        final MinecraftServer tempServer = sender.getEntityWorld().getMinecraftServer();
        if (tempServer != null)
        {
            final GameProfile profile = tempServer.getPlayerProfileCache().getGameProfileForUsername(args[0]);
            if (profile != null)
            {
                return profile.getId();
            }
        }
        return null;
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
