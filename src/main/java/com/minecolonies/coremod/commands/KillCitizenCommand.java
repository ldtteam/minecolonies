package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * List all colonies.
 */
public class KillCitizenCommand extends AbstractSingleCommand
{

    public static final  String       DESC                            = "kill";
    private static final String       CITIZEN_DESCRIPTION             = "§2ID: §f %d §2 Name: §f %s";
    private static final String       REMOVED_MESSAGE                 = "Has been removed";
    private static final String       NO_COLONY_CITIZEN_FOUND_MESSAGE = "No citizen %d found in colony %d.";
    private static final String       COORDINATES_XYZ                 = "§4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_DATA_NULL               = "Couldn't find citizen client side representation of %d in %d";
    private static final String       ENTITY_CITIZEN_NULL             = "Couldn't find entity of %d in %d";
    private static final String       COLONY_NULL                     = "Couldn't find colony %d";
    /**
     * The damage source used to kill citizens.
     */
    private static final DamageSource CONSOLE_DAMAGE_SOURCE           = new DamageSource("Console");

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public KillCitizenCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <CitizenId>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final int colonyId = getIthArgument(args, 0, -1);
        final int citizenId = getIthArgument(args, 1, -1);
        //todo add this in a feature update when we added argument parsing and permission handling.
        /*if(colonyId == -1)
        {
            colonyId = getColonyId(sender);
        }*/

        //No citizen or citizen defined.
        if (colonyId == -1 || citizenId == -1)
        {
            sender.sendMessage(new TextComponentString(String.format(NO_COLONY_CITIZEN_FOUND_MESSAGE, citizenId, colonyId)));
            return;
        }

        //Wasn't able to get the citizen from the colony.
        final Colony colony = ColonyManager.getColony(colonyId);
        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(String.format(COLONY_NULL, colonyId)));
            return;
        }

        final CitizenData citizenData = colony.getCitizen(citizenId);
        if (citizenData == null)
        {
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_DATA_NULL, citizenId, colonyId)));
            return;
        }

        //Wasn't able to get the entity from the citizenData.
        final EntityCitizen entityCitizen = citizenData.getCitizenEntity();
        if (entityCitizen == null)
        {
            sender.sendMessage(new TextComponentString(String.format(ENTITY_CITIZEN_NULL, citizenId, colonyId)));
            return;
        }

        sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION, entityCitizen.getEntityId(), entityCitizen.getName())));
        final BlockPos position = entityCitizen.getPosition();
        sender.sendMessage(new TextComponentString(String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));
        sender.sendMessage(new TextComponentString(REMOVED_MESSAGE));

        server.addScheduledTask(() -> entityCitizen.onDeath(CONSOLE_DAMAGE_SOURCE));
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
        return false;
    }
}
