package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GENERAL_OFFBIG;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GENERAL_ONBIG;

/**
 * Window for the shepherd hut.
 */
public class WindowHutShepherd extends AbstractWindowWorkerBuilding<BuildingShepherd.View>
{
    private static final String BUTTON_DYE_SHEEPS = "dyeSheeps";

    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link BuildingShepherd.View}.
     */
    public WindowHutShepherd(final BuildingShepherd.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowhutshepherd.xml");

        registerButton(BUTTON_DYE_SHEEPS, this::dyeSheepsClicked);
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.shepherdHut";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setDyeSheepsLabel();
    }

    /**
     * Called when a player press BUTTON_DYE_SHEEPS
     */
    private void dyeSheepsClicked()
    {
        building.setDyeSheeps(!building.isDyeSheeps());
        setDyeSheepsLabel();
    }

    /**
     * Changes BUTTON_DYE_SHEEPS label to correct state
     */
    private void setDyeSheepsLabel()
    {
        if (building.isDyeSheeps())
        {
            findPaneOfTypeByID(BUTTON_DYE_SHEEPS, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GENERAL_ONBIG));
        }
        else
        {
            findPaneOfTypeByID(BUTTON_DYE_SHEEPS, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GENERAL_OFFBIG));
        }
    }
}

