package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.ICreatesResolversModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;

import java.util.function.Function;

/**
 * Assignment module for miners.
 */
public class MinerBuildingModule extends WorkerBuildingModule implements ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    public MinerBuildingModule(
      final JobEntry entry,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, canWorkingDuringRain, sizeLimit);
    }

    @Override
    public boolean isFull()
    {
        return building.getAllAssignedCitizen().size() >= getModuleMax();
    }
}
