package com.minecolonies.core.client.gui.citizen;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * BOWindow for the citizen.
 */
public class HappinessWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public HappinessWindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen, new ResourceLocation(Constants.MOD_ID, "gui/citizen/happiness.xml"));
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
