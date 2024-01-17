package com.minecolonies.core.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.requestsystem.resolvers.BuildingRequestResolver;

import java.util.List;
import java.util.function.Function;

/**
 * The worker module for citizen where they are assigned to if they work at it.
 */
public class NoPrivateCrafterWorkerModule extends WorkerBuildingModule implements IAssignsJob, IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    public NoPrivateCrafterWorkerModule(
      final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, canWorkingDuringRain, sizeLimit);
    }

    @Override
    public List<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(building.getRequester().getLocation(), building.getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
    }
}
