package com.minecolonies.coremod.util;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for server-side teleporting.
 */
public final class TeleportHelper
{
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;

    /**
     * Private constructor to hide the implicit public one.
     */
    private TeleportHelper()
    {
        // Intentionally left empty.
    }

    public static boolean teleportCitizen(final AbstractEntityCitizen citizen, final World world, final BlockPos location)
    {
        if (citizen == null || world == null || world.isRemote)
        {
            return false;
        }

        final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, location);
        if (spawnPoint == null)
        {
            return false;
        }

        if (citizen.getCitizenSleepHandler().isAsleep())
        {
            citizen.getCitizenSleepHandler().onWakeUp();
        }

        citizen.stopRiding();
        citizen.setLocationAndAngles(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        if (citizen.getProxy() != null)
        {
            citizen.getProxy().reset();
        }
        citizen.getNavigator().clearPath();
        if (citizen.getProxy() != null)
        {
            citizen.getProxy().reset();
        }

        return true;
    }

    /**
     * Teleports the player to his home colony.
     *
     * @param player the player to teleport home.
     */
    public static void homeTeleport(@NotNull final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.getEntityWorld(), player);
        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.command.colonyidnotfound");
            return;
        }

        colonyTeleport(player, colony);
    }

    /**
     * Teleports the player to the nearest safe surface location above their current location
     */
    public static void surfaceTeleport(@NotNull final ServerPlayerEntity player)
    {
        BlockPos position = new BlockPos(player.getPosX(), 250, player.getPosZ()); //start at current position
        final ServerWorld world = player.getServerWorld();

        position = BlockPosUtil.findLand(position, world);

        ChunkPos chunkpos = new ChunkPos(position);
        world.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getEntityId());
        player.stopRiding();
        if (player.isSleeping())
        {
            player.stopSleepInBed(true, true);
        }

        player.teleport(world, position.getX(), position.getY() + 2.0, position.getZ(), player.rotationYaw, player.rotationPitch);
    }

    /**
     * Teleports the player to his home colony.
     *
     * @param dimension the dimension the colony is in.
     * @param player    the player to teleport.
     * @param id        the colony id.
     */
    public static void colonyTeleportByID(@NotNull final ServerPlayerEntity player, final int id, final RegistryKey<World> dimension)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(id, dimension);
        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.command.colonyidnotfound");
            return;
        }

        colonyTeleport(player, colony);
    }

    /**
     * Teleports the player to the given colony.
     *
     * @param colony the colony to teleport to.
     * @param player the player to teleport.
     */
    public static void colonyTeleport(@NotNull final ServerPlayerEntity player, @NotNull final IColony colony)
    {
        final BlockPos position;

        if (colony.getBuildingManager().getTownHall() != null)
        {
            position = colony.getBuildingManager().getTownHall().getPosition();
        }
        else
        {
            position = colony.getCenter();
        }

        final ServerWorld world = player.getServer().getWorld(colony.getDimension());


        ChunkPos chunkpos = new ChunkPos(position);
        world.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getEntityId());
        player.stopRiding();
        if (player.isSleeping())
        {
            player.stopSleepInBed(true, true);
        }

        player.teleport(world, position.getX(), position.getY() + 2.0, position.getZ(), player.rotationYaw, player.rotationPitch);
        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.command.teleport.success", colony.getName());
    }
}
