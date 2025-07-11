package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ColonyConnectionNode;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_STATUS;

/**
 * Connection manager interface.
 */
public interface IColonyConnectionManager extends INBTSerializable<CompoundTag>
{
    /**
     * Diplomacy Status between two colonies.
     */
    enum DiplomacyStatus
    {
        ALLIES,
        NEUTRAL,
        HOSTILE;

        /**
         * Get translation key for the diplomacy status.
         * @return the string key.
         */
        public String translationKey()
        {
            return "com.minecolonies.core.colony.diplomacy.status." + name().toLowerCase();
        }
    }

    /**
     * Diplomacy Status between two colonies.
     */
    enum ConnectionEventType
    {
        ALLY_REQUEST,
        ALLY_CONFIRMED,
        FEUD_STARTED,
        NEUTRAL_SET;

        /**
         * Get translation key for the diplomacy status.
         * @return the string key.
         */
        public String translationKey()
        {
            return "com.minecolonies.core.colony.connectionevent." + name().toLowerCase();
        }
    }

    /**
     * Connected Colony Data with:
     *
     * @param id              the colony id.
     * @param name            the colony name (cached).
     * @param pos             the colony gate position (cached).
     * @param diplomacyStatus the diplomacy status of the two colonies.
     */
    record ConnectedColonyData(
        int id,
        String name,
        BlockPos pos,
        DiplomacyStatus diplomacyStatus)
    {

        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_ID, id);
            compoundTag.putString(TAG_NAME, name);
            BlockPosUtil.write(compoundTag, TAG_POS, pos);
            compoundTag.putInt(TAG_STATUS, diplomacyStatus.ordinal());
            return compoundTag;
        }

        public void serializeByteBuf(final FriendlyByteBuf buf)
        {
            buf.writeInt(id);
            buf.writeUtf(name);
            buf.writeBlockPos(pos);
            buf.writeInt(diplomacyStatus.ordinal());
        }

        public static ConnectedColonyData deserializeNBT(final CompoundTag compoundTag)
        {
            return new ConnectedColonyData(compoundTag.getInt(TAG_ID),
                compoundTag.getString(TAG_NAME),
                BlockPosUtil.read(compoundTag, TAG_POS),
                DiplomacyStatus.values()[compoundTag.getInt(TAG_STATUS)]);
        }

        public static ConnectedColonyData deserializeByteBuf(final FriendlyByteBuf buf)
        {
            return new ConnectedColonyData(buf.readInt(), buf.readUtf(32767), buf.readBlockPos(), DiplomacyStatus.values()[buf.readInt()]);
        }
    }

    /**
     * Connected Event Data with:
     *
     * @param id              the colony id.
     * @param connectionEventType the event type enum.
     */
    record ConnectionEventData(int id, ConnectionEventType connectionEventType)
    {
        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_ID, id);
            compoundTag.putInt(TAG_STATUS, connectionEventType.ordinal());
            return compoundTag;
        }

        public void serializeByteBuf(final FriendlyByteBuf buf)
        {
            buf.writeInt(id);
            buf.writeInt(connectionEventType.ordinal());
        }

        public static ConnectionEventData deserializeNBT(final CompoundTag compoundTag)
        {
            return new ConnectionEventData(compoundTag.getInt(TAG_ID),
                ConnectionEventType.values()[compoundTag.getInt(TAG_STATUS)]);
        }

        public static ConnectionEventData deserializeByteBuf(final FriendlyByteBuf buf)
        {
            return new ConnectionEventData(buf.readInt(), ConnectionEventType.values()[buf.readInt()]);
        }
    }

    /**
     * Add a new connection point and connect to neighbors.
     *
     * @param connectionPoint the node position.
     * @return
     */
    boolean addNewConnectionNode(final BlockPos connectionPoint);

    /**
     * Remove a connection point and update neighbors.
     * @param connectionPoint the node position.
     */
    void removeConnectionNode(final BlockPos connectionPoint);

    /**
     * Tick to process work.
     */
    void tick();

    /**
     * Get all directly connected colonies.
     * @return the map of directly connected colonies.
     */
    Int2ObjectMap<ConnectedColonyData> getDirectlyConnectedColonies();


    /**
     * Get all indirectly connected colonies.
     * @return the map of them.
     */
    Int2ObjectMap<ConnectedColonyData> getIndirectlyConnectedColonies();

    /**
     * Get a connection node.
     *
     * @param blockPos its position.
     * @return the node object.
     */
    ColonyConnectionNode getNode(final BlockPos blockPos);

    /**
     * Add a new gatehouse.
     * @param gateHousePosition the blockpos.
     */
    void addNewGateHouse(final BlockPos gateHousePosition);

    /**
     * Remove a gatehouse.
     * @param gateHousePosition the blockpos.
     */
    void removeGateHouse(final BlockPos gateHousePosition);

    /**
     * Attempt to establish a connection.
     * @param clickedPos the clicked position.
     * @param targetColony, the colony we're trying to connect to.
     */
    boolean attemptEstablishConnection(final BlockPos clickedPos, final IColony targetColony);

    /**
     * Serialize connection manager to view.
     * @param buf the buf to serialize it to.
     */
    void serializeToView(@NotNull FriendlyByteBuf buf);

    /**
     * Deserialize connection manager from buffer for client side usage.
     * @param buf the buf to read it from.
     */
    void deserializeFromView(@NotNull FriendlyByteBuf buf);
}
