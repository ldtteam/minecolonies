package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.minecolonies.api.colony.ICitizenDataView;

/**
 *  Pupil module view.
 */
public class PupilBuildingModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canAssign(final ICitizenDataView data)
    {
        return data.isChild();
    }
}
