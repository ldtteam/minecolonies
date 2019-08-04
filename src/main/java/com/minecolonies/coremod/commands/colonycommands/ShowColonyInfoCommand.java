package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
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
public class ShowColonyInfoCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String DESC                       = "info";
    private static final String ID_TEXT                    = "§2ID: §f";
    private static final String NAME_TEXT                  = "§2 Name: §f";
    private static final String MAYOR_TEXT                 = "§2Mayor: §f";
    private static final String COORDINATES_TEXT           = "§2Coordinates: §f";
    private static final String COORDINATES_XYZ            = "§4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String CITIZENS                   = "§2Citizens: §f";
    private static final String NO_COLONY_FOR_PLAYER_FOUND_MESSAGE = "Colony with mayor %s not found.";
    private static final String NO_COLONY_FOUND_MESSAGE    = "Colony not found.";
    private static final String NO_COLONY_WITH_ID_FOUND_MESSAGE = "Colony with ID %d not found.";
    private static final String LAST_CONTACT_TEXT          = "Last contact with Owner or Officer: %d hours ago!";
    private static final String IS_DELETABLE               = "If true this colony cannot be deleted: ";
    private static final String CANNOT_BE_RAIDED           = "This colony is unable to be raided";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ShowColonyInfoCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ShowColonyInfoCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId|OwnerName>";
    }

    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        // See if we have a valid colony,
        IColony colony = actionMenuState.getColonyForArgument("colony");
        PlayerEntity player = null;
        if (null == colony)
        {
            // see if we have a valid player
            player = actionMenuState.getPlayerForArgument("player");
            if (null != player)
            {
                final IColony iColony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), player);
                if (iColony != null)
                {
                    if (!canPlayerUseCommand(player, SHOWCOLONYINFO, iColony.getID()))
                    {
                        sender.sendMessage(new TextComponentString(NOT_PERMITTED));
                        return;
                    }
                    colony = IColonyManager.getInstance().getColonyByWorld(iColony.getID(), server.getWorld(sender.getEntityWorld().provider.getDimension()));
                }
            }
        }

        if (null == colony)
        {
            // see if we have a sender that is a valid player
            if (sender instanceof EntityPlayer)
            {
                player = (EntityPlayer) sender;
                final UUID mayorID = player.getUniqueID();
                final IColony iColony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), mayorID);
                if (iColony != null)
                {
                    if (!canPlayerUseCommand(player, SHOWCOLONYINFO, iColony.getID()))
                    {
                        sender.sendMessage(new TextComponentString(NOT_PERMITTED));
                        return;
                    }
                    colony = IColonyManager.getInstance().getColonyByWorld(iColony.getID(), server.getWorld(sender.getEntityWorld().provider.getDimension()));
                }
            }
        }

        if (colony == null)
        {
            if (null != player)
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOR_PLAYER_FOUND_MESSAGE, player.getName())));
            }
            else
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE)));
            }
            return;
        }

        executeShared(server, sender, colony);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int colonyId = getIthArgument(args, 0, -1);
        IColony tempColony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().provider.getDimension()));

        if (colonyId == -1 && args.length >= 1)
        {
            final GameProfile playerProfile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);

            if (playerProfile != null)
            {
                tempColony = IColonyManager.getInstance().getIColonyByOwner(server.getEntityWorld(), playerProfile.getId());
            }
        }

        if (sender.getCommandSenderEntity() != null)
        {
            final UUID mayorID = sender.getCommandSenderEntity().getUniqueID();
            if (tempColony == null)
            {
                tempColony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), mayorID);
            }

            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }

            final PlayerEntity player = (EntityPlayer) sender;

            if (!canPlayerUseCommand(player, SHOWCOLONYINFO, colonyId))
            {
                sender.sendMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        if (tempColony == null)
        {
            if (colonyId == -1 && args.length != 0)
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOR_PLAYER_FOUND_MESSAGE, args[0])));
            }
            else
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_WITH_ID_FOUND_MESSAGE, colonyId)));
            }
            return;
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(tempColony.getID(), server.getWorld(sender.getEntityWorld().provider.getDimension()));
        if (colony == null)
        {
            if (colonyId == -1 && args.length != 0)
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOR_PLAYER_FOUND_MESSAGE, args[0])));
            }
            else
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_WITH_ID_FOUND_MESSAGE, colonyId)));
            }
            return;
        }

        executeShared(server, sender, colony);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final IColony colony) throws CommandException
    {
        final BlockPos position = colony.getCenter();
        sender.sendMessage(new TextComponentString(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()));
        final String mayor = colony.getPermissions().getOwnerName();
        sender.sendMessage(new TextComponentString(MAYOR_TEXT + mayor));
        sender.sendMessage(new TextComponentString(CITIZENS + colony.getCitizenManager().getCitizens().size() + "/" + colony.getCitizenManager().getMaxCitizens()));
        sender.sendMessage(new TextComponentString(COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));
        sender.sendMessage(new TextComponentString(String.format(LAST_CONTACT_TEXT, colony.getLastContactInHours())));
        sender.sendMessage(new TextComponentString(IS_DELETABLE + !colony.canBeAutoDeleted()));

        if (!colony.isCanHaveBarbEvents())
        {
            sender.sendMessage(new TextComponentString(CANNOT_BE_RAIDED));
        }
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
