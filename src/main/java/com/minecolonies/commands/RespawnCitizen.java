package com.minecolonies.commands;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.IColony;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * List all colonies.
 */
public class RespawnCitizen extends AbstractSingleCommand
{

    private static final String ID_TEXT                = "§2ID: §f";
    private static final String NAME_TEXT              = "§2 Name: §f";
    private static final String COORDINATES_TEXT       = "§2Coordinates: §f";
    private static final String REMOVED_MESSAGE        = "Has been removed";
    private static final String NO_COLONY_CITIZEN_FOUND_MESSAGE = "No citizen %d found in colony %d.";
    private static final String COORDINATES_XYZ = "§4x=§f%s §4y=§f%s §4z=§f%s";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RespawnCitizen(@NotNull final String... parents)
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
        int citizenId = -1;

        //todo framework for argument parsing
        if (args.length != 0)
        {
            if(args.length >= 2)
            {
                try
                {
                    colonyId = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e)
                {
                    //ignore and keep page 1.
                }
            }
            else
            {
                try
                {
                    citizenId = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e)
                {
                    //ignore and keep page 1.
                }
            }
        }

        Colony colony = null;
        final IColony tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), sender.getCommandSenderEntity().getUniqueID());
        if(tempColony != null)
        {
            colony = ColonyManager.getColony(sender.getEntityWorld(), tempColony.getCenter());
            colony = colony == null ? ColonyManager.getColony(colonyId) : colony;

            if(colony != null)
            {
                colonyId = colony.getID();
            }
        }

        if(colony == null || colonyId == -1 || citizenId == -1)
        {
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_CITIZEN_FOUND_MESSAGE, citizenId, colonyId)));
            return;
        }

        CitizenData citizenData = colony.getCitizen(citizenId);

        if(citizenData == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_CITIZEN_FOUND_MESSAGE, citizenId, colonyId)));
            return;
        }

        EntityCitizen entityCitizen = citizenData.getCitizenEntity();

        if(entityCitizen == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_CITIZEN_FOUND_MESSAGE, citizenId, colonyId)));
            return;
        }

        sender.addChatMessage(new TextComponentString(ID_TEXT + citizenData.getId() + NAME_TEXT + citizenData.getName()));
        final BlockPos position = entityCitizen.getPosition();
        sender.addChatMessage(new TextComponentString(COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));
        sender.addChatMessage(new TextComponentString(REMOVED_MESSAGE));

        entityCitizen.setDead();

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
