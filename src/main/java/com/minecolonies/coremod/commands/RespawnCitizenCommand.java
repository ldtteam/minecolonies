package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.Log;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * List all colonies.
 */
public class RespawnCitizenCommand extends AbstractSingleCommand
{

    public static final  String DESC                            = "respawn";
    private static final String CITIZEN_DESCRIPTION             = "§2ID: §f %d §2 Name: §f %s";
    private static final String REMOVED_MESSAGE                 = "Has been removed";
    private static final String COORDINATES_XYZ                 = "§4x=§f%s §4y=§f%s §4z=§f%s";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RespawnCitizenCommand(@NotNull final String... parents)
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
        int colonyId;
        int citizenId;
        try
        {
            colonyId = GetColonyAndCitizen.getColonyId(sender.getCommandSenderEntity().getUniqueID(), sender.getEntityWorld(), args);
            citizenId = GetColonyAndCitizen.getCitizenId(colonyId, args);
        }
        catch (IllegalArgumentException e)
        {
            sender.sendMessage(new TextComponentString(e.getMessage()));
            return;
        }
            final Colony colony = ColonyManager.getColony(colonyId);
            final CitizenData citizenData = colony.getCitizen(citizenId);
            final EntityCitizen entityCitizen = citizenData.getCitizenEntity();
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION, citizenData.getId(), citizenData.getName())));
            final BlockPos position = entityCitizen.getPosition();
            sender.sendMessage(new TextComponentString(String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));

            sender.sendMessage(new TextComponentString(REMOVED_MESSAGE));

            Log.getLogger().info("client? " + sender.getEntityWorld().isRemote);
            server.addScheduledTask(entityCitizen::setDead);
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
