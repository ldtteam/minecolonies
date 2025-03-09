package com.minecolonies.core.colony.buildingextensions.modules;

import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.modules.IBuildingExtensionModule;

/**
 * Abstract class for all building extension modules.
 */
public abstract class AbstractBuildingExtensionModule implements IBuildingExtensionModule
{
    /**
     * The building extension this module belongs to.
     */
    protected IBuildingExtension extension;

    /**
     * Default constructor.
     *
     * @param extension the building extension instance this module is working on.
     */
    protected AbstractBuildingExtensionModule(final IBuildingExtension extension)
    {
        this.extension = extension;
    }

    @Override
    public IBuildingExtension getExtension()
    {
        return extension;
    }
}
