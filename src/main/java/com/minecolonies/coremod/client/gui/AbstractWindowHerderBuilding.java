package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingHerder;

public abstract class AbstractWindowHerderBuilding<B extends AbstractBuildingHerder.View> extends AbstractWindowWorkerBuilding<B>
{
    /**
     * Button for toggling animal breeding.
     */
    public static final String BUTTON_BREEDING = "breedAnimals";

    /**
     * Button for toggling animal breeding.
     */
    private Button buttonBreeding;

    /**
     * Constructor for the window of the herder building.
     *
     * @param building class extending {@link AbstractBuildingHerder.View}.
     * @param resource Resource of the window.
     */
    public AbstractWindowHerderBuilding(B building, String resource)
    {
        super(building, resource);

        registerButton(BUTTON_BREEDING, this::breedingClicked);

        buttonBreeding = findPaneOfTypeByID(BUTTON_BREEDING, Button.class);

        if (building.isBreeding())
        {
            buttonBreeding.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HERDER_BREEDING));
        }
        else
        {
            buttonBreeding.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HERDER_NOTBREEDING));
        }
    }

    private void breedingClicked()
    {
        if (buttonBreeding.getLabel().equals(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HERDER_BREEDING)))
        {
            buttonBreeding.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HERDER_NOTBREEDING));
            building.setBreeding(false);
        }
        else
        {
            buttonBreeding.setLabel(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HERDER_BREEDING));
            building.setBreeding(true);
        }
    }
}
