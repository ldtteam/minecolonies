package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
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

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.SHOWCOLONYINFO;

/**
 * List all colonies.
 */
public class ShowColonyInfoCommand extends AbstractSingleCommand
{

    public static final  String DESC                       = "info";
    private static final String ID_TEXT                    = "§2ID: §f";
    private static final String NAME_TEXT                  = "§2 Name: §f";
    private static final String MAYOR_TEXT                 = "§2Mayor: §f";
    private static final String COORDINATES_TEXT           = "§2Coordinates: §f";
    private static final String COORDINATES_XYZ            = "§4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String CITIZENS                   = "§2Citizens: §f";
    private static final String NO_COLONY_FOUND_MESSAGE    = "Colony with mayor %s not found.";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";
    private static final String LAST_CONTACT_TEXT          = "Last contact with Owner or Officer: %d hours ago!";
    private static final String IS_DELETABLE               = "If true this colony cannot be deleted: ";
    private static final String CANNOT_BE_RAIDED           = "This colony is unable to be raided";

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

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int colonyId;
        colonyId = getIthArgument(args, 0, -1);
        IColony tempColony = ColonyManager.getColony(colonyId);

        if (colonyId == -1 && args.length >= 1)
        {
            final GameProfile playerProfile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);

            if (playerProfile != null)
            {
                tempColony = ColonyManager.getIColonyByOwner(server.getEntityWorld(), playerProfile.getId());
            }
        }

        if (sender.getCommandSenderEntity() != null)
        {
            final UUID mayorID = sender.getCommandSenderEntity().getUniqueID();
            if (tempColony == null)
            {
                tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID);
            }

            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }

            final EntityPlayer player = (EntityPlayer) sender;

            if (!canPlayerUseCommand(player, SHOWCOLONYINFO, colonyId))
            {
                sender.getCommandSenderEntity().sendMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        if (tempColony == null)
        {
            if (colonyId == -1 && args.length != 0)
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE, args[0])));
            }
            else
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE_ID, colonyId)));
            }
            return;
        }

        final Colony colony = ColonyManager.getColony(tempColony.getID());
        if (colony == null)
        {
            if (colonyId == -1 && args.length != 0)
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE, args[0])));
            }
            else
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE_ID, colonyId)));
            }
            return;
        }

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
