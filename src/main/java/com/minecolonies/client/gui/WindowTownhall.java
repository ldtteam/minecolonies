package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.util.LanguageHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowTownhall extends Window implements Button.Handler
{
    private static String BUTTON_INFO = "info",
            BUTTON_ACTIONS = "actions",
            BUTTON_SETTINGS = "settings",
            BUTTON_PERMISSIONS = "permissions",
            BUTTON_CITIZENS = "citizens",
            BUTTON_BUILD = "build",
            BUTTON_REPAIR = "repair",
            BUTTON_RECALL = "recall",
            BUTTON_CHANGESPEC = "changeSpec",
            BUTTON_RENAME = "rename",

            VIEW_PAGES = "pages",
            PAGE_INFO = "pageInfo",
            PAGE_ACTIONS = "pageActions",
            PAGE_SETTINGS = "pageSettings",
            PAGE_PERMISSIONS = "pagePermissions",
            PAGE_CITIZENS = "pageCitizens",

            LIST_USERS = "users",
            LIST_CITIZENS = "citizenList";

    private ColonyView            colony;
    private BuildingTownHall.View townhall;
    private List<Permissions.Player> users = new ArrayList<Permissions.Player>();
    private List<CitizenData.View>   citizens = new ArrayList<CitizenData.View>();

    private Map<String, String> tabsToPages = new HashMap<String, String>();
    private Button lastTabButton;

    public WindowTownhall(BuildingTownHall.View townhall)
    {
        super(Constants.MOD_ID + ":" + "gui/windowTownhall.xml");
        this.townhall = townhall;
        this.colony = townhall.getColony();
        this.users.addAll(colony.getPlayers().values());
        this.citizens.addAll(colony.getCitizens().values());

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFO, PAGE_INFO);
        tabsToPages.put(BUTTON_SETTINGS, PAGE_SETTINGS);
        tabsToPages.put(BUTTON_PERMISSIONS, PAGE_PERMISSIONS);
        tabsToPages.put(BUTTON_CITIZENS, PAGE_CITIZENS);
    }

    public void onOpened()
    {
        int citizensSize = colony.getCitizens().size();

        //TODO - Base these on server-side computed statistics
        int workers = 0;
        int builders = 0, deliverymen = 0;

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

            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);
            lastTabButton = findPaneOfTypeByID(BUTTON_ACTIONS, Button.class);
            lastTabButton.setEnabled(false);

            findPaneOfTypeByID(LIST_USERS, ScrollingList.class).setDataProvider(
                    new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return users.size();
                }

                @Override
                public void updateElement(int index, Pane rowPane)
                {
                    try
                    {
                        Permissions.Player player = users.get(index);

                        String rank = player.rank.name();
                        rank = Character.toUpperCase(rank.charAt(0)) + rank.toLowerCase().substring(1);

                        rowPane.findPaneOfTypeByID("name", Label.class).setLabel(player.name);
                        rowPane.findPaneOfTypeByID("rank", Label.class).setLabel(rank);
                    }
                    catch (NullPointerException exc) {}
                }
            });


            findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class).setDataProvider(
                    new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return citizens.size();
                }

                @Override
                public void updateElement(int index, Pane rowPane)
                {
                    try
                    {
                        CitizenData.View citizen = citizens.get(index);

                        rowPane.findPaneOfTypeByID("name", Label.class).setLabel(citizen.getName());
                        //rowPane.findPaneOfTypeByID("job", Label.class).setLabel("" /* Not working yet */);
                    }
                    catch (NullPointerException exc) {}
                }
            });
        }
        catch (NullPointerException exc)
        {
        }
    }

    private void onTabClicked(Button button)
    {
        String page = tabsToPages.get(button.getID());
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(page);

        lastTabButton.setEnabled(true);
        button.setEnabled(false);
        lastTabButton = button;
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (tabsToPages.containsKey(button.getID()))
        {
            onTabClicked(button);
        }
        else if (button.getID().equals(BUTTON_BUILD))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.BUILD));
        }
        else if (button.getID().equals(BUTTON_REPAIR))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.REPAIR));
        }
        else if (button.getID().equals(BUTTON_RECALL))
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
