package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import org.jetbrains.annotations.NotNull;

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
     * Get the max number of citizens this module supports.
     * @return the modules max.
     */
    int getModuleMax();
}
