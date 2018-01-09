package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.IUpdateStep;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.CraftingUpdate;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation.InitialUpdate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;

public class UpdateHandler
{
    private static final List<IUpdateStep> steps = new ArrayList<>();

    static {
        steps.add(new InitialUpdate());
        steps.add(new CraftingUpdate());
    }

    public static void handleUpdate(@NotNull final IStandardRequestManager manager)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return;
        }

        steps.stream()
          .filter(s -> s.updatesToVersion() > manager.getCurrentVersion())
          .sorted(Comparator.comparing(IUpdateStep::updatesToVersion))
          .forEachOrdered(s ->
          {
              s.update(manager);
              manager.setCurrentVersion(s.updatesToVersion());
          });
    }
}
