package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.controls.TextField;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.network.messages.ToggleJobMessage;
import com.minecolonies.util.LanguageHandler;

import java.util.*;

/**
 * Window for the town hall
 */
public class WindowTownHall extends AbstractWindowBuilding<BuildingTownHall.View>
{
    /**
     * Id of the info button in the GUI.
     */
    private static final String BUTTON_INFO = "info";

    /**
     * Id of the action button in the GUI.
     */
    private static final String BUTTON_ACTIONS = "actions";

    /**
     * Id of the settings button in the GUI.
     */
    private static final String BUTTON_SETTINGS = "settings";

    /**
     * Id of the permissions button in the GUI.
     */
    private static final String BUTTON_PERMISSIONS = "permissions";

    /**
     * Id of the citizens button in the GUI.
     */
    private static final String BUTTON_CITIZENS = "citizens";

    /**
     * Id of the recall button in the GUI.
     */
    private static final String BUTTON_RECALL = "recall";

    /**
     * Id of the change specialization button in the GUI.
     */
    private static final String BUTTON_CHANGE_SPEC = "changeSpec";

    /**
     * Id of the rename button in the GUI.
     */
    private static final String BUTTON_RENAME = "rename";

    /**
     * Id of the add player button in the GUI.
     */
    private static final String BUTTON_ADD_PLAYER = "addPlayer";

    /**
     * Id of the toggle job button in the GUI.
     */
    private static final String BUTTON_TOGGLE_JOB = "toggleJob";

    /**
     * Id of the remove player button in the GUI..
     */
    private static final String BUTTON_REMOVE_PLAYER = "removePlayer";

    /**
     * Id of the promote player button in the GUI..
     */
    private static final String BUTTON_PROMOTE = "promote";

    /**
     * Id of the demote player button in the GUI..
     */
    private static final String BUTTON_DEMOTE = "demote";

    /**
     * Id of the input bar to add players. in the GUI.
     */
    private static final String INPUT_ADDPLAYER_NAME = "addPlayerName";

    /**
     * Id of the page view in the GUI.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Id of the info page in the GUI.
     */
    private static final String PAGE_INFO = "pageInfo";

    /**
     * Id of the actions page in the GUI.
     */
    private static final String PAGE_ACTIONS = "pageActions";

    /**
     * Id of the settings page in the GUI.
     */
    private static final String PAGE_SETTINGS = "pageSettings";

    /**
     * Id of the permissions page in the GUI.
     */
    private static final String PAGE_PERMISSIONS = "pagePermissions";

    /**
     * Id of the citizens page in the GUI.
     */
    private static final String PAGE_CITIZENS = "pageCitizens";

    /**
     * Id of the user list in the GUI.
     */
    private static final String LIST_USERS = "users";

    /**
     * Id of the citizens list in the GUI.
     */
    private static final String LIST_CITIZENS = "citizenList";

    /**
     * Id of the current specializations label in the GUI.
     */
    private static final String CURRENT_SPEC_LABEL = "currentSpec";

    /**
     * Id of the total citizens label in the GUI.
     */
    private static final String TOTAL_CITIZENS_LABEL = "totalCitizens";

    /**
     * Id of the unemployed citizens label in the GUI.
     */
    private static final String UNEMP_CITIZENS_LABEL = "unemployedCitizens";

    /**
     * Id of the total builders label in the GUI.
     */
    private static final String BUILDERS_LABEL = "builders";

    /**
     * Id of the total deliverymen label in the GUI.
     */
    private static final String DELIVERY_MAN_LABEL = "deliverymen";

    /**
     * Link to the xml file of the window.
     */
    private static final String TOWNHALL_RESOURCE_SUFFIX = ":gui/windowTownHall.xml";

    /**
     * The view of the current building.
     */
    private BuildingTownHall.View townHall;

    /**
     * List of added users.
     */
    private List<Permissions.Player> users       = new ArrayList<>();

    /**
     * List of citizens.
     */
    private List<CitizenData.View>   citizens    = new ArrayList<>();

    /**
     * Map of the pages.
     */
    private Map<String, String>      tabsToPages = new HashMap<>();

    /**
     * The button f the last tab -> will be filled later on.
     */
    private Button        lastTabButton;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList userList;

    /**
     * Constructor for the town hall window
     *
     * @param townHall {@link BuildingTownHall.View}
     */
    public WindowTownHall(BuildingTownHall.View townHall)
    {
        super(townHall, Constants.MOD_ID + TOWNHALL_RESOURCE_SUFFIX);
        this.townHall = townHall;

        updateUsers();
        updateCitizens();

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFO, PAGE_INFO);
        tabsToPages.put(BUTTON_SETTINGS, PAGE_SETTINGS);
        tabsToPages.put(BUTTON_PERMISSIONS, PAGE_PERMISSIONS);
        tabsToPages.put(BUTTON_CITIZENS, PAGE_CITIZENS);

        tabsToPages.keySet().forEach(key -> registerButton(key, this::onTabClicked));
        registerButton(BUTTON_ADD_PLAYER, this::addPlayerCLicked);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_REMOVE_PLAYER, this::removePlayerClicked);
        registerButton(BUTTON_PROMOTE, this::promoteDemoteClicked);
        registerButton(BUTTON_DEMOTE, this::promoteDemoteClicked);
        registerButton(BUTTON_RECALL,this::doNothing);
        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_TOGGLE_JOB, this::toggleHiring);

    }


    /**
     * Clears and resets all users
     */
    private void updateUsers()
    {
        users.clear();
        users.addAll(townHall.getColony().getPlayers().values());
        Collections.sort(users, (player1, player2) -> player1.getRank().compareTo(player2.getRank()));
    }

    /**
     * Clears and resets all citizens
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(townHall.getColony().getCitizens().values());
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened.
     * Does tasks like setting buttons
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        int citizensSize = townHall.getColony().getCitizens().size();

        //TODO - Base these on server-side computed statistics
        int workers     = 0;
        int builders    = 0;
        int deliverymen = 0;

        String numberOfCitizens    = LanguageHandler.format("com.minecolonies.gui.townHall.population.totalCitizens", citizensSize, townHall.getColony().getMaxCitizens());
        String numberOfUnemployed  = LanguageHandler.format("com.minecolonies.gui.townHall.population.unemployed", citizensSize - workers);
        String numberOfBuilders    = LanguageHandler.format("com.minecolonies.gui.townHall.population.builders", builders);
        String numberOfDeliverymen = LanguageHandler.format("com.minecolonies.gui.townHall.population.deliverymen", deliverymen);

        findPaneOfTypeByID(CURRENT_SPEC_LABEL, Label.class).setText("<Industrial>");
        findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Label.class).setText(numberOfCitizens);
        findPaneOfTypeByID(UNEMP_CITIZENS_LABEL, Label.class).setText(numberOfUnemployed);
        findPaneOfTypeByID(BUILDERS_LABEL, Label.class).setText(numberOfBuilders);
        findPaneOfTypeByID(DELIVERY_MAN_LABEL, Label.class).setText(numberOfDeliverymen);
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);

        lastTabButton = findPaneOfTypeByID(BUTTON_ACTIONS, Button.class);
        lastTabButton.setEnabled(false);

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
                Permissions.Player player = users.get(index);
                String rank = player.getRank().name();
                rank = Character.toUpperCase(rank.charAt(0)) + rank.toLowerCase().substring(1);
                rowPane.findPaneOfTypeByID("name", Label.class).setText(player.getName());
                rowPane.findPaneOfTypeByID("rank", Label.class).setText(rank);
            }
        });


        /*
      The ScrollingList of the cit
     */
        ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
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
                CitizenData.View citizen = citizens.get(index);

                rowPane.findPaneOfTypeByID("name", Label.class).setText(citizen.getName());
                //rowPane.findPaneOfTypeByID("job", Label.class).setlabel("" /* Not working yet */);
            }
        });

        if(townHall.getColony().isManualHiring())
        {
            findPaneOfTypeByID("toggleJob", Button.class).setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.on"));
        }
    }

    /**
     * Returns the name of a building
     *
     * @return Name of a building
     */
    @Override
    public String getBuildingName()
    {
        return townHall.getColony().getName();
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     * @param button the pressed button
     */
    private void toggleHiring(Button button)
    {
        boolean toggle;
        if(button.getLabel().equals(LanguageHandler.format("com.minecolonies.gui.hiring.off")))
        {
            button.setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.on"));
            toggle = true;
        }
        else
        {
            button.setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.off"));
            toggle = false;
        }
        MineColonies.getNetwork().sendToServer(new ToggleJobMessage(this.building.getColony(),toggle));
    }

    /**
     * Sets the clicked tab
     *
     * @param button Tab button clicked on
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


    /**
     * Action performed when rename button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void renameClicked(Button ignored)
    {
        WindowTownHallNameEntry window = new WindowTownHallNameEntry(townHall.getColony());
        window.open();
    }


    /**
     * Action performed when add player button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void addPlayerCLicked(Button ignored)
    {
        TextField input = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(townHall.getColony(), input.getText()));
        input.setText("");
    }


    /**
     * Action performed when remove player button is clicked
     *
     * @param button    Button that holds the user clicked on
     */
    private void removePlayerClicked(Button button)
    {
        int row = userList.getListElementIndexByPane(button);
        if (row >= 0 && row < users.size())
        {
            Permissions.Player user = users.get(row);
            if (user.getRank() != Permissions.Rank.OWNER)
            {
                MineColonies.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(townHall.getColony(), user.getID()));
            }
        }
    }


    /**
     * Action performed when promote or demote button is clicked
     *
     * @param button    Button that holds the  user clicked on
     */
    private void promoteDemoteClicked(Button button)
    {
        int row = userList.getListElementIndexByPane(button);
        if (row >= 0 && row < users.size())
        {
            Permissions.Player user = users.get(row);

            if (button.getID().equals(BUTTON_PROMOTE))
            {
                MineColonies.getNetwork().sendToServer(new PermissionsMessage.ChangePlayerRank(townHall.getColony(), user.getID(), PermissionsMessage.ChangePlayerRank.Type.PROMOTE));
            }
            else
            {
                MineColonies.getNetwork().sendToServer(new PermissionsMessage.ChangePlayerRank(townHall.getColony(), user.getID(), PermissionsMessage.ChangePlayerRank.Type.DEMOTE));
            }
        }
    }
}
