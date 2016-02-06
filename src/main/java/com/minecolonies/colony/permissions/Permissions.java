package com.minecolonies.colony.permissions;

import com.minecolonies.network.PacketUtils;
import com.minecolonies.util.Utils;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;

import java.util.*;

/**
 * Colony Permissions System
 * Created: October 08, 2014
 *
 * @author Colton
 */
public class Permissions implements IPermissions
{
    public enum Rank
    {
        OWNER   (true),
        OFFICER (true),
        FRIEND  (true),
        NEUTRAL (false),
        HOSTILE (false);

        Rank(boolean isSubscriber) { this.isSubscriber = isSubscriber; }

        public final boolean isSubscriber;
    }

    public enum Action {
        ACCESS_HUTS(0),
        GUARDS_ATTACK(1),
        PLACE_HUTS(2),
        BREAK_HUTS(3),
        CAN_PROMOTE(4),
        CAN_DEMOTE(5),
        SEND_MESSAGES(6);
        //TODO grief control?

        private final int flag;

        Action(int bit) {
            this.flag = 0x1 << bit;
        }

        public int getFlag() {
            return flag;
        }
    }

    public static class Player
    {
        public Player(UUID id, String name, Rank rank)
        {
            this.id = id;
            this.name = name;
            this.rank = rank;
        }

        public UUID id;
        public String name;
        public Rank rank;
    }

    private final static String TAG_OWNERS      = "owners";
    private final static String TAG_ID          = "id";
    private final static String TAG_NAME        = "name";
    private final static String TAG_RANK        = "rank";
    private final static String TAG_PERMISSIONS = "permissions";
    private final static String TAG_FLAGS       = "flags";

    private Map<UUID, Player> players = new HashMap<UUID, Player>();
    private Map<Rank, Integer> permissions = new HashMap<Rank, Integer>();

    public Permissions() {
        //Owner
        permissions.put(Rank.OWNER, 0);
        this.setPermission(Rank.OWNER, Action.ACCESS_HUTS);
        this.setPermission(Rank.OWNER, Action.PLACE_HUTS);
        this.setPermission(Rank.OWNER, Action.BREAK_HUTS);
        this.setPermission(Rank.OWNER, Action.CAN_PROMOTE);
        this.setPermission(Rank.OWNER, Action.CAN_DEMOTE);
        this.setPermission(Rank.OWNER, Action.SEND_MESSAGES);
        //Officer
        permissions.put(Rank.OFFICER, 0);
        this.setPermission(Rank.OFFICER, Action.ACCESS_HUTS);
        this.setPermission(Rank.OFFICER, Action.PLACE_HUTS);
        this.setPermission(Rank.OFFICER, Action.BREAK_HUTS);
        this.setPermission(Rank.OFFICER, Action.CAN_PROMOTE);
        this.setPermission(Rank.OFFICER, Action.CAN_DEMOTE);
        this.setPermission(Rank.OFFICER, Action.SEND_MESSAGES);
        //Friend
        permissions.put(Rank.FRIEND, 0);
        this.setPermission(Rank.FRIEND, Action.ACCESS_HUTS);
        //Neutral
        permissions.put(Rank.NEUTRAL, 0);
        //Hostile
        permissions.put(Rank.HOSTILE, 0);
        this.setPermission(Rank.HOSTILE, Action.GUARDS_ATTACK);
    }

    public void loadPermissions(NBTTagCompound compound) {
        //  Owners
        NBTTagList ownerTagList = compound.getTagList(TAG_OWNERS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < ownerTagList.tagCount(); ++i)
        {
            NBTTagCompound ownerCompound = ownerTagList.getCompoundTagAt(i);
            UUID id = UUID.fromString(ownerCompound.getString(TAG_ID));
            String name = ownerCompound.getString(TAG_NAME);
            Rank rank = Rank.valueOf(ownerCompound.getString(TAG_RANK));
            players.put(id, new Player(id, name, rank));
        }

        //Permissions
        NBTTagList permissionsTagList = compound.getTagList(TAG_PERMISSIONS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < permissionsTagList.tagCount(); ++i) {
            NBTTagCompound permissionsCompound = permissionsTagList.getCompoundTagAt(i);
            Rank rank = Rank.valueOf(permissionsCompound.getString(TAG_RANK));

            NBTTagList flagsTagList = permissionsCompound.getTagList(TAG_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

            int flags = 0;

            for (int j = 0; j < flagsTagList.tagCount(); ++j) {
                String flag = flagsTagList.getStringTagAt(j);
                flags = Utils.setFlag(flags, Action.valueOf(flag).flag);
            }
            permissions.put(rank, flags);
        }
    }

    public void savePermissions(NBTTagCompound compound) {
        //  Owners
        NBTTagList ownerTagList = new NBTTagList();
        for (Player player : players.values())
        {
            NBTTagCompound ownersCompound = new NBTTagCompound();
            ownersCompound.setString(TAG_ID, player.id.toString());
            ownersCompound.setString(TAG_NAME, player.name);
            ownersCompound.setString(TAG_RANK, player.rank.name());
            ownerTagList.appendTag(ownersCompound);
        }
        compound.setTag(TAG_OWNERS, ownerTagList);

        // Permissions
        NBTTagList permissionsTagList = new NBTTagList();
        for (Map.Entry<Rank, Integer> entry : permissions.entrySet())
        {
            NBTTagCompound permissionsCompound = new NBTTagCompound();
            permissionsCompound.setString(TAG_RANK, entry.getKey().name());

            NBTTagList flagsTagList = new NBTTagList();
            for (Action action : Action.values())
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

    }

    public Map<UUID, Player> getPlayers()
    {
        return Collections.unmodifiableMap(players);
    }

    public Set<UUID> getMessagePlayers()
    {
        Set<UUID> messagePlayers = new HashSet<UUID>();
        for (Player player : players.values())
        {
            if (hasPermission(player.rank, Action.SEND_MESSAGES))
            {
                messagePlayers.add(player.id);
            }
        }
        return messagePlayers;
    }

    public Set<Player> getPlayersByRank(Rank rank)
    {
        Set<Player> players = new HashSet<Player>();
        for (Player player : this.players.values())
        {
            if (player.rank.equals(rank))
            {
                players.add(player);
            }
        }
        return players;
    }

    public Set<Player> getPlayersByRank(Set<Rank> ranks) {
        Set<Player> players = new HashSet<Player>();
        for (Player player : this.players.values())
        {
            if (ranks.contains(player.rank))
            {
                players.add(player);
            }
        }
        return players;
    }

    public Map<Rank, Integer> getPermissions() {
        return permissions;
    }

    public Rank getRank(EntityPlayer player)
    {
        return getRank(player.getGameProfile().getId());
    }

    public Rank getRank(UUID id)
    {
        Player player = players.get(id);
        return player != null ? player.rank : Rank.NEUTRAL;
    }

    public boolean hasPermission(EntityPlayer player, Action action) {
        return hasPermission(getRank(player), action);
    }

    public boolean hasPermission(UUID id, Action action) {
        return hasPermission(getRank(id), action);
    }

    public boolean hasPermission(Rank rank, Action action) {
        return Utils.testFlag(permissions.get(rank), action.flag);
    }

    public void setPermission(Rank rank, Action action) {
        int flags = permissions.get(rank);
        if(!Utils.testFlag(flags, action.flag))//check that flag isn't set
        {
            permissions.put(rank, Utils.setFlag(flags, action.flag));
            markDirty();
        }
    }

    public void removePermission(Rank rank, Action action) {
        int flags = permissions.get(rank);
        if(Utils.testFlag(flags, action.flag))
        {
            permissions.put(rank, Utils.unsetFlag(flags, action.flag));
            markDirty();
        }
    }

    public void togglePermission(Rank rank, Action action) {
        permissions.put(rank, Utils.toggleFlag(permissions.get(rank), action.flag));
        markDirty();
    }

    public boolean setPlayerRank(UUID id, Rank rank)
    {
        Player player = players.get(id);

        if (player != null)
        {
            player.rank = rank;
            markDirty();
        }
        else
        {
            GameProfile gameprofile = MinecraftServer.getServer().func_152358_ax().func_152652_a(id);

            return gameprofile != null && addPlayer(gameprofile, rank);

        }

        return true;
    }

    public boolean addPlayer(String player, Rank rank)
    {
        GameProfile gameprofile = MinecraftServer.getServer().func_152358_ax().func_152655_a(player);

        return gameprofile != null && addPlayer(gameprofile, rank);

    }

    public boolean addPlayer(GameProfile gameprofile, Rank rank)
    {
        Player p = new Player(gameprofile.getId(), gameprofile.getName(), rank);
        players.put(p.id, p);

        markDirty();

        return true;
    }

    public boolean removePlayer(UUID id)
    {
        if (players.remove(id) != null)
        {
            markDirty();
            return true;
        }

        return false;
    }

    public UUID getOwner()
    {
        for (Map.Entry<UUID, Player> entry : players.entrySet())
        {
            if (entry.getValue().rank.equals(Rank.OWNER))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isSubscriber(EntityPlayer player)
    {
        return isSubscriber(player.getGameProfile().getId());
    }

    public boolean isSubscriber(UUID player)
    {
        return getRank(player).isSubscriber;
    }

    boolean isDirty = false;

    public void markDirty()
    {
        isDirty = true;
    }

    public boolean isDirty()
    {
        return isDirty;
    }

    public void clearDirty()
    {
        isDirty = false;
    }

    public static class View implements IPermissions
    {
        private Rank userRank = Rank.NEUTRAL;
        private Map<UUID, Player> players = new HashMap<UUID, Player>();
        private Map<Rank, Integer> permissions = new HashMap<Rank, Integer>();

        public View() {}

        public Rank getUserRank() { return userRank; }

        public Map<UUID, Player> getPlayers() {
            return Collections.unmodifiableMap(players);
        }

        public Set<Player> getPlayersByRank(Rank rank)
        {
            Set<Player> players = new HashSet<Player>();
            for (Player player : this.players.values())
            {
                if (player.rank == rank)
                {
                    players.add(player);
                }
            }
            return Collections.unmodifiableSet(players);
        }

        public Set<Player> getPlayersByRank(Set<Rank> ranks)
        {
            Set<Player> players = new HashSet<Player>();
            for (Player player : this.players.values())
            {
                if (ranks.contains(player.rank))
                {
                    players.add(player);
                }
            }
            return Collections.unmodifiableSet(players);
        }

        public Map<Rank, Integer> getPermissions() {
            return permissions;
        }

        public Rank getRank(EntityPlayer player)
        {
            return getRank(player.getUniqueID());
        }

        public Rank getRank(UUID id)
        {
            Player player = players.get(id);
            return player != null ? player.rank : Rank.NEUTRAL;
        }

        public boolean hasPermission(EntityPlayer player, Action action)
        {
            return hasPermission(getRank(player), action);
        }

        public boolean hasPermission(UUID id, Action action)
        {
            return hasPermission(getRank(id), action);
        }

        public boolean hasPermission(Rank rank, Action action)
        {
            return Utils.testFlag(permissions.get(rank), action.flag);
        }

        public boolean setPermission(Rank rank, Action action)
        {
            int flags = permissions.get(rank);
            if(!Utils.testFlag(flags, action.flag))//check that flag isn't set
            {
                permissions.put(rank, Utils.setFlag(flags, action.flag));
                return true;
            }
            return false;
        }

        public boolean removePermission(Rank rank, Action action) {
            int flags = permissions.get(rank);
            if(Utils.testFlag(flags, action.flag))
            {
                permissions.put(rank, Utils.unsetFlag(flags, action.flag));
                return true;
            }
            return false;
        }

        public void togglePermission(Rank rank, Action action)
        {
            permissions.put(rank, Utils.toggleFlag(permissions.get(rank), action.flag));
        }

        public void deserialize(ByteBuf buf)
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
    }

    public void serializeViewNetworkData(ByteBuf buf, Permissions.Rank viewerRank)
    {
        ByteBufUtils.writeUTF8String(buf, viewerRank.name());

        //  Owners
        buf.writeInt(players.size());
        for (Map.Entry<UUID, Player> player : players.entrySet())
        {
            PacketUtils.writeUUID(buf, player.getKey());
            ByteBufUtils.writeUTF8String(buf, player.getValue().name);
            ByteBufUtils.writeUTF8String(buf, player.getValue().rank.name());
        }

        // Permissions
        buf.writeInt(permissions.size());
        for (Map.Entry<Rank, Integer> entry : permissions.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey().name());
            buf.writeInt(entry.getValue());
        }
    }

    private static class RankPair
    {
        RankPair(Rank p, Rank d) { promote = p; demote = d; }

        Rank promote;
        Rank demote;
    }

    private static Map<Rank, RankPair> promotionRanks = new HashMap<Rank, RankPair>();

    private static void setPromotionRanks(Rank r, Rank p, Rank d)
    {
        promotionRanks.put(r, new RankPair(p, d));
    }

    static
    {
        //setPromotionRanks(Rank.OWNER,   Rank.OWNER,     Rank.OWNER);
        setPromotionRanks(Rank.OFFICER, Rank.OFFICER,   Rank.FRIEND);
        setPromotionRanks(Rank.FRIEND,  Rank.OFFICER,   Rank.NEUTRAL);
        setPromotionRanks(Rank.NEUTRAL, Rank.FRIEND,    Rank.HOSTILE);
        setPromotionRanks(Rank.HOSTILE, Rank.NEUTRAL,   Rank.HOSTILE);
    }

    public static Rank getPromotionRank(Rank rank)
    {
        if (promotionRanks.containsKey(rank))
        {
            return promotionRanks.get(rank).promote;
        }

        return rank;
    }

    public static Rank getDemotionRank(Rank rank)
    {
        if (promotionRanks.containsKey(rank))
        {
            return promotionRanks.get(rank).demote;
        }

        return rank;
    }
}
