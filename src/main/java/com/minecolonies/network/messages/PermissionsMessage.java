package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.network.PacketUtils;
import com.minecolonies.util.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PermissionsMessage
{
    private static final String COLONY_DOES_NOT_EXIST = "Colony #%d does not exist.";

    /**
     * Enums for Message Type for the permission message
     * <p>
     * SET_PERMISSION       Setting a permission
     * REMOVE_PERMISSION    Removing a permission
     * TOGGLE_PERMISSION    Toggeling a permission
     */
    public enum MessageType
    {
        SET_PERMISSION,
        REMOVE_PERMISSION,
        TOGGLE_PERMISSION
    }

    public static class View implements IMessage, IMessageHandler<View, IMessage>
    {
        private int     colonyID;
        private ByteBuf data;

        public View()
        {
            //Required
        }

        public View(@NotNull Colony colony, @NotNull Permissions.Rank viewerRank)
        {
            this.colonyID = colony.getID();
            this.data = Unpooled.buffer();
            colony.getPermissions().serializeViewNetworkData(this.data, viewerRank);
        }

        @Override
        public void fromBytes(@NotNull ByteBuf buf)
        {
            colonyID = buf.readInt();
            data = buf;
        }

        @Nullable
        @Override
        public IMessage onMessage(@NotNull View message, MessageContext ctx)
        {
            return ColonyManager.handlePermissionsViewMessage(message.colonyID, message.data);
        }        @Override
        public void toBytes(@NotNull ByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeBytes(data);
        }


    }

    public static class Permission implements IMessage, IMessageHandler<Permission, IMessage>
    {
        private int                colonyID;
        private MessageType        type;
        private Permissions.Rank   rank;
        private Permissions.Action action;

        public Permission()
        {
            //Required
        }

        /**
         * {@link Permission}.
         *
         * @param colony Colony the permission is set in
         * @param type   Type of permission {@link MessageType}
         * @param rank   Rank of the permission {@link com.minecolonies.colony.permissions.Permissions.Rank}
         * @param action Action of the permission {@link com.minecolonies.colony.permissions.Permissions.Action}
         */
        public Permission(@NotNull ColonyView colony, MessageType type, Permissions.Rank rank, Permissions.Action action)
        {
            this.colonyID = colony.getID();
            this.type = type;
            this.rank = rank;
            this.action = action;
        }

        @Override
        public void toBytes(@NotNull ByteBuf buf)
        {
            buf.writeInt(colonyID);
            ByteBufUtils.writeUTF8String(buf, type.name());
            ByteBufUtils.writeUTF8String(buf, rank.name());
            ByteBufUtils.writeUTF8String(buf, action.name());
        }

        @Override
        public void fromBytes(@NotNull ByteBuf buf)
        {
            colonyID = buf.readInt();
            type = MessageType.valueOf(ByteBufUtils.readUTF8String(buf));
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
            action = Permissions.Action.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Nullable
        @Override
        public IMessage onMessage(@NotNull Permission message, @NotNull MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
                return null;
            }

            //Verify player has permission to do edit permissions
            if (!colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.EDIT_PERMISSIONS))
            {
                return null;
            }

            switch (message.type)
            {
                case SET_PERMISSION:
                    colony.getPermissions().setPermission(message.rank, message.action);
                    break;
                case REMOVE_PERMISSION:
                    colony.getPermissions().removePermission(message.rank, message.action);
                    break;
                case TOGGLE_PERMISSION:
                    colony.getPermissions().togglePermission(message.rank, message.action);
                    break;
                default:
                    Log.getLogger().error(String.format("Invalid MessageType %s", message.type.toString()));
            }
            return null;
        }
    }

    /**
     * Message class for adding a player to a permission set
     */
    public static class AddPlayer implements IMessage, IMessageHandler<AddPlayer, IMessage>
    {
        private int    colonyID;
        private String playerName;

        public AddPlayer()
        {
            //Required
        }

        /**
         * Constructor for adding player to permission message
         *
         * @param colony Colony the permission is set in
         * @param player New player name to be added
         */
        public AddPlayer(@NotNull ColonyView colony, String player)
        {
            this.colonyID = colony.getID();
            this.playerName = player;
        }

        @Override
        public void toBytes(@NotNull ByteBuf buf)
        {
            buf.writeInt(colonyID);
            ByteBufUtils.writeUTF8String(buf, playerName);
        }

        @Override
        public void fromBytes(@NotNull ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerName = ByteBufUtils.readUTF8String(buf);
        }

        @Nullable
        @Override
        public IMessage onMessage(@NotNull AddPlayer message, @NotNull MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony != null && colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.CAN_PROMOTE) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(message.playerName, Permissions.Rank.NEUTRAL, colony.getWorld());
            }
            else
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
            }
            return null;
        }
    }

    /**
     * Message class for setting a player rank in the permissions
     */
    public static class ChangePlayerRank implements IMessage, IMessageHandler<ChangePlayerRank, IMessage>
    {
        private int  colonyID;
        private UUID playerID;
        private Type type;

        public ChangePlayerRank()
        {
            //Required
        }

        /**
         * Constructor for setting a player rank.
         *
         * @param colony Colony the rank is set in.
         * @param player UUID of the player to set rank.
         * @param type   Promote or demote.
         */
        public ChangePlayerRank(@NotNull ColonyView colony, UUID player, Type type)
        {
            this.colonyID = colony.getID();
            this.playerID = player;
            this.type = type;
        }

        public enum Type
        {
            PROMOTE,
            DEMOTE
        }

        @Override
        public void toBytes(@NotNull ByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            ByteBufUtils.writeUTF8String(buf, type.name());
        }

        @Override
        public void fromBytes(@NotNull ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            type = Type.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Nullable
        @Override
        public IMessage onMessage(@NotNull ChangePlayerRank message, @NotNull MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony == null || colony.getWorld() == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
                return null;
            }

            if (message.type == Type.PROMOTE && colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.CAN_PROMOTE))
            {
                colony.getPermissions().setPlayerRank(message.playerID, Permissions.getPromotionRank(colony.getPermissions().getRank(message.playerID)), colony.getWorld());
            }
            else if (message.type == Type.DEMOTE && colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.CAN_DEMOTE))
            {
                colony.getPermissions().setPlayerRank(message.playerID, Permissions.getDemotionRank(colony.getPermissions().getRank(message.playerID)), colony.getWorld());
            }

            return null;
        }
    }

    /**
     * Message class for removing a player from a permission set
     */
    public static class RemovePlayer implements IMessage, IMessageHandler<RemovePlayer, IMessage>
    {
        private int  colonyID;
        private UUID playerID;

        public RemovePlayer()
        {
            //Required
        }

        /**
         * Constructor for removing player from permission set
         *
         * @param colony Colony the player is removed from the permission
         * @param player UUID of the removed player
         */
        public RemovePlayer(@NotNull ColonyView colony, UUID player)
        {
            this.colonyID = colony.getID();
            this.playerID = player;
        }

        @Override
        public void toBytes(@NotNull ByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
        }

        @Override
        public void fromBytes(@NotNull ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
        }

        @Nullable
        @Override
        public IMessage onMessage(@NotNull RemovePlayer message, @NotNull MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
                return null;
            }

            Permissions.Player player = colony.getPermissions().getPlayers().get(message.playerID);
            if ((player.getRank() == Permissions.Rank.HOSTILE && colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.CAN_PROMOTE)) ||
                  (player.getRank() != Permissions.Rank.HOSTILE && colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.CAN_DEMOTE)))
            {
                colony.getPermissions().removePlayer(message.playerID);
            }

            return null;
        }
    }
}
