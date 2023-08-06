package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import com.minecolonies.api.util.MessageUtils;

import java.util.function.Function;

/**
 * Assignment module for jobs that have to live at the work place mandatorily.
 */
public class WorkAtHomeBuildingModule extends WorkerBuildingModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    public WorkAtHomeBuildingModule(final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, canWorkingDuringRain, sizeLimit);
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (super.assignCitizen(citizen) && citizen != null)
        {
            // Set new home, since guards are housed at their workerbuilding.
            final IBuilding oldHome = citizen.getHomeBuilding();
            if (oldHome != null && !oldHome.getID().equals(building.getID()))
            {
                if (oldHome.hasModule(LivingBuildingModule.class) && !oldHome.hasModule(WorkAtHomeBuildingModule.class))
                {
                    final MutableComponent jobComponent = MessageUtils.format(citizen.getJob().getJobRegistryEntry().getTranslationKey()).create();
                    final MutableComponent buildingComponent = MessageUtils.format(oldHome.getBuildingDisplayName()).create();
                    MessageUtils.format("com.minecolonies.coremod.gui.workerhuts.assignedbed",
                        citizen.getName(),
                        jobComponent,
                        buildingComponent,
                        BlockPosUtil.getString(oldHome.getID()))
                      .sendTo(oldHome.getColony()).forAllPlayers();
                }

                if (oldHome.hasModule(LivingBuildingModule.class))
                {
                    oldHome.getFirstModuleOccurance(LivingBuildingModule.class).removeCitizen(citizen);
                }
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
