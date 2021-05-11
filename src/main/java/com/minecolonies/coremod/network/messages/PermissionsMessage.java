package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import static com.minecolonies.api.util.constant.WindowConstants.*;

import java.util.UUID;

/**
 * Permission message to set permissions on the colony from the GUI.
 */
public class PermissionsMessage
{
    private static final String COLONY_DOES_NOT_EXIST = "Colony #%d does not exist.";

    /**
     * Enums for Message Type for the permission
     * <p>
     * SET_PERMISSION       Setting a permission. REMOVE_PERMISSION    Removing a permission. TOGGLE_PERMISSION    Toggeling a permission.
     */
    public enum MessageType
    {
        SET_PERMISSION,
        REMOVE_PERMISSION,
        TOGGLE_PERMISSION
    }

    /**
     * Client side presentation of the
     */
    public static class View implements IMessage
    {
        private int          colonyID;
        private PacketBuffer data;

        /**
         * The dimension of the
         */
        private RegistryKey<World> dimension;

        /**
         * Empty constructor used when registering the
         */
        public View()
        {
            super();
        }

        /**
         * Instantiate
         *
         * @param colony     with the colony.
         * @param viewerRank and viewer rank.
         */
        public View(@NotNull final Colony colony, @NotNull final Rank viewerRank)
        {
            this.colonyID = colony.getID();
            this.data = new PacketBuffer(Unpooled.buffer());
            colony.getPermissions().serializeViewNetworkData(this.data, viewerRank);
            this.dimension = colony.getDimension();
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            final PacketBuffer newBuf = new PacketBuffer(buf.retain());
            colonyID = newBuf.readInt();
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(newBuf.readString(32767)));
            data = newBuf;
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.CLIENT;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            IColonyManager.getInstance().handlePermissionsViewMessage(colonyID, data, dimension);
            data.release();
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            buf.writeString(dimension.getLocation().toString());
            buf.writeBytes(data);
        }
    }

    /**
     * Permission message class.
     */
    public static class Permission implements IMessage
    {
        private int         colonyID;
        private MessageType type;
        private Rank        rank;
        private Action      action;

        /**
         * The dimension of the
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor.
         */
        public Permission()
        {
            super();
        }

        /**
         * {@link Permission}.
         *
         * @param colony Colony the permission is set in
         * @param type   Type of permission {@link MessageType}
         * @param rank   Rank of the permission {@link Rank}
         * @param action Action of the permission {@link Action}
         */
        public Permission(@NotNull final IColonyView colony, final MessageType type, final Rank rank, final Action action)
        {
            super();
            this.colonyID = colony.getID();
            this.type = type;
            this.rank = rank;
            this.action = action;
            this.dimension = colony.getDimension();
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);
            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }

            //Verify player has permission to do edit permissions
            if (!colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS))
            {
                return;
            }

            switch (type)
            {
                case SET_PERMISSION:
                    colony.getPermissions().setPermission(rank, action);
                    break;
                case REMOVE_PERMISSION:
                    colony.getPermissions().removePermission(rank, action);
                    break;
                case TOGGLE_PERMISSION:
                    colony.getPermissions().togglePermission(rank, action);
                    break;
                default:
                    Log.getLogger().error(String.format("Invalid MessageType %s", type.toString()), new Exception());
            }
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            buf.writeString(type.name());
            buf.writeInt(rank.getId());
            buf.writeString(action.name());
            buf.writeString(dimension.getLocation().toString());
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            type = MessageType.valueOf(buf.readString(32767));
            final int rankId = buf.readInt();
            action = Action.valueOf(buf.readString(32767));
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);
            if (colony != null)
            {
                rank = colony.getPermissions().getRanks().get(rankId);
            }
        }
    }

    /**
     * Message class for adding a player to a permission set.
     */
    public static class AddPlayer implements IMessage
    {
        private int    colonyID;
        private String playerName;

        /**
         * The dimension of the
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor.
         */
        public AddPlayer()
        {
            super();
        }

        /**
         * Constructor for adding player to permission
         *
         * @param colony Colony the permission is set in.
         * @param player New player name to be added.
         */
        public AddPlayer(@NotNull final IColonyView colony, final String player)
        {
            super();
            this.colonyID = colony.getID();
            this.playerName = player;
            this.dimension = colony.getDimension();
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            buf.writeString(playerName);
            buf.writeString(dimension.getLocation().toString());
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerName = buf.readString(32767);
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(playerName, colony.getPermissions().getRank(colony.getPermissions().NEUTRAL_RANK_ID), colony.getWorld());
            }
            else
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
            }
        }
    }

    /**
     * Message class for adding a rank to the colony
     */
    public static class AddRank implements IMessage
    {
        /**
         * the ID of the colony
         */
        private int colonyID;
        /**
         * the name of the new rank
         */
        private String rankName;
        /**
         * the dimension of the colony
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor
         */
        public AddRank()
        {
            super();
        }

        /**
         * Constructor for adding a rank to the colony
         * @param colony the colony to add the rank to
         * @param name the name of the rank
         */
        public AddRank(@NotNull IColonyView colony, @NotNull String name)
        {
            super();
            this.colonyID = colony.getID();
            this.rankName = name;
            this.dimension = colony.getDimension();
        }

        @Override
        public void toBytes(PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            buf.writeString(rankName);
            buf.writeString(dimension.getLocation().toString());
        }

        @Override
        public void fromBytes(PacketBuffer buf)
        {
            this.colonyID = buf.readInt();
            this.rankName = buf.readString(32767);
            this.dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);
            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS))
            {
                colony.getPermissions().addRank(rankName);
            }
        }

    }

    /**
     * Message class for adding a player or fakePlayer to a permission set.
     */
    public static class AddPlayerOrFakePlayer implements IMessage
    {
        private int    colonyID;
        private String playerName;
        private UUID   id;

        /**
         * The dimension of the
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor.
         */
        public AddPlayerOrFakePlayer()
        {
            super();
        }

        /**
         * Constructor for adding player to permission
         *
         * @param colony     Colony the permission is set in.
         * @param playerName New player name to be added.
         * @param id         the id of the player or fakeplayer.
         */
        public AddPlayerOrFakePlayer(@NotNull final IColonyView colony, final String playerName, final UUID id)
        {
            super();
            this.colonyID = colony.getID();
            this.playerName = playerName;
            this.id = id;
            this.dimension = colony.getDimension();
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            buf.writeString(playerName);
            PacketUtils.writeUUID(buf, id);
            buf.writeString(dimension.getLocation().toString());
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerName = buf.readString(32767);
            id = PacketUtils.readUUID(buf);
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(id, playerName, colony.getPermissions().getRank(colony.getPermissions().NEUTRAL_RANK_ID));
            }
            else
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
            }
        }
    }

    /**
     * Message class for setting a player rank in the permissions.
     */
    public static class ChangePlayerRank implements IMessage
    {
        private int  colonyID;
        private UUID playerID;
        private Rank rank;

        /**
         * The dimension of the
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor.
         */
        public ChangePlayerRank()
        {
            super();
        }

        /**
         * Constructor for setting a player rank.
         *
         * @param colony Colony the rank is set in.
         * @param player UUID of the player to set rank.
         * @param rank   Rank to change to.
         */
        public ChangePlayerRank(@NotNull final IColonyView colony, final UUID player, final Rank rank)
        {
            super();
            this.colonyID = colony.getID();
            this.playerID = player;
            this.dimension = colony.getDimension();
            this.rank = rank;
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            buf.writeString(dimension.getLocation().toString());
            buf.writeInt(rank.getId());
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
            IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);
            rank = colony.getPermissions().getRank(buf.readInt());

        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony == null || colony.getWorld() == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }
            final PlayerEntity player = ctxIn.getSender();
            if (colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS) && rank != colony.getPermissions().getRank(colony.getPermissions().OWNER_RANK_ID))
            {
                Log.getLogger().error(rank.getName());
                colony.getPermissions().setPlayerRank(playerID, rank, colony.getWorld());
            }
        }
    }

    /**
     * Message class for removing a player from a permission set.
     */
    public static class RemovePlayer implements IMessage
    {
        private int  colonyID;
        private UUID playerID;

        /**
         * The dimension of the
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor.
         */
        public RemovePlayer()
        {
            super();
        }

        /**
         * Constructor for removing player from permission set.
         *
         * @param colony Colony the player is removed from the permission.
         * @param player UUID of the removed player.
         */
        public RemovePlayer(@NotNull final IColonyView colony, final UUID player)
        {
            super();
            this.colonyID = colony.getID();
            this.playerID = player;
            this.dimension = colony.getDimension();
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            buf.writeString(dimension.getLocation().toString());
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }
            final PlayerEntity player = ctxIn.getSender();
            final Player permissionsPlayer = colony.getPermissions().getPlayers().get(playerID);
            if ((permissionsPlayer.getRank().isHostile() && colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
                  || (!permissionsPlayer.getRank().isHostile()
                        && colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS)
                        && colony.getPermissions().getRank(player).isColonyManager())
                  || player.getUniqueID().equals(playerID))
            {
                colony.getPermissions().removePlayer(playerID);
            }
        }
    }

    /**
     * Message class for removing a rank from a colony
     */
    public static class RemoveRank implements IMessage
    {
        /**
         * the colony ID
         */
        private int colonyId;
        /**
         * the rank ID
         */
        private int rankId;
        /**
         * the dimension of the colony
         */
        private RegistryKey<World> dimension;

        /**
         * Empty public constructor
         */
        public RemoveRank()
        {
            super();
        }

        /**
         * Constructor for removing a rank from a colony
         * @param colony the colony to remove the rank from
         * @param rank the rank to remove
         */
        public RemoveRank(@NotNull final IColonyView colony, @NotNull final Rank rank)
        {
            super();
            colonyId = colony.getID();
            rankId = rank.getId();
            dimension = colony.getDimension();
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyId);
            buf.writeInt(rankId);
            buf.writeString(dimension.getLocation().toString());
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyId = buf.readInt();
            rankId = buf.readInt();
            dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS))
            {
                colony.getPermissions().removeRank(colony.getPermissions().getRanks().get(rankId));
            }
        }
    }

    public static class EditRankType implements IMessage
    {
        private int colonyId;
        private int rankId;
        private RegistryKey<World> dimension;
        private int rankType;

        public EditRankType() { super(); }

        public EditRankType(@NotNull final IColonyView colony, @NotNull final Rank rank, @NotNull final int rankType)
        {
            this.colonyId = colony.getID();
            this.rankId = rank.getId();
            this.dimension = colony.getDimension();
            this.rankType = rankType;
        }

        @Override
        public void toBytes(final PacketBuffer buf)
        {
            buf.writeInt(colonyId);
            buf.writeInt(rankId);
            buf.writeString(dimension.getLocation().toString());
            buf.writeInt(rankType);
        }

        @Override
        public void fromBytes(final PacketBuffer buf)
        {
            this.colonyId = buf.readInt();
            this.rankId = buf.readInt();
            this.dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
            this.rankType = buf.readInt();
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS))
            {
                final Rank rank = colony.getPermissions().getRank(rankId);
                switch (rankType)
                {
                    case 0:
                        rank.setColonyManager(true);
                        rank.setHostile(false);
                        break;
                    case 1:
                        rank.setHostile(true);
                        rank.setColonyManager(false);
                        break;
                    default:
                        rank.setHostile(false);
                        rank.setColonyManager(false);
                        break;
                }
                colony.markDirty();
            }
        }
    }

    public static class SetSubscriber implements IMessage
    {
        private int colonyId;
        private int rankId;
        private RegistryKey<World> dimension;
        private boolean isSubscriber;

        public SetSubscriber() { super(); }

        public SetSubscriber(@NotNull final IColonyView colony, @NotNull final Rank rank, @NotNull final boolean isSubscriber)
        {
            this.colonyId = colony.getID();
            this.dimension = colony.getDimension();
            this.rankId = rank.getId();
            this.isSubscriber = isSubscriber;
        }

        @Override
        public void toBytes(final PacketBuffer buf)
        {
            buf.writeInt(colonyId);
            buf.writeString(dimension.getLocation().toString());
            buf.writeInt(rankId);
            buf.writeBoolean(isSubscriber);
        }

        @Override
        public void fromBytes(final PacketBuffer buf)
        {
            this.colonyId = buf.readInt();
            this.dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(buf.readString(32767)));
            this.rankId = buf.readInt();
            this.isSubscriber = buf.readBoolean();
        }

        @Override
        public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS))
            {
                colony.getPermissions().getRank(rankId).setSubscriber(isSubscriber);
                colony.markDirty();
            }
        }


    }
}
