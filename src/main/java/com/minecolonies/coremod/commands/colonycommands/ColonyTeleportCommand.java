package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.util.TeleportToColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.COLONYTP;

/**
 * this command is made to TP a player to a friends colony.
 */
public final class ColonyTeleportCommand extends AbstractSingleCommand implements IActionCommand
{
    /**
     * The description.
     */
    public static final String DESC             = "teleport";

    /**
     * Min distance to townhall to run teleport command.
     */
    private static final double MIN_DISTANCE_TO_TH = 20;

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ColonyTeleportCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ColonyTeleportCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "colonytp" + "<colID>";
    }

    @Override
    public boolean canRankUseCommand(@NotNull final Colony colony, @NotNull final PlayerEntity player)
    {
        return colony.getPermissions().hasPermission(player, Action.TELEPORT_TO_COLONY);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        Colony colony = actionMenuState.getColonyForArgument("colony");
        if (null == colony)
        {
            final PlayerEntity player = actionMenuState.getPlayerForArgument("player");
            if (player != null)
            {
                IColony iColony = ColonyManager.getIColonyByOwner(server.getEntityWorld(), player);
                if (null == iColony)
                {
                    if (sender instanceof PlayerEntity)
                    {
                        final Entity senderEntity = sender.getCommandSenderEntity();
                        if (senderEntity != null)
                        {
                            final UUID mayorID = senderEntity.getUniqueID();
                            if (iColony == null)
                            {
                                iColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID);
                            }
                        }
                    }
                }

                if (null != iColony)
                {
                    colony = ColonyManager.getColonyByWorld(iColony.getID(), server.getWorld(sender.getEntityWorld().provider.getDimension()));
                }
            }
        }

        // Required argument: would never be null at this point.
        if (null == colony)
        {
            sender.sendMessage(new StringTextComponent("You are not allowed to do this"));
            return;
        }

        executeShared(server, sender, colony);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        Colony colony = null;
        //see if player is allowed to use in the configs
        if (args.length == 1)
        {
            try
            {
                final int colonyId = Integer.parseInt(args[0]);
                colony = ColonyManager.getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().provider.getDimension()));
            }
            catch (final NumberFormatException e)
            {
                // we ignore the exception and deal with a null colony below.
            }
        }

        if (null == colony)
        {
            sender.sendMessage(new StringTextComponent("You are not allowed to do this"));
            return;
        }

        executeShared(server, sender, colony);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final Colony colony)
    {
        //see if player is allowed to use in the configs
        if ((sender instanceof PlayerEntity) && canPlayerUseCommand((PlayerEntity) sender, COLONYTP, colony.getID()))
        {
            final Colony colonyIn = ColonyManager.getColonyByPosFromWorld(server.getWorld(sender.getEntityWorld().provider.getDimension()), sender.getPosition());
            if (isPlayerOpped(sender)
                    || (colonyIn != null
                    && colonyIn.hasTownHall()
                    && colonyIn.getPermissions().hasPermission((PlayerEntity) sender, Action.TELEPORT_TO_COLONY)
                    && ((PlayerEntity) sender).getDistanceSq(colonyIn.getBuildingManager().getTownHall().getLocation()) < MIN_DISTANCE_TO_TH))
            {
                TeleportToColony.colonyTeleport(server, sender, String.valueOf(colony.getID()));
            }
            return;
        }
        sender.sendMessage(new StringTextComponent("You are not allowed to do this"));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return index == 0;
    }
}



