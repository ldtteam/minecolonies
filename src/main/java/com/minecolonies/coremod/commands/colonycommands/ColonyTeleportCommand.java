package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.util.TeleportToColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
    public boolean canRankUseCommand(@NotNull final IColony colony, @NotNull final EntityPlayer player)
    {
        return colony.getPermissions().hasPermission(player, Action.TELEPORT_TO_COLONY);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        IColony colony = actionMenuState.getColonyForArgument("colony");
        if (null == colony)
        {
            final EntityPlayer player = actionMenuState.getPlayerForArgument("player");
            if (player != null)
            {
                IColony iColony = IColonyManager.getInstance().getIColonyByOwner(server.getEntityWorld(), player);
                if (null == iColony)
                {
                    if (sender instanceof EntityPlayer)
                    {
                        final Entity senderEntity = sender.getCommandSenderEntity();
                        if (senderEntity != null)
                        {
                            final UUID mayorID = senderEntity.getUniqueID();
                            if (iColony == null)
                            {
                                iColony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), mayorID);
                            }
                        }
                    }
                }

                if (null != iColony)
                {
                    colony = IColonyManager.getInstance().getColonyByWorld(iColony.getID(), server.getWorld(sender.getEntityWorld().provider.getDimension()));
                }
            }
        }

        // Required argument: would never be null at this point.
        if (null == colony)
        {
            sender.sendMessage(new TextComponentString("You are not allowed to do this"));
            return;
        }

        executeShared(server, sender, colony);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        IColony colony = null;
        //see if player is allowed to use in the configs
        if (args.length == 1)
        {
            final int colonyId = getColonyIdFromArg(args, 0, -1);
            final int dimensionId = getDimensionIdFromArg(args, 0, 0);
            if (colonyId != -1) colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(dimensionId));
        }

        if (null == colony)
        {
            sender.sendMessage(new TextComponentString("You are not allowed to do this"));
            return;
        }

        executeShared(server, sender, colony);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final IColony colony)
    {
        //see if player is allowed to use in the configs
        if ((sender instanceof EntityPlayer) && canPlayerUseCommand((EntityPlayer) sender, COLONYTP, colony.getID()))
        {
            final IColony colonyIn = IColonyManager.getInstance().getColonyByPosFromWorld(server.getWorld(colony.getDimension()), sender.getPosition());
            if (isPlayerOpped(sender)
                    || (colonyIn != null
                    && colonyIn.hasTownHall()
                    && colonyIn.getPermissions().hasPermission((EntityPlayer) sender, Action.TELEPORT_TO_COLONY)
                    && ((EntityPlayer) sender).getDistanceSq(colonyIn.getBuildingManager().getTownHall().getPosition()) < MIN_DISTANCE_TO_TH))
            {
                TeleportToColony.colonyTeleport(server, sender, colony.getDimension() + "|" + colony.getID());
            }
            return;
        }
        sender.sendMessage(new TextComponentString("You are not allowed to do this"));
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



