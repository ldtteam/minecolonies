package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.permissions.Permissions;
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
    public static final String DESC = "teleport";

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
        return colony.getPermissions().hasPermission(player, Permissions.Action.TELEPORT_TO_COLONY);
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {
        //see if player is allowed to use in the configs
        if (sender instanceof EntityPlayer && args.length == 1)
        {
            final int colonyID = getIthArgument(args, 0, -1);
            if (colonyID != -1 && canPlayerUseCommand((EntityPlayer) sender, COLONYTP, colonyID))
            {
                TeleportToColony.colonyTeleport(server, sender, args);
                return;
            }
        }
        sender.getCommandSenderEntity().sendMessage(new TextComponentString("You are not allowed to do this"));
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
    public boolean isUsernameIndex(@NotNull String[] args, int index)
    {
        return index == 0;
    }
}



