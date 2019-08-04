package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Parent class for all citizen related commands, contains code which is the same for all commands relating citizens.
 */
public abstract class AbstractCitizensCommands extends AbstractSingleCommand implements IActionCommand
{
    private static final String NO_ARGUMENTS = "Please define a valid citizen and/or colony";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AbstractCitizensCommands(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final IColony colony = actionMenuState.getColonyForArgument("colony");
        if (colony == null)
        {
            sender.sendMessage(new StringTextComponent(NO_ARGUMENTS));
            return;
        }

        final ICitizenData citizenData = actionMenuState.getCitizenForArgument("citizen");
        if (null == citizenData && requiresCitizen())
        {
            sender.sendMessage(new StringTextComponent(NO_ARGUMENTS));
            return;
        }

        final int citizenId = citizenData == null ? -1 : citizenData.getId();
        executeSpecializedCode(server, sender, colony, citizenId);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final CommandSource sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <CitizenId>";
    }

    @NotNull
    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String... args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.sendMessage(new StringTextComponent(NO_ARGUMENTS));
            return;
        }

        boolean firstArgumentColonyId = true;
        int colonyId = -1;
        if (args.length >= 2)
        {
            colonyId = getIthArgument(args, 0, -1);
            if (colonyId == -1)
            {
                final PlayerEntity player = server.getEntityWorld().getPlayerEntityByName(args[0]);
                if (player != null)
                {
                    final IColony tempColony = IColonyManager.getInstance().getIColonyByOwner(server.getEntityWorld(), player);
                    if (tempColony != null)
                    {
                        colonyId = tempColony.getID();
                    }
                }

                firstArgumentColonyId = false;
            }
        }

        final IColony colony;
        if (sender instanceof PlayerEntity && colonyId == -1)
        {
            final IColony tempColony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), (PlayerEntity) sender);
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
                firstArgumentColonyId = false;
            }
        }

        colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().world.getDimension().getType().getId()));

        if (colony == null)
        {
            sender.sendMessage(new StringTextComponent(NO_ARGUMENTS));
            return;
        }

        if (sender instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) sender;
            if (!canPlayerUseCommand(player, getCommand(), colonyId))
            {
                sender.sendMessage(new StringTextComponent(NOT_PERMITTED));
                return;
            }
        }

        int citizenId = -1;
        if (requiresCitizen())
        {
            citizenId = getValidCitizenId(colony, firstArgumentColonyId, args);

            if ((citizenId == -1 || colony.getCitizenManager().getCitizen(citizenId) == null) && requiresCitizen())
            {
                sender.sendMessage(new StringTextComponent(NO_ARGUMENTS));
                return;
            }
        }

        executeSpecializedCode(server, sender, colony, citizenId);
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final CommandSource sender,
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

    /**
     * Returns the command enum describing this command.
     *
     * @return the command.
     */
    public abstract Commands getCommand();

    /**
     * Get a valid citizenid from the arguments.
     *
     * @param colony                the colony.
     * @param firstArgumentColonyId to define the offset.
     * @param args                  the arguments.
     * @return the valid id or -1 if not found.
     */
    private static int getValidCitizenId(final IColony colony, final boolean firstArgumentColonyId, final String... args)
    {
        int offset = 0;
        if (firstArgumentColonyId)
        {
            offset = 1;
        }

        final int citizenId = getIthArgument(args, offset, -1);
        if (citizenId == -1)
        {
            if (args.length >= offset + 2)
            {
                final String citizenName = args[offset] + " " + args[offset + 1] + " " + args[offset + 2];
                for (int i = 1; i <= colony.getCitizenManager().getCitizens().size(); i++)
                {
                    if (colony.getCitizenManager().getCitizen(i).getName().equals(citizenName))
                    {
                        return i;
                    }
                }
            }

            return citizenId;
        }
        return citizenId;
    }

    /**
     * Indicates if this command requires a citizen.
     * @return True for yes, false for optional or no.
     */
    protected boolean requiresCitizen()
    {
        return true;
    }

    /**
     * Citizen commands have to overwrite this to handle their specialized code.
     *
     * @param server    the minecraft server.
     * @param sender    the command sender.
     * @param colonyId  the id for the colony
     * @param citizenId the id for the citizen
     */
    public abstract void executeSpecializedCode(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final IColony colonyId, final int citizenId);
}
