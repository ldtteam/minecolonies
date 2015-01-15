package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

public class WindowHomeBuilding extends Window implements Button.Handler
{
    private final static String BUTTON_INVENTORY = "inventory",
            BUTTON_BUILD = "build",
            BUTTON_REPAIR = "repair",
            LABEL_BUILDINGNAME = "name";

    private BuildingHome.View building;

    public WindowHomeBuilding(BuildingHome.View building)
    {
        super(Constants.MOD_ID + ":gui/windowHutHome.xml");
        this.building = building;
    }

    @Override
    public void onOpened()
    {
        try
        {
            findPaneOfTypeByID(LABEL_BUILDINGNAME, Label.class).setLabel(
                    LanguageHandler.getString("com.minecolonies.gui.workerHuts.homeHut"));

            if (building.getBuildingLevel() == 0)
            {
                findPaneOfTypeByID(BUTTON_BUILD, Button.class).setLabel(
                        LanguageHandler.getString("com.minecolonies.gui.workerHuts.build"));
                findPaneByID(BUTTON_REPAIR).disable();
            }
            else if (building.isBuildingMaxLevel())
            {
                Button button = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
                button.setLabel(LanguageHandler.getString("com.minecolonies.gui.workerHuts.upgradeUnavailable"));
                button.disable();
            }
        }
        catch (NullPointerException exc) {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_INVENTORY))
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(building));
        }
        else if (button.getID().equals(BUTTON_BUILD))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
        }
        else if (button.getID().equals(BUTTON_REPAIR))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
        }
    }
}
