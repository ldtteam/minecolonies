package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.SwitchView;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.util.LanguageHandler;

public class WindowTownhall extends Window implements Button.Handler
{
    private static String BUTTON_INFO = "info",
            BUTTON_ACTIONS = "actions",
            BUTTON_SETTINGS = "settings",
            BUTTON_BUILD = "build",
            BUTTON_REPAIR = "repair",
            BUTTON_RECEALL = "recall",
            BUTTON_CHANGESPEC = "changeSpec",
            BUTTON_RENAME = "rename",

    VIEW_PAGES = "pages",
            PAGE_INFO = "pageInfo",
            PAGE_ACTIONS = "pageActions",
            PAGE_SETTINGS = "pageSettings";

    private ColonyView colony;
    private BuildingTownHall.View townhall;

    private Button buttonSettings, buttonActions, buttonInfo;

    public WindowTownhall(BuildingTownHall.View townhall)
    {
        super(Constants.MOD_ID + ":" + "gui/windowTownhall.xml");
        this.townhall = townhall;
        this.colony = townhall.getColony();
    }

    public void onOpened()
    {
        int citizensSize = colony.getCitizens().size();
        int workers = 0;
        int builders = 0, deliverymen = 0;

        //TODO - Rewrite this based on the CitizenData
        /*Map<UUID, CitizenData.View> citizens = colony.getCitizens();
        if (citizens != null)
        {
            //TODO access job without
            for (CitizenData.View citizen : citizens.values())
            {
                //if(citizen.)
                //{
                //    builders++;
                //}
                //else if(citizen instanceof EntityDeliveryman)
                //{
                //    deliverymen++;
                //}
            }
            //workers = builders + deliverymen;
        }*/

        String numberOfCitizens = LanguageHandler.format("com.minecolonies.gui.townhall.population.totalCitizens", citizensSize, colony.getMaxCitizens());
        String numberOfUnemployed = LanguageHandler.format("com.minecolonies.gui.townhall.population.unemployed", (citizensSize - workers));
        String numberOfBuilders = LanguageHandler.format("com.minecolonies.gui.townhall.population.builders", builders);
        String numberOfDeliverymen = LanguageHandler.format("com.minecolonies.gui.townhall.population.deliverymen", deliverymen);

        try
        {
            findPaneOfTypeByID("colonyName", Label.class).setLabel(colony.getName());
            findPaneOfTypeByID("currentSpec", Label.class).setLabel("<Industrial>");

            findPaneOfTypeByID("totalCitizens", Label.class).setLabel(numberOfCitizens);
            findPaneOfTypeByID("unemployedCitizens", Label.class).setLabel(numberOfUnemployed);
            findPaneOfTypeByID("builders", Label.class).setLabel(numberOfBuilders);
            findPaneOfTypeByID("deliverymen", Label.class).setLabel(numberOfDeliverymen);

            buttonSettings = findPaneOfTypeByID(BUTTON_SETTINGS, Button.class);
            buttonActions = findPaneOfTypeByID(BUTTON_ACTIONS, Button.class);
            buttonInfo = findPaneOfTypeByID(BUTTON_INFO, Button.class);

            buttonActions.setEnabled(false);
        }
        catch (NullPointerException exc)
        {
        }
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_INFO))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_INFO);
            buttonSettings.setEnabled(true);
            buttonActions.setEnabled(true);
            button.setEnabled(false);
        }
        else if (button.getID().equals(BUTTON_ACTIONS))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);
            buttonSettings.setEnabled(true);
            buttonInfo.setEnabled(true);
            button.setEnabled(false);
        }
        else if (button.getID().equals(BUTTON_SETTINGS))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_SETTINGS);
            buttonActions.setEnabled(true);
            buttonInfo.setEnabled(true);
            button.setEnabled(false);
        }
        else if (button.getID().equals(BUTTON_BUILD))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.BUILD));
        }
        else if (button.getID().equals(BUTTON_REPAIR))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.REPAIR));
        }
        else if (button.getID().equals(BUTTON_RECEALL))
        {
        }
        else if (button.getID().equals(BUTTON_CHANGESPEC))
        {
        }
        else if (button.getID().equals(BUTTON_RENAME))
        {
            townhall.openGui(EnumGUI.TOWNHALL_RENAME);
        }
    }
}
