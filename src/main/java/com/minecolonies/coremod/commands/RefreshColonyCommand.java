package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
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

/**
 * List all colonies.
 */
public class RefreshColonyCommand extends AbstractSingleCommand
{

    public static final  String DESC                       = "refresh";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";
    private static final String NO_ARGUMENTS               = "Please define a colony to refresh";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RefreshColonyCommand(@NotNull final String... parents)
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
        final int colonyId;
        if(args.length == 0)
        {
            IColony colony = null;
            if(sender instanceof EntityPlayer)
            {
                colony = ColonyManager.getIColonyByOwner(((EntityPlayer) sender).worldObj, (EntityPlayer) sender);
            }

            if(colony == null)
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
                return;
            }
            colonyId = colony.getID();
        }
        else
        {
            colonyId = getIthArgument(args, 0, -1);
        }

        final EntityPlayer player = (EntityPlayer) sender;
        if (!canPlayerUseCommand (player, Commands.valueOf("REFRESH_COLONY"), colonyId))
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NOT_PERMITTED));
            return;
        }

        final Colony colony = ColonyManager.getColony(colonyId);

        if(colony == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_COLONY_FOUND_MESSAGE_ID));
            return;
        }

        colony.getPermissions().restoreOwnerIfNull();
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
