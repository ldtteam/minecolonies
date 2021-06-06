package com.minecolonies.coremod.client.gui.citizen;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;

import static com.minecolonies.api.util.constant.WindowConstants.CITIZEN_JOB_RESOURCE_SUFFIX;

/**
 * Window for the citizen.
 */
public class JobWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public JobWindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen, Constants.MOD_ID + CITIZEN_JOB_RESOURCE_SUFFIX);
        CitizenWindowUtils.updateJobPage(citizen, this, colony);
    }
}
