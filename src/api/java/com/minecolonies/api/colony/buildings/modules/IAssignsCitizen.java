package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    List<ICitizenData> getAssignedCitizen();

    boolean isFull();

    /**
     * Get the max number of citizens this module supports.
     * @return the modules max.
     */
    int getModuleMax();

    boolean hasAssignedCitizen(ICitizenData citizen);

    @Nullable
    List<Optional<AbstractEntityCitizen>> getAssignedEntities();

    boolean hasAssignedCitizen();
}
