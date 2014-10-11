package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.permissions.Permissions;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.nbt.NBTTagCompound;
import sun.plugin2.message.Message;

import java.util.UUID;

/**
 * CLASS DESCRIPTION
 * Created: October 10, 2014
 *
 * @author Colton
 */
public class PermissionsMessage {

    public static class View implements IMessage
    {
        public View(){}

        UUID colonyID;
        NBTTagCompound data;

        public View(UUID id, NBTTagCompound data)
        {
            this.colonyID = id;
            this.data = data;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            this.colonyID = new UUID(buf.readLong(), buf.readLong());
            this.data = ByteBufUtils.readTag(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeLong(colonyID.getMostSignificantBits());
            buf.writeLong(colonyID.getLeastSignificantBits());
            ByteBufUtils.writeTag(buf, data);
        }

        public static class Handler implements IMessageHandler<View, IMessage>
        {
            @Override
            public IMessage onMessage(View message, MessageContext ctx)
            {
                return ColonyManager.handlePermissionsViewPacket(message.colonyID, message.data);
            }
        }
    }

    public enum MessageType
    {
        SET_PERMISSION,
        REMOVE_PERMISSION,
        TOGGLE_PERMISSION
    }

    public static class Permission implements IMessage
    {
        public Permission(){}

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
        public void fromBytes(ByteBuf buf)
        {
            colonyID = new UUID(buf.readLong(), buf.readLong());
            type = MessageType.valueOf(ByteBufUtils.readUTF8String(buf));
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
            action = Permissions.Action.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeLong(colonyID.getMostSignificantBits());
            buf.writeLong(colonyID.getLeastSignificantBits());

            ByteBufUtils.writeUTF8String(buf, type.name());
            ByteBufUtils.writeUTF8String(buf, rank.name());
            ByteBufUtils.writeUTF8String(buf, action.name());
        }

        public static class Handler implements IMessageHandler<Permission, IMessage>
        {
            @Override
            public IMessage onMessage(Permission message, MessageContext ctx) {

                Colony colony = ColonyManager.getColonyById(message.colonyID);

                if(colony == null)
                {
                    //todo log error
                    return null;
                }

                switch(message.type)
                {
                    case SET_PERMISSION:
                        colony.getPermissionHandler().setPermission(message.rank, message.action);
                        break;
                    case REMOVE_PERMISSION:
                        colony.getPermissionHandler().removePermission(message.rank, message.action);
                        break;
                    case TOGGLE_PERMISSION:
                        colony.getPermissionHandler().togglePermission(message.rank, message.action);
                        break;
                default:
                    //todo log error
                }
                return null;
            }
        }
    }

    public static class AddPlayer implements IMessage
    {
        public AddPlayer() {}

        UUID colonyID;
        UUID playerID;
        Permissions.Rank rank;

        public AddPlayer(UUID id, UUID player, Permissions.Rank rank) {
            this.colonyID = id;
            this.playerID = player;
            this.rank = rank;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            colonyID = new UUID(buf.readLong(), buf.readLong());
            playerID = new UUID(buf.readLong(), buf.readLong());
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(colonyID.getMostSignificantBits());
            buf.writeLong(colonyID.getLeastSignificantBits());

            buf.writeLong(playerID.getMostSignificantBits());
            buf.writeLong(playerID.getLeastSignificantBits());

            ByteBufUtils.writeUTF8String(buf, rank.name());
        }

        public static class Handler implements IMessageHandler<AddPlayer, IMessage> {
            @Override
            public IMessage onMessage(AddPlayer message, MessageContext ctx) {

                Colony colony = ColonyManager.getColonyById(message.colonyID);

                if (colony != null) {
                    colony.addPlayer(message.playerID, message.rank);
                } else {
                    //todo log error
                }
                return null;
            }
        }
    }

    public static class RemovePlayer implements IMessage
    {
        public RemovePlayer() {}

        UUID colonyID;
        UUID playerID;

        public RemovePlayer(UUID id, UUID player) {
            this.colonyID = id;
            this.playerID = player;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            colonyID = new UUID(buf.readLong(), buf.readLong());
            playerID = new UUID(buf.readLong(), buf.readLong());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(colonyID.getMostSignificantBits());
            buf.writeLong(colonyID.getLeastSignificantBits());

            buf.writeLong(playerID.getMostSignificantBits());
            buf.writeLong(playerID.getLeastSignificantBits());
        }

        public static class Handler implements IMessageHandler<RemovePlayer, IMessage> {
            @Override
            public IMessage onMessage(RemovePlayer message, MessageContext ctx) {

                Colony colony = ColonyManager.getColonyById(message.colonyID);

                if (colony != null) {
                    colony.removePlayer(message.playerID);
                } else {
                    //todo log error
                }
                return null;
            }
        }
    }
}
