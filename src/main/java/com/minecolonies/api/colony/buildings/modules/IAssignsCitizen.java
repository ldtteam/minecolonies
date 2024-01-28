package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Interface for all modules that need special assignment handling.
 */
public interface IAssignsCitizen extends IBuildingModule
{
    /**
     * Specific citizen removal hook for modules.
     * @param citizen the removed citizen.
     * @return true if one was removed.
     */
    boolean removeCitizen(@NotNull ICitizenData citizen);

    /**
     * Specific citizen assignment hook for modules.
     * @param citizen the added citizen.
     * @return true if one was added.
     */
    boolean assignCitizen(ICitizenData citizen);

    /**
     * Get all assigned citizens to this module.
     * @return the list.
     */
    List<ICitizenData> getAssignedCitizen();

    /**
     * Check if we can fit additional citizens into the module.
     * @return true if so.
     */
    boolean isFull();

    /**
     * Get the max number of citizens this module supports.
     * @return the modules max.
     */
    int getModuleMax();

    /**
     * Check if a given citizen is assigned to this module.
     * @param citizen the citizen to check.
     * @return true if so.
     */
    boolean hasAssignedCitizen(ICitizenData citizen);

    /**
     * Get a list of entities from the assigned citizens.
     * @return optional list of entities.
     */
    List<Optional<AbstractEntityCitizen>> getAssignedEntities();

    /**
     * Check if there are any assigned citizens in the module.
     * @return true if so.
     */
    boolean hasAssignedCitizen();

    /**
     * Set the hiring mode (automatic or manual or colony default).
     * @param hiringMode the mode to set.
     */
    void setHiringMode(final HiringMode hiringMode);

    /**
     * Get the hiring mode of the module.
     * @return the mode.
     */
    HiringMode getHiringMode();
}
