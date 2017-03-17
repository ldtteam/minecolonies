package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for teleporting players to home or a friendly colony.
 */
public final class TeleportToColony
{
    private static final String CANT_FIND_COLONY = "No Colony found for teleport, please define one.";
    private static final String CANT_FIND_PLAYER = "No player found for teleport, please define one.";

    /**
     * Private constructor to hide the implicit public one.
     */
    private TeleportToColony(@NotNull final String... parents)
    {

    }

    /**
     * colonyTeleport is used with Home and Colony to teleport people to either there home
     * or to another colony, when you specified a colonyID
     * @param server the server instance.
     * @param sender the player that is initiating the command.
     * @param args this is the colony ID that the player wishes to TP to.
     */

    @NotNull
    public static void colonyTeleport(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args)
    {
        EntityPlayer playerToTeleport;
        IColony colony;
        int colonyId;
        //see if sent by a player and grab their name and Get the players Colony ID that sent the command
        if (sender instanceof EntityPlayer)
        {
            //if no args then this is a home colony TP and we get the players Colony ID
            if ("99999".equals(args[0]))
            {
                playerToTeleport = (EntityPlayer) sender;
                colony = ColonyManager.getIColonyByOwner(((EntityPlayer) sender).worldObj, (EntityPlayer) sender);
                colonyId = colony.getID();
            }
            else
            {
                //if there is args then this will be to a friends colony TP and we use the Colony ID they specify
                //will need to see if they friendly to destination colony
                playerToTeleport = (EntityPlayer) sender;
                colonyId =  Integer.valueOf(args[0]);
            }
        }
        else
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(CANT_FIND_PLAYER));
            return;
        }
            playerToTeleport.getCommandSenderEntity().addChatMessage(new TextComponentString("We got places to go, kid..."));
            teleportPlayer(playerToTeleport, colonyId);
    }

    /**
     * Method used to teleport the player.
     *
     * @param colID           the senders colony ID.
     * @param playerToTeleport the player which shall be teleported.
     */
    private static void teleportPlayer(final EntityPlayer playerToTeleport,int colID)
    {
        final Colony colony = ColonyManager.getColony(colID);
        final BlockPos position = colony.getCenter();

        if (colID >= 1)
        {
            playerToTeleport.setPositionAndUpdate(position.getX(),position.getY()+2.0,position.getZ());
        }
        else
        {
            playerToTeleport.getCommandSenderEntity().addChatMessage(new TextComponentString(CANT_FIND_COLONY));
        }
    }
}


