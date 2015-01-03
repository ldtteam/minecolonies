package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
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
        private UUID    colonyID;
        private ByteBuf data = Unpooled.buffer();

        public View()
        {
        }

        public View(Colony colony)
        {
            this.colonyID = colony.getID();
            colony.getPermissions().serializeViewNetworkData(this.data);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            PacketUtils.writeUUID(buf, colonyID);
            buf.writeBytes(data);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = PacketUtils.readUUID(buf);
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

        UUID colonyID;
        MessageType type;
        Permissions.Rank rank;
        Permissions.Action action;

        public Permission(UUID id, MessageType type, Permissions.Rank rank, Permissions.Action action)
        {
            this.colonyID = id;
            this.type = type;
            this.rank = rank;
            this.action = action;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            PacketUtils.writeUUID(buf, colonyID);
            ByteBufUtils.writeUTF8String(buf, type.name());
            ByteBufUtils.writeUTF8String(buf, rank.name());
            ByteBufUtils.writeUTF8String(buf, action.name());
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = PacketUtils.readUUID(buf);
            type = MessageType.valueOf(ByteBufUtils.readUTF8String(buf));
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
            action = Permissions.Action.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public IMessage onMessage(Permission message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColonyById(message.colonyID);

            if (colony == null)
            {
                //todo log error
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
                //todo log error
            }
            return null;
        }
    }

    public static class AddPlayer implements IMessage, IMessageHandler<AddPlayer, IMessage>
    {
        public AddPlayer()
        {
        }

        UUID colonyID;
        UUID playerID;
        Permissions.Rank rank;

        public AddPlayer(UUID id, UUID player, Permissions.Rank rank)
        {
            this.colonyID = id;
            this.playerID = player;
            this.rank = rank;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            PacketUtils.writeUUID(buf, colonyID);
            PacketUtils.writeUUID(buf, playerID);
            ByteBufUtils.writeUTF8String(buf, rank.name());
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = PacketUtils.readUUID(buf);
            playerID = PacketUtils.readUUID(buf);
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public IMessage onMessage(AddPlayer message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColonyById(message.colonyID);

            if (colony != null)
            {
                colony.getPermissions().addPlayer(message.playerID, message.rank);
            } else
            {
                //todo log error
            }
            return null;
        }
    }

    public static class RemovePlayer implements IMessage, IMessageHandler<RemovePlayer, IMessage>
    {
        public RemovePlayer()
        {
        }

        UUID colonyID;
        UUID playerID;

        public RemovePlayer(UUID id, UUID player)
        {
            this.colonyID = id;
            this.playerID = player;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            PacketUtils.writeUUID(buf, colonyID);
            PacketUtils.writeUUID(buf, playerID);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            colonyID = PacketUtils.readUUID(buf);
            playerID = PacketUtils.readUUID(buf);
        }

        @Override
        public IMessage onMessage(RemovePlayer message, MessageContext ctx)
        {

            Colony colony = ColonyManager.getColonyById(message.colonyID);

            if (colony != null)
            {
                colony.getPermissions().removePlayer(message.playerID);
            } else
            {
                //todo log error
            }
            return null;
        }
    }
}
