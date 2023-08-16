package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.colony.Colony;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Permission message to set permissions on the colony from the GUI.
 */
public class PermissionsMessage
{
    private static final String COLONY_DOES_NOT_EXIST = "Colony #%d does not exist.";

    /**
     * Client side presentation of the
     */
    public static class View implements IMessage
    {
        private int          colonyID;
        private FriendlyByteBuf data;

        /**
         * The dimension of the
         */
        private ResourceKey<Level> dimension;

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
            this.data = new FriendlyByteBuf(Unpooled.buffer());
            colony.getPermissions().serializeViewNetworkData(this.data, viewerRank);
            this.dimension = colony.getDimension();
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            final FriendlyByteBuf newBuf = new FriendlyByteBuf(buf.retain());
            colonyID = newBuf.readInt();
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(newBuf.readUtf(32767)));
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
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            IColonyManager.getInstance().handlePermissionsViewMessage(colonyID, data, dimension);
            data.release();
        }

        @Override
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            data.resetReaderIndex();
            buf.writeInt(colonyID);
            buf.writeUtf(dimension.location().toString());
            buf.writeBytes(data);
        }
    }

    /**
     * Permission message class.
     */
    public static class Permission implements IMessage
    {
        private int     colonyID;
        private boolean enable;
        private Rank    rank;
        private Action  action;

        /**
         * The dimension of the
         */
        private ResourceKey<Level> dimension;

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
         * @param enable Whether the permission gets enabled or disabled
         * @param rank   Rank of the permission {@link Rank}
         * @param action Action of the permission {@link Action}
         */
        public Permission(@NotNull final IColonyView colony, final boolean enable, final Rank rank, final Action action)
        {
            super();
            this.colonyID = colony.getID();
            this.enable = enable;
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
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);
            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }

            colony.getPermissions().alterPermission(colony.getPermissions().getRank(ctxIn.getSender()), rank, action, enable);
        }

        @Override
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeBoolean(enable);
            buf.writeInt(rank.getId());
            buf.writeUtf(action.name());
            buf.writeUtf(dimension.location().toString());
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            colonyID = buf.readInt();
            enable = buf.readBoolean();
            final int rankId = buf.readInt();
            action = Action.valueOf(buf.readUtf(32767));
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
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
        private ResourceKey<Level> dimension;

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
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeUtf(playerName);
            buf.writeUtf(dimension.location().toString());
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            colonyID = buf.readInt();
            playerName = buf.readUtf(32767);
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
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
        private ResourceKey<Level> dimension;

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
        public void toBytes(FriendlyByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeUtf(rankName);
            buf.writeUtf(dimension.location().toString());
        }

        @Override
        public void fromBytes(FriendlyByteBuf buf)
        {
            this.colonyID = buf.readInt();
            this.rankName = buf.readUtf(32767);
            this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
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
        private ResourceKey<Level> dimension;

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
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyID);
            buf.writeUtf(playerName);
            PacketUtils.writeUUID(buf, id);
            buf.writeUtf(dimension.location().toString());
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            colonyID = buf.readInt();
            playerName = buf.readUtf(32767);
            id = PacketUtils.readUUID(buf);
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS) && colony.getWorld() != null)
            {
                colony.getPermissions().addPlayer(id, playerName, colony.getPermissions().getRank(colony.getPermissions().NEUTRAL_RANK_ID));
                Optional.ofNullable(colony.getBuildingManager().getTownHall()).ifPresent(th -> th.removePermissionEvents(id));
                SoundUtils.playSuccessSound(ctxIn.getSender(), ctxIn.getSender().blockPosition());
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
        private ResourceKey<Level> dimension;

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
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            buf.writeUtf(dimension.location().toString());
            buf.writeInt(rank.getId());
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
            IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);
            rank = colony.getPermissions().getRank(buf.readInt());

        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony == null || colony.getWorld() == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }
            final Player player = ctxIn.getSender();
            if (colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS) && rank != colony.getPermissions().getRankOwner())
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
        private ResourceKey<Level> dimension;

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
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyID);
            PacketUtils.writeUUID(buf, playerID);
            buf.writeUtf(dimension.location().toString());
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            colonyID = buf.readInt();
            playerID = PacketUtils.readUUID(buf);
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        }

        @Nullable
        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

            if (colony == null)
            {
                Log.getLogger().error(String.format(COLONY_DOES_NOT_EXIST, colonyID), new Exception());
                return;
            }
            final ServerPlayer player = ctxIn.getSender();
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
        private ResourceKey<Level> dimension;

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
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyId);
            buf.writeInt(rankId);
            buf.writeUtf(dimension.location().toString());
        }

        @Override
        public void fromBytes(@NotNull final FriendlyByteBuf buf)
        {
            colonyId = buf.readInt();
            rankId = buf.readInt();
            dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
            if (colony != null && colony.getPermissions().hasPermission(ctxIn.getSender(), Action.EDIT_PERMISSIONS))
            {
                colony.getPermissions().removeRank(colony.getPermissions().getRanks().get(rankId));
            }
        }
    }

    /**
     * Message for changing the rank type of a given rank on a colony
     */
    public static class EditRankType implements IMessage
    {
        /**
         * the colony id
         */
        private int colonyId;
        /**
         * the rank id
         */
        private int rankId;
        /**
         * the dimension
         */
        private ResourceKey<Level> dimension;
        /**
         * the new rank type
         */
        private int rankType;

        /**
         * empty public constructor
         */
        public EditRankType() { super(); }

        /**
         * Constructor for changing the rank type
         * @param colony the colony of the rank
         * @param rank the rank
         * @param rankType the new rank type
         */
        public EditRankType(@NotNull final IColonyView colony, @NotNull final Rank rank, @NotNull final int rankType)
        {
            this.colonyId = colony.getID();
            this.rankId = rank.getId();
            this.dimension = colony.getDimension();
            this.rankType = rankType;
        }

        @Override
        public void toBytes(final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyId);
            buf.writeInt(rankId);
            buf.writeUtf(dimension.location().toString());
            buf.writeInt(rankType);
        }

        @Override
        public void fromBytes(final FriendlyByteBuf buf)
        {
            this.colonyId = buf.readInt();
            this.rankId = buf.readInt();
            this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
            this.rankType = buf.readInt();
        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
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

    /**
     * Message to change whether a rank is a subscriber to certain colony events
     */
    public static class SetSubscriber implements IMessage
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
         * the dimension
         */
        private ResourceKey<Level> dimension;
        /**
         * the new isSubscriber state
         */
        private boolean isSubscriber;

        /**
         * Empty public constructor
         */
        public SetSubscriber() { super(); }

        /**
         * Constructor to change whether the given rank is a subscriber
         * @param colony the colony of the rank
         * @param rank the rank
         * @param isSubscriber whether the rank should be a subscriber
         */
        public SetSubscriber(@NotNull final IColonyView colony, @NotNull final Rank rank, @NotNull final boolean isSubscriber)
        {
            this.colonyId = colony.getID();
            this.dimension = colony.getDimension();
            this.rankId = rank.getId();
            this.isSubscriber = isSubscriber;
        }

        @Override
        public void toBytes(final FriendlyByteBuf buf)
        {
            buf.writeInt(colonyId);
            buf.writeUtf(dimension.location().toString());
            buf.writeInt(rankId);
            buf.writeBoolean(isSubscriber);
        }

        @Override
        public void fromBytes(final FriendlyByteBuf buf)
        {
            this.colonyId = buf.readInt();
            this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
            this.rankId = buf.readInt();
            this.isSubscriber = buf.readBoolean();
        }

        @Override
        public void onExecute(final net.minecraftforge.network.NetworkEvent.Context ctxIn, final boolean isLogicalServer)
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
