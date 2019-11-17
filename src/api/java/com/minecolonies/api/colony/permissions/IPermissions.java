package com.minecolonies.api.colony.permissions;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Permission interface.
 */
public interface IPermissions
{
    boolean hasPermission(Rank rank, @NotNull Action action);

    Set<Player> getPlayersByRank(Rank rank);

    Set<Player> getPlayersByRank(@NotNull Set<Rank> ranks);

    /**
     * Returns whether the player has the permission for an action.
     *
     * @param player {@link PlayerEntity} player.
     * @param action {@link Action} action.
     * @return true if has permission, otherwise false.
     */
    boolean hasPermission(PlayerEntity player, Action action);

    boolean addPlayer(@NotNull String player, Rank rank, World world);

    boolean addPlayer(@NotNull UUID id, String name, Rank rank);

    @Nullable
    String getOwnerName();

    /**
     * Checks if a user is a subscriber.
     *
     * @param player {@link PlayerEntity} to check for subscription.
     * @return True is subscriber, otherwise false.
     */
    boolean isSubscriber(@NotNull PlayerEntity player);

    /**
     * Returns whether the player is a member of the colony.
     *
     * @param player {@link PlayerEntity} to check.
     * @return true if the player is a member of the colony.
     */
    boolean isColonyMember(PlayerEntity player);

    void togglePermission(Rank rank, @NotNull Action action);

    @Nullable
    Map.Entry<UUID, Player> getOwnerEntry();

    boolean setOwner(PlayerEntity player);

    /**
     * Sets the owner to abandoned
     */
    void setOwnerAbandoned();

    @NotNull
    UUID getOwner();

    /**
     * Returns an unmodifiable map of the players list.
     *
     * @return map of UUIDs and player objects.
     */
    @NotNull
    Map<UUID, Player> getPlayers();

    boolean setPlayerRank(UUID id, Rank rank, World world);

    boolean addPlayer(@NotNull GameProfile gameprofile, Rank rank);

    /**
     * Get the rank of a UUID.
     *
     * @param player UUID to check rank of.
     * @return Rank of the player.
     */
    @NotNull
    Rank getRank(UUID player);

    Rank getRank(PlayerEntity player);

    void restoreOwnerIfNull();

    boolean setPermission(Rank rank, Action action);

    boolean removePermission(Rank rank, Action action);

    boolean removePlayer(UUID playerID);
}
