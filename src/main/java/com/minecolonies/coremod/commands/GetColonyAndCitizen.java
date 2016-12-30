package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Get the Colony ID and Citizen ID out of a command
 */
public abstract class GetColonyAndCitizen
{
    public static int ColonyId;
    public static int CitizenId;

    public static int getColonyId(@NotNull final ICommandSender sender, @NotNull final String... args)
    {
        final UUID mayorID = sender.getCommandSenderEntity().getUniqueID();
        if (args.length == 2)
        {
            try
            {
                ColonyId = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                ColonyId = -1;
            }
        }
        else if (args.length == 1)
        {
            ColonyId = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID).getID();
        }
        else
        {
            ColonyId = -1;
        }


        return ColonyId;
    }

    public static int getCitizenId(@NotNull final ICommandSender sender, @NotNull final String... args)
    {
        if (args.length == 2)
        {
            CitizenId = Integer.parseInt(args[1]);
        }
        else if (args.length == 1)
        {
            CitizenId = Integer.parseInt(args[0]);
        }
        else
        {
            CitizenId = -1;
        }
        return CitizenId;
    }
}
