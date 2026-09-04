package com.minecolonies.core.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
        if (citizen == null || world == null || world.isClientSide())
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

        citizen.getNavigation().stop();
        citizen.stopRiding();
        citizen.setPos(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET);
        citizen.absSnapRotationTo(citizen.getRotationYaw(), citizen.getRotationPitch());
        return true;
    }

    /**
     * Teleports the player to his home colony.
     *
     * @param player the player to teleport home.
     */
    public static void homeTeleport(@NotNull final ServerPlayer player)
    {
        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.level(), player);
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
        BlockPos position = BlockPos.containing(player.getX(), 250, player.getZ()); //start at current position

        final ServerLevel currentLevel = player.level();
        position = BlockPosUtil.findLand(position, currentLevel);

        ChunkPos chunkpos = ChunkPos.containing(position);
        currentLevel.getChunkSource().addTicketWithRadius(TicketType.UNKNOWN, chunkpos, 1);
        player.stopRiding();
        if (player.isSleeping())
        {
            player.stopSleepInBed(true, true);
        }

        player.teleportTo(currentLevel,
            position.getX(),
            position.getY() + 2.0,
            position.getZ(),
            Set.of(),
            player.getYRot(),
            player.getXRot(),
            false);
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
     * @param player the player to teleport.
     * @param colony the colony to teleport to.
     */
    public static void colonyTeleport(@NotNull final ServerPlayer player, @NotNull final IColony colony)
    {
        colonyTeleport(player, colony, null);
    }

    /**
     * Teleports the player to the given colony.
     *
     * @param player the player to teleport.
     * @param colony the colony to teleport to.
     * @param pos the preferred position to teleport to.
     */
    public static void colonyTeleport(@NotNull final ServerPlayer player, @NotNull final IColony colony, final BlockPos pos)
    {
        BlockPos position = pos;
        if (pos == null)
        {
            if (colony.getServerBuildingManager().getTownHall() != null)
            {
                position = colony.getServerBuildingManager().getTownHall().getPosition();
            }
            else
            {
                position = colony.getCenter();
            }
        }

        final ServerLevel world = player.level().getServer().getLevel(colony.getDimension());

        position = BlockPosUtil.findAround(world,
          position,
          5,
          5,
          (predWorld, predPos) -> predWorld.getBlockState(predPos).isAir() && predWorld.getBlockState(predPos.above()).isAir());

        if (position == null)
        {
            return;
        }

        ChunkPos chunkpos = ChunkPos.containing(position);
        world.getChunkSource().addTicketWithRadius(TicketType.UNKNOWN, chunkpos, 1);
        player.stopRiding();
        if (player.isSleeping())
        {
            player.stopSleepInBed(true, true);
        }

        player.teleportTo(world,
            position.getX(),
            position.getY(),
            position.getZ(),
            Set.of(),
            player.getYRot(),
            player.getXRot(),
            false);
        MessageUtils.format(COMMAND_TELEPORT_SUCCESS, colony.getName()).sendTo(player);
    }
}
