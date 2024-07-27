package com.minecolonies.core.colony.permissions;

import com.minecolonies.api.colony.permissions.*;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.core.colony.Colony;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Colony Permissions System.
 */
public class Permissions implements IPermissions
{
    /**
     * All tags to store and retrieve data from nbt.
     */
    private static final String TAG_OWNERS          = "owners";
    private static final String TAG_ID              = "id";
    private static final String TAG_RANK            = "rank";
    private static final String TAG_PERMISSIONS     = "permissionMap";
    private static final String TAG_FLAGS           = "flags";
    private static final String TAG_OWNER           = "owner";
    private static final String TAG_OWNER_ID        = "ownerid";
    private static final String TAG_FULLY_ABANDONED = "fully_abandoned";
    private static final String TAG_RANKS           = "ranks";
    private static final String TAG_SUBSCRIBER      = "is_subscriber";
    private static final String TAG_INITIAL         = "is_initial";
    private static final String TAG_COLONY_MANAGER  = "is_colony_manager";
    private static final String TAG_HOSTILE         = "is_hostile";

    /**
     * NBTTarget for the permission version, used for updating.
     */
    private static final String TAG_VERSION = "permissionVersion";

    /**
     * All defined ranks
     */
    private final Map<Integer, Rank> ranks = new LinkedHashMap<>();

    /**
     * A flag for all the permissions unlocked in a fully abandoned colony.
     */
    private static int fullyAbandonedPermissionsFlag = 0;
    static
    {
        /*
         * Generate the fully abandoned flag.
         */
        for (Action a : Action.values())
        {
            if (a != Action.GUARDS_ATTACK
                  && a != Action.EDIT_PERMISSIONS && a != Action.MANAGE_HUTS
                  && a != Action.TELEPORT_TO_COLONY && a != Action.EXPLODE
                  && a != Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY && a != Action.RALLY_GUARDS)
            {
                fullyAbandonedPermissionsFlag |= a.getFlag();
            }
        }
    }

    /**
     * The colony the permissions belong to.
     */
    @NotNull
    private final Colony colony;

    /**
     * Players registered to the colony.
     */
    @NotNull
    private final Map<UUID, ColonyPlayer> players = new HashMap<>();

    /**
     * Used to check if the permissions have to by synchronized.
     */
    private boolean dirty = false;

    /**
     * The name of the owner.
     */
    private String ownerName = "";

    /**
     * The UUID of the owner.
     */
    private UUID ownerUUID = null;

    /**
     * True if this character has no owner or officer left and thus can be mined by anyone.
     */
    private boolean fullyAbandoned = false;

    /**
     * The current version of the permissions, increase upon changes to the preset permissions
     */
    private static final int permissionsVersion = 5;

    /**
     * Saves the permissionMap with allowed actions.
     *
     * @param colony the colony this permissionMap object belongs to.
     */
    public Permissions(@NotNull final Colony colony)
    {
        this.clearDirty();
        this.colony = colony;
        this.loadRanks();
    }

    /**
     * Load ranks from old enum and create according class instances
     */
    private void loadRanks()
    {
        ranks.clear();
        for (OldRank oldRank : OldRank.values())
        {
            String name = oldRank.name();
            name = name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1).toLowerCase(Locale.US);
            Rank rank = new Rank(oldRank.ordinal(), name, oldRank.isSubscriber, true);
            ranks.put(rank.getId(), rank);
            switch (oldRank)
            {
                case OWNER:
                    rank.addPermission(Action.EDIT_PERMISSIONS);
                    rank.addPermission(Action.MAP_BORDER);
                    rank.addPermission(Action.MAP_DEATHS);
                case OFFICER:
                    rank.addPermission(Action.PLACE_HUTS);
                    rank.addPermission(Action.BREAK_HUTS);
                    rank.addPermission(Action.MANAGE_HUTS);
                    rank.addPermission(Action.RECEIVE_MESSAGES);
                    rank.addPermission(Action.PLACE_BLOCKS);
                    rank.addPermission(Action.BREAK_BLOCKS);
                    rank.addPermission(Action.FILL_BUCKET);
                    rank.addPermission(Action.OPEN_CONTAINER);
                    rank.addPermission(Action.RECEIVE_MESSAGES_FAR_AWAY);
                    rank.addPermission(Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY);
                    rank.addPermission(Action.RALLY_GUARDS);
                    rank.addPermission(Action.MAP_BORDER);
                    rank.addPermission(Action.MAP_DEATHS);
                    rank.setColonyManager(true);
                case FRIEND:
                    rank.addPermission(Action.ACCESS_HUTS);
                    rank.addPermission(Action.USE_SCAN_TOOL);
                    rank.addPermission(Action.TOSS_ITEM);
                    rank.addPermission(Action.PICKUP_ITEM);
                    rank.addPermission(Action.RIGHTCLICK_BLOCK);
                    rank.addPermission(Action.RIGHTCLICK_ENTITY);
                    rank.addPermission(Action.THROW_POTION);
                    rank.addPermission(Action.SHOOT_ARROW);
                    rank.addPermission(Action.ATTACK_CITIZEN);
                    rank.addPermission(Action.ATTACK_ENTITY);
                    rank.addPermission(Action.TELEPORT_TO_COLONY);
                    rank.addPermission(Action.ACCESS_TOGGLEABLES);
                    rank.addPermission(Action.MAP_BORDER);
                case NEUTRAL:
                    rank.addPermission(Action.ACCESS_FREE_BLOCKS);
                    rank.addPermission(Action.ACCESS_TOGGLEABLES);
                    rank.addPermission(Action.MAP_BORDER);
                    break;
                case HOSTILE:
                    rank.addPermission(Action.GUARDS_ATTACK);
                    rank.addPermission(Action.HURT_CITIZEN);
                    rank.addPermission(Action.HURT_VISITOR);
                    rank.addPermission(Action.MAP_BORDER);
                    rank.setHostile(true);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Data version/correctness upgrades
     *
     * @param version
     * @param rank
     */
    private void upgradePermissions(final int version, final Rank rank)
    {
        // keep this consistent with loadRanks(), as that's still used for new colonies

        if (version < 4)
        {
            if (rank.isHostile())
            {
                this.setPermission(rank, Action.HURT_CITIZEN, true);
                this.setPermission(rank, Action.HURT_VISITOR, true);
            }

            if (rank.isColonyManager())
            {
                this.setPermission(rank, Action.MAP_DEATHS, true);
            }

            this.setPermission(rank, Action.MAP_BORDER, true);
        }

        if (version < 5)
        {
            if (!rank.isHostile())
            {
                this.setPermission(rank, Action.ACCESS_TOGGLEABLES, true);
            }
        }

        // Fix bad saved values
        if (rank == getRankOwner())
        {
            rank.addPermission(Action.EDIT_PERMISSIONS);
            rank.addPermission(Action.ACCESS_HUTS);
            rank.addPermission(Action.MANAGE_HUTS);
        }
    }

    /**
     * Sets the rank for a specific action.
     *
     * @param rank   Desired rank.
     * @param action Action that should have desired rank.
     */
    @Override
    public final boolean setPermission(final Rank rank, @NotNull final Action action, boolean enable)
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

        if (changed)
        {
            markDirty();
        }
        return changed;
    }

    /**
     * Marks instance dirty.
     */
    private void markDirty()
    {
        dirty = true;
        if (colony != null)
        {
            colony.markDirty();
        }
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

        return hasPermission(actor, Action.EDIT_PERMISSIONS) && (actor != rank || action != Action.EDIT_PERMISSIONS && action != Action.MANAGE_HUTS && action != Action.ACCESS_HUTS);
    }

    /**
     * Reads the permissionMap from a NBT.
     *
     * @param compound NBT to read from.
     */
    public void loadPermissions(@NotNull final CompoundTag compound)
    {
        final int version = compound.getInt(TAG_VERSION);
        // Ranks
        if (compound.contains(TAG_RANKS))
        {
            ranks.clear();

            final ListTag rankTagList = compound.getList(TAG_RANKS, Tag.TAG_COMPOUND);
            for (int i = 0; i < rankTagList.size(); ++i)
            {
                final CompoundTag rankCompound = rankTagList.getCompound(i);
                final int id = rankCompound.getInt(TAG_ID);
                final String name = rankCompound.getString(TAG_NAME);
                final boolean isSubscriber = rankCompound.getBoolean(TAG_SUBSCRIBER);
                final boolean isInitial = rankCompound.getBoolean(TAG_INITIAL);
                final boolean isColonyManager = rankCompound.getBoolean(TAG_COLONY_MANAGER);
                final boolean isHostile = rankCompound.getBoolean(TAG_HOSTILE);

                final Rank rank = new Rank(id, 0L, name, isSubscriber, isInitial, isColonyManager, isHostile);
                ranks.put(id, rank);
                upgradePermissions(version, rank);
            }

            final ListTag permissionsTagList = compound.getList(TAG_PERMISSIONS, Tag.TAG_COMPOUND);
            for (int i = 0; i < permissionsTagList.size(); ++i)
            {
                final CompoundTag permissionsCompound = permissionsTagList.getCompound(i);

                final Rank rank = ranks.get(permissionsCompound.getInt(TAG_RANK));
                if (rank == null)
                {
                    continue;
                }

                final ListTag flagsTagList = permissionsCompound.getList(TAG_FLAGS, Tag.TAG_STRING);

                for (int j = 0; j < flagsTagList.size(); ++j)
                {
                    final String flag = flagsTagList.getString(j);
                    try
                    {
                        rank.addPermission(Action.valueOf(flag));
                    }
                    catch (IllegalArgumentException ex)
                    {
                        // noop, this can happen with backwards compat.
                    }
                }
            }
        }
        else
        {
            this.loadRanks();
        }

        players.clear();
        //  Owners
        final ListTag ownerTagList = compound.getList(TAG_OWNERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < ownerTagList.size(); ++i)
        {
            final CompoundTag ownerCompound = ownerTagList.getCompound(i);
            @NotNull final UUID id = UUID.fromString(ownerCompound.getString(TAG_ID));
            String name = "";
            if (ownerCompound.contains(TAG_NAME))
            {
                name = ownerCompound.getString(TAG_NAME);
            }
            Rank rank;
            if (version >= 3)
            {
                rank = ranks.get(ownerCompound.getInt(TAG_RANK));
            }
            else
            {
                final OldRank oldRank = OldRank.valueOf(ownerCompound.getString(TAG_RANK));
                rank = ranks.get(oldRank.ordinal());
            }

            final GameProfile player = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(id).orElse(null);

            if (player != null && rank != null)
            {
                players.put(id, new ColonyPlayer(id, player.getName(), rank));
            }
            else if (!name.isEmpty() && rank != null)
            {
                players.put(id, new ColonyPlayer(id, name, rank));
            }
        }

        if (compound.contains(TAG_OWNER))
        {
            ownerName = compound.getString(TAG_OWNER);
        }
        if (compound.contains(TAG_OWNER_ID))
        {
            try
            {
                ownerUUID = UUID.fromString(compound.getString(TAG_OWNER_ID));
            }
            catch (final IllegalArgumentException e)
            {
                /*
                 * Intentionally left empty. Happens when the UUID hasn't been saved yet.
                 */
            }
        }

        if (compound.contains(TAG_FULLY_ABANDONED))
        {
            fullyAbandoned = compound.getBoolean(TAG_FULLY_ABANDONED);
        }
        else
        {
            checkFullyAbandoned();
        }

        restoreOwnerIfNull();
    }

    /**
     * Restores the owner from other variables if he is null on loading.
     */
    public void restoreOwnerIfNull()
    {
        final Map.Entry<UUID, ColonyPlayer> owner = getOwnerEntry();
        if (owner == null && ownerUUID != null)
        {
            final GameProfile player = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getProfileCache().get(ownerUUID).orElse(null);

            if (player != null)
            {
                players.put(ownerUUID, new ColonyPlayer(ownerUUID, player.getName(), ranks.get(OWNER_RANK_ID)));
            }
        }
        else if (owner == null)
        {
            setOwnerAbandoned();
        }
    }

    /**
     * Compute the owner of a colony.
     * <p>
     * Can be quite expensive in colonies with many players.
     *
     * @return the corresponding entry or null.
     */
    @Override
    @Nullable
    public Map.Entry<UUID, ColonyPlayer> getOwnerEntry()
    {
        for (@NotNull final Map.Entry<UUID, ColonyPlayer> entry : players.entrySet())
        {
            if (entry.getValue().getRank().getId() == OWNER_RANK_ID)
            {
                return entry;
            }
        }
        return null;
    }

    /**
     * Change the owner of a colony.
     *
     * @param player the player to set.
     * @return true if successful.
     */
    @Override
    public boolean setOwner(final Player player)
    {
        players.remove(getOwner());

        ownerName = player.getName().getString();
        ownerUUID = player.getUUID();

        players.put(ownerUUID, new ColonyPlayer(ownerUUID, player.getName().getString(), ranks.get(OWNER_RANK_ID)));

        fullyAbandoned = false;

        markDirty();
        return true;
    }

    /**
     * Change the owner of a colony to [abandoned]
     */
    @Override
    public void setOwnerAbandoned()
    {
        players.remove(ownerUUID);

        ownerName = "[abandoned]";
        ownerUUID = UUID.randomUUID();

        players.put(ownerUUID, new ColonyPlayer(ownerUUID, ownerName, ranks.get(OWNER_RANK_ID)));

        checkFullyAbandoned();
        markDirty();
    }

    /**
     * Returns the owner of this permission instance.
     *
     * @return UUID of the owner.
     */
    @Override
    @NotNull
    public UUID getOwner()
    {
        if (ownerUUID == null)
        {
            final Map.Entry<UUID, ColonyPlayer> owner = getOwnerEntry();
            if (owner != null)
            {
                ownerUUID = owner.getKey();
            }
            else
            {
                restoreOwnerIfNull();
            }
        }
        return ownerUUID;
    }

    /**
     * Save the permissionMap to a NBT.
     *
     * @param compound NBT to write to.
     */
    public void savePermissions(@NotNull final CompoundTag compound)
    {
        //  Ranks
        @NotNull final ListTag rankTagList = new ListTag();
        for (@NotNull final Rank rank : ranks.values())
        {
            @NotNull final CompoundTag rankCompound = new CompoundTag();
            rankCompound.putInt(TAG_ID, rank.getId());
            rankCompound.putString(TAG_NAME, rank.getName());
            rankCompound.putBoolean(TAG_SUBSCRIBER, rank.isSubscriber());
            rankCompound.putBoolean(TAG_INITIAL, rank.isInitial());
            rankCompound.putBoolean(TAG_COLONY_MANAGER, rank.isColonyManager());
            rankCompound.putBoolean(TAG_HOSTILE, rank.isHostile());
            rankTagList.add(rankCompound);
        }
        compound.put(TAG_RANKS, rankTagList);

        //  Owners
        @NotNull final ListTag ownerTagList = new ListTag();
        for (@NotNull final ColonyPlayer player : players.values())
        {
            @NotNull final CompoundTag ownersCompound = new CompoundTag();
            ownersCompound.putString(TAG_ID, player.getID().toString());
            ownersCompound.putString(TAG_NAME, player.getName());
            ownersCompound.putInt(TAG_RANK, player.getRank().getId());
            ownerTagList.add(ownersCompound);
        }
        compound.put(TAG_OWNERS, ownerTagList);

        // Permissions
        @NotNull final ListTag permissionsTagList = new ListTag();
        for (@NotNull final Rank rank : ranks.values())
        {
            @NotNull final CompoundTag permissionsCompound = new CompoundTag();
            permissionsCompound.putInt(TAG_RANK, rank.getId());

            @NotNull final ListTag flagsTagList = new ListTag();
            for (@NotNull final Action action : Action.values())
            {
                if (Utils.testFlag(rank.getPermissions(), action.getFlag()))
                {
                    flagsTagList.add(StringTag.valueOf(action.name()));
                }
            }
            permissionsCompound.put(TAG_FLAGS, flagsTagList);

            permissionsTagList.add(permissionsCompound);
        }

        compound.put(TAG_PERMISSIONS, permissionsTagList);

        if (!ownerName.isEmpty())
        {
            compound.putString(TAG_OWNER, ownerName);
        }
        if (ownerUUID != null)
        {
            compound.putString(TAG_OWNER_ID, ownerUUID.toString());
        }

        compound.putBoolean(TAG_FULLY_ABANDONED, fullyAbandoned);

        compound.putInt(TAG_VERSION, permissionsVersion);
    }

    @Override
    @NotNull
    public Map<UUID, ColonyPlayer> getPlayers()
    {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Checks if a rank can perform an action.
     *
     * @param rank   Rank you want to check.
     * @param action Action you want to perform.
     * @return true if rank has permission for action, otherwise false.
     */
    @Override
    public boolean hasPermission(final Rank rank, @NotNull final Action action)
    {
        return Utils.testFlag(rank.getPermissions(), action.getFlag())
                 || (fullyAbandoned && Utils.testFlag(fullyAbandonedPermissionsFlag, action.getFlag()));
    }

    /**
     * Gets all player by a certain rank.
     *
     * @param rank the rank.
     * @return set of players.
     */
    @Override
    public Set<ColonyPlayer> getPlayersByRank(final Rank rank)
    {
        return this.players.values().stream()
          .filter(player -> player.getRank() != null && player.getRank().equals(rank))
          .collect(Collectors.toSet());
    }

    /**
     * Gets all player by a set of ranks.
     *
     * @param ranks the set of Ranks.
     * @return set of players.
     */
    @Override
    public Set<ColonyPlayer> getPlayersByRank(@NotNull final Set<Rank> ranks)
    {
        return this.players.values().stream()
          .filter(player -> ranks.contains(player.getRank()))
          .collect(Collectors.toSet());
    }

    @Override
    public Set<ColonyPlayer> getFilteredPlayers(@NotNull final Predicate<Rank> predicate)
    {
        return this.players.values().stream()
          .filter(player -> predicate.test(player.getRank()))
          .collect(Collectors.toSet());
    }

    /**
     * Checks if the player has the permission of an action.
     *
     * @param player {@link Player} player.
     * @param action {@link Action} action.
     * @return true if player has permissionMap, otherwise false.
     */
    @Override
    public boolean hasPermission(@NotNull final Player player, @NotNull final Action action)
    {
        return hasPermission(getRank(player), action);
    }

    @Override
    public Rank getRank(@NotNull final Player player)
    {
        return getRank(player.getGameProfile().getId());
    }

    /**
     * Sets the player's rank to a given rank.
     *
     * @param id    UUID of the player of the new oldRank.
     * @param rank  Desired rank.
     * @param world the world the player is in.
     * @return True if successful, otherwise false.
     */
    @Override
    public boolean setPlayerRank(final UUID id, final Rank rank, final Level world)
    {

        final ColonyPlayer player = getPlayers().get(id);

        if (player != null)
        {
            player.setRank(rank);

            if (rank.isColonyManager())
            {
                fullyAbandoned = false;
            }
            else
            {
                checkFullyAbandoned();
            }

            markDirty();
        }
        else
        {

            final GameProfile gameprofile = world.getServer().getProfileCache().get(id).orElse(null);

            return gameprofile != null && addPlayer(gameprofile, rank);
        }

        return true;
    }

    /**
     * Adds a player to the rankings.
     *
     * @param id   UUID of the player..
     * @param rank Desired rank.
     * @param name name of the player.
     * @return True if successful, otherwise false.
     */
    @Override
    public boolean addPlayer(@NotNull final UUID id, final String name, final Rank rank)
    {
        @NotNull final ColonyPlayer p = new ColonyPlayer(id, name, rank);

        players.remove(p.getID());
        players.put(p.getID(), p);

        if (rank.getId() == OWNER_RANK_ID || rank.getId() == OFFICER_RANK_ID)
        {
            fullyAbandoned = false;
        }

        markDirty();
        return true;
    }

    /**
     * Returns the rank belonging to the UUID.
     *
     * @param id UUID that you want to check rank of.
     * @return Rank of the UUID.
     */
    @NotNull
    @Override
    public Rank getRank(final UUID id)
    {
        final ColonyPlayer player = players.get(id);
        return player != null ? player.getRank() : ranks.get(NEUTRAL_RANK_ID);
    }

    /**
     * Add a player to the rankings.
     *
     * @param player String playername of the player to add.
     * @param rank   Rank desired starting rank.
     * @param world  the world the player is in.
     * @return True if successful, otherwise false.
     */
    @Override
    public boolean addPlayer(@NotNull final String player, final Rank rank, final Level world)
    {
        if (player.isEmpty())
        {
            return false;
        }
        final GameProfile gameprofile = world.getServer().getProfileCache().get(player).orElse(null);
        //Check if the player already exists so that their rank isn't overridden

        // Adds new subscribers
        if (!world.isClientSide() && gameprofile != null)
        {
            final ServerPlayer playerEntity = (ServerPlayer) world.getPlayerByUUID(gameprofile.getId());
            if (playerEntity != null)
            {
                if (rank.getId() == OFFICER_RANK_ID)
                {
                    colony.getPackageManager().addImportantColonyPlayer(playerEntity);
                    colony.getPackageManager().updateSubscribers();
                    fullyAbandoned = false;
                }
                else if (rank.getId() == OWNER_RANK_ID)
                {
                    fullyAbandoned = false;
                }
                else
                {
                    // Check claim
                    final LevelChunk chunk = world.getChunk(playerEntity.chunkPosition().x, playerEntity.chunkPosition().z);
                    final int owningColonyId = ColonyUtils.getOwningColony(chunk);
                    if (owningColonyId == colony.getID() && world.dimension() == colony.getDimension())
                    {
                        colony.getPackageManager().addCloseSubscriber(playerEntity);
                        colony.getPackageManager().updateSubscribers();
                    }
                }
            }
        }

        return gameprofile != null && !ownerUUID.equals(gameprofile.getId()) && addPlayer(gameprofile, rank);
    }

    /**
     * Adds a player to the rankings.
     *
     * @param gameprofile GameProfile of the player.
     * @param rank        Desired rank.
     * @return True if successful, otherwise false.
     */
    @Override
    public boolean addPlayer(@NotNull final GameProfile gameprofile, final Rank rank)
    {
        @NotNull final ColonyPlayer p = new ColonyPlayer(gameprofile.getId(), gameprofile.getName(), rank);

        players.remove(p.getID());
        players.put(p.getID(), p);

        if (rank.getId() == OWNER_RANK_ID || rank.getId() == OFFICER_RANK_ID)
        {
            fullyAbandoned = false;
        }

        markDirty();
        return true;
    }

    /**
     * Remove a player from the permissionMap.
     *
     * @param id UUID of the player.
     * @return True if successful, otherwise false.
     */
    public boolean removePlayer(final UUID id)
    {
        final ColonyPlayer player = players.get(id);
        if (player != null && player.getRank().getId() != OWNER_RANK_ID && players.remove(id) != null)
        {
            checkFullyAbandoned();
            markDirty();
            return true;
        }

        return false;
    }

    /**
     * Returns the name of the owner of this permission instance.
     *
     * @return Name of the owner.
     */
    @Override
    @Nullable
    public String getOwnerName()
    {
        if (ownerName.isEmpty())
        {
            final Map.Entry<UUID, ColonyPlayer> owner = getOwnerEntry();
            if (owner != null)
            {
                ownerName = owner.getValue().getName();
            }
        }
        return ownerName;
    }

    /**
     * Checks if a user is a subscriber.
     *
     * @param player {@link Player} to check for subscription.
     * @return True is subscriber, otherwise false.
     */
    @Override
    public boolean isSubscriber(@NotNull final Player player)
    {
        return isSubscriber(player.getGameProfile().getId());
    }

    /**
     * See {@link #isSubscriber(Player)}.
     *
     * @param player {@link UUID} of the player.
     * @return True if subscriber, otherwise false.
     */
    private boolean isSubscriber(final UUID player)
    {
        return getRank(player).isSubscriber();
    }

    /**
     * Returns if the instance is dirty.
     *
     * @return True if dirty, otherwise false.
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Marks instance not dirty.
     */
    public void clearDirty()
    {
        dirty = false;
    }

    /**
     * Serializes network data.
     *
     * @param buf        {@link FriendlyByteBuf} to write to.
     * @param viewerRank Rank of the viewer.
     */
    public void serializeViewNetworkData(@NotNull final FriendlyByteBuf buf, @NotNull final Rank viewerRank)
    {
        buf.writeVarInt(ranks.size());
        for (Rank rank : ranks.values())
        {
            buf.writeVarInt(rank.getId());
            buf.writeLong(rank.getPermissions());
            buf.writeUtf(rank.getName());
            buf.writeBoolean(rank.isSubscriber());
            buf.writeBoolean(rank.isInitial());
            buf.writeBoolean(rank.isColonyManager());
            buf.writeBoolean(rank.isHostile());
        }

        buf.writeVarInt(viewerRank.getId());

        //  Owners
        buf.writeVarInt(players.size());
        for (@NotNull final Map.Entry<UUID, ColonyPlayer> player : players.entrySet())
        {
            buf.writeUUID(player.getKey());
            buf.writeUtf(player.getValue().getName());
            buf.writeVarInt(player.getValue().getRank().getId());
        }
    }

    @Override
    public boolean isColonyMember(@NotNull final Player player)
    {
        return players.containsKey(player.getGameProfile().getId());
    }

    /**
     * Checks whether this colony is fully abandoned, meaning it can be destroyed.
     * A colony is fully abandoned when it has the owner "[abandoned]" and has no officers left.
     */
    private void checkFullyAbandoned()
    {
        if (getOwnerName().equals("[abandoned]") && getPlayersByRank(ranks.get(OFFICER_RANK_ID)).isEmpty())
        {
            fullyAbandoned = true;
        }
    }

    /**
     * Get rank instance of owner
     *
     * @return the rank
     */
    @Override
    public Rank getRankOwner()
    {
        return ranks.get(OWNER_RANK_ID);
    }

    /**
     * Get rank instance of officer
     *
     * @return the rank
     */
    @Override
    public Rank getRankOfficer()
    {
        return ranks.get(OFFICER_RANK_ID);
    }

    /**
     * Get rank instance of hostile
     *
     * @return the rank
     */
    @Override
    public Rank getRankHostile()
    {
        return ranks.get(HOSTILE_RANK_ID);
    }

    /**
     * Get rank instance of friend
     *
     * @return the rank
     */
    @Override
    public Rank getRankFriend()
    {
        return ranks.get(FRIEND_RANK_ID);
    }

    /**
     * Get rank instance of neutral
     *
     * @return the rank
     */
    @Override
    public Rank getRankNeutral()
    {
        return ranks.get(NEUTRAL_RANK_ID);
    }

    @Override
    public Rank getRank(final int id)
    {
        return ranks.get(id);
    }

    /**
     * Get a map of all ranks with their according ID as key
     *
     * @return the ranks
     */
    @Override
    public Map<Integer, Rank> getRanks()
    {
        return ranks;
    }

    /**
     * Create a new rank instance with the given name, auto assign lowest unused ID, no permissions
     *
     * @param name the name of the rank
     */
    @Override
    public void addRank(String name)
    {

        int id = HOSTILE_RANK_ID + 1;
        for (int i = HOSTILE_RANK_ID; i <= ranks.size() + 1; ++i)
        {
            if (ranks.get(i) == null)
            {
                id = i;
                break;
            }
        }
        Rank rank = new Rank(id, name, false, false);
        ranks.put(id, rank);
        markDirty();
    }

    /**
     * Remove the given rank, if it is not an initial rank
     * Set all players with the given rank to neutral
     *
     * @param rank the rank to remove
     */
    @Override
    public void removeRank(Rank rank)
    {
        if (rank.isInitial())
        {
            return;
        }
        for (ColonyPlayer player : getPlayersByRank(rank))
        {
            player.setRank(getRankNeutral());
        }
        ranks.remove(rank.getId());
        markDirty();
    }
}
