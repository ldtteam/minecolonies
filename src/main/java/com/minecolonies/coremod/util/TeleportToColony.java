package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.MinecoloniesCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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
    public static void colonyTeleport(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String... args)
    {
        final PlayerEntity playerToTeleport;
        final IColony colony;
        final int colonyId;
        final Entity senderEntity = sender.getEntity();
        //see if sent by a player and grab their name and Get the players Colony ID that sent the command
        if (senderEntity instanceof PlayerEntity)
        {
            //if no args then this is a home colony TP and we get the players Colony ID
            if (args.length == 0)
            {
                playerToTeleport = (PlayerEntity) sender.getEntity();
                colony = IColonyManager.getInstance().getIColonyByOwner(((PlayerEntity) sender.getEntity()).world, (PlayerEntity) sender.getEntity());

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
                playerToTeleport = (PlayerEntity) sender.getEntity();
                colonyId = Integer.valueOf(args[0]);
            }
        }
        else
        {
            sender.getEntity().sendMessage(new StringTextComponent(CANT_FIND_PLAYER));
            return;
        }

        if (MinecoloniesCommand.canExecuteCommand((PlayerEntity) sender.getEntity()))
        {
            teleportPlayer(playerToTeleport, colonyId, sender);
            return;
        }
        sender.getEntity().sendMessage(new StringTextComponent("Please wait at least " + MineColonies.getConfig().getCommon().teleportBuffer.get() + " seconds to teleport again"));
    }

    /**
     * Method used to teleport the player.
     *
     * @param colID            the senders colony ID.
     * @param playerToTeleport the player which shall be teleported.
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private static void teleportPlayer(final PlayerEntity playerToTeleport, final int colID, final CommandSource sender)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colID, ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD));
        final ITownHall townHall = colony.getBuildingManager().getTownHall();

        if (townHall == null)
        {
            sender.getEntity().sendMessage(new StringTextComponent(NO_TOWNHALL));
            return;
        }

        final BlockPos position = townHall.getPosition();

        final int dimension = playerToTeleport.getEntityWorld().getDimension().getType().getId();
        final int colonyDimension = townHall.getColony().getDimension();

        if (colID < MIN_COLONY_ID)
        {
            sender.getEntity().sendMessage(new StringTextComponent(CANT_FIND_COLONY));
            return;
        }

        final ServerPlayerEntity player = (ServerPlayerEntity) sender.getEntity();
        if (dimension != colonyDimension)
        {
            playerToTeleport.sendMessage(new StringTextComponent("We got places to go, kid..."));
            playerToTeleport.sendMessage(new StringTextComponent("Buckle up buttercup, this ain't no joy ride!!!"));
            final MinecraftServer server = sender.getWorld().getServer();
            final ServerWorld worldServer = server.getWorld(DimensionType.getById(colonyDimension));

            ChunkPos chunkpos = new ChunkPos(new BlockPos(position.getX(), position.getY() + 2.0, position.getZ()));
            worldServer.getChunkProvider().func_217228_a(TicketType.POST_TELEPORT, chunkpos, 1, player.getEntityId());
            player.stopRiding();
            if (player.isSleeping()) {
                player.wakeUpPlayer(true, true, false);
            }

            ((ServerPlayerEntity)player).teleport(worldServer, position.getX(), position.getY() + 2.0, position.getZ(), player.rotationYaw, player.rotationPitch);
        }
        else
        {
            player.connection.setPlayerLocation(position.getX(), position.getY() + 2.0, position.getZ(), player.rotationYaw, player.rotationPitch);
            player.setRotationYawHead(player.rotationYaw);

        }
    }
}


