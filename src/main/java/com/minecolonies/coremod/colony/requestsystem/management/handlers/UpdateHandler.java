package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.IUpdateStep;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.CraftingUpdate;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.InitialUpdate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UpdateHandler
{
    @VisibleForTesting
    public static final List<IUpdateStep> UPDATE_STEPS = new ArrayList<>();

    static {
        UPDATE_STEPS.add(new InitialUpdate());
        UPDATE_STEPS.add(new CraftingUpdate());
    }

    public static void handleUpdate(@NotNull final IStandardRequestManager manager)
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
              s.update(manager);
              manager.setCurrentVersion(s.updatesToVersion());
          });
    }

    public static int getCurrentVersion()
    {
        return UPDATE_STEPS.stream().max(Comparator.comparing(IUpdateStep::updatesToVersion)).get().updatesToVersion();
    }
}
