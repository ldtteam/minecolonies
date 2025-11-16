package com.minecolonies.core.client.gui.citizen;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * BOWindow for the citizen.
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
        super(citizen, new ResourceLocation(Constants.MOD_ID, "gui/citizen/job.xml"));
        CitizenWindowUtils.updateJobPage(citizen, this, colony);
    }
}
