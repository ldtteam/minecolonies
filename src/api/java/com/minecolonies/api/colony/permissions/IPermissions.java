package com.minecolonies.api.colony.permissions;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Permission interface.
 */
public interface IPermissions
{
    /**
     * Returns whether the player has the permission for an action.
     *
     * @param player {@link PlayerEntity} player.
     * @param action {@link Action} action.
     * @return true if has permission, otherwise false.
     */
    boolean hasPermission(PlayerEntity player, Action action);

    /**
     * Returns whether the player is a member of the colony.
     *
     * @param player {@link PlayerEntity} to check.
     * @return true if the player is a member of the colony.
     */
    boolean isColonyMember(PlayerEntity player);

    /**
     * Returns an unmodifiable map of the players list.
     *
     * @return map of UUIDs and player objects.
     */
    @NotNull
    Map<UUID, Player> getPlayers();

    /**
     * Get the rank of a UUID.
     *
     * @param player UUID to check rank of.
     * @return Rank of the player.
     */
    @NotNull
    Rank getRank(UUID player);
}
