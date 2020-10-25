package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;

/**
 * Abstract class for all modules. Has base methods for all the necessary methods that have to be called from the building.
 */
public abstract class AbstractBuildingModule implements IBuildingModule
{
    /**
     * If the module is dirty.
     */
    public boolean isDirty = false;

    /**
     * The building this module belongs to.
     */
    protected final IBuilding building;

    /**
     * Instantiates a new citizen hut.
     * @param building the building it is registered too.
     */
    public AbstractBuildingModule(final IBuilding building)
    {
        this.building = building;
    }

    @Override
    public void markDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void clearDirty()
    {
        this.isDirty = false;
    }

    @Override
    public boolean checkDirty()
    {
        return this.isDirty;
    }
}
