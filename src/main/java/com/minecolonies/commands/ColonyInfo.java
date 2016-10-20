package com.minecolonies.commands;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.IColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * List all colonies.
 */
public class ColonyInfo extends AbstractSingleCommand
{

    private static final String ID_TEXT                    = "§2ID: §f";
    private static final String NAME_TEXT                  = "§2 Name: §f";
    private static final String MAYOR_TEXT                 = "§2Mayor: §f";
    private static final String COORDINATES_TEXT           = "§2Coordinates: §f";
    private static final String COORDINATES_XYZ            = "§4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String CITIZENS                   = "§2Citizens: §f";
    private static final String NO_COLONY_FOUND_MESSAGE    = "Colony with mayor %s not found.";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";

    public static final String DESC                        = "info";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ColonyInfo(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int colonyId = -1;
        UUID mayorID = sender.getCommandSenderEntity().getUniqueID();
        final boolean found;

        if (args.length != 0)
        {
            try
            {
                colonyId = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                final UUID tempMayorID = sender.getEntityWorld().getMinecraftServer().getPlayerProfileCache().getGameProfileForUsername(args[0]).getId();
                mayorID = tempMayorID;
            }
        }

        Colony colony = null;
        final IColony tempColony;
        if(colonyId == -1)
        {
            tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID);
        }
        else
        {
            tempColony = ColonyManager.getColony(colonyId);
        }
        if(tempColony == null)
        {
            found = false;
        }
        else
        {
            colony = ColonyManager.getColony(sender.getEntityWorld(), tempColony.getCenter());

            if (colony == null)
            {
                found = false;
            }
            else
            {
                found = true;
                colonyId = colony.getID();
            }
        }

        if (!found)
        {
            if (colonyId == -1)
            {
                sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE, args[0])));
                return;
            }
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE_ID, colonyId)));
            return;
        }

        final BlockPos position = colony.getCenter();
        sender.addChatMessage(new TextComponentString(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()));
        final String mayor = colony.getPermissions().getOwnerName();
        sender.addChatMessage(new TextComponentString(MAYOR_TEXT + mayor));
        sender.addChatMessage(new TextComponentString(CITIZENS + colony.getCitizens().size() + "/" + colony.getMaxCitizens()));
        sender.addChatMessage(new TextComponentString(COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
