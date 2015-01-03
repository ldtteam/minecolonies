package com.minecolonies.colony.permissions;

import com.minecolonies.network.PacketUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;

/**
 * Colony Permissions System
 * Created: October 08, 2014
 *
 * @author Colton
 */
public class Permissions {

    public enum Rank {
        OWNER,
        OFFICER,
        FRIEND,
        NEUTRAL,
        HOSTILE
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

    private final static String TAG_OWNERS = "owners";
    private final static String TAG_OWNERS_ID = "ownersID";
    private final static String TAG_OWNERS_RANK = "ownersRank";
    private final static String TAG_PERMISSIONS = "permissions";
    private final static String TAG_PERMISSIONS_FLAGS = "permissionsFlags";

    private Map<UUID, Rank> players = new HashMap<UUID, Rank>();
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
        for (int i = 0; i < ownerTagList.tagCount(); ++i) {
            NBTTagCompound ownerCompound = ownerTagList.getCompoundTagAt(i);
            String owner = ownerCompound.getString(TAG_OWNERS_ID);
            Rank rank = Rank.valueOf(ownerCompound.getString(TAG_OWNERS_RANK));
            players.put(UUID.fromString(owner), rank);
        }

        //Permissions
        NBTTagList permissionsTagList = compound.getTagList(TAG_PERMISSIONS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < permissionsTagList.tagCount(); ++i) {
            NBTTagCompound permissionsCompound = permissionsTagList.getCompoundTagAt(i);
            Rank rank = Rank.valueOf(permissionsCompound.getString(TAG_OWNERS_RANK));

            NBTTagList flagsTagList = permissionsCompound.getTagList(TAG_PERMISSIONS_FLAGS, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);

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
        for (Map.Entry<UUID, Rank> owner : players.entrySet()) {
            NBTTagCompound ownersCompound = new NBTTagCompound();
            ownersCompound.setString(TAG_OWNERS_ID, owner.getKey().toString());
            ownersCompound.setString(TAG_OWNERS_RANK, owner.getValue().name());
            ownerTagList.appendTag(ownersCompound);
        }
        compound.setTag(TAG_OWNERS, ownerTagList);

        // Permissions
        NBTTagList permissionsTagList = new NBTTagList();
        for (Map.Entry<Rank, Integer> entry : permissions.entrySet()) {
            NBTTagCompound permissionsCompound = new NBTTagCompound();
            permissionsCompound.setString(TAG_OWNERS_RANK, entry.getKey().name());

            NBTTagList flagsTagList = new NBTTagList();
            for (Action action : Action.values()) {
                if (Utils.testFlag(entry.getValue(), action.flag)) {
                    flagsTagList.appendTag(new NBTTagString(action.name()));
                }
            }
            permissionsCompound.setTag(TAG_PERMISSIONS_FLAGS, flagsTagList);

            permissionsTagList.appendTag(permissionsCompound);
        }
        compound.setTag(TAG_PERMISSIONS, permissionsTagList);

    }

    public Map<UUID, Rank> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public Set<UUID> getMessagePlayers() {
        Set<Rank> ranks = new HashSet<Rank>();
        for (Rank rank : permissions.keySet()) {
            if (hasPermission(rank, Action.SEND_MESSAGES)) {
                ranks.add(rank);
            }
        }
        return getPlayersByRank(ranks);
    }

    public Set<UUID> getPlayersByRank(Rank rank) {
        Set<UUID> players = new HashSet<UUID>();
        for (Map.Entry<UUID, Rank> entry : this.players.entrySet()) {
            if (entry.getValue().equals(rank)) {
                players.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(players);
    }

    public Set<UUID> getPlayersByRank(Set<Rank> ranks) {
        Set<UUID> players = new HashSet<UUID>();
        for (Map.Entry<UUID, Rank> entry : this.players.entrySet()) {
            if (ranks.contains(entry.getValue())) {
                players.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(players);
    }

    public Map<Rank, Integer> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(EntityPlayer player, Action action) {
        return hasPermission(player.getGameProfile().getId(), action);
    }

    public boolean hasPermission(UUID id, Action action) {
        Rank rank = players.get(id);
        return rank != null && hasPermission(rank, action);
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

    public void addPlayer(UUID id, Rank rank) {
        players.put(id, rank);
        markDirty();
    }

    public void removePlayer(UUID id) {
        players.remove(id);
        markDirty();
    }

    public UUID getOwner() {
        for (Map.Entry<UUID, Rank> entry : players.entrySet()) {
            if (entry.getValue().equals(Rank.OWNER)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Set<UUID> getSubscribers() {
        Set<Rank> ranks = new HashSet<Rank>();
        ranks.add(Rank.OWNER);
        ranks.add(Rank.OFFICER);
        ranks.add(Rank.FRIEND);
        return getPlayersByRank(ranks);
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

    public static class View {
        private Map<UUID, Rank> players = new HashMap<UUID, Rank>();
        private Map<Rank, Integer> permissions = new HashMap<Rank, Integer>();

        public View() {}

        public Map<UUID, Rank> getPlayers() {
            return Collections.unmodifiableMap(players);
        }

        public Set<UUID> getPlayersByRank(Rank rank) {
            Set<UUID> players = new HashSet<UUID>();
            for (Map.Entry<UUID, Rank> entry : this.players.entrySet()) {
                if (entry.getValue().equals(rank)) {
                    players.add(entry.getKey());
                }
            }
            return Collections.unmodifiableSet(players);
        }

        public Set<UUID> getPlayersByRank(Set<Rank> ranks) {
            Set<UUID> players = new HashSet<UUID>();
            for (Map.Entry<UUID, Rank> entry : this.players.entrySet()) {
                if (ranks.contains(entry.getValue())) {
                    players.add(entry.getKey());
                }
            }
            return Collections.unmodifiableSet(players);
        }

        public Map<Rank, Integer> getPermissions() {
            return permissions;
        }

        public boolean hasPermission(EntityPlayer player, Action action) {
            return hasPermission(player.getGameProfile().getId(), action);
        }

        public boolean hasPermission(UUID id, Action action) {
            Rank rank = players.get(id);
            return rank != null && hasPermission(rank, action);
        }

        public boolean hasPermission(Rank rank, Action action) {
            return Utils.testFlag(permissions.get(rank), action.flag);
        }

        public boolean setPermission(Rank rank, Action action) {
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

        public void togglePermission(Rank rank, Action action) {
            permissions.put(rank, Utils.toggleFlag(permissions.get(rank), action.flag));
        }

        public void addPlayer(UUID id, Rank rank) {
            players.put(id, rank);
        }

        public void removePlayer(UUID id) {
            players.remove(id);
        }

        public void deserialize(ByteBuf buf)
        {
            //  Owners
            int numOwners = buf.readInt();
            for (int i = 0; i < numOwners; ++i)
            {
                UUID owner = PacketUtils.readUUID(buf);
                Rank rank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));
                players.put(owner, rank);
            }

            //Permissions
            int numPermissions = buf.readInt();
            for (int i = 0; i < numPermissions; ++i)
            {
                Rank rank = Rank.valueOf(ByteBufUtils.readUTF8String(buf));
                int flags = buf.readInt();
                permissions.put(rank, flags);
            }
        }
    }

    public void serializeViewNetworkData(ByteBuf buf)
    {
        //  Owners
        buf.writeInt(players.size());
        for (Map.Entry<UUID, Rank> owner : players.entrySet())
        {
            PacketUtils.writeUUID(buf, owner.getKey());
            ByteBufUtils.writeUTF8String(buf, owner.getValue().name());
        }

        // Permissions
        buf.writeInt(permissions.size());
        for (Map.Entry<Rank, Integer> entry : permissions.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey().name());
            buf.writeInt(entry.getValue());
        }
    }
}
