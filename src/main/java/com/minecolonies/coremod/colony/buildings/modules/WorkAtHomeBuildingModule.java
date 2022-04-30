package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Assignment module for jobs that have to live at the work place mandatorily.
 */
public class WorkAtHomeBuildingModule extends WorkerBuildingModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    public WorkAtHomeBuildingModule(final JobEntry entry,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, canWorkingDuringRain, sizeLimit);
    }

    @Override
    public boolean assignCitizen(@Nullable final ICitizenData citizen)
    {
        if (citizen != null && super.assignCitizen(citizen))
        {
            // Set new home, since guards are housed at their workerbuilding.
            final IBuilding oldHome = citizen.getHomeBuilding();
            if (oldHome != null && !oldHome.getID().equals(building.getID()))
            {
                if (oldHome.hasModule(LivingBuildingModule.class) && !oldHome.hasModule(WorkAtHomeBuildingModule.class))
                {
                    oldHome.getColony().notifyPlayers(
                      new TranslatableComponent("com.minecolonies.coremod.gui.workerhuts.assignedbed",
                      citizen.getName(),
                      new TranslatableComponent(citizen.getJob().getJobRegistryEntry().getTranslationKey()),
                      new TranslatableComponent("block.minecolonies." + oldHome.getBuildingType().getBuildingBlock().getHutName() + ".name"),
                      BlockPosUtil.getString(oldHome.getID())));
                }
                oldHome.getFirstModuleOccurance(LivingBuildingModule.class).removeCitizen(citizen);
            }
            citizen.setHomeBuilding(building);
            return true;
        }

        return false;
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        super.onRemoval(citizen);
        citizen.setHomeBuilding(null);
    }
}
