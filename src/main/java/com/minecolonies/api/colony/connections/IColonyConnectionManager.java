package com.minecolonies.api.colony.connections;

import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeMap;

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
        NEUTRAL_SET,
        DISCONNECTED;

        /**
         * Get translation key for the diplomacy status.
         * @return the string key.
         */
        public String translationKey()
        {
            return "com.minecolonies.core.gui.connectionevent." + name().toLowerCase();
        }
    }


    /**
     * Connected Event Data with:
     *
     * @param id              the colony id.
     * @param connectionEventType the event type enum.
     */
    record ConnectionEventData(int id, String name, ConnectionEventType connectionEventType)
    {
        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_ID, id);
            compoundTag.putString(TAG_NAME, name);
            compoundTag.putInt(TAG_STATUS, connectionEventType.ordinal());
            return compoundTag;
        }

        public void serializeByteBuf(final FriendlyByteBuf buf)
        {
            buf.writeInt(id);
            buf.writeUtf(name);
            buf.writeInt(connectionEventType.ordinal());
        }

        public static ConnectionEventData deserializeNBT(final CompoundTag compoundTag)
        {
            return new ConnectionEventData(compoundTag.getInt(TAG_ID),
                compoundTag.getString(TAG_NAME),
                ConnectionEventType.values()[compoundTag.getInt(TAG_STATUS)]);
        }

        public static ConnectionEventData deserializeByteBuf(final FriendlyByteBuf buf)
        {
            return new ConnectionEventData(buf.readInt(), buf.readUtf(32767), ConnectionEventType.values()[buf.readInt()]);
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
    TreeMap<Integer, ConnectedColonyData> getDirectlyConnectedColonies();


    /**
     * Get all indirectly connected colonies.
     * @return the map of them.
     */
    TreeMap<Integer, ConnectedColonyData> getIndirectlyConnectedColonies();

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

    /**
     * Trigger a connection event at a colony.
     * @param connectionEventData the source colony data.
     */
    void triggerConnectionEvent(ConnectionEventData connectionEventData);

    /**
     * Get the list of connection events.
     * @return the connection events.
     */
    List<ConnectionEventData> getConnectionEvents();

    /**
     * Get colony diplomacy status by id.
     * @param id the id to query from.
     * @return the diplomacy status.
     */
    DiplomacyStatus getColonyDiplomacyStatus(int id);
}
