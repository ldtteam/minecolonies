package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import org.jetbrains.annotations.Nullable;

public interface ICitizenColonyHandler
{
    /**
     * calculate this worker building.
     *
     * @return the building or null if none present.
     */
    @Nullable
    IBuildingWorker getWorkBuilding();

    @Nullable
    IBuilding getHomeBuilding();

    /**
     * Server-specific update for the EntityCitizen.
     */
    void registerWithColony(final int colonyID, final int citizenID);

    /**
     * Update the client side of the citizen entity.
     */
    void updateColonyClient();

    /**
     * Get the amount the worker should decrease its saturation by each action done or x blocks traveled.
     * @return the double describing it.
     */
    double getPerBuildingFoodCost();

    /**
     * Getter for the colony.
     * @return the colony of the citizen or null.
     */
    @Nullable
    IColony getColony();

    /**
     * Getter for the colonz id.
     * @return the colony id.
     */
    int getColonyId();

    /**
     * Setter for the colony id.
     * @param colonyId the new colonyId.
     */
    void setColonyId(int colonyId);

    /**
     *  Actions when the entity is removed.
     */
    void onCitizenRemoved();

    /**
     * Check if a citizen is at home.
     * @return true if so.
     */
    boolean isAtHome();
}
