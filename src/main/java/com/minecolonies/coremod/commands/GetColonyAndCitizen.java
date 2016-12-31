package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Get the Colony ID and Citizen ID out of a command.
 */

public class GetColonyAndCitizen
{

    private static final String ONLY_NUMBERS            = "Please only use numbers for the %s ID!";
    private static final String TOO_MANY_ARGUMENTS      = "Too many arguments!";
    private static final String UNKNOWN_ERROR           = "Unknown Error!";
    private static final String NOT_FOUND               = "%s not found!";
    private static final String NO_COLONY               = "You haven't got a colony!";
    private static final int    ONLY_NUMBERS_CODE       = -1;
    private static final int    TOO_MANY_ARGUMENTS_CODE = -2;
    private static final int    UNKNOWN_ERROR_CODE      = -3;
    private static final int    NOT_FOUND_CODE          = -4;
    private static final int    NO_COLONY_CODE          = -5;

    private GetColonyAndCitizen()
    {
        throw new IllegalAccessError("Utility Class");
    }

    /**
     * Getting the colony ID.
     */
    public static int getColonyId(@NotNull final ICommandSender sender, @NotNull final String... args)
    {
        int colonyId;
        final UUID mayorID = sender.getCommandSenderEntity().getUniqueID();
        if (args.length == 2 || args.length == 4)
        {
            try
            {
                colonyId = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                colonyId = ONLY_NUMBERS_CODE;
            }
        }
        else if (args.length == 1 || args.length == 3)
        {
            if (ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID) == null)
            {
                colonyId = NO_COLONY_CODE;
            }
            else
            {
                colonyId = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID).getID();
            }
        }
        else if (args.length >= 5)
        {
            colonyId = TOO_MANY_ARGUMENTS_CODE;
        }
        else
        {
            colonyId = UNKNOWN_ERROR_CODE;
        }
        if (colonyId >= 0)
        {
            if (ColonyManager.getColony(colonyId) == null)
            {
                colonyId = NOT_FOUND_CODE;
            }
        }


        return colonyId;
    }

    /**
     * Getting the citizen ID.
     * @param colonyId
     * @param args
     * @return
     */
    public static int getCitizenId(@NotNull final int colonyId, @NotNull final String... args)
    {
        int citizenId;
        String citizenName;
        citizenId = NOT_FOUND_CODE;
        if (args.length == 2)
        {
            try
            {
                citizenId = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                citizenId = ONLY_NUMBERS_CODE;
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
                citizenId = ONLY_NUMBERS_CODE;
            }
        }
        else if (args.length == 3 && colonyId >= 0)
        {
            citizenName = args[0] + " " + args[1] + " " + args[2];
            for (int i = 1
                   ; i <= ColonyManager.getColony(colonyId).getCitizens().size(); i++)
            {
                if (ColonyManager.getColony(colonyId).getCitizen(i).getName() != null)
                {
                    if (ColonyManager.getColony(colonyId).getCitizen(i).getName().equals(citizenName))
                    {
                        citizenId = i;
                    }
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
            citizenId = TOO_MANY_ARGUMENTS_CODE;
        }
        else
        {
            citizenId = UNKNOWN_ERROR_CODE;
        }
        if (citizenId >= 0 && colonyId >= 0)
        {
            if (ColonyManager.getColony(colonyId).getCitizen(citizenId) == null)
            {
                citizenId = NOT_FOUND_CODE;
            }
        }
        return citizenId;
    }

    /**
     * Getting any errors.
     * @param colonyId
     * @param citizenId
     * @return
     */
    public static String getErrors(@NotNull final int colonyId, @NotNull final int citizenId)
    {
        String errorMessage = null;
        if (colonyId >= 0)
        {
            errorMessage = null;
        }
        else if (colonyId == ONLY_NUMBERS_CODE)
        {
            errorMessage = String.format(ONLY_NUMBERS, "colony");
        }
        else if (colonyId == TOO_MANY_ARGUMENTS_CODE)
        {
            errorMessage = TOO_MANY_ARGUMENTS;
        }
        else if (colonyId == UNKNOWN_ERROR_CODE)
        {
            errorMessage = UNKNOWN_ERROR;
        }
        else if (colonyId == NOT_FOUND_CODE)
        {
            errorMessage = String.format(NOT_FOUND, "Colony");
        }
        else if (colonyId == -NO_COLONY_CODE)
        {
            errorMessage = NO_COLONY;
        }

        if (citizenId >= 0 && errorMessage == null)
        {
            errorMessage = null;
        }
        else if (citizenId == ONLY_NUMBERS_CODE && errorMessage == null)
        {
            errorMessage = String.format(ONLY_NUMBERS, "citizen");
        }
        else if (citizenId == TOO_MANY_ARGUMENTS_CODE && errorMessage == null)
        {
            errorMessage = TOO_MANY_ARGUMENTS;
        }
        else if (citizenId == UNKNOWN_ERROR_CODE && errorMessage == null)
        {
            errorMessage = UNKNOWN_ERROR;
        }
        else if (citizenId == NOT_FOUND_CODE && errorMessage == null)
        {
            errorMessage = String.format(NOT_FOUND, "Citizen");
        }

        return errorMessage;
    }
}
