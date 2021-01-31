package com.minecolonies.coremod.client.gui;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import org.jetbrains.annotations.NotNull;

public class WindowHutBarracksTower extends WindowHutGuardTower {
    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingGuards.View}.
     */
    public WindowHutBarracksTower(AbstractBuildingGuards.View building) {
        super(building);
    }

    @NotNull
    @Override
    public String getBuildingName() {
        return "com.minecolonies.coremod.gui.workerhuts.BarracksTower";
    }
}
