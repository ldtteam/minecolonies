package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

/**
 * Window for the citizen
 */
public class WindowCitizen extends Window implements Button.Handler
{
    private static final String              INVENTORY_BUTTON_ID     = "inventory";
    private static final String              CITIZIN_RESOURCE_SUFFIX = ":gui/windowCitizen.xml";
    private static final String              STRENGTH                = "strength";
    private static final String              STAMINA                 = "stamina";
    private static final String              WISDOM                  = "wisdom";
    private static final String              INTELLIGENCE            = "intelligence";
    private static final String              CHARISMA                = "charisma";

    private              CitizenData.View    citizen;

    /**
     * Window for the citizen
     *
     * @param citizen       View object of the citizen data
     */
    public WindowCitizen(CitizenData.View citizen)
    {
        super(Constants.MOD_ID + CITIZIN_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    @Override
    public void onOpened()
    {

        findPaneOfTypeByID(STRENGTH, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
        findPaneOfTypeByID(STAMINA, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
        findPaneOfTypeByID(WISDOM, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom));
        findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
        findPaneOfTypeByID(CHARISMA, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));

    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(INVENTORY_BUTTON_ID))
        {
            MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
