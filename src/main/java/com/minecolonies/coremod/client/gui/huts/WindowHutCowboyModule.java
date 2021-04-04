package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.client.gui.AbstractWindowHerderModuleBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the cowboy hut.
 */
public class WindowHutCowboyModule extends AbstractWindowHerderModuleBuilding<BuildingCowboy.View>
{
    /**
     * Button for toggling milk collection.
     */
    private static final String BUTTON_MILK_COWS = "milkCows";

    /**
     * Button for toggling milk collection.
     */
    private Button buttonMilkCows;

    /**
     * Constructor for the window of the cowboy.
     *
     * @param building {@link BuildingCowboy.View}.
     */
    public WindowHutCowboyModule(final BuildingCowboy.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowhutcowboy.xml");

        registerButton(BUTTON_MILK_COWS, this::milkCowsClicked);

        buttonMilkCows = findPaneOfTypeByID(BUTTON_MILK_COWS, Button.class);

        if (building.isMilkCows())
        {
            buttonMilkCows.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_COLLECT));
        }
        else
        {
            buttonMilkCows.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_NOTCOLLECT));
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
        return "com.minecolonies.coremod.gui.workerhuts.cowboyHut";
    }

    private void milkCowsClicked()
    {
        if (buttonMilkCows.getTextAsString().equals(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_COLLECT)))
        {
            buttonMilkCows.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_NOTCOLLECT));
            building.setMilkCows(false);
        }
        else
        {
            buttonMilkCows.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_COWBOY_COLLECT));
            building.setMilkCows(true);
        }
    }
}

