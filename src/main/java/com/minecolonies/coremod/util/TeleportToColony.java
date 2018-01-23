package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.BuildingTownHall;
import com.minecolonies.coremod.commands.MinecoloniesCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for teleporting players to home or a friendly colony.
 */
public final class TeleportToColony
{
    private static final String CANT_FIND_COLONY = "No Colony found for teleport, please define one.";
    private static final String CANT_FIND_PLAYER = "No player found for teleport, please define one.";
    private static final String NO_TOWNHALL      = "Target colony has no town hall, can't teleport.";

    /**
     * The minimum valid colony id.
     */
    private static final int MIN_COLONY_ID       = 1;

    /**
     * Private constructor to hide the implicit public one.
     */
    private TeleportToColony()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * colonyTeleport is used with Home and Colony to teleport people to either
     * there home. or to another colony, when you specified a colonyID.
     *
     * @param server the server instance.
     * @param sender the player that is initiating the command.
     * @param args   this is the colony ID that the player wishes to TP to.
     */

    @NotNull
    public static void colonyTeleport(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args)
    {
        final EntityPlayer playerToTeleport;
        final IColony colony;
        final int colonyId;
        final Entity senderEntity = sender.getCommandSenderEntity();
        //see if sent by a player and grab their name and Get the players Colony ID that sent the command
        if (senderEntity instanceof EntityPlayer)
        {
            //if no args then this is a home colony TP and we get the players Colony ID
            if (args.length == 0)
            {
                playerToTeleport = (EntityPlayer) sender;
                colony = ColonyManager.getIColonyByOwner(((EntityPlayer) sender).world, (EntityPlayer) sender);

                if(colony == null)
                {
                    return;
                }
                colonyId = colony.getID();
            }
            else
            {
                //if there is args then this will be to a friends colony TP and we use the Colony ID they specify
                //will need to see if they friendly to destination colony
                playerToTeleport = (EntityPlayer) sender;
                colonyId = Integer.valueOf(args[0]);
            }
        }
        else
        {
            sender.sendMessage(new TextComponentString(CANT_FIND_PLAYER));
            return;
        }

        if (MinecoloniesCommand.canExecuteCommand((EntityPlayer) sender))
        {
            teleportPlayer(playerToTeleport, colonyId, sender);
            return;
        }
        sender.getCommandSenderEntity().sendMessage(new TextComponentString("Please wait at least " + Configurations.gameplay.teleportBuffer + " seconds to teleport again"));
    }

    /**
     * Method used to teleport the player.
     *
     * @param colID            the senders colony ID.
     * @param playerToTeleport the player which shall be teleported.
     */
    private static void teleportPlayer(final EntityPlayer playerToTeleport, final int colID, final ICommandSender sender)
    {
        final Colony colony = ColonyManager.getColony(colID);
        final BuildingTownHall townHall = colony.getBuildingManager().getTownHall();

        if (townHall == null)
        {
            sender.sendMessage(new TextComponentString(NO_TOWNHALL));
            return;
        }

        playerToTeleport.sendMessage(new TextComponentString("We got places to go, kid..."));

        final BlockPos position = townHall.getLocation();

        final int dimension = playerToTeleport.getEntityWorld().provider.getDimension();
        final int colonyDimension = townHall.getColony().getDimension();

        if (dimension != colonyDimension)
        {
            playerToTeleport.sendMessage(new TextComponentString("Buckle up buttercup, this ain't no joy ride!!!"));
            final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) sender;
            final MinecraftServer server = sender.getEntityWorld().getMinecraftServer();
            final WorldServer worldServer = server.getWorld(colonyDimension);

            playerToTeleport.sendMessage(new TextComponentString("Hold onto your pants, we're going Inter-Dimensional!"));
            playerToTeleport.changeDimension(colonyDimension);

            worldServer.getMinecraftServer().getPlayerList()
                    .transferPlayerToDimension(entityPlayerMP, colonyDimension, new Teleporter(worldServer));
            playerToTeleport.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
            if (dimension == 1)
            {
                playerToTeleport.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
                worldServer.spawnEntity(playerToTeleport);
                worldServer.updateEntityWithOptionalForce(playerToTeleport, false);
            }
        }

        if (colID >= MIN_COLONY_ID)
        {
            playerToTeleport.setPositionAndUpdate(position.getX(), position.getY() + 2.0, position.getZ());
        }
        else
        {
            playerToTeleport.sendMessage(new TextComponentString(CANT_FIND_COLONY));
        }
    }
}


