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
    protected IBuilding building;

    @Override
    public void markDirty()
    {
        this.isDirty = true;
        building.markDirty();
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

    @Override
    public IBuildingModule setBuilding(final IBuilding building)
    {
        this.building = building;
        return this;
    }
}
