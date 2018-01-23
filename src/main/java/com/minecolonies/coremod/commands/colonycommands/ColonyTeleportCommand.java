package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.util.TeleportToColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.COLONYTP;

/**
 * this command is made to TP a player to a friends colony.
 */
public final class ColonyTeleportCommand extends AbstractSingleCommand
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
    public boolean canRankUseCommand(@NotNull final Colony colony, @NotNull final EntityPlayer player)
    {
        return colony.getPermissions().hasPermission(player, Action.TELEPORT_TO_COLONY);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        //see if player is allowed to use in the configs
        if (sender instanceof EntityPlayer && args.length == 1)
        {
            final int colonyID = getIthArgument(args, 0, -1);
            if (colonyID != -1 && canPlayerUseCommand((EntityPlayer) sender, COLONYTP, colonyID))
            {
                final Colony colonyIn = ColonyManager.getColony(((EntityPlayer) sender).world, sender.getPosition());
                if (isPlayerOpped(sender)
                        || (colonyIn != null
                        && colonyIn.hasTownHall()
                        && colonyIn.getPermissions().hasPermission((EntityPlayer) sender, Action.TELEPORT_TO_COLONY)
                        && ((EntityPlayer) sender).getDistanceSq(colonyIn.getBuildingManager().getTownHall().getLocation()) < MIN_DISTANCE_TO_TH))
                {
                    TeleportToColony.colonyTeleport(server, sender, args);
                }
                return;
            }
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



