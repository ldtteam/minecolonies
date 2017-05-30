package com.minecolonies.coremod.colony.permissions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.Utils;
import com.minecolonies.coremod.network.PacketUtils;
import com.minecolonies.coremod.util.AchievementUtils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.permissions.Action.*;
import static com.minecolonies.api.colony.permissions.Rank.*;

/**
 * Colony Permissions System.
 */
public class Permissions implements IPermissions
{
    /**
     * All tags to store and retrieve data from nbt.
     */
    private static final String TAG_UPDATE      = "update";
    private static final String TAG_OWNERS      = "owners";
    private static final String TAG_ID          = "id";
    private static final String TAG_RANK        = "rank";
    private static final String TAG_PERMISSIONS = "permissionMap";
    private static final String TAG_FLAGS       = "flags";
    private static final String TAG_OWNER       = "owner";
    private static final String TAG_OWNER_ID    = "ownerid";

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
        setPromotionRanks(OFFICER, OFFICER, FRIEND);
        setPromotionRanks(FRIEND, OFFICER, NEUTRAL);
        setPromotionRanks(NEUTRAL, FRIEND, HOSTILE);
        setPromotionRanks(HOSTILE, NEUTRAL, HOSTILE);
    }

    /**
     * The colony the permissions belong to.
     */
    @NotNull
    private final IColony colony;

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
     * Is it an old colony and has the permission been already updated?
     */
    private boolean updatedPermissionAlready = false;

    /**
     * Saves the permissionMap with allowed actions.
     *
     * @param colony the colony this permissionMap object belongs to.
     */
    public Permissions(@NotNull final IColony colony)
    {
        //Owner
        permissionMap.put(OWNER, 0);
        this.setPermission(OWNER, ACCESS_HUTS);
        this.setPermission(OWNER, PLACE_HUTS);
        this.setPermission(OWNER, BREAK_HUTS);
        this.setPermission(OWNER, CAN_PROMOTE);
        this.setPermission(OWNER, CAN_DEMOTE);
        this.setPermission(OWNER, SEND_MESSAGES);
        this.setPermission(OWNER, EDIT_PERMISSIONS);
        this.setPermission(OWNER, MANAGE_HUTS);
        this.setPermission(OWNER, RECEIVE_MESSAGES);
        this.setPermission(OWNER, USE_SCAN_TOOL);
        this.setPermission(OWNER, PLACE_BLOCKS);
        this.setPermission(OWNER, BREAK_BLOCKS);
        this.setPermission(OWNER, TOSS_ITEM);
        this.setPermission(OWNER, PICKUP_ITEM);
        this.setPermission(OWNER, FILL_BUCKET);
        this.setPermission(OWNER, OPEN_CONTAINER);
        this.setPermission(OWNER, RIGHTCLICK_BLOCK);
        this.setPermission(OWNER, RIGHTCLICK_ENTITY);
        this.setPermission(OWNER, THROW_POTION);
        this.setPermission(OWNER, SHOOT_ARROW);
        this.setPermission(OWNER, ATTACK_CITIZEN);
        this.setPermission(OWNER, ATTACK_ENTITY);
        this.setPermission(OWNER, ACCESS_FREE_BLOCKS);
        this.setPermission(OWNER, TELEPORT_TO_COLONY);


        //Officer
        permissionMap.put(OFFICER, 0);
        this.setPermission(OFFICER, ACCESS_HUTS);
        this.setPermission(OFFICER, PLACE_HUTS);
        this.setPermission(OFFICER, BREAK_HUTS);
        this.setPermission(OFFICER, CAN_PROMOTE);
        this.setPermission(OFFICER, CAN_DEMOTE);
        this.setPermission(OFFICER, SEND_MESSAGES);
        this.setPermission(OFFICER, MANAGE_HUTS);
        this.setPermission(OFFICER, RECEIVE_MESSAGES);
        this.setPermission(OFFICER, USE_SCAN_TOOL);
        this.setPermission(OFFICER, PLACE_BLOCKS);
        this.setPermission(OFFICER, BREAK_BLOCKS);
        this.setPermission(OFFICER, TOSS_ITEM);
        this.setPermission(OFFICER, PICKUP_ITEM);
        this.setPermission(OFFICER, FILL_BUCKET);
        this.setPermission(OFFICER, OPEN_CONTAINER);
        this.setPermission(OFFICER, RIGHTCLICK_BLOCK);
        this.setPermission(OFFICER, RIGHTCLICK_ENTITY);
        this.setPermission(OFFICER, THROW_POTION);
        this.setPermission(OFFICER, SHOOT_ARROW);
        this.setPermission(OFFICER, ATTACK_CITIZEN);
        this.setPermission(OFFICER, ATTACK_ENTITY);
        this.setPermission(OFFICER, ACCESS_FREE_BLOCKS);
        this.setPermission(OFFICER, TELEPORT_TO_COLONY);


        //Friend
        permissionMap.put(FRIEND, 0);
        this.setPermission(FRIEND, ACCESS_HUTS);
        this.setPermission(FRIEND, USE_SCAN_TOOL);
        this.setPermission(FRIEND, TOSS_ITEM);
        this.setPermission(FRIEND, PICKUP_ITEM);
        this.setPermission(FRIEND, RIGHTCLICK_BLOCK);
        this.setPermission(FRIEND, RIGHTCLICK_ENTITY);
        this.setPermission(FRIEND, THROW_POTION);
        this.setPermission(FRIEND, SHOOT_ARROW);
        this.setPermission(FRIEND, ATTACK_CITIZEN);
        this.setPermission(FRIEND, ATTACK_ENTITY);
        this.setPermission(FRIEND, ACCESS_FREE_BLOCKS);
        this.setPermission(FRIEND, TELEPORT_TO_COLONY);


        //Neutral
        permissionMap.put(NEUTRAL, 0);
        this.setPermission(NEUTRAL, ACCESS_FREE_BLOCKS);

        //Hostile
        permissionMap.put(HOSTILE, 0);
        this.setPermission(HOSTILE, GUARDS_ATTACK);

        this.colony = colony;

        updatedPermissionAlready = true;
    }

    /**
     * Sets the rank for a specific action.
     *
     * @param rank   Desired rank.
     * @param action Action that should have desired rank.
     */
    public final void setPermission(final Rank rank, @NotNull final Action action)
    {
        final int flags = permissionMap.get(rank);

        //check that flag isn't set
        if (!Utils.testFlag(flags, action.getFlag()))
        {
            permissionMap.put(rank, Utils.setFlag(flags, action.getFlag()));
            markDirty();
        }
    }

    /**
     * Marks instance dirty.
     */
    private void markDirty()
    {
        dirty = true;
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
    public void loadPermissions(@NotNull final NBTTagCompound compound)
    {
        //  Owners
        final NBTTagList ownerTagList = compound.getTagList(TAG_OWNERS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < ownerTagList.tagCount(); ++i)
        {
            final NBTTagCompound ownerCompound = ownerTagList.getCompoundTagAt(i);
            @NotNull final UUID id = UUID.fromString(ownerCompound.getString(TAG_ID));
            final Rank rank = Rank.valueOf(ownerCompound.getString(TAG_RANK));

            final GameProfile player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(id);

            if (player != null)
            {
                players.put(id, new Player(id, player.getName(), rank));
            }
        }

        //Permissions
        final NBTTagList permissionsTagList = compound.getTagList(TAG_PERMISSIONS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < permissionsTagList.tagCount(); ++i)
        {
            final NBTTagCompound permissionsCompound = permissionsTagList.getCompoundTagAt(i);
            final Rank rank = Rank.valueOf(permissionsCompound.getString(TAG_RANK));

            final NBTTagList flagsTagList = permissionsCompound.getTagList(TAG_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

            int flags = 0;

            for (int j = 0; j < flagsTagList.tagCount(); ++j)
            {
                final String flag = flagsTagList.getStringTagAt(j);
                flags = Utils.setFlag(flags, Action.valueOf(flag).getFlag());
            }
            permissionMap.put(rank, flags);
        }

        if (compound.hasKey(TAG_OWNER))
        {
            ownerName = compound.getString(TAG_OWNER);
        }
        if (compound.hasKey(TAG_OWNER_ID))
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

        this.updatedPermissionAlready = compound.getBoolean(TAG_UPDATE);

        if (!updatedPermissionAlready)
        {
            updateNewPermissions();
        }

        restoreOwnerIfNull();
    }

    /**
     * This method should be used to update new permissionMap added to the game which old colonies probably don't have yet.
     */
    private void updateNewPermissions()
    {
        this.setPermission(OWNER, MANAGE_HUTS);
        this.setPermission(OWNER, RECEIVE_MESSAGES);
        this.setPermission(OWNER, USE_SCAN_TOOL);
        this.setPermission(OWNER, PLACE_BLOCKS);
        this.setPermission(OWNER, BREAK_BLOCKS);
        this.setPermission(OWNER, TOSS_ITEM);
        this.setPermission(OWNER, PICKUP_ITEM);
        this.setPermission(OWNER, FILL_BUCKET);
        this.setPermission(OWNER, OPEN_CONTAINER);
        this.setPermission(OWNER, RIGHTCLICK_BLOCK);
        this.setPermission(OWNER, RIGHTCLICK_ENTITY);
        this.setPermission(OWNER, THROW_POTION);
        this.setPermission(OWNER, SHOOT_ARROW);
        this.setPermission(OWNER, ATTACK_CITIZEN);
        this.setPermission(OWNER, ATTACK_ENTITY);
        this.setPermission(OWNER, ACCESS_FREE_BLOCKS);
        this.setPermission(OWNER, TELEPORT_TO_COLONY);

        this.setPermission(OFFICER, MANAGE_HUTS);
        this.setPermission(OFFICER, RECEIVE_MESSAGES);
        this.setPermission(OFFICER, USE_SCAN_TOOL);
        this.setPermission(OFFICER, PLACE_BLOCKS);
        this.setPermission(OFFICER, BREAK_BLOCKS);
        this.setPermission(OFFICER, TOSS_ITEM);
        this.setPermission(OFFICER, PICKUP_ITEM);
        this.setPermission(OFFICER, FILL_BUCKET);
        this.setPermission(OFFICER, OPEN_CONTAINER);
        this.setPermission(OFFICER, RIGHTCLICK_BLOCK);
        this.setPermission(OFFICER, RIGHTCLICK_ENTITY);
        this.setPermission(OFFICER, THROW_POTION);
        this.setPermission(OFFICER, SHOOT_ARROW);
        this.setPermission(OFFICER, ATTACK_CITIZEN);
        this.setPermission(OFFICER, ATTACK_ENTITY);
        this.setPermission(OFFICER, ACCESS_FREE_BLOCKS);
        this.setPermission(OFFICER, TELEPORT_TO_COLONY);

        this.setPermission(FRIEND, ACCESS_HUTS);
        this.setPermission(FRIEND, USE_SCAN_TOOL);
        this.setPermission(FRIEND, TOSS_ITEM);
        this.setPermission(FRIEND, PICKUP_ITEM);
        this.setPermission(FRIEND, RIGHTCLICK_BLOCK);
        this.setPermission(FRIEND, RIGHTCLICK_ENTITY);
        this.setPermission(FRIEND, THROW_POTION);
        this.setPermission(FRIEND, SHOOT_ARROW);
        this.setPermission(FRIEND, ATTACK_CITIZEN);
        this.setPermission(FRIEND, ATTACK_ENTITY);
        this.setPermission(FRIEND, ACCESS_FREE_BLOCKS);
        this.setPermission(FRIEND, TELEPORT_TO_COLONY);

        this.setPermission(NEUTRAL, ACCESS_FREE_BLOCKS);

        updatedPermissionAlready = true;
    }

    /**
     * Restores the owner from other variables if he is null on loading.
     */
    public void restoreOwnerIfNull()
    {
        final Map.Entry<UUID, Player> owner = getOwnerEntry();
        if (owner == null && ownerUUID != null)
        {
            final GameProfile player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(ownerUUID);

            if (player != null)
            {
                players.put(ownerUUID, new Player(ownerUUID, player.getName(), OWNER));
            }
        }
        markDirty();
    }

    /**
     * Compute the owner of a colony.
     * <p>
     * Can be quite expensive in colonies with many players.
     *
     * @return the corresponding entry or null.
     */
    @Nullable
    private Map.Entry<UUID, Player> getOwnerEntry()
    {
        for (@NotNull final Map.Entry<UUID, Player> entry : players.entrySet())
        {
            if (entry.getValue().getRank().equals(OWNER))
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
    public boolean setOwner(final EntityPlayer player)
    {
        players.remove(getOwner());

        ownerName = player.getName();
        ownerUUID = player.getUniqueID();

        players.put(ownerUUID, new Player(ownerUUID, player.getName(), OWNER));

        markDirty();
        return true;
    }

    @Override
    @Nullable
    public UUID getOwner()
    {
        if (ownerUUID == null)
        {
            final Map.Entry<UUID, Player> owner = getOwnerEntry();
            if (owner != null)
            {
                ownerUUID = owner.getKey();
            }
        }
        return ownerUUID;
    }

    /**
     * Save the permissionMap to a NBT.
     *
     * @param compound NBT to write to.
     */
    public void savePermissions(@NotNull final NBTTagCompound compound)
    {
        //  Owners
        @NotNull final NBTTagList ownerTagList = new NBTTagList();
        for (@NotNull final Player player : players.values())
        {
            @NotNull final NBTTagCompound ownersCompound = new NBTTagCompound();
            ownersCompound.setString(TAG_ID, player.getID().toString());
            ownersCompound.setString(TAG_RANK, player.getRank().name());
            ownerTagList.appendTag(ownersCompound);
        }
        compound.setTag(TAG_OWNERS, ownerTagList);

        // Permissions
        @NotNull final NBTTagList permissionsTagList = new NBTTagList();
        for (@NotNull final Map.Entry<Rank, Integer> entry : permissionMap.entrySet())
        {
            @NotNull final NBTTagCompound permissionsCompound = new NBTTagCompound();
            permissionsCompound.setString(TAG_RANK, entry.getKey().name());

            @NotNull final NBTTagList flagsTagList = new NBTTagList();
            for (@NotNull final Action action : Action.values())
            {
                if (Utils.testFlag(entry.getValue(), action.getFlag()))
                {
                    flagsTagList.appendTag(new NBTTagString(action.name()));
                }
            }
            permissionsCompound.setTag(TAG_FLAGS, flagsTagList);

            permissionsTagList.appendTag(permissionsCompound);
        }
        compound.setTag(TAG_PERMISSIONS, permissionsTagList);

        if (!ownerName.isEmpty())
        {
            compound.setString(TAG_OWNER, ownerName);
        }
        if (ownerUUID != null)
        {
            compound.setString(TAG_OWNER_ID, ownerUUID.toString());
        }

        compound.setBoolean(TAG_UPDATE, updatedPermissionAlready);
    }

    @Override
    @NotNull
    public Map<UUID, Player> getPlayers()
    {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Returns a set of UUID's that have permission to send (and receive) messages.
     *
     * @return Set of UUID's allowed to send and receive messages.
     */
    public Set<UUID> getMessagePlayers()
    {
        return players.values().stream()
                 .filter(player -> hasPermission(player.getRank(), Action.RECEIVE_MESSAGES))
                 .map(Player::getID)
                 .collect(Collectors.toSet());
    }

    /**
     * Checks if a rank can perform an action.
     *
     * @param rank   Rank you want to check.
     * @param action Action you want to perform.
     * @return true if rank has permission for action, otherwise false.
     */
    public boolean hasPermission(final Rank rank, @NotNull final Action action)
    {
        return (rank == OWNER && action != GUARDS_ATTACK)
                 || Utils.testFlag(permissionMap.get(rank), action.getFlag());
    }

    /**
     * Gets all player by a certain rank.
     *
     * @param rank the rank.
     * @return set of players.
     */
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
     * @param player {@link EntityPlayer} player.
     * @param action {@link Action} action.
     * @return true if player has permissionMap, otherwise false.
     */
    @Override
    public boolean hasPermission(@NotNull final EntityPlayer player, @NotNull final Action action)
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
    public Rank getRank(@NotNull final EntityPlayer player)
    {
        return getRank(player.getGameProfile().getId());
    }

    /**
     * Remove permission for a specific rank.
     *
     * @param rank   Rank to remove permission.
     * @param action Action to remove from rank.
     */
    public void removePermission(final Rank rank, @NotNull final Action action)
    {
        final int flags = permissionMap.get(rank);
        if (Utils.testFlag(flags, action.getFlag()))
        {
            permissionMap.put(rank, Utils.unsetFlag(flags, action.getFlag()));
            markDirty();
        }
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
     * Sets the player's rank to a given rank.
     *
     * @param id    UUID of the player of the new rank.
     * @param rank  Desired rank.
     * @param world the world the player is in.
     * @return True if successful, otherwise false.
     */
    public boolean setPlayerRank(final UUID id, final Rank rank, final World world)
    {
        final Player player = players.get(id);

        if (player != null)
        {
            player.setRank(rank);
            markDirty();
            AchievementUtils.syncAchievements(colony);
        }
        else
        {

            final GameProfile gameprofile = world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(id);

            return gameprofile != null && addPlayer(gameprofile, rank);
        }

        return true;
    }

    /**
     * Adds a player to the rankings.
     *
     * @param gameprofile GameProfile of the player.
     * @param rank        Desired rank.
     * @return True if succesful, otherwise false.
     */
    private boolean addPlayer(@NotNull final GameProfile gameprofile, final Rank rank)
    {
        @NotNull final Player p = new Player(gameprofile.getId(), gameprofile.getName(), rank);

        if (players.containsKey(p.getID()))
        {
            players.remove(p.getID());
        }
        players.put(p.getID(), p);

        markDirty();
        AchievementUtils.syncAchievements(colony);
        return true;
    }

    /**
     * Add a player to the rankings.
     *
     * @param player String playername of the player to add.
     * @param rank   Rank desired starting rank.
     * @param world  the world the player is in.
     * @return True if successful, otherwise false.
     */
    public boolean addPlayer(@NotNull final String player, final Rank rank, final World world)
    {
        if (player.isEmpty())
        {
            return false;
        }
        final GameProfile gameprofile = world.getMinecraftServer().getPlayerProfileCache().getGameProfileForUsername(player);
        //Check if the player already exists so that their rank isn't overridden
        return gameprofile != null && !players.containsKey(gameprofile.getId()) && addPlayer(gameprofile, rank);
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
        AchievementUtils.syncAchievements(colony);
        if (player != null && player.getRank() != OWNER && players.remove(id) != null)
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
     * @param player {@link EntityPlayer} to check for subscription.
     * @return True is subscriber, otherwise false.
     */
    public boolean isSubscriber(@NotNull final EntityPlayer player)
    {
        return isSubscriber(player.getGameProfile().getId());
    }

    /**
     * See {@link #isSubscriber(EntityPlayer)}.
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

    @Override
    public boolean isColonyMember(@NotNull final EntityPlayer player)
    {
        return players.containsKey(player.getGameProfile().getId());
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
     * @param buf        {@link ByteBuf} to write to.
     * @param viewerRank Rank of the viewer.
     */
    public void serializeViewNetworkData(@NotNull final ByteBuf buf, @NotNull final Rank viewerRank)
    {
        ByteBufUtils.writeUTF8String(buf, viewerRank.name());

        //  Owners
        buf.writeInt(players.size());
        for (@NotNull final Map.Entry<UUID, Player> player : players.entrySet())
        {
            PacketUtils.writeUUID(buf, player.getKey());
            ByteBufUtils.writeUTF8String(buf, player.getValue().getName());
            ByteBufUtils.writeUTF8String(buf, player.getValue().getRank().name());
        }

        // Permissions
        buf.writeInt(permissionMap.size());
        for (@NotNull final Map.Entry<Rank, Integer> entry : permissionMap.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey().name());
            buf.writeInt(entry.getValue());
        }
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

    /**
     * A client side representation of the permissions.
     */
    public static class View implements IPermissions
    {
        @NotNull
        private final Map<UUID, Player>  players     = new HashMap<>();
        @NotNull
        private final Map<Rank, Integer> permissions = new EnumMap<>(Rank.class);
        private       Rank               userRank    = NEUTRAL;

        public Rank getUserRank()
        {
            return userRank;
        }

        @Override
        @NotNull
        public Map<UUID, Player> getPlayers()
        {
            return Collections.unmodifiableMap(players);
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

        @NotNull
        public Map<Rank, Integer> getPermissions()
        {
            return permissions;
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
            return (rank == OWNER && action != GUARDS_ATTACK)
                     || Utils.testFlag(permissions.get(rank), action.getFlag());
        }

        /**
         * Sets if the rank has the permission to do an action.
         *
         * @param rank   the rank to set.
         * @param action the action he is trying to execute.
         * @return true if so.
         */
        public boolean setPermission(final Rank rank, @NotNull final Action action)
        {
            final int flags = permissions.get(rank);

            //check that flag isn't set
            if (!Utils.testFlag(flags, action.getFlag()))
            {
                permissions.put(rank, Utils.setFlag(flags, action.getFlag()));
                return true;
            }
            return false;
        }

        /**
         * Remove if the rank has the permission to do an action.
         *
         * @param rank   the rank to set.
         * @param action the action he is trying to execute.
         * @return true if so.
         */
        public boolean removePermission(final Rank rank, @NotNull final Action action)
        {
            final int flags = permissions.get(rank);
            if (Utils.testFlag(flags, action.getFlag()))
            {
                permissions.put(rank, Utils.unsetFlag(flags, action.getFlag()));
                return true;
            }
            return false;
        }

        /**
         * Toggle a permission flag.
         *
         * @param rank   the rank.
         * @param action the action.
         */
        public void togglePermission(final Rank rank, @NotNull final Action action)
        {
            permissions.put(rank, Utils.toggleFlag(permissions.get(rank), action.getFlag()));
        }

        /**
         * Deserialize content of class to a buffer.
         *
         * @param buf the buffer.
         */
        public void deserialize(@NotNull final ByteBuf buf)
        {
            userRank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));

            //  Owners
            players.clear();
            final int numOwners = buf.readInt();
            for (int i = 0; i < numOwners; ++i)
            {
                final UUID id = PacketUtils.readUUID(buf);
                final String name = ByteBufUtils.readUTF8String(buf);
                final Rank rank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));

                players.put(id, new Player(id, name, rank));
            }

            //Permissions
            permissions.clear();
            final int numPermissions = buf.readInt();
            for (int i = 0; i < numPermissions; ++i)
            {
                final Rank rank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));
                final int flags = buf.readInt();
                permissions.put(rank, flags);
            }
        }

        /**
         * Get the rank of a certain player.
         *
         * @param player the player.
         * @return the rank.
         */
        @NotNull
        public Rank getRank(@NotNull final EntityPlayer player)
        {
            return getRank(player.getUniqueID());
        }

        @NotNull
        @Override
        public Rank getRank(final UUID id)
        {
            final Player player = players.get(id);
            return player == null ? NEUTRAL : player.getRank();
        }

        @Override
        public boolean hasPermission(@NotNull final EntityPlayer player, @NotNull final Action action)
        {
            return hasPermission(getRank(player), action);
        }

        @Override
        public boolean isColonyMember(@NotNull final EntityPlayer player)
        {
            return players.containsKey(player.getUniqueID());
        }

        @Nullable
        @Override
        public UUID getOwner()
        {
            Player owner = getPlayersByRank(Rank.OWNER).stream().findFirst().orElse(null);

            if (owner == null) {
                return null;
            }

            return owner.getID();
        }
    }




}
