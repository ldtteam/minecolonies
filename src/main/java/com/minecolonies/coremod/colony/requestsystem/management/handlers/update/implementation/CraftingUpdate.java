package com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.IUpdateStep;
import org.jetbrains.annotations.NotNull;

public class CraftingUpdate implements IUpdateStep
{
    @Override
    public int updatesToVersion()
    {
        return 1;
    }

    @Override
    public void update(@NotNull final IStandardRequestManager manager)
    {
        final Colony colony = (Colony) manager.getColony();

        colony.getBuildingManager().getBuildings().values().stream().forEach(manager::onProviderAddedToColony);
    }
}
