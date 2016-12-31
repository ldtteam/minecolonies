package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Get the Colony ID and Citizen ID out of a command
 */
public abstract class GetColonyAndCitizen
{
    public static int colonyId;
    public static int citizenId;

    public static int getColonyId(@NotNull final ICommandSender sender, @NotNull final String... args)
    {
        final UUID mayorID = sender.getCommandSenderEntity().getUniqueID();
        if (args.length == 2 || args.length == 4)
        {
            try
            {
                colonyId = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                colonyId = -1;
            }
        }
        else if (args.length == 1 || args.length == 3)
        {
            colonyId = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID).getID();
        }
        else if (args.length >= 5)
        {
            colonyId = -2;
        }
        else
        {
            colonyId = -3;
        }
        if (ColonyManager.getColony(colonyId) == null)
        {
            colonyId = -4;
        }


        return colonyId;
    }

    public static int getCitizenId(@NotNull final int colonyId, @NotNull final String... args)
    {
        String citizenName = null;
        if (args.length == 2)
        {
            citizenId = Integer.parseInt(args[1]);
        }
        else if (args.length == 1)
        {
            citizenId = Integer.parseInt(args[0]);
        }
        else if (args.length == 3 && colonyId >= 0)
        {
            final List<CitizenData> citizens = new ArrayList<>(ColonyManager.getColony(colonyId).getCitizens().values());
            citizenName = args[0] + " " + args[1] + " " + args[2];
            for (int i = 0; i == citizens.size(); i++)
            {
                if (ColonyManager.getColony(colonyId).getCitizen(i).getName() == citizenName)
                {
                    citizenId = i;
                }
            }

        }
        else if (args.length == 4)
        {
            final List<CitizenData> citizens = new ArrayList<>(ColonyManager.getColony(colonyId).getCitizens().values());
            citizenName = args[1] + " " + args[2] + " " + args[3];
            for (int i = 0; i == citizens.size(); i++)
            {
                if (ColonyManager.getColony(colonyId).getCitizen(i).getName() == citizenName)
                {
                    citizenId = i;
                }
            }
        }
        else if (args.length >= 5)
        {
            citizenId = -1;
        }
        else
        {
            citizenId = -2;
        }
        if (citizenId >= 0 && colonyId >= 0 && ColonyManager.getColony(colonyId).getCitizen(citizenId) == null)
        {
            citizenId = -3;
        }
        return citizenId;
    }

    public static String getErrors(@NotNull final int colonyId, @NotNull final int citizenId)
    {
        String ErrorMessage = null;
        if (colonyId >= 0)
        {
            ErrorMessage = null;
        }
        else if (colonyId == -1)
        {
            ErrorMessage = "Please only use numbers for the colony ID!";
        }
        else if (colonyId == -2)
        {
            ErrorMessage = "Too many arguments!";
        }
        else if (colonyId == -3)
        {
            ErrorMessage = "Unknown Error!";
        }
        else if (colonyId == -4)
        {
            ErrorMessage = "Colony not found!";
        }

        if (citizenId >= 0 && ErrorMessage == null)
        {
            ErrorMessage = null;
        }
        else if (citizenId == -1 && ErrorMessage == null)
        {
            ErrorMessage = "Too many arguments!";
        }
        else if (citizenId == -2 && ErrorMessage == null)
        {
            ErrorMessage = "Unknown Error!";
        }
        else if (citizenId == -3 && ErrorMessage == null)
        {
            ErrorMessage = "Citizen not found!";
        }

        return ErrorMessage;
    }
}
