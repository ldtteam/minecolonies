package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.coremod.colony.buildings.*;

public interface IBuildingDeliveryman extends ISchematicProvider, ICitizenAssignable, IBuildingContainer, IBuilding, IBuildingWorker
{
    /**
     * Get the building the deliveryman should deliver to.
     *
     * @return the building.
     */
    ILocation getBuildingToDeliver();

    /**
     * Set the building the deliveryman should deliver to.
     *
     * @param building building to deliver to.
     */
    void setBuildingToDeliver(ILocation building);
}
