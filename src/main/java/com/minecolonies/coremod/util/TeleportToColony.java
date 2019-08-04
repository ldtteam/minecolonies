package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.commands.MinecoloniesCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for teleporting players to home or a friendly colony.
 */
public final class TeleportToColony
{
    private static final String CANT_FIND_COLONY = "No Colony found for teleport, please define one.";
    private static final String CANT_FIND_PLAYER = "No player found for teleport, please define one.";
    private static final String NO_TOWNHALL      = "Target colony has no town hall, can't teleport.";
    private static final int    SOUND_TYPE       = 1032;
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
        final PlayerEntity playerToTeleport;
        final IColony colony;
        final int colonyId;
        final Entity senderEntity = sender.getCommandSenderEntity();
        //see if sent by a player and grab their name and Get the players Colony ID that sent the command
        if (senderEntity instanceof PlayerEntity)
        {
            //if no args then this is a home colony TP and we get the players Colony ID
            if (args.length == 0)
            {
                playerToTeleport = (PlayerEntity) sender;
                colony = IColonyManager.getInstance().getIColonyByOwner(((PlayerEntity) sender).world, (PlayerEntity) sender);

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
                playerToTeleport = (PlayerEntity) sender;
                colonyId = Integer.valueOf(args[0]);
            }
        }
        else
        {
            sender.sendMessage(new StringTextComponent(CANT_FIND_PLAYER));
            return;
        }

        if (MinecoloniesCommand.canExecuteCommand((PlayerEntity) sender))
        {
            teleportPlayer(playerToTeleport, colonyId, sender);
            return;
        }
        sender.sendMessage(new StringTextComponent("Please wait at least " + Configurations.gameplay.teleportBuffer + " seconds to teleport again"));
    }

    /**
     * Method used to teleport the player.
     *
     * @param colID            the senders colony ID.
     * @param playerToTeleport the player which shall be teleported.
     */
    private static void teleportPlayer(final PlayerEntity playerToTeleport, final int colID, final ICommandSender sender)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colID, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
        final ITownHall townHall = colony.getBuildingManager().getTownHall();

        if (townHall == null)
        {
            sender.sendMessage(new StringTextComponent(NO_TOWNHALL));
            return;
        }

        final BlockPos position = townHall.getPosition();

        final int dimension = playerToTeleport.getEntityWorld().world.getDimension().getType().getId();
        final int colonyDimension = townHall.getColony().getDimension();

        if (colID < MIN_COLONY_ID)
        {
            sender.sendMessage(new StringTextComponent(CANT_FIND_COLONY));
            return;
        }

        final ServerPlayerEntity ServerPlayerEntity = (ServerPlayerEntity) sender;
        if (dimension != colonyDimension)
        {
            playerToTeleport.sendMessage(new StringTextComponent("We got places to go, kid..."));
            playerToTeleport.sendMessage(new StringTextComponent("Buckle up buttercup, this ain't no joy ride!!!"));
            final MinecraftServer server = sender.getEntityWorld().getMinecraftServer();
            final WorldServer worldServer = server.getWorld(colonyDimension);

            // Vanilla does that as well.
            ServerPlayerEntity.connection.sendPacket(new SPacketEffect(SOUND_TYPE, BlockPos.ORIGIN, 0, false));
            ServerPlayerEntity.addExperience(0);
            ServerPlayerEntity.setPlayerHealthUpdated();

            playerToTeleport.sendMessage(new StringTextComponent("Hold onto your pants, we're going Inter-Dimensional!"));

            worldServer.getMinecraftServer().getPlayerList()
                    .transferPlayerToDimension(ServerPlayerEntity, colonyDimension, new Teleporter(worldServer));
            if (dimension == 1)
            {
                worldServer.addEntity(playerToTeleport);
                worldServer.updateEntityWithOptionalForce(playerToTeleport, false);
            }
        }
        ServerPlayerEntity.connection.setPlayerLocation(position.getX(), position.getY() + 2.0, position.getZ(), ServerPlayerEntity.rotationYaw, ServerPlayerEntity.rotationPitch);
    }
}


