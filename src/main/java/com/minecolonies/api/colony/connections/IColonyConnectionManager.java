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
    TreeMap<Integer, ColonyConnection> getDirectlyConnectedColonies();


    /**
     * Get all indirectly connected colonies.
     * @return the map of them.
     */
    TreeMap<Integer, ColonyConnection> getIndirectlyConnectedColonies();

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
    void triggerConnectionEvent(ConnectionEvent connectionEventData);

    /**
     * Get the list of connection events.
     * @return the connection events.
     */
    List<ConnectionEvent> getConnectionEvents();

    /**
     * Get colony diplomacy status by id.
     * @param id the id to query from.
     * @return the diplomacy status.
     */
    DiplomacyStatus getColonyDiplomacyStatus(int id);
}
