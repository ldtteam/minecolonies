package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import org.apache.commons.codec.language.bm.Lang;
import scala.tools.nsc.backend.icode.TypeKinds;

public class WindowCitizen extends Window implements Button.Handler
{
    private static String INVENTORY_BUTTON_ID = "inventory";
    private static String CITIZIN_RESOURCE_SUFFIX = ":gui/windowCitizen.xml";
    private static String STRENGTH = "strength";
    private static String STAMINA = "stamina";
    private static String WISDOM = "wisdom";
    private static String INTELLIGENCE= "intelligence";
    private static String CHARISMA = "charisma";

    private CitizenData.View citizen;

    public WindowCitizen(CitizenData.View citizen)
    {
        super(Constants.MOD_ID + CITIZIN_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    public void onOpened()
    {
        try
        {
            findPaneOfTypeByID(STRENGTH, Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
            findPaneOfTypeByID(STAMINA, Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
            findPaneOfTypeByID(WISDOM, Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom));
            findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
            findPaneOfTypeByID(CHARISMA, Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));
        }
        catch (NullPointerException exc) {
            MineColonies.logger.error("findPane error, report to mod authors");
        }
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
