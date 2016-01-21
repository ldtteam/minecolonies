package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.controls.TextField;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.util.LanguageHandler;

import java.util.*;

public class WindowTownhall extends Window implements Button.Handler
{
    private static final String BUTTON_INFO = "info",
            BUTTON_ACTIONS = "actions",
            BUTTON_SETTINGS = "settings",
            BUTTON_PERMISSIONS = "permissions",
            BUTTON_CITIZENS = "citizens",
            BUTTON_BUILD = "build",
            BUTTON_REPAIR = "repair",
            BUTTON_RECALL = "recall",
            BUTTON_CHANGESPEC = "changeSpec",
            BUTTON_RENAME = "rename",

            BUTTON_ADDPLAYER = "addPlayer",
            INPUT_ADDPLAYER_NAME = "addPlayerName",

            BUTTON_REMOVEPLAYER = "removePlayer",
            BUTTON_PROMOTE = "promote",
            BUTTON_DEMOTE = "demote",

            VIEW_PAGES = "pages",
            PAGE_INFO = "pageInfo",
            PAGE_ACTIONS = "pageActions",
            PAGE_SETTINGS = "pageSettings",
            PAGE_PERMISSIONS = "pagePermissions",
            PAGE_CITIZENS = "pageCitizens",

            LIST_USERS = "users",
            LIST_CITIZENS = "citizenList";

    private BuildingTownHall.View townhall;
    private List<Permissions.Player> users = new ArrayList<Permissions.Player>();
    private List<CitizenData.View>   citizens = new ArrayList<CitizenData.View>();

    private Map<String, String> tabsToPages = new HashMap<String, String>();
    private Button lastTabButton;
    private ScrollingList citizenList;
    private ScrollingList userList;

    public WindowTownhall(BuildingTownHall.View townhall)
    {
        super(Constants.MOD_ID + ":" + "gui/windowTownhall.xml");
        this.townhall = townhall;

        updateUsers();
        updateCitizens();

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFO, PAGE_INFO);
        tabsToPages.put(BUTTON_SETTINGS, PAGE_SETTINGS);
        tabsToPages.put(BUTTON_PERMISSIONS, PAGE_PERMISSIONS);
        tabsToPages.put(BUTTON_CITIZENS, PAGE_CITIZENS);
    }

    private void updateUsers()
    {
        users.clear();
        users.addAll(townhall.getColony().getPlayers().values());
        Collections.sort(users, new Comparator<Permissions.Player>(){
            @Override
            public int compare(Permissions.Player o1, Permissions.Player o2)
            {
                return o1.rank.compareTo(o2.rank);
            }
        });
    }

    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(townhall.getColony().getCitizens().values());
    }

    public void onOpened()
    {
        int citizensSize = townhall.getColony().getCitizens().size();

        //TODO - Base these on server-side computed statistics
        int workers = 0;
        int builders = 0, deliverymen = 0;

        String numberOfCitizens = LanguageHandler.format("com.minecolonies.gui.townhall.population.totalCitizens", citizensSize, townhall.getColony().getMaxCitizens());
        String numberOfUnemployed = LanguageHandler.format("com.minecolonies.gui.townhall.population.unemployed", (citizensSize - workers));
        String numberOfBuilders = LanguageHandler.format("com.minecolonies.gui.townhall.population.builders", builders);
        String numberOfDeliverymen = LanguageHandler.format("com.minecolonies.gui.townhall.population.deliverymen", deliverymen);

        try
        {
            findPaneOfTypeByID("colonyName", Label.class).setLabel(townhall.getColony().getName());
            findPaneOfTypeByID("currentSpec", Label.class).setLabel("<Industrial>");

            findPaneOfTypeByID("totalCitizens", Label.class).setLabel(numberOfCitizens);
            findPaneOfTypeByID("unemployedCitizens", Label.class).setLabel(numberOfUnemployed);
            findPaneOfTypeByID("builders", Label.class).setLabel(numberOfBuilders);
            findPaneOfTypeByID("deliverymen", Label.class).setLabel(numberOfDeliverymen);

            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);
            lastTabButton = findPaneOfTypeByID(BUTTON_ACTIONS, Button.class);
            lastTabButton.setEnabled(false);

            if (townhall.getBuildingLevel() == 0)
            {
                findPaneOfTypeByID(BUTTON_BUILD, Button.class).setLabel(
                        LanguageHandler.getString("com.minecolonies.gui.workerHuts.build"));
                findPaneByID(BUTTON_REPAIR).disable();
            }
            else if (townhall.isBuildingMaxLevel())
            {
                Button button = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
                button.setLabel(LanguageHandler.getString("com.minecolonies.gui.workerHuts.upgradeUnavailable"));
                button.disable();
            }

            userList = findPaneOfTypeByID(LIST_USERS, ScrollingList.class);
            userList.setDataProvider(new ScrollingList.DataProvider()
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
                            catch (NullPointerException exc){}
                        }
                    });


            citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
            citizenList.setDataProvider(new ScrollingList.DataProvider()
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
                            catch (NullPointerException exc){}
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
    public void onUpdate()
    {
        String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_PERMISSIONS))
        {
            updateUsers();
            window.findPaneOfTypeByID(LIST_USERS, ScrollingList.class).refreshElementPanes();
        }
        else if (currentPage.equals(PAGE_CITIZENS))
        {
            updateCitizens();
            window.findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class).refreshElementPanes();
        }
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
        else if (button.getID().equals(BUTTON_ADDPLAYER))
        {
            TextField input = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
            MineColonies.network.sendToServer(new PermissionsMessage.AddPlayer(townhall.getColony(), input.getText()));
            input.setText("");
        }
        else if (button.getID().equals(BUTTON_REMOVEPLAYER))
        {
            int row = userList.getListElementIndexByPane(button);
            if (row >= 0 && row < users.size())
            {
                Permissions.Player user = users.get(row);
                if (user.rank != Permissions.Rank.OWNER)
                {
                    MineColonies.network.sendToServer(new PermissionsMessage.RemovePlayer(townhall.getColony(), user.id));
                }
            }
        }
        else if (button.getID().equals(BUTTON_PROMOTE) ||
                button.getID().equals(BUTTON_DEMOTE))
        {
            int row = userList.getListElementIndexByPane(button);
            if (row >= 0 && row < users.size())
            {
                Permissions.Player user = users.get(row);
                Permissions.Rank newRank = user.rank;

                if (button.getID().equals(BUTTON_PROMOTE))
                {
                    newRank = Permissions.getPromotionRank(user.rank);
                }
                else
                {
                    newRank = Permissions.getDemotionRank(user.rank);
                }

                if (newRank != user.rank)
                {
                    MineColonies.network.sendToServer(new PermissionsMessage.SetPlayerRank(townhall.getColony(), user.id, newRank));
                }
            }
        }
        else if (button.getID().equals(BUTTON_RECALL))
        {
        }
        else if (button.getID().equals(BUTTON_CHANGESPEC))
        {
        }
        else if (button.getID().equals(BUTTON_RENAME))
        {
            GuiHandler.showGuiWindow(new WindowTownhallNameEntry(townhall.getColony()));
        }
    }
}
