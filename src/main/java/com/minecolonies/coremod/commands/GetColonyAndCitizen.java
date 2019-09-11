package com.minecolonies.coremod.commands;

import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Get the Colony ID and Citizen ID out of a command.
 */

public final class GetColonyAndCitizen
{

    private static final String ONLY_NUMBERS                = "Please only use numbers for the %s ID!";
    private static final String TOO_MANY_ARGUMENTS          = "Too many arguments!";
    private static final String UNKNOWN_ERROR               = "Unknown Error!";
    private static final String NOT_FOUND                   = "%s not found!";
    private static final String NO_COLONY                   = "You haven't got a colony!";
    private static final int    SHORT_ARGUMENT_LENGTH       = 1;
    private static final int    NORMAL_ARGUMENT_LENGTH      = 2;
    private static final int    NAME_ARGUMENT_LENGTH        = 3;
    private static final int    ID_AND_NAME_ARGUMENT_LENGTH = 4;
    private static final int    TOO_MANY_ARGUMENTS_LENGTH   = 5;
    private static final int    ARGUMENT_ZERO               = 0;
    private static final int    ARGUMENT_ONE                = 1;
    private static final int    ARGUMENT_TWO                = 2;
    private static final int    ARGUMENT_THREE              = 3;
    private static final int    STANDARD_CITIZEN_ID         = 0;

    private GetColonyAndCitizen()
    {
        throw new IllegalAccessError("Utility Class");
    }

    /**
     * Getting the colony ID.
     *
     * @param mayorID The ID of the mayor.
     * @param world   The world.
     * @param args    The arguments.
     * @return Return colony ID.
     */
    public static int getColonyId(@NotNull final UUID mayorID, @NotNull final World world, @NotNull final String... args)
    {
        final int colonyId;
        if (args.length == NORMAL_ARGUMENT_LENGTH || args.length == ID_AND_NAME_ARGUMENT_LENGTH)
        {
            try
            {
                colonyId = Integer.parseInt(args[ARGUMENT_ZERO]);
            }
            catch (final NumberFormatException e)
            {
                throw new IllegalArgumentException(String.format(ONLY_NUMBERS, "colony"));
            }
        }
        else if (args.length == SHORT_ARGUMENT_LENGTH || args.length == NAME_ARGUMENT_LENGTH)
        {
            if (IColonyManager.getInstance().getIColonyByOwner(world, mayorID) == null)
            {
                throw new IllegalArgumentException(NO_COLONY);
            }
            else
            {
                colonyId = IColonyManager.getInstance().getIColonyByOwner(world, mayorID).getID();
            }
        }
        else if (args.length >= TOO_MANY_ARGUMENTS_LENGTH)
        {
            throw new IllegalArgumentException(TOO_MANY_ARGUMENTS);
        }
        else
        {
            throw new IllegalArgumentException(UNKNOWN_ERROR);
        }
        if (colonyId >= 0 && IColonyManager.getInstance().getColonyByWorld(colonyId, world) == null)
        {
            throw new IllegalArgumentException(String.format(NOT_FOUND, "Colony"));
        }


        return colonyId;
    }

    /**
     * Getting the citizen ID.
     *
     * @param colonyId The colony ID for getting the citizen ID
     * @param args     The given arguments
     * @return Returns citizen ID
     */
    public static int getCitizenId(@NotNull final int colonyId, @NotNull final String... args)
    {
        final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        int citizenId;
        final String citizenName;
        citizenId = STANDARD_CITIZEN_ID;
        if (args.length == NORMAL_ARGUMENT_LENGTH)
        {
            try
            {
                citizenId = Integer.parseInt(args[ARGUMENT_ONE]);
            }
            catch (final NumberFormatException e)
            {
                throw new IllegalArgumentException(String.format(ONLY_NUMBERS, "citizen"));
            }
        }
        else if (args.length == SHORT_ARGUMENT_LENGTH)
        {
            try
            {
                citizenId = Integer.parseInt(args[ARGUMENT_ZERO]);
            }
            catch (final NumberFormatException e)
            {
                throw new IllegalArgumentException(String.format(ONLY_NUMBERS, "citizen"));
            }
        }
        else if (args.length == NAME_ARGUMENT_LENGTH && colonyId >= 0)
        {
            citizenName = args[ARGUMENT_ZERO] + " " + args[ARGUMENT_ONE] + " " + args[ARGUMENT_TWO];
            for (int i = 1
                   ; i <= IColonyManager.getInstance().getColonyByWorld(colonyId, world).getCitizenManager().getCitizens().size(); i++)
            {
                if (IColonyManager.getInstance().getColonyByWorld(colonyId, world).getCitizenManager().getCitizen(i).getName() != null
                        && IColonyManager.getInstance().getColonyByWorld(colonyId, world).getCitizenManager().getCitizen(i).getName().equals(citizenName))
                {
                    citizenId = i;
                }
            }
        }
        else if (args.length == ID_AND_NAME_ARGUMENT_LENGTH && colonyId >= 0)
        {
            citizenName = args[ARGUMENT_ONE] + " " + args[ARGUMENT_TWO] + " " + args[ARGUMENT_THREE];
            for (int i = 1; i <= IColonyManager.getInstance().getColonyByWorld(colonyId, world).getCitizenManager().getCitizens().size(); i++)
            {
                if (IColonyManager.getInstance().getColonyByWorld(colonyId, world).getCitizenManager().getCitizen(i).getName().equals(citizenName))
                {
                    citizenId = i;
                }
            }
        }
        else if (args.length >= TOO_MANY_ARGUMENTS_LENGTH)
        {
            throw new IllegalArgumentException(TOO_MANY_ARGUMENTS);
        }
        else
        {
            throw new IllegalArgumentException(UNKNOWN_ERROR);
        }
        if (citizenId >= 0 && colonyId >= 0 && IColonyManager.getInstance().getColonyByWorld(colonyId, world).getCitizenManager().getCitizen(citizenId) == null)
        {
            throw new IllegalArgumentException(String.format(NOT_FOUND, "Citizen"));
        }
        return citizenId;
    }
}

