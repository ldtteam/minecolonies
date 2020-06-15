package com.minecolonies.coremod.colony.permissions;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.util.Utils;
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
    private static final String TAG_OWNERS      = "owners";
    private static final String TAG_ID          = "id";
    private static final String TAG_RANK        = "rank";
    private static final String TAG_PERMISSIONS = "permissionMap";
    private static final String TAG_FLAGS       = "flags";
    private static final String TAG_OWNER       = "owner";
    private static final String TAG_OWNER_ID    = "ownerid";

    /**
     * NBTTarget for the permission version, used for updating.
     */
    private static final String TAG_VERSION = "permissionVersion";

    /**
     * All promotion rank possibilities.
     */
    @NotNull
    private static final Map<Rank, RankPair> promotionRanks = new EnumMap<>(Rank.class);
    /**
     * Fill the promotion ranks.
     */
    static
    {
        setPromotionRanks(Rank.OFFICER, Rank.OFFICER, Rank.FRIEND);
        setPromotionRanks(Rank.FRIEND, Rank.OFFICER, Rank.NEUTRAL);
        setPromotionRanks(Rank.NEUTRAL, Rank.FRIEND, Rank.HOSTILE);
        setPromotionRanks(Rank.HOSTILE, Rank.NEUTRAL, Rank.HOSTILE);
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
    private final Map<Rank, Integer> permissionMap = new EnumMap<>(Rank.class);

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
     * The current version of the permissions, increase upon changes to the preset permissions
     */
    private static final int permissionsVersion = 1;

    /**
     * Saves the permissionMap with allowed actions.
     *
     * @param colony the colony this permissionMap object belongs to.
     */
    public Permissions(@NotNull final Colony colony)
    {
        //Owner
        permissionMap.put(Rank.OWNER, 0);
        this.setPermission(Rank.OWNER, Action.ACCESS_HUTS);
        this.setPermission(Rank.OWNER, Action.PLACE_HUTS);
        this.setPermission(Rank.OWNER, Action.BREAK_HUTS);
        this.setPermission(Rank.OWNER, Action.CAN_PROMOTE);
        this.setPermission(Rank.OWNER, Action.CAN_DEMOTE);
        this.setPermission(Rank.OWNER, Action.SEND_MESSAGES);
        this.setPermission(Rank.OWNER, Action.EDIT_PERMISSIONS);
        this.setPermission(Rank.OWNER, Action.MANAGE_HUTS);
        this.setPermission(Rank.OWNER, Action.RECEIVE_MESSAGES);
        this.setPermission(Rank.OWNER, Action.USE_SCAN_TOOL);
        this.setPermission(Rank.OWNER, Action.PLACE_BLOCKS);
        this.setPermission(Rank.OWNER, Action.BREAK_BLOCKS);
        this.setPermission(Rank.OWNER, Action.TOSS_ITEM);
        this.setPermission(Rank.OWNER, Action.PICKUP_ITEM);
        this.setPermission(Rank.OWNER, Action.FILL_BUCKET);
        this.setPermission(Rank.OWNER, Action.OPEN_CONTAINER);
        this.setPermission(Rank.OWNER, Action.RIGHTCLICK_BLOCK);
        this.setPermission(Rank.OWNER, Action.RIGHTCLICK_ENTITY);
        this.setPermission(Rank.OWNER, Action.THROW_POTION);
        this.setPermission(Rank.OWNER, Action.SHOOT_ARROW);
        this.setPermission(Rank.OWNER, Action.ATTACK_CITIZEN);
        this.setPermission(Rank.OWNER, Action.ATTACK_ENTITY);
        this.setPermission(Rank.OWNER, Action.ACCESS_FREE_BLOCKS);
        this.setPermission(Rank.OWNER, Action.TELEPORT_TO_COLONY);
        this.setPermission(Rank.OWNER, Action.RECEIVE_MESSAGES_FAR_AWAY);
        this.setPermission(Rank.OWNER, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY);
        this.setPermission(Rank.OWNER, Action.RALLY_GUARDS);

        //Officer
        permissionMap.put(Rank.OFFICER, 0);
        this.setPermission(Rank.OFFICER, Action.ACCESS_HUTS);
        this.setPermission(Rank.OFFICER, Action.PLACE_HUTS);
        this.setPermission(Rank.OFFICER, Action.BREAK_HUTS);
        this.setPermission(Rank.OFFICER, Action.CAN_PROMOTE);
        this.setPermission(Rank.OFFICER, Action.CAN_DEMOTE);
        this.setPermission(Rank.OFFICER, Action.SEND_MESSAGES);
        this.setPermission(Rank.OFFICER, Action.MANAGE_HUTS);
        this.setPermission(Rank.OFFICER, Action.RECEIVE_MESSAGES);
        this.setPermission(Rank.OFFICER, Action.USE_SCAN_TOOL);
        this.setPermission(Rank.OFFICER, Action.PLACE_BLOCKS);
        this.setPermission(Rank.OFFICER, Action.BREAK_BLOCKS);
        this.setPermission(Rank.OFFICER, Action.TOSS_ITEM);
        this.setPermission(Rank.OFFICER, Action.PICKUP_ITEM);
        this.setPermission(Rank.OFFICER, Action.FILL_BUCKET);
        this.setPermission(Rank.OFFICER, Action.OPEN_CONTAINER);
        this.setPermission(Rank.OFFICER, Action.RIGHTCLICK_BLOCK);
        this.setPermission(Rank.OFFICER, Action.RIGHTCLICK_ENTITY);
        this.setPermission(Rank.OFFICER, Action.THROW_POTION);
        this.setPermission(Rank.OFFICER, Action.SHOOT_ARROW);
        this.setPermission(Rank.OFFICER, Action.ATTACK_CITIZEN);
        this.setPermission(Rank.OFFICER, Action.ATTACK_ENTITY);
        this.setPermission(Rank.OFFICER, Action.ACCESS_FREE_BLOCKS);
        this.setPermission(Rank.OFFICER, Action.TELEPORT_TO_COLONY);
        this.setPermission(Rank.OFFICER, Action.RECEIVE_MESSAGES_FAR_AWAY);
        this.setPermission(Rank.OFFICER, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY);


        //Friend
        permissionMap.put(Rank.FRIEND, 0);
        this.setPermission(Rank.FRIEND, Action.ACCESS_HUTS);
        this.setPermission(Rank.FRIEND, Action.USE_SCAN_TOOL);
        this.setPermission(Rank.FRIEND, Action.TOSS_ITEM);
        this.setPermission(Rank.FRIEND, Action.PICKUP_ITEM);
        this.setPermission(Rank.FRIEND, Action.RIGHTCLICK_BLOCK);
        this.setPermission(Rank.FRIEND, Action.RIGHTCLICK_ENTITY);
        this.setPermission(Rank.FRIEND, Action.THROW_POTION);
        this.setPermission(Rank.FRIEND, Action.SHOOT_ARROW);
        this.setPermission(Rank.FRIEND, Action.ATTACK_CITIZEN);
        this.setPermission(Rank.FRIEND, Action.ATTACK_ENTITY);
        this.setPermission(Rank.FRIEND, Action.ACCESS_FREE_BLOCKS);
        this.setPermission(Rank.FRIEND, Action.TELEPORT_TO_COLONY);


        //Neutral
        permissionMap.put(Rank.NEUTRAL, 0);
        this.setPermission(Rank.NEUTRAL, Action.ACCESS_FREE_BLOCKS);

        //Hostile
        permissionMap.put(Rank.HOSTILE, 0);
        this.setPermission(Rank.HOSTILE, Action.GUARDS_ATTACK);

        this.clearDirty();
        this.colony = colony;
    }

    /**
     * Sets the rank for a specific action.
     *
     * @param rank   Desired rank.
     * @param action Action that should have desired rank.
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
        if(colony != null)
        {
            colony.markDirty();
        }
    }

    /**
     * Stores the list of promotion/demotion ranks.
     *
     * @param r Rank to set pro- and demotion of.
     * @param p Promotion rank.
     * @param d Demotion rank.
     */
    private static void setPromotionRanks(final Rank r, final Rank p, final Rank d)
    {
        promotionRanks.put(r, new RankPair(p, d));
    }

    /**
     * Returns the promotion rank of a specific rank.
     * E.G.: Neutral will return Friend.
     *
     * @param rank Rank to check promotion of.
     * @return {@link Rank} after promotion.
     */
    public static Rank getPromotionRank(final Rank rank)
    {
        if (promotionRanks.containsKey(rank))
        {
            return promotionRanks.get(rank).promote;
        }

        return rank;
    }

    /**
     * Returns the demotion rank of a specific rank.
     * E.G.: Neutral will return Hostile.
     *
     * @param rank Rank to check demotion of.
     * @return {@link Rank} after demotion.
     */
    public static Rank getDemotionRank(final Rank rank)
    {
        if (promotionRanks.containsKey(rank))
        {
            return promotionRanks.get(rank).demote;
        }

        return rank;
    }

    /**
     * Toggle permission for a specific rank.
     *
     * @param rank   Rank to toggle permission.
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
        players.clear();
        //  Owners
        final ListNBT ownerTagList = compound.getList(TAG_OWNERS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < ownerTagList.size(); ++i)
        {
            final CompoundNBT ownerCompound = ownerTagList.getCompound(i);
            @NotNull final UUID id = UUID.fromString(ownerCompound.getString(TAG_ID));
            String name = "";
            if (ownerCompound.keySet().contains(TAG_NAME))
            {
                name = ownerCompound.getString(TAG_NAME);
            }

            final Rank rank = Rank.valueOf(ownerCompound.getString(TAG_RANK));

            final GameProfile player = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(id);

            if (player != null)
            {
                players.put(id, new Player(id, player.getName(), rank));
            }
            else if(!name.isEmpty())
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
                final Rank rank = Rank.valueOf(permissionsCompound.getString(TAG_RANK));

                final ListNBT flagsTagList = permissionsCompound.getList(TAG_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

                int flags = 0;

                for (int j = 0; j < flagsTagList.size(); ++j)
                {
                    final String flag = flagsTagList.getString(j);
                    flags = Utils.setFlag(flags, Action.valueOf(flag).getFlag());
                }
                permissionMap.put(rank, flags);
            }

            if (compound.keySet().contains(TAG_OWNER))
            {
                ownerName = compound.getString(TAG_OWNER);
            }
            if (compound.keySet().contains(TAG_OWNER_ID))
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
                players.put(ownerUUID, new Player(ownerUUID, player.getName(), Rank.OWNER));
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
            if (entry.getValue().getRank().equals(Rank.OWNER))
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
     * @return true if succesful.
     */
    @Override
    public boolean setOwner(final PlayerEntity player)
    {
        players.remove(getOwner());

        ownerName = player.getName().getFormattedText();
        ownerUUID = player.getUniqueID();

        players.put(ownerUUID, new Player(ownerUUID, player.getName().getFormattedText(), Rank.OWNER));

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

        players.put(ownerUUID, new Player(ownerUUID, ownerName, Rank.OWNER));

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
        //  Owners
        @NotNull final ListNBT ownerTagList = new ListNBT();
        for (@NotNull final Player player : players.values())
        {
            @NotNull final CompoundNBT ownersCompound = new CompoundNBT();
            ownersCompound.putString(TAG_ID, player.getID().toString());
            ownersCompound.putString(TAG_NAME, player.getName());
            ownersCompound.putString(TAG_RANK, player.getRank().name());
            ownerTagList.add(ownersCompound);
        }
        compound.put(TAG_OWNERS, ownerTagList);

        // Permissions
        @NotNull final ListNBT permissionsTagList = new ListNBT();
        for (@NotNull final Map.Entry<Rank, Integer> entry : permissionMap.entrySet())
        {
            @NotNull final CompoundNBT permissionsCompound = new CompoundNBT();
            permissionsCompound.putString(TAG_RANK, entry.getKey().name());

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

        compound.putInt(TAG_VERSION, permissionsVersion);
    }

    @Override
    @NotNull
    public Map<UUID, Player> getPlayers()
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
        return (rank == Rank.OWNER && action != Action.GUARDS_ATTACK)
                 || Utils.testFlag(permissionMap.get(rank), action.getFlag());
    }

    /**
     * Gets all player by a certain rank.
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
     * Gets all player by a set of ranks.
     *
     * @param ranks the set of ranks.
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
     * @return Rank of te player.
     */
    @NotNull
    public Rank getRank(@NotNull final PlayerEntity player)
    {
        return getRank(player.getGameProfile().getId());
    }

    /**
     * Remove permission for a specific rank.
     *
     * @param rank   Rank to remove permission.
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
     * Sets the player's rank to a given rank.
     *
     * @param id    UUID of the player of the new rank.
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
     * @param id UUID of the player..
     * @param rank Desired rank.
     * @param name name of the player.
     * @return True if succesful, otherwise false.
     */
    @Override
    public boolean addPlayer(@NotNull final UUID id, final String name, final Rank rank)
    {
        @NotNull final Player p = new Player(id, name, rank);

        players.remove(p.getID());
        players.put(p.getID(), p);

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
        return player != null ? player.getRank() : Rank.NEUTRAL;
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
                if (rank == Rank.OFFICER)
                {
                    colony.getPackageManager().addImportantColonyPlayer(playerEntity);
                    colony.getPackageManager().updateSubscribers();
                }
                else
                {
                    // Check claim
                    final Chunk chunk = world.getChunk(playerEntity.chunkCoordX, playerEntity.chunkCoordZ);

                    final IColonyTagCapability colonyCap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (colonyCap != null)
                    {
                        if (colonyCap.getOwningColony() == colony.getID() && world.getDimension().getType().getId() == colony.getDimension())
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
     * @return True if succesful, otherwise false.
     */
    @Override
    public boolean addPlayer(@NotNull final GameProfile gameprofile, final Rank rank)
    {
        @NotNull final Player p = new Player(gameprofile.getId(), gameprofile.getName(), rank);

        players.remove(p.getID());
        players.put(p.getID(), p);

        markDirty();
        return true;
    }

    /**
     * Remove a player from the permissionMap.
     *
     * @param id UUID of the player.
     * @return True if succesfull, otherwise false.
     */
    public boolean removePlayer(final UUID id)
    {
        final Player player = players.get(id);
        if (player != null && player.getRank() != Rank.OWNER && players.remove(id) != null)
        {
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
        return getRank(player).isSubscriber;
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
        buf.writeString(viewerRank.name());

        //  Owners
        buf.writeInt(players.size());
        for (@NotNull final Map.Entry<UUID, Player> player : players.entrySet())
        {
            PacketUtils.writeUUID(buf, player.getKey());
            buf.writeString(player.getValue().getName());
            buf.writeString(player.getValue().getRank().name());
        }

        // Permissions
        buf.writeInt(permissionMap.size());
        for (@NotNull final Map.Entry<Rank, Integer> entry : permissionMap.entrySet())
        {
            buf.writeString(entry.getKey().name());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public boolean isColonyMember(@NotNull final PlayerEntity player)
    {
        return players.containsKey(player.getGameProfile().getId());
    }

    private static class RankPair
    {
        /**
         * The rank if promoted.
         */
        private final Rank promote;

        /**
         * The rank if demoted.
         */
        private final Rank demote;

        /**
         * Links promotion and demotion.
         *
         * @param p Promoting rank.
         * @param d Demoting rank.
         */
        RankPair(final Rank p, final Rank d)
        {
            promote = p;
            demote = d;
        }
    }
}
