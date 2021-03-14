package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IModuleWindow;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;

/**
 * Window for all the filterable lists.
 */
public abstract class AbstractModuleWindow<T extends IBuildingModuleView> extends AbstractWindowSkeleton implements IModuleWindow
{
    /**
     * Constructor for the window of the the filterable lists.
     *
     * @param building   {@link AbstractBuildingView}.
     * @param res        the resource String.
     */
    public AbstractModuleWindow(final IBuildingView building, final String res)
    {
        super(res);
    }
}
