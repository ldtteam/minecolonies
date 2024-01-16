package com.minecolonies.core.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.world.level.material.Material;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_TELEPORT_SUCCESS;

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

    public static boolean teleportCitizen(final AbstractEntityCitizen citizen, final Level world, final BlockPos location)
    {
        if (citizen == null || world == null || world.isClientSide)
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
        citizen.moveTo(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        if (citizen.getProxy() != null)
        {
            citizen.getProxy().reset();
        }
        citizen.getNavigation().stop();
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
    public static void homeTeleport(@NotNull final ServerPlayer player)
    {
        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.getCommandSenderWorld(), player);
        if (colony == null)
        {
            MessageUtils.format(COMMAND_COLONY_ID_NOT_FOUND).sendTo(player);
            return;
        }

        colonyTeleport(player, colony);
    }

    /**
     * Teleports the player to the nearest safe surface location above their current location
     */
    public static void surfaceTeleport(@NotNull final ServerPlayer player)
    {
        BlockPos position = new BlockPos(player.getX(), 250, player.getZ()); //start at current position
        final ServerLevel world = player.getLevel();

        position = BlockPosUtil.findLand(position, world);

        ChunkPos chunkpos = new ChunkPos(position);
        world.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getId());
        player.stopRiding();
        if (player.isSleeping())
        {
            player.stopSleepInBed(true, true);
        }

        player.teleportTo(world, position.getX(), position.getY() + 2.0, position.getZ(), player.getYRot(), player.getXRot());
    }

    /**
     * Teleports the player to his home colony.
     *
     * @param dimension the dimension the colony is in.
     * @param player    the player to teleport.
     * @param id        the colony id.
     */
    public static void colonyTeleportByID(@NotNull final ServerPlayer player, final int id, final ResourceKey<Level> dimension)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(id, dimension);
        if (colony == null)
        {
            MessageUtils.format(COMMAND_COLONY_ID_NOT_FOUND).sendTo(player);
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
    public static void colonyTeleport(@NotNull final ServerPlayer player, @NotNull final IColony colony)
    {
        BlockPos position;

        if (colony.getBuildingManager().getTownHall() != null)
        {
            position = colony.getBuildingManager().getTownHall().getPosition();
        }
        else
        {
            position = colony.getCenter();
        }

        final ServerLevel world = player.getServer().getLevel(colony.getDimension());

        position = BlockPosUtil.findAround(world,
          position,
          5,
          5,
          (predWorld, predPos) -> predWorld.getBlockState(predPos).getMaterial() == Material.AIR && predWorld.getBlockState(predPos.above()).getMaterial() == Material.AIR);

        if (position == null)
        {
            return;
        }

        ChunkPos chunkpos = new ChunkPos(position);
        world.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getId());
        player.stopRiding();
        if (player.isSleeping())
        {
            player.stopSleepInBed(true, true);
        }

        player.teleportTo(world, position.getX(), position.getY(), position.getZ(), player.getYRot(), player.getXRot());
        MessageUtils.format(COMMAND_TELEPORT_SUCCESS, colony.getName()).sendTo(player);
    }
}
