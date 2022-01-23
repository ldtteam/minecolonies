package com.minecolonies.coremod.colony.permissions;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.util.Utils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A client side representation of the permissions.
 */
public class PermissionsView implements IPermissions
{
    @NotNull
    private final Map<UUID, Player> players     = new HashMap<>();
    @NotNull
    private final Map<Rank, Long>   permissions = new HashMap<>();
    private       Rank               userRank;
    private final Map<Integer, Rank> ranks = new LinkedHashMap<>();

    private UUID   colonyOwner;
    private String ownerName = "";

    /**
     * Getter for the user rank.
     *
     * @return the rank.
     */
    public Rank getUserRank()
    {
        return userRank;
    }

    /**
     * Gets all player by a certain rank.
     *
     * @param rank the rank.
     * @return set of players.
     */
    @NotNull
    public Set<Player> getPlayersByRank(final Rank rank)
    {
        return Collections.unmodifiableSet(
          this.players.values()
            .stream()
            .filter(player -> player.getRank() == rank)
            .collect(Collectors.toSet()));
    }

    @NotNull
    public Map<UUID, Player> getPlayers()
    {
        return Collections.unmodifiableMap(players);
    }

    @Override
    public boolean setPlayerRank(final UUID id, final Rank rank, final World world)
    {
        return false;
    }

    @Override
    public boolean addPlayer(@NotNull final GameProfile gameprofile, final Rank rank)
    {
        return false;
    }

    /**
     * Gets all player by a certain set of rank.
     *
     * @param ranks the set of rank.
     * @return set of players.
     */
    @NotNull
    public Set<Player> getPlayersByRank(@NotNull final Set<Rank> ranks)
    {
        return Collections.unmodifiableSet(
          this.players.values()
            .stream()
            .filter(player -> ranks.contains(player.getRank()))
            .collect(Collectors.toSet()));
    }

    @Override
    public Set<Player> getFilteredPlayers(@NotNull final Predicate<Rank> predicate)
    {
        return Collections.unmodifiableSet(
          this.players.values().stream()
          .filter(player -> predicate.test(player.getRank()))
          .collect(Collectors.toSet()));
    }

    /**
     * Checks if the player has the permission to do an action.
     *
     * @param id     the id of the player.
     * @param action the action he is trying to execute.
     * @return true if so.
     */
    public boolean hasPermission(final UUID id, @NotNull final Action action)
    {
        return hasPermission(getRank(id), action);
    }

    /**
     * Checks if the rank has the permission to do an action.
     *
     * @param rank   the rank of the player.
     * @param action the action he is trying to execute.
     * @return true if so.
     */
    public boolean hasPermission(final Rank rank, @NotNull final Action action)
    {
        return Utils.testFlag(rank.getPermissions(), action.getFlag());
    }

    /**
     * Sets if the rank has the permission to do an action.
     *
     * @param rank   the rank to set.
     * @param action the action he is trying to execute.
     * @return true if so.
     */
    public boolean setPermission(final Rank rank, @NotNull final Action action, final boolean enable)
    {
        boolean changed;
        if (enable)
        {
            changed = rank.addPermission(action);
        }
        else
        {
            changed = rank.removePermission(action);
        }

        return changed;
    }

    @Override
    public boolean removePlayer(final UUID playerID)
    {
        return false;
    }

    @Override
    public boolean alterPermission(final Rank actor, final Rank rank, @NotNull final Action action, final boolean enable)
    {
        if (!canAlterPermission(actor, rank, action))
        {
            return false;
        }

        return setPermission(rank, action, enable);
    }

    @Override
    public boolean canAlterPermission(final Rank actor, final Rank rank, @NotNull final Action action)
    {
        if (rank == getRankOwner() && actor != getRankOwner())
        {
            return false;
        }

        return hasPermission(actor, Action.EDIT_PERMISSIONS) && (actor != rank
                                                                   || action != Action.EDIT_PERMISSIONS && action != Action.MANAGE_HUTS && action != Action.ACCESS_HUTS);
    }

    @Nullable
    @Override
    public Map.Entry<UUID, Player> getOwnerEntry()
    {
        for (@NotNull final Map.Entry<UUID, Player> entry : players.entrySet())
        {
            if (entry.getValue().getRank().equals(getRanks().get(OWNER_RANK_ID)))
            {
                return entry;
            }
        }
        return null;
    }

    @Override
    public boolean setOwner(final PlayerEntity player)
    {
        return false;
    }

    @Override
    public void setOwnerAbandoned()
    {
    }

    @NotNull
    @Override
    public UUID getOwner()
    {
        if (colonyOwner == null)
        {
            final Map.Entry<UUID, Player> owner = getOwnerEntry();
            if (owner != null)
            {
                colonyOwner = owner.getKey();
            }
            else
            {
                restoreOwnerIfNull();
            }
        }
        return colonyOwner;
    }

    /**
     * Deserialize content of class to a buffer.
     *
     * @param buf the buffer.
     */
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        final int ranksSize = buf.readVarInt();
        for (int i = 0; i < ranksSize; ++i)
        {
            final int id = buf.readVarInt();
            final Rank rank = new Rank(id, buf.readLong(), buf.readUtf(32767), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
            ranks.put(id, rank);
        }
        userRank = ranks.get(buf.readVarInt());

        //  Owners
        players.clear();
        final int numOwners = buf.readVarInt();
        for (int i = 0; i < numOwners; ++i)
        {
            final UUID id = PacketUtils.readUUID(buf);
            final String name = buf.readUtf(32767);
            final Rank rank = ranks.get(buf.readVarInt());
            if (rank.getId() == OWNER_RANK_ID)
            {
                colonyOwner = id;
            }

            players.put(id, new Player(id, name, rank));
        }
    }

    @Override
    public Rank getRank(@NotNull final PlayerEntity player)
    {
        return getRank(player.getUUID());
    }

    @Override
    public Rank getRank(final int id)
    {
        return ranks.get(id);
    }

    @Override
    public void restoreOwnerIfNull()
    {
        //Noop happens on the server side.
    }

    @NotNull
    @Override
    public Rank getRank(final UUID id)
    {
        final Player player = players.get(id);
        return player == null ? ranks.get(NEUTRAL_RANK_ID) : player.getRank();
    }

    @Override
    public boolean hasPermission(@NotNull final PlayerEntity player, @NotNull final Action action)
    {
        return hasPermission(getRank(player), action);
    }

    @Override
    public boolean addPlayer(@NotNull final String player, final Rank rank, final World world)
    {
        return false;
    }

    @Override
    public boolean addPlayer(@NotNull final UUID id, final String name, final Rank rank)
    {
        return false;
    }

    @Nullable
    @Override
    public String getOwnerName()
    {
        if (ownerName.isEmpty())
        {
            final Map.Entry<UUID, Player> owner = getOwnerEntry();
            if (owner != null)
            {
                ownerName = owner.getValue().getName();
            }
        }
        return ownerName;
    }

    @Override
    public boolean isSubscriber(@NotNull final PlayerEntity player)
    {
        return false;
    }

    @Override
    public boolean isColonyMember(@NotNull final PlayerEntity player)
    {
        return players.containsKey(player.getUUID());
    }

    @Override
    public Map<Integer, Rank> getRanks() { return ranks; }

    @Override
    public Rank getRankOwner()
    {
        return ranks.get(OWNER_RANK_ID);
    }

    @Override
    public Rank getRankOfficer()
    {
        return ranks.get(OFFICER_RANK_ID);
    }

    @Override
    public Rank getRankFriend()
    {
        return ranks.get(FRIEND_RANK_ID);
    }

    @Override
    public Rank getRankNeutral()
    {
        return ranks.get(NEUTRAL_RANK_ID);
    }

    @Override
    public Rank getRankHostile()
    {
        return ranks.get(HOSTILE_RANK_ID);
    }

    @Override
    public void addRank(String name)
    {
    }

    @Override
    public void removeRank(Rank rank)
    {
        if (!rank.isInitial())
        {
            ranks.remove(rank.getId());
        }
    }
}
