package com.minecolonies.api.colony.permissions;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Permission interface.
 */
public interface IPermissions
{
    /**
     * IDs of the initial ranks according to their old enum values
     */
    int OWNER_RANK_ID = 0;
    int OFFICER_RANK_ID = 1;
    int FRIEND_RANK_ID = 2;
    int NEUTRAL_RANK_ID = 3;
    int HOSTILE_RANK_ID = 4;

    boolean hasPermission(Rank rank, @NotNull Action action);

    Set<ColonyPlayer> getPlayersByRank(Rank rank);

    Set<ColonyPlayer> getPlayersByRank(@NotNull Set<Rank> ranks);

    /**
     * Returns a map of all ranks present in the colony, identified by their ID
     * @return the map
     */
    Map<Integer, Rank> getRanks();

    /**
     * Returns the rank with the given ID
     * @param id the id
     * @return the rank
     */
    Rank getRank(int id);

    /**
     * Returns the owner rank
     * @return the rank
     */
    Rank getRankOwner();

    /**
     * Returns the officer rank
     * @return the rank
     */
    Rank getRankOfficer();

    /**
     * Returns the hostile rank
     * @return the rank
     */
    Rank getRankHostile();

    /**
     * Returns the neutral rank
     * @return the rank
     */
    Rank getRankNeutral();

    /**
     * Returns the friend rank
     * @return the rank
     */
    Rank getRankFriend();

    /**
     * Returns whether the player has the permission for an action.
     *
     * @param player {@link Player} player.
     * @param action {@link Action} action.
     * @return true if has permission, otherwise false.
     */
    boolean hasPermission(Player player, Action action);

    boolean addPlayer(@NotNull String player, Rank rank, Level world);

    boolean addPlayer(@NotNull UUID id, String name, Rank rank);

    @Nullable
    String getOwnerName();

    /**
     * Checks if a user is a subscriber.
     *
     * @param player {@link Player} to check for subscription.
     * @return True is subscriber, otherwise false.
     */
    boolean isSubscriber(@NotNull Player player);

    /**
     * Returns whether the player is a member of the colony.
     *
     * @param player {@link Player} to check.
     * @return true if the player is a member of the colony.
     */
    boolean isColonyMember(Player player);

    void togglePermission(Rank rank, @NotNull Action action);

    @Nullable
    Map.Entry<UUID, ColonyPlayer> getOwnerEntry();

    boolean setOwner(ColonyPlayer player);

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
    Map<UUID, ColonyPlayer> getPlayers();

    boolean setPlayerRank(UUID id, Rank rank, Level world);

    boolean addPlayer(@NotNull GameProfile gameprofile, Rank rank);

    /**
     * Get the rank of a UUID.
     *
     * @param player UUID to check rank of.
     * @return rank of the player.
     */
    @NotNull
    Rank getRank(UUID player);

    Rank getRank(Player player);

    void restoreOwnerIfNull();

    boolean setPermission(Rank rank, Action action);

    boolean removePermission(Rank rank, Action action);

    boolean removePlayer(UUID playerID);

    /**
     * Adds a rink with the given name to the colony
     * @param name the chosen name
     */
    void addRank(String name);

    /**
     * Removes the given rank from the colony
     * @param rank the rank
     */
    void removeRank(Rank rank);

    Set<ColonyPlayer> getFilteredPlayers(Predicate<Rank> p);
}
