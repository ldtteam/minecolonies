package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.util.Log;

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

    /**
     * Creator and Identifier of this module
     */
    private BuildingEntry.ModuleProducer producer = null;

    @Override
    public void markDirty()
    {
        this.isDirty = true;
        if (building != null)
        {
            building.markDirty();
        }
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
    public IBuilding getBuilding()
    {
        return this.building;
    }

    @Override
    public IBuildingModule setBuilding(final IBuilding building)
    {
        this.building = building;
        return this;
    }

    @Override
    public IBuildingModule setProducer(final BuildingEntry.ModuleProducer moduleSet)
    {
        if (producer != null)
        {
            Log.getLogger().error("Changing a producer is not allowed, trace:", new Exception());
            return this;
        }
        producer = moduleSet;
        return this;
    }

    @Override
    public BuildingEntry.ModuleProducer getProducer()
    {
        return producer;
    }
}
