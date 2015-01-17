package com.minecolonies.network.messages;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.network.PacketUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.UUID;

public class PermissionsMessage
{

    public static class View implements IMessage, IMessageHandler<View, IMessage>
    {
        private int     colonyID;
        private ByteBuf data = Unpooled.buffer();

        public View()
        {
        }

        public View(Colony colony, Permissions.Rank viewerRank)
        {
            this.colonyID = colony.getID();
            colony.getPermissions().serializeViewNetworkData(this.data, viewerRank);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeBytes(data);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = buf.readInt();
            buf.readBytes(data, buf.readableBytes());
        }

        @Override
        public IMessage onMessage(View message, MessageContext ctx)
        {
            return ColonyManager.handlePermissionsViewMessage(message.colonyID, message.data);
        }
    }

    public enum MessageType
    {
        SET_PERMISSION,
        REMOVE_PERMISSION,
        TOGGLE_PERMISSION
    }

    public static class Permission implements IMessage, IMessageHandler<Permission, IMessage>
    {
        public Permission()
        {
        }

        int colonyID;
        MessageType type;
        Permissions.Rank rank;
        Permissions.Action action;

        public Permission(ColonyView colony, MessageType type, Permissions.Rank rank, Permissions.Action action)
        {
            this.colonyID = colony.getID();
            this.type = type;
            this.rank = rank;
            this.action = action;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(colonyID);
            ByteBufUtils.writeUTF8String(buf, type.name());
            ByteBufUtils.writeUTF8String(buf, rank.name());
            ByteBufUtils.writeUTF8String(buf, action.name());
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = buf.readInt();
            type = MessageType.valueOf(ByteBufUtils.readUTF8String(buf));
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
            action = Permissions.Action.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public IMessage onMessage(Permission message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony == null)
            {
                MineColonies.logger.error(String.format("Colony #%d does not exist.", message.colonyID));
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
                MineColonies.logger.error(String.format("Invalid MessageType %s", message.type.toString()));
            }
            return null;
        }
    }

    public static class AddPlayer implements IMessage, IMessageHandler<AddPlayer, IMessage>
    {
        int colonyID;
        String playerName;

        public AddPlayer() {}

        public AddPlayer(ColonyView colony, String player)
        {
            this.colonyID = colony.getID();
            this.playerName = player;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(colonyID);
            ByteBufUtils.writeUTF8String(buf, playerName);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerName = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public IMessage onMessage(AddPlayer message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony != null)
            {
                colony.getPermissions().addPlayer(message.playerName, Permissions.Rank.NEUTRAL);
            }
            else
            {
                MineColonies.logger.error(String.format("Colony #%d does not exist.", message.colonyID));
            }
            return null;
        }
    }

    public static class SetPlayerRank implements IMessage, IMessageHandler<SetPlayerRank, IMessage>
    {
        int colonyID;
        UUID playerID;
        Permissions.Rank rank;

        public SetPlayerRank() {}

        public SetPlayerRank(ColonyView colony, UUID player, Permissions.Rank rank)
        {
            this.colonyID = colony.getID();
            this.playerID = player;
            this.rank = rank;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            ByteBufUtils.writeUTF8String(buf, rank.name());
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public IMessage onMessage(SetPlayerRank message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony != null)
            {
                colony.getPermissions().setPlayerRank(message.playerID, message.rank);
            }
            else
            {
                MineColonies.logger.error(String.format("Colony #%d does not exist.", message.colonyID));
            }
            return null;
        }
    }

    public static class RemovePlayer implements IMessage, IMessageHandler<RemovePlayer, IMessage>
    {
        int colonyID;
        UUID playerID;

        public RemovePlayer() {}

        public RemovePlayer(ColonyView colony, UUID player)
        {
            this.colonyID = colony.getID();
            this.playerID = player;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
        }

        @Override
        public IMessage onMessage(RemovePlayer message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony != null)
            {
                colony.getPermissions().removePlayer(message.playerID);
            }
            else
            {
                MineColonies.logger.error(String.format("Colony '#%d' does not exist.", message.colonyID));
            }
            return null;
        }
    }
}
