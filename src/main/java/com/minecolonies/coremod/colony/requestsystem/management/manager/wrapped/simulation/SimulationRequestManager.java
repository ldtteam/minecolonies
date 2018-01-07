package com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.simulation;

import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.AbstractWrappedRequestManager;
import org.jetbrains.annotations.NotNull;

public class SimulationRequestManager extends AbstractWrappedRequestManager
{
    public static SimulationRequestManager create(@NotNull final StandardRequestManager from)
    {
        final StandardRequestManager readOnlyCopy = new StandardRequestManager(from.getColony());
        readOnlyCopy.deserializeNBT(from.serializeNBT());

        return new SimulationRequestManager(readOnlyCopy);
    }


    private SimulationRequestManager(
      @NotNull final IStandardRequestManager wrappedManager)
    {
        super(wrappedManager);
    }

    @Override
    public boolean isSimulating()
    {
        return true;
    }
}
