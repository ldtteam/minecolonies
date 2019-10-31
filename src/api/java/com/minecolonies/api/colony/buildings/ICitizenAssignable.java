package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface ICitizenAssignable extends ISchematicProvider
{
    /**
     * Returns the colony of the building.
     *
     * @return {@link IColony} of the current object.
     */
    @NotNull
    IColony getColony();

    /**
     * Method to do things when a block is destroyed.
     */
    void onDestroyed();

    /**
     * On tick of the server.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    void onServerTick(TickEvent.ServerTickEvent event);

    /**
     * On tick of the colony.
     *
     * @param colony {@link TickEvent.WorldTickEvent}
     */
    void onColonyTick(IColony colony);

    /**
     * Get the main worker of the building (the first in the list).
     *
     * @return the matching CitizenData.
     */
    ICitizenData getMainCitizen();

    /**
     * Returns the worker of the current building.
     *
     * @return {@link ICitizenData} of the current building
     */
    List<ICitizenData> getAssignedCitizen();

    /**
     * Method to remove a citizen.
     *
     * @param citizen Citizen to be removed.
     */
    void removeCitizen(ICitizenData citizen);

    /**
     * Returns if the {@link ICitizenData} is the same as the worker.
     *
     * @param citizen {@link ICitizenData} you want to compare
     * @return true if same citizen, otherwise false
     */
    boolean isCitizenAssigned(ICitizenData citizen);

    /**
     * Returns the first worker in the list.
     *
     * @return the EntityCitizen of that worker.
     */
    Optional<AbstractEntityCitizen> getMainCitizenEntity();

    /**
     * Returns whether or not the building has a worker.
     *
     * @return true if building has worker, otherwise false.
     */
    boolean hasAssignedCitizen();

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Nullable
    List<Optional<AbstractEntityCitizen>> getAssignedEntities();

    /**
     * Assign the citizen to the current building.
     *
     * @param citizen {@link ICitizenData} of the worker
     */
    boolean assignCitizen(ICitizenData citizen);

    /**
     * Returns whether the citizen has this as home or not.
     *
     * @param citizen Citizen to check.
     * @return True if citizen lives here, otherwise false.
     */
    boolean hasAssignedCitizen(ICitizenData citizen);

    /**
     * Checks if the building is full.
     *
     * @return true if so.
     */
    boolean isFull();

    /**
     * Returns the max amount of inhabitants.
     *
     * @return Max inhabitants.
     */
    int getMaxInhabitants();
}
