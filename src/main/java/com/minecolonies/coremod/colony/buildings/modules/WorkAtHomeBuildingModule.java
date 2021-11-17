package com.minecolonies.coremod.colony.buildings.modules;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.network.chat.TranslatableComponent;

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
            final IBuilding building = citizen.getHomeBuilding();
            if (building != null && !building.getID().equals(building.getID()))
            {
                if (building.hasModule(LivingBuildingModule.class))
                {
                    LanguageHandler.sendPlayersMessage(building.getColony().getMessagePlayerEntities(),
                      "com.minecolonies.coremod.gui.workerhuts.assignedbed",
                      citizen.getName(),
                      new TranslatableComponent(citizen.getJob().getJobRegistryEntry().getTranslationKey()),
                      LanguageHandler.format("block.minecolonies." + building.getBuildingType().getBuildingBlock().getHutName() + ".name"),
                      BlockPosUtil.getString(building.getID()));
                }
                building.getFirstModuleOccurance(LivingBuildingModule.class).removeCitizen(citizen);
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
