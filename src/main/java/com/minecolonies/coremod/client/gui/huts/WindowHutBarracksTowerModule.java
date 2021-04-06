package com.minecolonies.coremod.client.gui.huts;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import org.jetbrains.annotations.NotNull;

/**
 * Our building hut view
 */
public class WindowHutBarracksTowerModule extends WindowHutGuardTowerModule
{
    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingGuards.View}.
     */
    public WindowHutBarracksTowerModule(AbstractBuildingGuards.View building)
    {
        super(building);
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.barrackstower";
    }
}
