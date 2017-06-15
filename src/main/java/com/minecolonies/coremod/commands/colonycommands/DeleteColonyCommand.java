package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
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

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.DELETECOLONY;

/**
 * List all colonies.
 */
public class DeleteColonyCommand extends AbstractSingleCommand
{

    public static final  String DESC                       = "delete";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";
    private static final String NO_ARGUMENTS               = "Please define a colony to delete";

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
    public boolean canRankUseCommand(@NotNull final Colony colony, @NotNull final EntityPlayer player)
    {
        return colony.getPermissions().getRank(player).equals(Rank.OWNER);
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
                colony = ColonyManager.getIColonyByOwner(CompatibilityUtils.getWorld(((EntityPlayer) sender)), (EntityPlayer) sender);
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

        final Colony colony = ColonyManager.getColony(colonyId);
        if(colony == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_COLONY_FOUND_MESSAGE_ID));
            return;
        }

        if(sender instanceof EntityPlayer)
        {
            final EntityPlayer player = (EntityPlayer) sender;
            if (!canPlayerUseCommand(player, DELETECOLONY, colonyId))
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
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
