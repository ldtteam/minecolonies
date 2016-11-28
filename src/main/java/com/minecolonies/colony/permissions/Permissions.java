package com.minecolonies.colony.permissions;

import com.minecolonies.colony.Colony;
import com.minecolonies.network.PacketUtils;
import com.minecolonies.util.AchievementUtils;
import com.minecolonies.util.Utils;
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

/**
 * Colony Permissions System
 * Created: October 08, 2014
 *
 * @author Colton
 */
public class Permissions implements IPermissions
{
    private static final String TAG_OWNERS      = "owners";
    private static final String TAG_ID          = "id";
    private static final String TAG_RANK        = "rank";
    private static final String TAG_PERMISSIONS = "permissions";
    private static final String TAG_FLAGS       = "flags";
    private static final String TAG_OWNER       = "owner";
    private static final String TAG_OWNER_ID    = "ownerid";

    @NotNull
    private static Map<Rank, RankPair> promotionRanks = new EnumMap<>(Rank.class);
    static
    {
        setPromotionRanks(Rank.OFFICER, Rank.OFFICER, Rank.FRIEND);
        setPromotionRanks(Rank.FRIEND, Rank.OFFICER, Rank.NEUTRAL);
        setPromotionRanks(Rank.NEUTRAL, Rank.FRIEND, Rank.HOSTILE);
        setPromotionRanks(Rank.HOSTILE, Rank.NEUTRAL, Rank.HOSTILE);
    }

    @NotNull
    private final Colony colony;
    @NotNull
    private Map<UUID, Player>  players     = new HashMap<>();
    @NotNull
    private Map<Rank, Integer> permissions = new EnumMap<>(Rank.class);
    private boolean            dirty       = false;

    /**
     * The name of the owner.
     */
    private String ownerName = "";
    /**
     * The UUID of the owner.
     */
    private UUID   ownerUUID = null;

    /**
     * Saves the permissions with allowed actions
     *
     * @param colony the colony this permissions object belongs to.
     */
    public Permissions(@NotNull final Colony colony)
    {
        //Owner
        permissions.put(Rank.OWNER, 0);
        this.setPermission(Rank.OWNER, Action.ACCESS_HUTS);
        this.setPermission(Rank.OWNER, Action.PLACE_HUTS);
        this.setPermission(Rank.OWNER, Action.BREAK_HUTS);
        this.setPermission(Rank.OWNER, Action.CAN_PROMOTE);
        this.setPermission(Rank.OWNER, Action.CAN_DEMOTE);
        this.setPermission(Rank.OWNER, Action.SEND_MESSAGES);
        this.setPermission(Rank.OWNER, Action.EDIT_PERMISSIONS);
        this.setPermission(Rank.OWNER, Action.MANAGE_HUTS);
        //Officer
        permissions.put(Rank.OFFICER, 0);
        this.setPermission(Rank.OFFICER, Action.ACCESS_HUTS);
        this.setPermission(Rank.OFFICER, Action.PLACE_HUTS);
        this.setPermission(Rank.OFFICER, Action.BREAK_HUTS);
        this.setPermission(Rank.OFFICER, Action.CAN_PROMOTE);
        this.setPermission(Rank.OFFICER, Action.CAN_DEMOTE);
        this.setPermission(Rank.OFFICER, Action.SEND_MESSAGES);
        this.setPermission(Rank.OFFICER, Action.MANAGE_HUTS);
        //Friend
        permissions.put(Rank.FRIEND, 0);
        this.setPermission(Rank.FRIEND, Action.ACCESS_HUTS);
        //Neutral
        permissions.put(Rank.NEUTRAL, 0);
        //Hostile
        permissions.put(Rank.HOSTILE, 0);
        this.setPermission(Rank.HOSTILE, Action.GUARDS_ATTACK);

        //Add new additional Permissions inside this method.
        updateNewPermissions();

        this.colony = colony;
    }

    /**
     * Sets the rank for a specific action
     *
     * @param rank   Desired rank
     * @param action Action that should have desired rank
     */
    public final void setPermission(Rank rank, @NotNull Action action)
    {
        int flags = permissions.get(rank);

        //check that flag isn't set
        if (!Utils.testFlag(flags, action.flag))
        {
            permissions.put(rank, Utils.setFlag(flags, action.flag));
            markDirty();
        }
    }

    /**
     * Marks instance dirty
     */
    private void markDirty()
    {
        dirty = true;
    }

    /**
     * Stores the list of promotion/demotion ranks
     *
     * @param r Rank to set pro- and demotion of
     * @param p Promotion rank
     * @param d Demotion rank
     */
    private static void setPromotionRanks(Rank r, Rank p, Rank d)
    {
        promotionRanks.put(r, new RankPair(p, d));
    }

    /**
     * Returns the promotion rank of a specific rank
     * E.G.: Neutral will return Friend
     *
     * @param rank Rank to check promotion of
     * @return {@link Rank} after promotion
     */
    public static Rank getPromotionRank(Rank rank)
    {
        if (promotionRanks.containsKey(rank))
        {
            return promotionRanks.get(rank).promote;
        }

        return rank;
    }

    /**
     * Returns the demotion rank of a specific rank
     * E.G.: Neutral will return Hostile
     *
     * @param rank Rank to check demotion of
     * @return {@link Rank} after demotion
     */
    public static Rank getDemotionRank(Rank rank)
    {
        if (promotionRanks.containsKey(rank))
        {
            return promotionRanks.get(rank).demote;
        }

        return rank;
    }

    /**
     * Reads the permissions from a NBT
     *
     * @param compound NBT to read from
     */
    public void loadPermissions(@NotNull NBTTagCompound compound)
    {
        //  Owners
        NBTTagList ownerTagList = compound.getTagList(TAG_OWNERS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < ownerTagList.tagCount(); ++i)
        {
            NBTTagCompound ownerCompound = ownerTagList.getCompoundTagAt(i);
            @NotNull UUID id = UUID.fromString(ownerCompound.getString(TAG_ID));
            Rank rank = Rank.valueOf(ownerCompound.getString(TAG_RANK));

            GameProfile player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(id);

            if (player != null)
            {
                players.put(id, new Player(id, player.getName(), rank));
            }
        }

        //Permissions
        NBTTagList permissionsTagList = compound.getTagList(TAG_PERMISSIONS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < permissionsTagList.tagCount(); ++i)
        {
            NBTTagCompound permissionsCompound = permissionsTagList.getCompoundTagAt(i);
            Rank rank = Rank.valueOf(permissionsCompound.getString(TAG_RANK));

            NBTTagList flagsTagList = permissionsCompound.getTagList(TAG_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

            int flags = 0;

            for (int j = 0; j < flagsTagList.tagCount(); ++j)
            {
                String flag = flagsTagList.getStringTagAt(j);
                flags = Utils.setFlag(flags, Action.valueOf(flag).flag);
            }
            permissions.put(rank, flags);
        }

        updateNewPermissions();

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
            catch (IllegalArgumentException e)
            {
                /*
                 * Intentionally left empty. Happens when the UUID hasn't been saved yet.
                 */
            }
        }

        final Map.Entry<UUID, Player> owner = getOwnerEntry();
        if(owner == null && ownerUUID != null)
        {
            GameProfile player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(ownerUUID);

            if(player != null)
            {
                players.put(ownerUUID, new Player(ownerUUID, player.getName(), Rank.OWNER));
            }
        }
    }

    /**
     * This method should be used to update new permissions added to the game which old colonies probably don't have yet.
     */
    private void updateNewPermissions()
    {
        this.setPermission(Rank.OWNER, Action.MANAGE_HUTS);
        this.setPermission(Rank.OFFICER, Action.MANAGE_HUTS);
    }

    /**
     * Save the permissions to a NBT
     *
     * @param compound NBT to write to
     */
    public void savePermissions(@NotNull NBTTagCompound compound)
    {
        //  Owners
        @NotNull NBTTagList ownerTagList = new NBTTagList();
        for (@NotNull Player player : players.values())
        {
            @NotNull NBTTagCompound ownersCompound = new NBTTagCompound();
            ownersCompound.setString(TAG_ID, player.id.toString());
            ownersCompound.setString(TAG_RANK, player.rank.name());
            ownerTagList.appendTag(ownersCompound);
        }
        compound.setTag(TAG_OWNERS, ownerTagList);

        // Permissions
        @NotNull NBTTagList permissionsTagList = new NBTTagList();
        for (@NotNull Map.Entry<Rank, Integer> entry : permissions.entrySet())
        {
            @NotNull NBTTagCompound permissionsCompound = new NBTTagCompound();
            permissionsCompound.setString(TAG_RANK, entry.getKey().name());

            @NotNull NBTTagList flagsTagList = new NBTTagList();
            for (@NotNull Action action : Action.values())
            {
                if (Utils.testFlag(entry.getValue(), action.flag))
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
    }

    @NotNull
    public Map<UUID, Player> getPlayers()
    {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Returns a set of UUID's that have permission to send (and receive) messages
     *
     * @return Set of UUID's allowed to send and receive messages
     */
    public Set<UUID> getMessagePlayers()
    {
        return players.values().stream().filter(player ->
                                                  hasPermission(player.rank, Action.SEND_MESSAGES)).map(player -> player.id).collect(Collectors.toSet());
    }

    /**
     * Checks if a rank can perform an action
     *
     * @param rank   Rank you want to check
     * @param action Action you want to perform
     * @return true if rank has permission for action, otherwise false
     */
    public boolean hasPermission(Rank rank, @NotNull Action action)
    {
        return (rank == Rank.OWNER && action != Action.GUARDS_ATTACK)
                 || Utils.testFlag(permissions.get(rank), action.flag);
    }

    public Set<Player> getPlayersByRank(Rank rank)
    {
        return this.players.values().stream()
                 .filter(player -> player.rank.equals(rank))
                 .collect(Collectors.toSet());
    }

    public Set<Player> getPlayersByRank(@NotNull Set<Rank> ranks)
    {
        return this.players.values().stream()
                 .filter(player -> ranks.contains(player.rank))
                 .collect(Collectors.toSet());
    }

    /**
     * Returns the map of permissions and ranks
     *
     * @return map of permissions
     */
    @NotNull
    public Map<Rank, Integer> getPermissions()
    {
        return permissions;
    }

    /**
     * Checks if the player has the permission of an action
     *
     * @param player {@link EntityPlayer} player
     * @param action {@link Permissions.Action} action
     * @return true if player has permissions, otherwise false
     */
    @Override
    public boolean hasPermission(@NotNull EntityPlayer player, @NotNull Action action)
    {
        return hasPermission(getRank(player), action);
    }

    /**
     * Returns the rank of a player
     *
     * @param player player to check rank
     * @return Rank of te player
     */
    @NotNull
    public Rank getRank(@NotNull EntityPlayer player)
    {
        return getRank(player.getGameProfile().getId());
    }

    /**
     * Remove permission for a specific rank
     *
     * @param rank   Rank to remove permission
     * @param action Action to remove from rank
     */
    public void removePermission(Rank rank, @NotNull Action action)
    {
        int flags = permissions.get(rank);
        if (Utils.testFlag(flags, action.flag))
        {
            permissions.put(rank, Utils.unsetFlag(flags, action.flag));
            markDirty();
        }
    }

    /**
     * Returns the rank belonging to the UUID
     *
     * @param id UUID that you want to check rank of
     * @return Rank of the UUID
     */
    @NotNull
    @Override
    public Rank getRank(UUID id)
    {
        Player player = players.get(id);
        return player != null ? player.rank : Rank.NEUTRAL;
    }

    /**
     * Toggle permission for a specific rank
     *
     * @param rank   Rank to toggle permission
     * @param action Action to toggle permission
     */
    public void togglePermission(Rank rank, @NotNull Action action)
    {
        permissions.put(rank, Utils.toggleFlag(permissions.get(rank), action.flag));
        markDirty();
    }

    /**
     * Sets the player's rank to a given rank
     *
     * @param id   UUID of the player of the new rank
     * @param rank Desired rank
     * @return True if successful, otherwise false
     */
    public boolean setPlayerRank(UUID id, Rank rank, World world)
    {
        Player player = players.get(id);

        if (player != null)
        {
            player.rank = rank;
            markDirty();
            AchievementUtils.syncAchievements(colony);
        }
        else
        {

            GameProfile gameprofile = world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(id);

            return gameprofile != null && addPlayer(gameprofile, rank);
        }

        return true;
    }

    /**
     * Adds a player to the rankings
     *
     * @param gameprofile GameProfile of the player
     * @param rank        Desired rank
     * @return True if succesful, otherwise false.
     */
    private boolean addPlayer(@NotNull GameProfile gameprofile, Rank rank)
    {
        @NotNull Player p = new Player(gameprofile.getId(), gameprofile.getName(), rank);
        players.put(p.id, p);

        markDirty();
        AchievementUtils.syncAchievements(colony);
        return true;
    }

    /**
     * Add a player to the rankings
     *
     * @param player String playername of the player to add
     * @param rank   Rank desired starting rank
     * @return True if successful, otherwise false
     */
    public boolean addPlayer(@NotNull String player, Rank rank, World world)
    {
        if (player.isEmpty())
        {
            return false;
        }
        GameProfile gameprofile = world.getMinecraftServer().getPlayerProfileCache().getGameProfileForUsername(player);
        //Check if the player already exists so that their rank isn't overridden
        return gameprofile != null && !players.containsKey(gameprofile.getId()) && addPlayer(gameprofile, rank);
    }

    /**
     * Remove a player from the permissions.
     *
     * @param id UUID of the player
     * @return True if succesfull, otherwise false.
     */
    public boolean removePlayer(UUID id)
    {
        Player player = players.get(id);
        AchievementUtils.syncAchievements(colony);
        if (player != null && player.getRank() != Rank.OWNER && players.remove(id) != null)
        {
            markDirty();
            return true;
        }

        return false;
    }

    /**
     * Returns the owner of this permission instance.
     *
     * @return UUID of the owner
     */
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
     * Compute the owner of a colony.
     * <p>
     * Can be quite expensive in colonies with many players.
     *
     * @return the corresponding entry or null
     */
    @Nullable
    private Map.Entry<UUID, Player> getOwnerEntry()
    {
        for (@NotNull final Map.Entry<UUID, Player> entry : players.entrySet())
        {
            if (entry.getValue().rank.equals(Rank.OWNER))
            {
                return entry;
            }
        }
        return null;
    }

    @Override
    public boolean isColonyMember(@NotNull EntityPlayer player)
    {
        return players.containsKey(player.getGameProfile().getId());
    }

    /**
     * Returns the name of the owner of this permission instance.
     *
     * @return Name of the owner
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
     * Checks if a user is a subscriber
     *
     * @param player {@link EntityPlayer} to check for subscription.
     * @return True is subscriber, otherwise false.
     */
    public boolean isSubscriber(@NotNull EntityPlayer player)
    {
        return isSubscriber(player.getGameProfile().getId());
    }

    /**
     * See {@link #isSubscriber(EntityPlayer)}
     *
     * @param player {@link UUID} of the player
     * @return True if subscriber, otherwise false.
     */
    private boolean isSubscriber(UUID player)
    {
        return getRank(player).isSubscriber;
    }

    /**
     * Returns if the instance is dirty
     *
     * @return True if dirty, otherise false.
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Marks instance not dirty
     */
    public void clearDirty()
    {
        dirty = false;
    }

    /**
     * Serializes network data
     *
     * @param buf        {@link ByteBuf} to write to
     * @param viewerRank Rank of the viewer
     */
    public void serializeViewNetworkData(@NotNull ByteBuf buf, @NotNull Permissions.Rank viewerRank)
    {
        ByteBufUtils.writeUTF8String(buf, viewerRank.name());

        //  Owners
        buf.writeInt(players.size());
        for (@NotNull Map.Entry<UUID, Player> player : players.entrySet())
        {
            PacketUtils.writeUUID(buf, player.getKey());
            ByteBufUtils.writeUTF8String(buf, player.getValue().name);
            ByteBufUtils.writeUTF8String(buf, player.getValue().rank.name());
        }

        // Permissions
        buf.writeInt(permissions.size());
        for (@NotNull Map.Entry<Rank, Integer> entry : permissions.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey().name());
            buf.writeInt(entry.getValue());
        }
    }

    /**
     * Ranks within a colony
     */
    public enum Rank
    {
        OWNER(true),
        OFFICER(true),
        FRIEND(true),
        NEUTRAL(false),
        HOSTILE(false);

        public final boolean isSubscriber;

        /**
         * Ranks enum constructor.
         * <p>
         * Subscribers are receiving events from the colony.
         * They are either citizens or near enough.
         * Ranks with true are automatically subscribed to the colony.
         *
         * @param isSubscriber boolean whether auto-subscribed to this colony.
         */
        Rank(boolean isSubscriber)
        {
            this.isSubscriber = isSubscriber;
        }

    }

    /**
     * Actions that can be performed in a colony
     */
    public enum Action
    {
        ACCESS_HUTS(0),
        GUARDS_ATTACK(1),
        PLACE_HUTS(2),
        BREAK_HUTS(3),
        CAN_PROMOTE(4),
        CAN_DEMOTE(5),
        SEND_MESSAGES(6),
        EDIT_PERMISSIONS(7),
        MANAGE_HUTS(8);

        private final int flag;

        /**
         * Stores the action as byte.
         * {@link #ACCESS_HUTS} has value 0000 0000
         * {@link #SEND_MESSAGES} has value 0100 0000
         *
         * @param bit how many bits should be shifted and set
         */
        Action(int bit)
        {
            this.flag = 0x1 << bit;
        }

        public int getFlag()
        {
            return flag;
        }
    }

    /**
     * Player within a colony
     */
    public static class Player
    {
        private UUID   id;
        private String name;
        private Rank   rank;

        public Player(UUID id, String name, Rank rank)
        {
            this.id = id;
            this.name = name;
            this.rank = rank;
        }

        /**
         * @return The UUID of the player.
         */
        public UUID getID()
        {
            return id;
        }

        /**
         * @return The player's current name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return The player's current rank.
         */
        public Rank getRank()
        {
            return rank;
        }
    }

    private static class RankPair
    {
        private Rank promote;
        private Rank demote;

        /**
         * Links promotion and demotion
         *
         * @param p Promoting rank
         * @param d Demoting rank
         */
        RankPair(Rank p, Rank d)
        {
            promote = p;
            demote = d;
        }
    }

    public static class View implements IPermissions
    {
        private Rank               userRank    = Rank.NEUTRAL;
        @NotNull
        private Map<UUID, Player>  players     = new HashMap<>();
        @NotNull
        private Map<Rank, Integer> permissions = new EnumMap<>(Rank.class);

        public Rank getUserRank()
        {
            return userRank;
        }

        @NotNull
        public Map<UUID, Player> getPlayers()
        {
            return Collections.unmodifiableMap(players);
        }

        @NotNull
        public Set<Player> getPlayersByRank(Rank rank)
        {
            return Collections.unmodifiableSet(
              this.players.values()
                .stream()
                .filter(player -> player.rank == rank)
                .collect(Collectors.toSet()));
        }

        @NotNull
        public Set<Player> getPlayersByRank(@NotNull Set<Rank> ranks)
        {
            return Collections.unmodifiableSet(
              this.players.values()
                .stream()
                .filter(player -> ranks.contains(player.rank))
                .collect(Collectors.toSet()));
        }

        @NotNull
        public Map<Rank, Integer> getPermissions()
        {
            return permissions;
        }

        public boolean hasPermission(UUID id, @NotNull Action action)
        {
            return hasPermission(getRank(id), action);
        }

        public boolean hasPermission(Rank rank, @NotNull Action action)
        {
            return (rank == Rank.OWNER && action != Action.GUARDS_ATTACK)
                     || Utils.testFlag(permissions.get(rank), action.flag);
        }

        public boolean setPermission(Rank rank, @NotNull Action action)
        {
            int flags = permissions.get(rank);

            //check that flag isn't set
            if (!Utils.testFlag(flags, action.flag))
            {
                permissions.put(rank, Utils.setFlag(flags, action.flag));
                return true;
            }
            return false;
        }

        public boolean removePermission(Rank rank, @NotNull Action action)
        {
            int flags = permissions.get(rank);
            if (Utils.testFlag(flags, action.flag))
            {
                permissions.put(rank, Utils.unsetFlag(flags, action.flag));
                return true;
            }
            return false;
        }

        public void togglePermission(Rank rank, @NotNull Action action)
        {
            permissions.put(rank, Utils.toggleFlag(permissions.get(rank), action.flag));
        }

        public void deserialize(@NotNull ByteBuf buf)
        {
            userRank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));

            //  Owners
            players.clear();
            int numOwners = buf.readInt();
            for (int i = 0; i < numOwners; ++i)
            {
                UUID id = PacketUtils.readUUID(buf);
                String name = ByteBufUtils.readUTF8String(buf);
                Rank rank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));

                players.put(id, new Player(id, name, rank));
            }

            //Permissions
            permissions.clear();
            int numPermissions = buf.readInt();
            for (int i = 0; i < numPermissions; ++i)
            {
                Rank rank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));
                int flags = buf.readInt();
                permissions.put(rank, flags);
            }
        }

        @NotNull
        public Rank getRank(@NotNull EntityPlayer player)
        {
            return getRank(player.getUniqueID());
        }

        @NotNull
        @Override
        public Rank getRank(UUID id)
        {
            Player player = players.get(id);
            return player != null ? player.rank : Rank.NEUTRAL;
        }

        @Override
        public boolean hasPermission(@NotNull EntityPlayer player, @NotNull Action action)
        {
            return hasPermission(getRank(player), action);
        }

        @Override
        public boolean isColonyMember(@NotNull EntityPlayer player)
        {
            return players.containsKey(player.getUniqueID());
        }
    }
}
