package com.minecolonies.coremod.client.gui.citizen;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;

import static com.minecolonies.api.util.constant.WindowConstants.CITIZEN_HAP_RESOURCE_SUFFIX;

/**
 * Window for the citizen.
 */
public class HappinessWindowCitizen extends AbstractWindowCitizen
{
    /**
     * The citizenData.View object.
     */
    private final ICitizenDataView citizen;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public HappinessWindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen, Constants.MOD_ID + CITIZEN_HAP_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    public ICitizenDataView getCitizen()
    {
        return citizen;
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        CitizenWindowUtils.updateHappiness(citizen, this);
    }
}
