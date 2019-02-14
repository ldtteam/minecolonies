package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.WindowConstants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;
import org.jetbrains.annotations.NotNull;

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
        return "com.minecolonies.coremod.gui.workerHuts.shepherdHut";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setDyeSheepsLabel();
    }

    private void dyeSheepsClicked()
    {
        building.setDyeSheeps(!building.isDyeSheeps());
        setDyeSheepsLabel();
    }

    private void setDyeSheepsLabel()
    {
        if (building.isDyeSheeps())
        {
            findPaneOfTypeByID(BUTTON_DYE_SHEEPS, Button.class).setLabel(WindowConstants.GENERAL_ON_BIG);
        }
        else
        {
            findPaneOfTypeByID(BUTTON_DYE_SHEEPS, Button.class).setLabel(WindowConstants.GENERAL_OFF_BIG);
        }
    }
}

