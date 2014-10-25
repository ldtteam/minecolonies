package com.minecolonies.client.gui;

import com.blockout.*;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.util.ResourceLocation;

public class GuiEntityCitizenWindow extends GuiMineColoniesWindow implements Button.Handler
{
    private static String INVENTORY_BUTTON_ID = "inventory";

    private CitizenData.View citizen;

    public GuiEntityCitizenWindow(CitizenData.View citizen)
    {
        super();
        this.citizen = citizen;

        ResourceLocation xmlfile = new ResourceLocation(Constants.MODID + ":" + "gui/windowEntityCitizen.xml");
        Loader.createFromXMLFile(xmlfile, this);

        try {
            findPaneOfTypeByID("strength", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
            findPaneOfTypeByID("stamina", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
            findPaneOfTypeByID("wisdom", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom));
            findPaneOfTypeByID("intelligence", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
            findPaneOfTypeByID("charisma", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));
        }
        catch (NullPointerException exc) {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(INVENTORY_BUTTON_ID))
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
