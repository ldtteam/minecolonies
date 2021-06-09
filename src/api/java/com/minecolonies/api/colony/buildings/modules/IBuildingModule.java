package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.IHasDirty;

/**
 * Default interface for all building modules.
 */
public interface IBuildingModule extends IHasDirty
{
    /**
     * Set the building of the module.
     * @param building the building to set.
     * @return the module itself.
     */
    IBuildingModule setBuilding(final IBuilding building);
}
