package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.core.colony.jobs.views.DmanJobView;

import java.util.ArrayList;
import java.util.List;

/**
 * Courier task module to display tasks in the UI.
 */
public class CourierRequestTaskModuleView extends RequestTaskModuleView
{
    @Override
    public List<IToken<?>> getTasks()
    {
        final List<IToken<?>> tasks = new ArrayList<>();
        for (final WorkerBuildingModuleView moduleView : buildingView.getModuleViews(WorkerBuildingModuleView.class))
        {
            for (final int citizenId : moduleView.getAssignedCitizens())
            {
                ICitizenDataView citizen = buildingView.getColony().getCitizen(citizenId);
                if (citizen != null && citizen.getJobView() instanceof DmanJobView)
                {
                    tasks.addAll(((DmanJobView) citizen.getJobView()).getDataStore().getQueue());
                }
            }
        }

        return tasks;
    }
}
