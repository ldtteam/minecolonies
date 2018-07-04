package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.RESPAWNCITIZENS;

/**
 * List all colonies.
 */
public class RespawnCitizenCommand extends AbstractCitizensCommands
{

    public static final  String DESC                = "respawn";
    private static final String CITIZEN_DESCRIPTION = "§2ID: §f %d §2 Name: §f %s";
    private static final String REMOVED_MESSAGE     = "Has been removed";
    private static final String COORDINATES_XYZ     = "§4x=§f%s §4y=§f%s §4z=§f%s";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public RespawnCitizenCommand()
    {
        super();
    }

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
    public void executeSpecializedCode(@NotNull final MinecraftServer server, final ICommandSender sender, final Colony colony, final int citizenId)
    {
        final CitizenData citizenData = colony.getCitizenManager().getCitizen(citizenId);
        final Optional<EntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();

        sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION, citizenData.getId(), citizenData.getName())));
        if (!optionalEntityCitizen.isPresent())
        {
            citizenData.updateCitizenEntityIfNecessary();
        }

        final EntityCitizen entityCitizen = citizenData.getCitizenEntity().get();

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

    @Override
    public Commands getCommand()
    {
        return RESPAWNCITIZENS;
    }
}
