package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFisherman;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the fisherman hut.
 */
public class WindowHutCowboy extends AbstractWindowWorkerBuilding<BuildingCowboy.View>
{
    /**
     * Button leading the player to the previous page.
     */
    private static final String BUTTON_MILK_COWS = "milkCows";

    /**
     * Button for toggling milk collection.
     */
    private Button buttonMilkCows;

    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link BuildingFisherman.View}.
     */
    public WindowHutCowboy(final BuildingCowboy.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutCowboy.xml");

        registerButton(BUTTON_MILK_COWS, this::milkCowsClicked);

        buttonMilkCows = findPaneOfTypeByID(BUTTON_MILK_COWS, Button.class);

        if (building.isMilkCows())
        {
            buttonMilkCows.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_COLLECT));
        }
        else
        {
            buttonMilkCows.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_NOTCOLLECT));
        }
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
        return "com.minecolonies.coremod.gui.workerHuts.cowboyHut";
    }

    private void milkCowsClicked()
    {
        if (buttonMilkCows.getLabel().equals(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_COLLECT)))
        {
            buttonMilkCows.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_NOTCOLLECT));
            building.setMilkCows(false);
        }
        else
        {
            buttonMilkCows.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_COLLECT));
            building.setMilkCows(true);
        }
    }
}

