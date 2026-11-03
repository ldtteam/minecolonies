package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.guardtype.GuardType;

import java.util.function.Function;

/**
 * Stable-specific assignment module for cavalry guards.
 * <p>
 * Barracks guard modules intentionally share a combined hiring limit across all guard jobs at the building.
 * Stables need different accounting: the Stablemaster has its own worker module capped at one citizen, while
 * cavalry should still be allowed up to the building level. This module keeps the cavalry cap local to the
 * cavalry assignees so a stable can employ one Stablemaster plus {@code buildingLevel} cavalry units.
 */
public class StableCavalryBuildingModule extends GuardBuildingModule
{
    /**
     * Create a cavalry assignment module for the Stable.
     *
     * @param type the cavalry guard type.
     * @param canWorkingDuringRain if the cavalry can work during rain.
     * @param sizeLimit function defining the cavalry limit for the building.
     */
    public StableCavalryBuildingModule(
      final GuardType type,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(type, canWorkingDuringRain, sizeLimit);
    }

    @Override
    public boolean isFull()
    {
        return getAssignedCitizen().size() >= getModuleMax();
    }
}
