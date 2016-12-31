package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ColonyManager;
import com.typesafe.config.ConfigException;
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

    private static final String ONLY_NUMBERS       = "Please only use numbers for the %s ID!";
    private static final String TOO_MANY_ARGUMENTS = "Too many arguments!";
    private static final String UNKNOWN_ERROR      = "Unknown Error!";
    private static final String NOT_FOUND          = "%s not found";


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
        if (colonyId >= 0)
        {
            if (ColonyManager.getColony(colonyId) == null)
            {
                colonyId = -4;
            }
        }


        return colonyId;
    }

    public static int getCitizenId(@NotNull final int colonyId, @NotNull final String... args)
    {
        String citizenName;
        citizenId = -4;
        if (args.length == 2)
        {
            try
            {
                citizenId = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                citizenId = -1;
            }
        }
        else if (args.length == 1)
        {
            try
            {
                citizenId = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                citizenId = -1;
            }
        }
        else if (args.length == 3 && colonyId >= 0)
        {
            citizenName = args[0] + " " + args[1] + " " + args[2];
            for (int i = 1
                   ; i <= ColonyManager.getColony(colonyId).getCitizens().size(); i++)
            {
                try
                {
                    if (ColonyManager.getColony(colonyId).getCitizen(i).getName().equals(citizenName))
                    {
                        citizenId = i;
                    }
                }
                catch (NullPointerException e)
                {
                    continue;
                }
            }

        }
        else if (args.length == 4 && colonyId >= 0)
        {
            citizenName = args[1] + " " + args[2] + " " + args[3];
            for (int i = 1; i <= ColonyManager.getColony(colonyId).getCitizens().size(); i++)
            {
                if (ColonyManager.getColony(colonyId).getCitizen(i).getName().equals(citizenName))
                {
                    citizenId = i;
                }
            }
        }
        else if (args.length >= 5)
        {
            citizenId = -2;
        }
        else
        {
            citizenId = -3;
        }
        if (citizenId >= 0 && colonyId >= 0)
        {
            if (ColonyManager.getColony(colonyId).getCitizen(citizenId) == null)
            {
                citizenId = -4;
            }
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
            ErrorMessage = String.format(ONLY_NUMBERS, "colony");
        }
        else if (colonyId == -2)
        {
            ErrorMessage = TOO_MANY_ARGUMENTS;
        }
        else if (colonyId == -3)
        {
            ErrorMessage = UNKNOWN_ERROR;
        }
        else if (colonyId == -4)
        {
            ErrorMessage = String.format(NOT_FOUND, "Colony");
        }

        if (citizenId >= 0 && ErrorMessage == null)
        {
            ErrorMessage = null;
        }
        else if (citizenId == -1 && ErrorMessage == null)
        {
            ErrorMessage = String.format(ONLY_NUMBERS, "citizen");
        }
        else if (citizenId == -2 && ErrorMessage == null)
        {
            ErrorMessage = TOO_MANY_ARGUMENTS;
        }
        else if (citizenId == -3 && ErrorMessage == null)
        {
            ErrorMessage = UNKNOWN_ERROR;
        }
        else if (citizenId == -4 && ErrorMessage == null)
        {
            ErrorMessage = String.format(NOT_FOUND, "Citizen");
        }

        return ErrorMessage;
    }
}
