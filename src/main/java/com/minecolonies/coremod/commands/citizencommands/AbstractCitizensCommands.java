package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Parent class for all citizen related commands, contains code which is the same for all commands relating citizens.
 */
public abstract class AbstractCitizensCommands extends AbstractSingleCommand
{
    private static final String NO_ARGUMENTS                    = "Please define a valid citizen and/or colony";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AbstractCitizensCommands(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <CitizenId>";
    }

    @NotNull
    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if(args.length == 0)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
            return;
        }

        boolean firstArgumentColonyId = true;
        int colonyId = -1;
        if(args.length >= 2)
        {
            colonyId = getIthArgument(args, 0, -1);
            if(colonyId == -1)
            {
                final EntityPlayer player = server.getEntityWorld().getPlayerEntityByName(args[0]);
                if(player != null)
                {
                    IColony tempColony = ColonyManager.getIColonyByOwner(server.getEntityWorld(), player);
                    if(tempColony != null)
                    {
                        colonyId = tempColony.getID();
                    }
                }

                firstArgumentColonyId = false;
            }
        }

        final Colony colony;
        if(sender instanceof EntityPlayer && colonyId == -1)
        {
            final IColony tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), (EntityPlayer) sender);
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
                firstArgumentColonyId = false;
            }
        }

        colony = ColonyManager.getColony(colonyId);

        if(colony == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
            return;
        }

        if(sender instanceof EntityPlayer)
        {
            final EntityPlayer player = (EntityPlayer) sender;
            if (!canPlayerUseCommand(player, getCommand(), colonyId))
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        final int citizenId = getValidCitizenId(colony, firstArgumentColonyId, args);

        if(citizenId == -1 || colony.getCitizen(citizenId) == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
            return;
        }

        executeSpecializedCode(server, sender, colony, citizenId);
    }

    /**
     * Get a valid citizenid from the arguments.
     * @param colony the colony.
     * @param firstArgumentColonyId to define the offset.
     * @param args the arguments.
     * @return the valid id or -1 if not found.
     */
    private static int getValidCitizenId(final Colony colony, final boolean firstArgumentColonyId, final String...args)
    {
        int offset = 0;
        if(firstArgumentColonyId)
        {
            offset = 1;
        }

        final int citizenId = getIthArgument(args, offset, -1);
        if(citizenId == -1)
        {
            if(args.length >= offset + 2)
            {
                final String citizenName = args[offset] + " " + args[offset + 1] + " " + args[offset + 2];
                for (int i = 1; i <= colony.getCitizens().size(); i++)
                {
                    if (colony.getCitizen(i).getName().equals(citizenName))
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
     * Citizen commands have to overwrite this to handle their specialized code.
     * @param colonyId  the id for the colony
     * @param citizenId  the id for the citizen
     */
    abstract void executeSpecializedCode(@NotNull final MinecraftServer server, final ICommandSender sender, final Colony colonyId, final int citizenId);

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

    /**
     * Returns the command enum describing this command.
     * @return the command.
     */
    abstract Commands getCommand();
}
