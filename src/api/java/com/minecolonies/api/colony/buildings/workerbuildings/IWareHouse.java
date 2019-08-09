package com.minecolonies.api.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.ICitizenAssignable;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.AbstractTileEntityWareHouse;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public interface IWareHouse extends ISchematicProvider, ICitizenAssignable, IBuildingContainer, IBuilding
{
    /**
     * Register deliveryman with the warehouse.
     *
     * @param buildingWorker the building of the worker.
     * @return true if able to register or already registered
     */
    boolean registerWithWareHouse(IBuildingDeliveryman buildingWorker);

    /**
     * Check if deliveryman is allowed to access warehouse.
     *
     * @param buildingWorker the building of the deliveryman.
     * @return true if able to.
     */
    boolean canAccessWareHouse(IBuildingDeliveryman buildingWorker);

    /**
     * Get the deliverymen connected with this building.
     *
     * @return the unmodifiable List of positions of them.
     */
    List<Vec3d> getRegisteredDeliverymen();

    /**
     * Upgrade all containers by 9 slots.
     *
     * @param world the world object.
     */
    void upgradeContainers(World world);

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link AbstractTileEntityColonyBuilding} object of the building.
     */
    @Override
    AbstractTileEntityWareHouse getTileEntity();
}
