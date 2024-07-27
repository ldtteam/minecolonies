package com.minecolonies.core.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Permission message to set permissions on the colony from the GUI.
 */
public class PermissionsMessage
{
    /**
     * Client side presentation of the
     */
    public static class View extends AbstractClientPlayMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "permission_view", View::new, true, false);

        private final int          colonyID;
        private final FriendlyByteBuf data;

        /**
         * The dimension of the
         */
        private final ResourceKey<Level> dimension;

        /**
         * Instantiate
         *
         * @param colony     with the colony.
         * @param viewerRank and viewer rank.
         */
        public View(@NotNull final Colony colony, @NotNull final Rank viewerRank)
        {
            super(TYPE);
            this.colonyID = colony.getID();
            this.data = new FriendlyByteBuf(Unpooled.buffer());
            colony.getPermissions().serializeViewNetworkData(this.data, viewerRank);
            this.dimension = colony.getDimension();
        }

        protected View(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            colonyID = buf.readInt();
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
            data = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()));
        }

        
        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, @Nullable final Player player)
        {
            IColonyManager.getInstance().handlePermissionsViewMessage(colonyID, data, dimension);
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            data.resetReaderIndex();
            buf.writeInt(colonyID);
            buf.writeUtf(dimension.location().toString());
            buf.writeByteArray(data.array());
        }
    }

    /**
     * Permission message class.
     */
    public static class Permission extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_permission", Permission::new);

        private final boolean enable;
        private final int     rankId;
        private final Action  action;

        /**
         * {@link Permission}.
         *
         * @param colony Colony the permission is set in
         * @param enable Whether the permission gets enabled or disabled
         * @param rank   Rank of the permission {@link Rank}
         * @param action Action of the permission {@link Action}
         */
        public Permission(@NotNull final IColonyView colony, final boolean enable, final Rank rank, final Action action)
        {
            super(TYPE, colony);
            this.enable = enable;
            this.rankId = rank.getId();
            this.action = action;
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            colony.getPermissions().alterPermission(colony.getPermissions().getRank(player), colony.getPermissions().getRanks().get(rankId), action, enable);
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeBoolean(enable);
            buf.writeInt(rankId);
            buf.writeUtf(action.name());
        }

        protected Permission(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            enable = buf.readBoolean();
            rankId = buf.readInt();
            action = Action.valueOf(buf.readUtf(32767));
        }
    }

    /**
     * Message class for adding a player to a permission set.
     */
    public static class AddPlayer extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_add_player", AddPlayer::new);

        private final String playerName;

        /**
         * Constructor for adding player to permission
         *
         * @param colony Colony the permission is set in.
         * @param player New player name to be added.
         */
        public AddPlayer(@NotNull final IColonyView colony, final String player)
        {
            super(TYPE, colony);
            this.playerName = player;
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeUtf(playerName);
        }

        protected AddPlayer(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            playerName = buf.readUtf(32767);
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            colony.getPermissions().addPlayer(playerName, colony.getPermissions().getRank(IPermissions.NEUTRAL_RANK_ID), colony.getWorld());
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message class for adding a rank to the colony
     */
    public static class AddRank extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_add_rank", AddRank::new);

        /**
         * the name of the new rank
         */
        private final String rankName;

        /**
         * Constructor for adding a rank to the colony
         * @param colony the colony to add the rank to
         * @param name the name of the rank
         */
        public AddRank(@NotNull final IColonyView colony, @NotNull final String name)
        {
            super(TYPE, colony);
            this.rankName = name;
        }

        @Override
        protected void toBytes(final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeUtf(rankName);
        }

        protected AddRank(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            this.rankName = buf.readUtf(32767);
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            colony.getPermissions().addRank(rankName);
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message class for adding a player or fakePlayer to a permission set.
     */
    public static class AddPlayerOrFakePlayer extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_add_player_or_fake_player", AddPlayerOrFakePlayer::new);

        private final String playerName;
        private final UUID   id;

        /**
         * Constructor for adding player to permission
         *
         * @param colony     Colony the permission is set in.
         * @param playerName New player name to be added.
         * @param id         the id of the player or fakeplayer.
         */
        public AddPlayerOrFakePlayer(@NotNull final IColonyView colony, final String playerName, final UUID id)
        {
            super(TYPE, colony);
            this.playerName = playerName;
            this.id = id;
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeUtf(playerName);
            buf.writeUUID(id);
        }

        protected AddPlayerOrFakePlayer(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            playerName = buf.readUtf(32767);
            id = buf.readUUID();
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            colony.getPermissions().addPlayer(id, playerName, colony.getPermissions().getRank(IPermissions.NEUTRAL_RANK_ID));
            Optional.ofNullable(colony.getBuildingManager().getTownHall()).ifPresent(th -> th.removePermissionEvents(id));
            SoundUtils.playSuccessSound(player, player.blockPosition());
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message class for setting a player rank in the permissions.
     */
    public static class ChangePlayerRank extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_change_player_rank", ChangePlayerRank::new);

        private final UUID playerID;
        private final int rankId;

        /**
         * Constructor for setting a player rank.
         *
         * @param colony Colony the rank is set in.
         * @param player UUID of the player to set rank.
         * @param rank   Rank to change to.
         */
        public ChangePlayerRank(@NotNull final IColonyView colony, final UUID player, final Rank rank)
        {
            super(TYPE, colony);
            this.playerID = player;
            this.rankId = rank.getId();
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeUUID(playerID);
            buf.writeInt(rankId);
        }

        protected ChangePlayerRank(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            playerID = buf.readUUID();
            rankId = buf.readInt();
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            final Rank rank = colony.getPermissions().getRanks().get(rankId);
            if (rank != colony.getPermissions().getRankOwner())
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }
            final Player player = ctxIn.getSender();
            if (colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS) && rank != colony.getPermissions().getRankOwner())
            {
                colony.getPermissions().setPlayerRank(playerID, rank, colony.getWorld());
            }
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message class for removing a player from a permission set.
     */
    public static class RemovePlayer extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_remove_player", RemovePlayer::new);

        private final UUID playerID;

        /**
         * Constructor for removing player from permission set.
         *
         * @param colony Colony the player is removed from the permission.
         * @param player UUID of the removed player.
         */
        public RemovePlayer(@NotNull final IColonyView colony, final UUID player)
        {
            super(TYPE, colony);
            this.playerID = player;
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeUUID(playerID);
        }

        protected RemovePlayer(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            playerID = buf.readUUID();
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            final ColonyPlayer permissionsPlayer = colony.getPermissions().getPlayers().get(playerID);
            if ((permissionsPlayer.getRank().isHostile() && colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
                  || (!permissionsPlayer.getRank().isHostile()
                        && colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS)
                        && colony.getPermissions().getRank(player).isColonyManager())
                  || player.getUUID().equals(playerID))
            {
                colony.getPermissions().removePlayer(playerID);
            }
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message class for removing a rank from a colony
     */
    public static class RemoveRank extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_remove_rank", RemoveRank::new);

        /**
         * the rank ID
         */
        private final int rankId;

        /**
         * Constructor for removing a rank from a colony
         * @param colony the colony to remove the rank from
         * @param rank the rank to remove
         */
        public RemoveRank(@NotNull final IColonyView colony, @NotNull final Rank rank)
        {
            super(TYPE, colony);
            rankId = rank.getId();
        }

        @Override
        protected void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeInt(rankId);
        }

        protected RemoveRank(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            rankId = buf.readInt();
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            colony.getPermissions().removeRank(colony.getPermissions().getRanks().get(rankId));
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message for changing the rank type of a given rank on a colony
     */
    public static class EditRankType extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_edit_rank_type", EditRankType::new);

        /**
         * the rank id
         */
        private final int rankId;
        /**
         * the new rank type
         */
        private final int rankType;

        /**
         * Constructor for changing the rank type
         * @param colony the colony of the rank
         * @param rank the rank
         * @param rankType the new rank type
         */
        public EditRankType(@NotNull final IColonyView colony, @NotNull final Rank rank, final int rankType)
        {
            super(TYPE, colony);
            this.rankId = rank.getId();
            this.rankType = rankType;
        }

        @Override
        protected void toBytes(final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeInt(rankId);
            buf.writeInt(rankType);
        }

        protected EditRankType(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            this.rankId = buf.readInt();
            this.rankType = buf.readInt();
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
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

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }

    /**
     * Message to change whether a rank is a subscriber to certain colony events
     */
    public static class SetSubscriber extends AbstractColonyServerMessage
    {
        public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "permission_set_subscriber", SetSubscriber::new);

        /**
         * the rank ID
         */
        private final int rankId;
        /**
         * the new isSubscriber state
         */
        private final boolean isSubscriber;

        /**
         * Constructor to change whether the given rank is a subscriber
         * @param colony the colony of the rank
         * @param rank the rank
         * @param isSubscriber whether the rank should be a subscriber
         */
        public SetSubscriber(@NotNull final IColonyView colony, @NotNull final Rank rank, final boolean isSubscriber)
        {
            super(TYPE, colony);
            this.rankId = rank.getId();
            this.isSubscriber = isSubscriber;
        }

        @Override
        protected void toBytes(final FriendlyByteBuf buf)
        {
            super.toBytes(buf);
            buf.writeInt(rankId);
            buf.writeBoolean(isSubscriber);
        }

        protected SetSubscriber(final FriendlyByteBuf buf, final PlayMessageType<?> type)
        {
            super(buf, type);
            this.rankId = buf.readInt();
            this.isSubscriber = buf.readBoolean();
        }

        @Override
        protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
        {
            colony.getPermissions().getRank(rankId).setSubscriber(isSubscriber);
            colony.markDirty();
        }

        @Override
        @Nullable
        protected Action permissionNeeded()
        {
            return Action.EDIT_PERMISSIONS;
        }
    }
}
