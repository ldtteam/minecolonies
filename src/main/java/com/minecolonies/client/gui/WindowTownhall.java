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
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.util.LanguageHandler;

import java.util.*;

public class WindowTownhall extends Window implements Button.Handler
{
    private static final    String                      BUTTON_INFO                 = "info";
    private static final    String                      BUTTON_ACTIONS              = "actions";
    private static final    String                      BUTTON_SETTINGS             = "settings";
    private static final    String                      BUTTON_PERMISSIONS          = "permissions";
    private static final    String                      BUTTON_CITIZENS             = "citizens";
    private static final    String                      BUTTON_BUILD                = "build";
    private static final    String                      BUTTON_REPAIR               = "repair";
    private static final    String                      BUTTON_RECALL               = "recall";
    private static final    String                      BUTTON_CHANGESPEC           = "changeSpec";
    private static final    String                      BUTTON_RENAME               = "rename";

    private static final    String                      BUTTON_ADDPLAYER            = "addPlayer";
    private static final    String                      INPUT_ADDPLAYER_NAME        = "addPlayerName";

    private static final    String                      BUTTON_REMOVEPLAYER         = "removePlayer";
    private static final    String                      BUTTON_PROMOTE              = "promote";
    private static final    String                      BUTTON_DEMOTE               = "demote";

    private static final    String                      VIEW_PAGES                  = "pages";
    private static final    String                      PAGE_INFO                   = "pageInfo";
    private static final    String                      PAGE_ACTIONS                = "pageActions";
    private static final    String                      PAGE_SETTINGS               = "pageSettings";
    private static final    String                      PAGE_PERMISSIONS            = "pagePermissions";
    private static final    String                      PAGE_CITIZENS               = "pageCitizens";

    private static final    String                      LIST_USERS                  = "users";
    private static final    String                      LIST_CITIZENS               = "citizenList";

    private static final    String                      COLONY_NAME                 = "colonyName";
    private static final    String                      CURRENT_SPEC                = "currentSpec";
    private static final    String                      TOTAL_CITIZENS              = "totalCitizens";
    private static final    String                      UNEMP_CITIZENS              = "unemployedCitizens";
    private static final    String                      BUILDERS                    = "builders";
    private static final    String                      DELIVERY_MAN                = "deliverymen";

    private static final    String                      TOWNHALL_RESOURCE_SUFFIX    = ":gui/windowTownhall.xml";

    private                 BuildingTownHall.View       townhall;
    private                 List<Permissions.Player>    users                       = new ArrayList<>();
    private                 List<CitizenData.View>      citizens                    = new ArrayList<>();

    private                 Map<String, String>         tabsToPages                 = new HashMap<>();
    private                 Button                      lastTabButton;
    private                 ScrollingList               citizenList;
    private                 ScrollingList               userList;

    public WindowTownhall(BuildingTownHall.View townhall)
    {
        super(Constants.MOD_ID + TOWNHALL_RESOURCE_SUFFIX);
        this.townhall = townhall;

        updateUsers();
        updateCitizens();

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFO, PAGE_INFO);
        tabsToPages.put(BUTTON_SETTINGS, PAGE_SETTINGS);
        tabsToPages.put(BUTTON_PERMISSIONS, PAGE_PERMISSIONS);
        tabsToPages.put(BUTTON_CITIZENS, PAGE_CITIZENS);
    }

    /**
     *  Clears and resets all users
     */
    private void updateUsers()
    {
        users.clear();
        users.addAll(townhall.getColony().getPlayers().values());
        Collections.sort(users, (o1, o2) -> o1.rank.compareTo(o2.rank));
    }

    /**
     * Clears and resets all citizens
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(townhall.getColony().getCitizens().values());
    }

    /**
     * Executed when <code>WindowTownhall</code> is opened.
     * Does tasks like setting buttons
     */
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
            findPaneOfTypeByID(COLONY_NAME, Label.class).setLabel(townhall.getColony().getName());
            findPaneOfTypeByID(CURRENT_SPEC, Label.class).setLabel("<Industrial>");
            findPaneOfTypeByID(TOTAL_CITIZENS, Label.class).setLabel(numberOfCitizens);
            findPaneOfTypeByID(UNEMP_CITIZENS, Label.class).setLabel(numberOfUnemployed);
            findPaneOfTypeByID(BUILDERS, Label.class).setLabel(numberOfBuilders);
            findPaneOfTypeByID(DELIVERY_MAN, Label.class).setLabel(numberOfDeliverymen);
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);
        } catch (NullPointerException e)
        {
            MineColonies.logger.error("findPane error, report to mod authors");
        }
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
                 catch (NullPointerException exc)
                 {
                     MineColonies.logger.error("findPane error, report to mod authors");
                 }
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
                catch (NullPointerException exc)
                {
                    MineColonies.logger.error("findPane error, report to mod authors");
                }
            }
        });
    }

    /**
     * Sets the clicked tab
     *
     * @param button    Tab button clicked on
     */
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
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.BUILD));
        }
        else if (button.getID().equals(BUTTON_REPAIR))
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.REPAIR));
        }
        else if (button.getID().equals(BUTTON_ADDPLAYER))
        {
            TextField input = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(townhall.getColony(), input.getText()));
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
                    MineColonies.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(townhall.getColony(), user.id));
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
                Permissions.Rank newRank;

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
                    MineColonies.getNetwork().sendToServer(new PermissionsMessage.SetPlayerRank(townhall.getColony(), user.id, newRank));
                }
            }
        }
        else if (button.getID().equals(BUTTON_RECALL))
        {
            /* TODO unused */
        }
        else if (button.getID().equals(BUTTON_CHANGESPEC))
        {
            /* TODO unused */
        }
        else if (button.getID().equals(BUTTON_RENAME))
        {
            WindowTownhallNameEntry window = new WindowTownhallNameEntry(townhall.getColony());
            window.open();
        }
    }
}
