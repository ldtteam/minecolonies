package com.minecolonies.coremod.colony.permissions;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.permissions.*;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.colony.Colony;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.acl.LastOwnerException;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

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

    /**
     * NBTTarget for the permission version, used for updating.
     */
    private static final String TAG_VERSION = "permissionVersion";

    /**
     * All promotion rank possibilities.
     */
    @NotNull
    private static final Map<OldRank, RankPair> promotionRanks = new EnumMap<>(OldRank.class);

    /**
     * All defined ranks
     */
    private static final Map<Integer, Rank> ranks = new HashMap<>();

    /**
     * A flag for all the permissions unlocked in a fully abandoned colony.
     */
    private static int fullyAbandonedPermissionsFlag = 0;

    static
    {
        /*
         * Fill the promotion ranks.
         */
        setPromotionRanks(OldRank.OFFICER, OldRank.OFFICER, OldRank.FRIEND);
        setPromotionRanks(OldRank.FRIEND, OldRank.OFFICER, OldRank.NEUTRAL);
        setPromotionRanks(OldRank.NEUTRAL, OldRank.FRIEND, OldRank.HOSTILE);
        setPromotionRanks(OldRank.HOSTILE, OldRank.NEUTRAL, OldRank.HOSTILE);

        /*
         * Generate the fully abandoned flag.
         */
        for (Action a:Action.values())
        {
            if (a != Action.GUARDS_ATTACK && a != Action.CAN_PROMOTE && a != Action.CAN_DEMOTE
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
    private final Map<UUID, Player> players = new HashMap<>();

    /**
     * Permissions of these players.
     */
    @NotNull
    private final Map<Rank, Integer> permissionMap = new HashMap<>();

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
    private UUID   ownerUUID = null;

    /**
     * True if this character has no owner or officer left and thus can be mined by anyone.
     */
    private boolean fullyAbandoned = false;

    /**
     * The current version of the permissions, increase upon changes to the preset permissions
     */
    private static final int permissionsVersion = 3;

    /**
     * Saves the permissionMap with allowed actions.
     *
     * @param colony the colony this permissionMap object belongs to.
     */
    public Permissions(@NotNull final Colony colony)
    {
        this.clearDirty();
        this.colony = colony;
    }

    /**
     * Sets the oldRank for a specific action.
     *
     * @param rank   Desired rank.
     * @param action Action that should have desired oldRank.
     */
    public final boolean setPermission(final Rank rank, @NotNull final Action action)
    {
        final int flags = permissionMap.get(rank);

        //check that flag isn't set
        if (!Utils.testFlag(flags, action.getFlag()))
        {
            permissionMap.put(rank, Utils.setFlag(flags, action.getFlag()));
            markDirty();

            return true;
        }

        return false;
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

    /**
     * Stores the list of promotion/demotion ranks.
     *
     * @param r OldRank to set pro- and demotion of.
     * @param p Promotion rank.
     * @param d Demotion rank.
     */
    private static void setPromotionRanks(final OldRank r, final OldRank p, final OldRank d)
    {
        promotionRanks.put(r, new RankPair(p, d));
    }

    /**
     * Returns the promotion oldRank of a specific oldRank. E.G.: Neutral will return Friend.
     *
     * @param oldRank OldRank to check promotion of.
     * @return {@link OldRank} after promotion.
     */
    public static OldRank getPromotionRank(final OldRank oldRank)
    {
        if (promotionRanks.containsKey(oldRank))
        {
            return promotionRanks.get(oldRank).promote;
        }

        return oldRank;
    }

    /**
     * Returns the demotion oldRank of a specific oldRank. E.G.: Neutral will return Hostile.
     *
     * @param oldRank OldRank to check demotion of.
     * @return {@link OldRank} after demotion.
     */
    public static OldRank getDemotionRank(final OldRank oldRank)
    {
        if (promotionRanks.containsKey(oldRank))
        {
            return promotionRanks.get(oldRank).demote;
        }

        return oldRank;
    }

    /**
     * Toggle permission for a specific oldRank.
     *
     * @param rank   OldRank to toggle permission.
     * @param action Action to toggle permission.
     */
    @Override
    public void togglePermission(final Rank rank, @NotNull final Action action)
    {
        permissionMap.put(rank, Utils.toggleFlag(permissionMap.get(rank), action.getFlag()));
        markDirty();
    }

    /**
     * Reads the permissionMap from a NBT.
     *
     * @param compound NBT to read from.
     */
    public void loadPermissions(@NotNull final CompoundNBT compound)
    {
        // Ranks
        ranks.clear();
        if (compound.contains(TAG_RANKS))
        {
            final ListNBT rankTagList = compound.getList(TAG_RANKS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < rankTagList.size(); ++i)
            {
                final CompoundNBT rankCompound = rankTagList.getCompound(i);
                final int id = rankCompound.getInt(TAG_ID);
                final String name = rankCompound.getString(TAG_NAME);
                final boolean isSubscriber = rankCompound.getBoolean(TAG_SUBSCRIBER);
                final boolean isInitial = rankCompound.getBoolean(TAG_INITIAL);
                final Rank rank = new Rank(id, name, isSubscriber, isInitial);
                ranks.put(id, rank);
            }
        }
        else
        {
            for (OldRank oldRank : OldRank.values())
            {
                String name = oldRank.name();
                name = name.substring(0,1).toUpperCase(Locale.ENGLISH) + name.substring(1).toLowerCase(Locale.ENGLISH);
                Rank rank = new Rank(oldRank.ordinal(), name, oldRank.isSubscriber, true);
                ranks.put(rank.getId(), rank);
                permissionMap.put(rank, 0);
                switch (oldRank)
                {
                    case OWNER:
                        this.setPermission(rank, Action.EDIT_PERMISSIONS);
                    case OFFICER:
                        this.setPermission(rank, Action.PLACE_HUTS);
                        this.setPermission(rank, Action.BREAK_HUTS);
                        this.setPermission(rank, Action.CAN_PROMOTE);
                        this.setPermission(rank, Action.CAN_DEMOTE);
                        this.setPermission(rank, Action.SEND_MESSAGES);
                        this.setPermission(rank, Action.MANAGE_HUTS);
                        this.setPermission(rank, Action.RECEIVE_MESSAGES);
                        this.setPermission(rank, Action.PLACE_BLOCKS);
                        this.setPermission(rank, Action.BREAK_BLOCKS);
                        this.setPermission(rank, Action.FILL_BUCKET);
                        this.setPermission(rank, Action.OPEN_CONTAINER);
                        this.setPermission(rank, Action.RECEIVE_MESSAGES_FAR_AWAY);
                        this.setPermission(rank, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY);
                        this.setPermission(rank, Action.RALLY_GUARDS);
                    case FRIEND:
                        this.setPermission(rank, Action.ACCESS_HUTS);
                        this.setPermission(rank, Action.USE_SCAN_TOOL);
                        this.setPermission(rank, Action.TOSS_ITEM);
                        this.setPermission(rank, Action.PICKUP_ITEM);
                        this.setPermission(rank, Action.RIGHTCLICK_BLOCK);
                        this.setPermission(rank, Action.RIGHTCLICK_ENTITY);
                        this.setPermission(rank, Action.THROW_POTION);
                        this.setPermission(rank, Action.SHOOT_ARROW);
                        this.setPermission(rank, Action.ATTACK_CITIZEN);
                        this.setPermission(rank, Action.ATTACK_ENTITY);
                        this.setPermission(rank, Action.TELEPORT_TO_COLONY);
                    case NEUTRAL:
                        this.setPermission(rank, Action.ACCESS_FREE_BLOCKS);
                        break;
                    case HOSTILE:
                        this.setPermission(rank, Action.GUARDS_ATTACK);
                        break;
                    default:
                        break;
                }
            }
        }
        players.clear();
        //  Owners
        final ListNBT ownerTagList = compound.getList(TAG_OWNERS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        final int version = compound.getInt(TAG_VERSION);
        for (int i = 0; i < ownerTagList.size(); ++i)
        {
            final CompoundNBT ownerCompound = ownerTagList.getCompound(i);
            @NotNull final UUID id = UUID.fromString(ownerCompound.getString(TAG_ID));
            String name = "";
            if (ownerCompound.keySet().contains(TAG_NAME))
            {
                name = ownerCompound.getString(TAG_NAME);
            }
            Rank rank;
            if (version == permissionsVersion)
            {
                rank = ranks.get(ownerCompound.getInt(TAG_RANK));
            }
            else
            {
                final OldRank oldRank = OldRank.valueOf(ownerCompound.getString(TAG_RANK));
                rank = ranks.get(oldRank.ordinal());
            }

            final GameProfile player = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(id);

            if (player != null && rank != null)
            {
                players.put(id, new Player(id, player.getName(), rank));
            }
            else if (!name.isEmpty() && rank != null)
            {
                players.put(id, new Player(id, name, rank));
            }
        }

        //Permissions
        if (compound.getInt(TAG_VERSION) == permissionsVersion)
        {
            permissionMap.clear();
            final ListNBT permissionsTagList = compound.getList(TAG_PERMISSIONS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < permissionsTagList.size(); ++i)
            {
                final CompoundNBT permissionsCompound = permissionsTagList.getCompound(i);
                final Rank rank = ranks.get(permissionsCompound.getInt(TAG_RANK));

                final ListNBT flagsTagList = permissionsCompound.getList(TAG_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

                int flags = 0;

                for (int j = 0; j < flagsTagList.size(); ++j)
                {
                    final String flag = flagsTagList.getString(j);
                    flags = Utils.setFlag(flags, Action.valueOf(flag).getFlag());
                }
                permissionMap.put(rank, flags);
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
        }
        else
        {
            permissionMap.clear();
            final ListNBT permissionsTagList = compound.getList(TAG_PERMISSIONS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < permissionsTagList.size(); ++i)
            {
                final CompoundNBT permissionsCompound = permissionsTagList.getCompound(i);
                final OldRank oldRank = OldRank.valueOf(permissionsCompound.getString(TAG_RANK));
                final Rank rank = ranks.get(oldRank.ordinal());
                final ListNBT flagsTagList = permissionsCompound.getList(TAG_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

                int flags = 0;

                for (int j = 0; j < flagsTagList.size(); ++j)
                {
                    final String flag = flagsTagList.getString(j);
                    flags = Utils.setFlag(flags, Action.valueOf(flag).getFlag());
                }
                permissionMap.put(rank, flags);
            }
        }

        restoreOwnerIfNull();
    }

    /**
     * Restores the owner from other variables if he is null on loading.
     */
    public void restoreOwnerIfNull()
    {
        final Map.Entry<UUID, Player> owner = getOwnerEntry();
        if (owner == null && ownerUUID != null)
        {
            final GameProfile player = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(ownerUUID);

            if (player != null)
            {
                players.put(ownerUUID, new Player(ownerUUID, player.getName(), ranks.get(OWNER_RANK_ID)));
            }
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
    public Map.Entry<UUID, Player> getOwnerEntry()
    {
        for (@NotNull final Map.Entry<UUID, Player> entry : players.entrySet())
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
    public boolean setOwner(final PlayerEntity player)
    {
        players.remove(getOwner());

        ownerName = player.getName().getString();
        ownerUUID = player.getUniqueID();

        players.put(ownerUUID, new Player(ownerUUID, player.getName().getString(), ranks.get(OWNER_RANK_ID)));

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
        players.remove(getOwner());

        ownerName = "[abandoned]";
        ownerUUID = UUID.randomUUID();

        players.put(ownerUUID, new Player(ownerUUID, ownerName, ranks.get(OWNER_RANK_ID)));

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
            final Map.Entry<UUID, Player> owner = getOwnerEntry();
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
    public void savePermissions(@NotNull final CompoundNBT compound)
    {
        //  Ranks
        @NotNull final ListNBT rankTagList = new ListNBT();
        for (@NotNull final Rank rank : ranks.values())
        {
            @NotNull final CompoundNBT rankCompound = new CompoundNBT();
            rankCompound.putInt(TAG_ID, rank.getId());
            rankCompound.putString(TAG_NAME, rank.getName());
            rankCompound.putBoolean(TAG_SUBSCRIBER, rank.isSubscriber());
            rankCompound.putBoolean(TAG_INITIAL, rank.isInitial());
            rankTagList.add(rankCompound);
        }
        compound.put(TAG_RANKS, rankTagList);

        //  Owners
        @NotNull final ListNBT ownerTagList = new ListNBT();
        for (@NotNull final Player player : players.values())
        {
            @NotNull final CompoundNBT ownersCompound = new CompoundNBT();
            ownersCompound.putString(TAG_ID, player.getID().toString());
            ownersCompound.putString(TAG_NAME, player.getName());
            ownersCompound.putInt(TAG_RANK, player.getRank().getId());
            ownerTagList.add(ownersCompound);
        }
        compound.put(TAG_OWNERS, ownerTagList);

        // Permissions
        @NotNull final ListNBT permissionsTagList = new ListNBT();
        for (@NotNull final Map.Entry<Rank, Integer> entry : permissionMap.entrySet())
        {
            @NotNull final CompoundNBT permissionsCompound = new CompoundNBT();
            permissionsCompound.putInt(TAG_RANK, entry.getKey().getId());

            @NotNull final ListNBT flagsTagList = new ListNBT();
            for (@NotNull final Action action : Action.values())
            {
                if (Utils.testFlag(entry.getValue(), action.getFlag()))
                {
                    flagsTagList.add(StringNBT.valueOf(action.name()));
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
    public Map<UUID, Player> getPlayers()
    {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Checks if a oldRank can perform an action.
     *
     * @param rank   Rank you want to check.
     * @param action Action you want to perform.
     * @return true if oldRank has permission for action, otherwise false.
     */
    @Override
    public boolean hasPermission(final Rank rank, @NotNull final Action action)
    {
        return (rank == ranks.get(OWNER_RANK_ID) && action != Action.GUARDS_ATTACK)
                 || Utils.testFlag(permissionMap.get(rank), action.getFlag())
                 || (fullyAbandoned && Utils.testFlag(fullyAbandonedPermissionsFlag, action.getFlag()));
    }

    /**
     * Gets all player by a certain oldRank.
     *
     * @param rank the rank.
     * @return set of players.
     */
    @Override
    public Set<Player> getPlayersByRank(final Rank rank)
    {
        return this.players.values().stream()
                 .filter(player -> player.getRank().equals(rank))
                 .collect(Collectors.toSet());
    }

    /**
     * Gets all player by a set of oldRanks.
     *
     * @param ranks the set of Ranks.
     * @return set of players.
     */
    @Override
    public Set<Player> getPlayersByRank(@NotNull final Set<Rank> ranks)
    {
        return this.players.values().stream()
                 .filter(player -> ranks.contains(player.getRank()))
                 .collect(Collectors.toSet());
    }

    /**
     * Returns the map of permissionMap and ranks.
     *
     * @return map of permissionMap.
     */
    @NotNull
    public Map<Rank, Integer> getPermissionMap()
    {
        return permissionMap;
    }

    /**
     * Checks if the player has the permission of an action.
     *
     * @param player {@link PlayerEntity} player.
     * @param action {@link Action} action.
     * @return true if player has permissionMap, otherwise false.
     */
    @Override
    public boolean hasPermission(@NotNull final PlayerEntity player, @NotNull final Action action)
    {
        return hasPermission(getRank(player), action);
    }

    /**
     * Returns the rank of a player.
     *
     * @param player player to check rank.
     * @return Rank of the player.
     */
    @NotNull
    public Rank getRank(@NotNull final PlayerEntity player)
    {
        return getRank(player.getGameProfile().getId());
    }

    /**
     * Remove permission for a specific oldRank.
     *
     * @param rank   OldRank to remove permission.
     * @param action Action to remove from rank.
     */
    public boolean removePermission(final Rank rank, @NotNull final Action action)
    {
        final int flags = permissionMap.get(rank);
        if (Utils.testFlag(flags, action.getFlag()))
        {
            permissionMap.put(rank, Utils.unsetFlag(flags, action.getFlag()));
            markDirty();

            return true;
        }

        return false;
    }

    /**
     * Sets the player's oldRank to a given oldRank.
     *
     * @param id    UUID of the player of the new oldRank.
     * @param rank  Desired rank.
     * @param world the world the player is in.
     * @return True if successful, otherwise false.
     */
    @Override
    public boolean setPlayerRank(final UUID id, final Rank rank, final World world)
    {

        final Player player = getPlayers().get(id);

        if (player != null)
        {
            player.setRank(rank);

            if (rank == ranks.get(OFFICER_RANK_ID) || rank == ranks.get(OWNER_RANK_ID))
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

            final GameProfile gameprofile = world.getServer().getPlayerProfileCache().getProfileByUUID(id);

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
        @NotNull final Player p = new Player(id, name, rank);

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
        final Player player = players.get(id);
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
    public boolean addPlayer(@NotNull final String player, final Rank rank, final World world)
    {
        if (player.isEmpty())
        {
            return false;
        }
        final GameProfile gameprofile = world.getServer().getPlayerProfileCache().getGameProfileForUsername(player);
        //Check if the player already exists so that their rank isn't overridden

        // Adds new subscribers
        if (!world.isRemote() && gameprofile != null)
        {
            final ServerPlayerEntity playerEntity = (ServerPlayerEntity) world.getPlayerByUuid(gameprofile.getId());
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
                    final Chunk chunk = world.getChunk(playerEntity.chunkCoordX, playerEntity.chunkCoordZ);

                    final IColonyTagCapability colonyCap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (colonyCap != null)
                    {
                        if (colonyCap.getOwningColony() == colony.getID() && world.getDimensionKey() == colony.getDimension())
                        {
                            colony.getPackageManager().addCloseSubscriber(playerEntity);
                            colony.getPackageManager().updateSubscribers();
                        }
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
        @NotNull final Player p = new Player(gameprofile.getId(), gameprofile.getName(), rank);

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
     * @return True if successfull, otherwise false.
     */
    public boolean removePlayer(final UUID id)
    {
        final Player player = players.get(id);
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
            final Map.Entry<UUID, Player> owner = getOwnerEntry();
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
     * @param player {@link PlayerEntity} to check for subscription.
     * @return True is subscriber, otherwise false.
     */
    @Override
    public boolean isSubscriber(@NotNull final PlayerEntity player)
    {
        return isSubscriber(player.getGameProfile().getId());
    }

    /**
     * See {@link #isSubscriber(PlayerEntity)}.
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
     * @param buf        {@link PacketBuffer} to write to.
     * @param viewerRank Rank of the viewer.
     */
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf, @NotNull final Rank viewerRank)
    {
        buf.writeInt(ranks.size());
        for (Rank rank : ranks.values())
        {
            buf.writeInt(rank.getId());
            buf.writeString(rank.getName());
            buf.writeBoolean(rank.isSubscriber());
            buf.writeBoolean(rank.isInitial());
        }

        buf.writeInt(viewerRank.getId());

        //  Owners
        buf.writeInt(players.size());
        for (@NotNull final Map.Entry<UUID, Player> player : players.entrySet())
        {
            PacketUtils.writeUUID(buf, player.getKey());
            buf.writeString(player.getValue().getName());
            buf.writeInt(player.getValue().getRank().getId());
        }

        // Permissions
        buf.writeInt(permissionMap.size());
        for (@NotNull final Map.Entry<Rank, Integer> entry : permissionMap.entrySet())
        {
            buf.writeInt(entry.getKey().getId());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public boolean isColonyMember(@NotNull final PlayerEntity player)
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
    public Rank getRankHostile()
    {
        return ranks.get(HOSTILE_RANK_ID);
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
    public Map<Integer, Rank> getRanks()
    {
        return ranks;
    }

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
        permissionMap.put(rank, 0);
        markDirty();
    }

    private static class RankPair
    {
        /**
         * The rank if promoted.
         */
        private final OldRank promote;

        /**
         * The rank if demoted.
         */
        private final OldRank demote;

        /**
         * Links promotion and demotion.
         *
         * @param p Promoting rank.
         * @param d Demoting rank.
         */
        RankPair(final OldRank p, final OldRank d)
        {
            promote = p;
            demote = d;
        }
    }
}
