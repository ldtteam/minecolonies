package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.IUpdateStep;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.UpdateType;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.CraftingUpdate;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.InitialUpdate;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.ResetRSToAddDelivermanPickups;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.ResetRSToFixBuildingId;

import java.util.Comparator;
import java.util.List;

public class UpdateHandler implements IUpdateHandler
{
    @VisibleForTesting
    private static final List<IUpdateStep> UPDATE_STEPS = Lists.newArrayList(
      new InitialUpdate(),
      new CraftingUpdate(),
      new ResetRSToFixBuildingId(),
      new ResetRSToAddDelivermanPickups()
    );

    private final IStandardRequestManager manager;

    public UpdateHandler(final IStandardRequestManager manager) {this.manager = manager;}

    @Override
    public IRequestManager getManager()
    {
        return manager;
    }

    @Override
    public void handleUpdate(final UpdateType type)
    {
        if (manager.getColony().isRemote())
        {
            return;
        }

        UPDATE_STEPS.stream()
          .filter(s -> s.updatesToVersion() > manager.getCurrentVersion())
          .sorted(Comparator.comparing(IUpdateStep::updatesToVersion))
          .forEachOrdered(s ->
          {
              manager.setCurrentVersion(s.updatesToVersion());
              s.update(manager);
          });
    }

    @Override
    public int getCurrentVersion()
    {
        return UPDATE_STEPS.stream().max(Comparator.comparing(IUpdateStep::updatesToVersion)).get().updatesToVersion();
    }
}
