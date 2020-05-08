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
import com.minecolonies.coremod.colony.permissions.Permissions;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        private int dimension;

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
            dimension = newBuf.readInt();
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
            buf.writeInt(dimension);
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
        private int dimension;

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
            buf.writeString(rank.name());
            buf.writeString(action.name());
            buf.writeInt(dimension);
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            type = MessageType.valueOf(buf.readString(32767));
            rank = Rank.valueOf(buf.readString(32767));
            action = Action.valueOf(buf.readString(32767));
            dimension = buf.readInt();
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
        private int dimension;

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
            buf.writeInt(dimension);
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerName = buf.readString(32767);
            dimension = buf.readInt();
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

            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.CAN_PROMOTE) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(playerName, Rank.NEUTRAL, colony.getWorld());
            }
            else
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
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
        private int dimension;

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
            buf.writeInt(dimension);
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerName = buf.readString(32767);
            id = PacketUtils.readUUID(buf);
            dimension = buf.readInt();
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

            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.CAN_PROMOTE) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(id, playerName, Rank.NEUTRAL);
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
        private Type type;

        /**
         * The dimension of the
         */
        private int dimension;

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
         * @param type   Promote or demote.
         */
        public ChangePlayerRank(@NotNull final IColonyView colony, final UUID player, final Type type)
        {
            super();
            this.colonyID = colony.getID();
            this.playerID = player;
            this.type = type;
            this.dimension = colony.getDimension();
        }

        /**
         * Possible type of action.
         */
        public enum Type
        {
            PROMOTE,
            DEMOTE
        }

        @Override
        public void toBytes(@NotNull final PacketBuffer buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            buf.writeString(type.name());
            buf.writeInt(dimension);
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            type = Type.valueOf(buf.readString(32767));
            dimension = buf.readInt();
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

            if (colony == null || colony.getWorld() == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }
            final PlayerEntity player = ctxIn.getSender();
            if (colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
            {
                if (type == Type.PROMOTE && colony.getPermissions().getRank(player).ordinal() < colony.getPermissions().getRank(playerID).ordinal())
                {
                    colony.getPermissions().setPlayerRank(playerID, Permissions.getPromotionRank(colony.getPermissions().getRank(playerID)), colony.getWorld());
                }
                else if (type == Type.DEMOTE
                           && (colony.getPermissions().getRank(player).ordinal() < colony.getPermissions().getRank(playerID).ordinal()
                                 || player.getUniqueID().equals(playerID)))
                {
                    colony.getPermissions().setPlayerRank(playerID, Permissions.getDemotionRank(colony.getPermissions().getRank(playerID)), colony.getWorld());
                }
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
        private int dimension;

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
            buf.writeInt(dimension);
        }

        @Override
        public void fromBytes(@NotNull final PacketBuffer buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            dimension = buf.readInt();
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
            if ((permissionsPlayer.getRank() == Rank.HOSTILE && colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
                  || (permissionsPlayer.getRank() != Rank.HOSTILE
                        && colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS)
                        && colony.getPermissions().getRank(player).ordinal() < colony.getPermissions().getRank(playerID).ordinal())
                  || player.getUniqueID().equals(playerID))
            {
                colony.getPermissions().removePlayer(playerID);
            }
        }
    }
}
