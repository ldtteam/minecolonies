package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ColonyConnectionNode;
import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

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
}
