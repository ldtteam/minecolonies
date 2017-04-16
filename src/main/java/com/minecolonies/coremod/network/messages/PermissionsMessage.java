package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.network.PacketUtils;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
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
     * Enums for Message Type for the permission message.
     * <p>
     * SET_PERMISSION       Setting a permission.
     * REMOVE_PERMISSION    Removing a permission.
     * TOGGLE_PERMISSION    Toggeling a permission.
     */
    public enum MessageType
    {
        SET_PERMISSION,
        REMOVE_PERMISSION,
        TOGGLE_PERMISSION
    }

    /**
     * Client side presentation of the message.
     */
    public static class View implements IMessage, IMessageHandler<View, IMessage>
    {
        private int     colonyID;
        private ByteBuf data;

        /**
         * Empty constructor used when registering the message.
         */
        public View()
        {
            super();
        }

        /**
         * Instantiate message.
         *
         * @param colony     with the colony.
         * @param viewerRank and viewer rank.
         */
        public View(@NotNull final Colony colony, @NotNull final Permissions.Rank viewerRank)
        {
            this.colonyID = colony.getID();
            this.data = Unpooled.buffer();
            colony.getPermissions().serializeViewNetworkData(this.data, viewerRank);
        }

        @Override
        public void fromBytes(@NotNull final ByteBuf buf)
        {
            colonyID = buf.readInt();
            data = buf;
        }

        @Nullable
        @Override
        public IMessage onMessage(@NotNull final View message, final MessageContext ctx)
        {
            return ColonyManager.handlePermissionsViewMessage(message.colonyID, message.data);
        }

        @Override
        public void toBytes(@NotNull final ByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeBytes(data);
        }
    }

    /**
     * Permission message class.
     */
    public static class Permission extends AbstractMessage<Permission, IMessage>
    {
        private int                colonyID;
        private MessageType        type;
        private Permissions.Rank   rank;
        private Permissions.Action action;

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
         * @param rank   Rank of the permission {@link com.minecolonies.coremod.colony.permissions.Permissions.Rank}
         * @param action Action of the permission {@link com.minecolonies.coremod.colony.permissions.Permissions.Action}
         */
        public Permission(@NotNull final ColonyView colony, final MessageType type, final Permissions.Rank rank, final Permissions.Action action)
        {
            super();
            this.colonyID = colony.getID();
            this.type = type;
            this.rank = rank;
            this.action = action;
        }

        @Override
        public void messageOnServerThread(final Permission message, final EntityPlayerMP player)
        {
            final Colony colony = ColonyManager.getColony(message.colonyID);
            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
                return;
            }

            //Verify player has permission to do edit permissions
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.EDIT_PERMISSIONS))
            {
                return;
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
        }

        @Override
        public void toBytes(@NotNull final ByteBuf buf)
        {
            buf.writeInt(colonyID);
            ByteBufUtils.writeUTF8String(buf, type.name());
            ByteBufUtils.writeUTF8String(buf, rank.name());
            ByteBufUtils.writeUTF8String(buf, action.name());
        }

        @Override
        public void fromBytes(@NotNull final ByteBuf buf)
        {
            colonyID = buf.readInt();
            type = MessageType.valueOf(ByteBufUtils.readUTF8String(buf));
            rank = Permissions.Rank.valueOf(ByteBufUtils.readUTF8String(buf));
            action = Permissions.Action.valueOf(ByteBufUtils.readUTF8String(buf));
        }
    }

    /**
     * Message class for adding a player to a permission set.
     */
    public static class AddPlayer extends AbstractMessage<AddPlayer, IMessage>
    {
        private int    colonyID;
        private String playerName;

        /**
         * Empty public constructor.
         */
        public AddPlayer()
        {
            super();
        }

        /**
         * Constructor for adding player to permission message.
         *
         * @param colony Colony the permission is set in.
         * @param player New player name to be added.
         */
        public AddPlayer(@NotNull final ColonyView colony, final String player)
        {
            super();
            this.colonyID = colony.getID();
            this.playerName = player;
        }

        @Override
        public void toBytes(@NotNull final ByteBuf buf)
        {
            buf.writeInt(colonyID);
            ByteBufUtils.writeUTF8String(buf, playerName);
        }

        @Override
        public void fromBytes(@NotNull final ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerName = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void messageOnServerThread(final AddPlayer message, final EntityPlayerMP player)
        {
            final Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony != null && colony.getPermissions().hasPermission(player, Permissions.Action.CAN_PROMOTE) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(message.playerName, Permissions.Rank.NEUTRAL, colony.getWorld());
            }
            else
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
            }
        }
    }

    /**
     * Message class for setting a player rank in the permissions.
     */
    public static class ChangePlayerRank extends AbstractMessage<ChangePlayerRank, IMessage>
    {
        private int  colonyID;
        private UUID playerID;
        private Type type;

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
        public ChangePlayerRank(@NotNull final ColonyView colony, final UUID player, final Type type)
        {
            super();
            this.colonyID = colony.getID();
            this.playerID = player;
            this.type = type;
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
        public void toBytes(@NotNull final ByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            ByteBufUtils.writeUTF8String(buf, type.name());
        }

        @Override
        public void fromBytes(@NotNull final ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            type = Type.valueOf(ByteBufUtils.readUTF8String(buf));
        }

        @Override
        public void messageOnServerThread(final ChangePlayerRank message, final EntityPlayerMP player)
        {
            final Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony == null || colony.getWorld() == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
                return;
            }

            if (colony.getPermissions().hasPermission(player, Permissions.Action.EDIT_PERMISSIONS))
            {
                if (message.type == Type.PROMOTE && colony.getPermissions().getRank(player).ordinal() < colony.getPermissions().getRank(message.playerID).ordinal())
                {
                    colony.getPermissions().setPlayerRank(message.playerID, Permissions.getPromotionRank(colony.getPermissions().getRank(message.playerID)), colony.getWorld());
                }
                else if (message.type == Type.DEMOTE
                           && (colony.getPermissions().getRank(player).ordinal() < colony.getPermissions().getRank(message.playerID).ordinal()
                                 || player.getUniqueID().equals(message.playerID)))
                {
                    colony.getPermissions().setPlayerRank(message.playerID, Permissions.getDemotionRank(colony.getPermissions().getRank(message.playerID)), colony.getWorld());
                }
            }
        }
    }

    /**
     * Message class for removing a player from a permission set.
     */
    public static class RemovePlayer extends AbstractMessage<RemovePlayer, IMessage>
    {
        private int  colonyID;
        private UUID playerID;

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
        public RemovePlayer(@NotNull final ColonyView colony, final UUID player)
        {
            super();
            this.colonyID = colony.getID();
            this.playerID = player;
        }

        @Override
        public void toBytes(@NotNull final ByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
        }

        @Override
        public void fromBytes(@NotNull final ByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
        }

        @Override
        public void messageOnServerThread(final RemovePlayer message, final EntityPlayerMP player)
        {
            final Colony colony = ColonyManager.getColony(message.colonyID);

            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, message.colonyID));
                return;
            }

            final Permissions.Player permissionsPlayer = colony.getPermissions().getPlayers().get(message.playerID);
            if ((permissionsPlayer.getRank() == Permissions.Rank.HOSTILE && colony.getPermissions().hasPermission(player, Permissions.Action.EDIT_PERMISSIONS))
                  || (permissionsPlayer.getRank() != Permissions.Rank.HOSTILE
                        && colony.getPermissions().hasPermission(player, Permissions.Action.EDIT_PERMISSIONS)
                        && colony.getPermissions().getRank(player).ordinal() < colony.getPermissions().getRank(message.playerID).ordinal())
                  || player.getUniqueID().equals(message.playerID))
            {
                colony.getPermissions().removePlayer(message.playerID);
            }
        }
    }
}
