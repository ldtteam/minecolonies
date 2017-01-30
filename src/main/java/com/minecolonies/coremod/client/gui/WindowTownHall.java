package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.WorkOrderView;
import com.minecolonies.coremod.colony.buildings.BuildingTownHall;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.util.LanguageHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Window for the town hall.
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
     * Id of the citizens button in the GUI.
     */
    private static final String BUTTON_WORKORDER = "workOrder";

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
     * Id of the up button in the GUI.
     */
    private static final String BUTTON_UP = "up";

    /**
     * Id of the up button in the GUI.
     */
    private static final String BUTTON_DOWN = "down";

    /**
     * The id of the delete button in the GUI.
     */
    private static final String BUTTON_DELETE = "delete";

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
     * Id of the citizens page in the GUI.
     */
    private static final String PAGE_WORKORDER = "pageWorkOrder";

    /**
     * Id of the user list in the GUI.
     */
    private static final String LIST_USERS = "users";

    /**
     * Id of the citizens list in the GUI.
     */
    private static final String LIST_CITIZENS = "citizenList";

    /**
     * Id of the workOrder list in the GUI.
     */
    private static final String LIST_WORKORDER     = "workOrderList";
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
     * Id of the total assignee label in the GUI.
     */
    private static final String ASSIGNEE_LABEL = "assignee";

    /**
     * Id of the total work label in the GUI.
     */
    private static final String WORK_LABEL = "work";

    /**
     * Id of the hidden workorder id in the GUI.
     */
    private static final String HIDDEN_WORKORDER_ID = "hiddenId";

    /**
     * The position of the hidden id in the workOrder window.
     */
    private static final int HIDDEN_ID_POSITION = 5;

    /**
     * Link to the xml file of the window.
     */
    private static final String TOWNHALL_RESOURCE_SUFFIX = ":gui/windowtownhall.xml";

    /**
     * The builders job description string.
     */
    private static final String BUILDER_JOB = "com.minecolonies.coremod.job.Builder";

    /**
     * The deliverymen job description string.
     */
    private static final String              DELIVERYMEN_JOB = "com.minecolonies.coremod.job.Deliveryman";
    /**
     * List of workOrders.
     */
    private final        List<WorkOrderView> workOrders      = new ArrayList<>();
    /**
     * The view of the current building.
     */
    private final BuildingTownHall.View townHall;
    /**
     * List of added users.
     */
    @NotNull
    private final List<Permissions.Player> users       = new ArrayList<>();
    /**
     * List of citizens.
     */
    @NotNull
    private final List<CitizenDataView>    citizens    = new ArrayList<>();
    /**
     * Map of the pages.
     */
    @NotNull
    private final Map<String, String>      tabsToPages = new HashMap<>();

    /**
     * The button f the last tab -> will be filled later on.
     */
    private Button lastTabButton;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList userList;

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowTownHall(final BuildingTownHall.View townHall)
    {
        super(townHall, Constants.MOD_ID + TOWNHALL_RESOURCE_SUFFIX);
        this.townHall = townHall;

        updateUsers();
        updateCitizens();
        updateWorkOrders();

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFO, PAGE_INFO);
        tabsToPages.put(BUTTON_SETTINGS, PAGE_SETTINGS);
        tabsToPages.put(BUTTON_PERMISSIONS, PAGE_PERMISSIONS);
        tabsToPages.put(BUTTON_CITIZENS, PAGE_CITIZENS);
        tabsToPages.put(BUTTON_WORKORDER, PAGE_WORKORDER);


        tabsToPages.keySet().forEach(key -> registerButton(key, this::onTabClicked));
        registerButton(BUTTON_ADD_PLAYER, this::addPlayerCLicked);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_REMOVE_PLAYER, this::removePlayerClicked);
        registerButton(BUTTON_PROMOTE, this::promoteDemoteClicked);
        registerButton(BUTTON_DEMOTE, this::promoteDemoteClicked);
        registerButton(BUTTON_RECALL, this::recallClicked);
        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_TOGGLE_JOB, this::toggleHiring);

        registerButton(BUTTON_UP, this::updatePriority);
        registerButton(BUTTON_DOWN, this::updatePriority);
        registerButton(BUTTON_DELETE, this::deleteWorkOrder);
    }

    /**
     * Clears and resets all users.
     */
    private void updateUsers()
    {
        users.clear();
        users.addAll(townHall.getColony().getPlayers().values());
        users.sort(Comparator.comparing(Permissions.Player::getRank, Permissions.Rank::compareTo));
    }

    /**
     * Clears and resets all citizens.
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(townHall.getColony().getCitizens().values());
    }

    /**
     * Clears and resets all citizens.
     */
    private void updateWorkOrders()
    {
        workOrders.clear();
        workOrders.addAll(townHall.getColony().getWorkOrders());
        workOrders.sort((first, second) -> second.getPriority() > first.getPriority() ? 1 : (second.getPriority() < first.getPriority() ? -1 : 0));
    }

    /**
     * On Button click update the priority.
     *
     * @param button the clicked button.
     */
    private void updatePriority(@NotNull final Button button)
    {
        @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(HIDDEN_ID_POSITION);
        final int id = Integer.parseInt(idLabel.getLabelText());
        final String buttonLabel = button.getID();

        for (int i = 0; i < workOrders.size(); i++)
        {
            final WorkOrderView workOrder = workOrders.get(i);
            if (workOrder.getId() == id)
            {
                if (buttonLabel.equals(BUTTON_UP) && i > 0)
                {
                    workOrder.setPriority(workOrders.get(i - 1).getPriority() + 1);
                    MineColonies.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()));
                }
                else if (buttonLabel.equals(BUTTON_DOWN) && i <= workOrders.size())
                {
                    workOrder.setPriority(workOrders.get(i + 1).getPriority() - 1);
                    MineColonies.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()));
                }

                workOrders.sort((first, second) -> second.getPriority() > first.getPriority() ? 1 : (second.getPriority() < first.getPriority() ? -1 : 0));
                window.findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class).refreshElementPanes();
                return;
            }
        }
    }

    /**
     * On Button click remove the workOrder.
     *
     * @param button the clicked button.
     */
    private void deleteWorkOrder(@NotNull final Button button)
    {
        @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(HIDDEN_ID_POSITION);
        final int id = Integer.parseInt(idLabel.getLabelText());
        for (int i = 0; i < workOrders.size(); i++)
        {
            if (workOrders.get(i).getId() == id)
            {
                workOrders.remove(i);
                break;
            }
        }
        MineColonies.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, true, 0));
        window.findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened.
     * Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        createAndSetStatistics();

        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);

        lastTabButton = findPaneOfTypeByID(BUTTON_ACTIONS, Button.class);
        lastTabButton.setEnabled(false);

        fillUserList();
        fillCitizensList();
        fillWorkOrderList();

        if (townHall.getColony().isManualHiring())
        {
            findPaneOfTypeByID("toggleJob", Button.class).setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.hiring.on"));
        }
    }

    /**
     * Creates several statistics and sets them in the townHall GUI.
     */
    private void createAndSetStatistics()
    {
        final int citizensSize = townHall.getColony().getCitizens().size();

        int workers = 0;
        int builders = 0;
        int deliverymen = 0;

        for (@NotNull final CitizenDataView citizen : citizens)
        {
            switch (citizen.getJob())
            {
                case BUILDER_JOB:
                    builders++;
                    break;
                case DELIVERYMEN_JOB:
                    deliverymen++;
                    break;
                case "":
                    break;
                default:
                    workers++;
            }
        }

        workers += deliverymen + builders;

        final String numberOfCitizens =
          LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.totalCitizens", citizensSize, townHall.getColony().getMaxCitizens());
        final String numberOfUnemployed = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.unemployed", citizensSize - workers);
        final String numberOfBuilders = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.builders", builders);
        final String numberOfDeliverymen = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.deliverymen", deliverymen);

        findPaneOfTypeByID(CURRENT_SPEC_LABEL, Label.class).setLabelText("<Industrial>");
        findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Label.class).setLabelText(numberOfCitizens);
        findPaneOfTypeByID(UNEMP_CITIZENS_LABEL, Label.class).setLabelText(numberOfUnemployed);
        findPaneOfTypeByID(BUILDERS_LABEL, Label.class).setLabelText(numberOfBuilders);
        findPaneOfTypeByID(DELIVERY_MAN_LABEL, Label.class).setLabelText(numberOfDeliverymen);
    }

    /**
     * Fills the userList in the GUI.
     */
    private void fillUserList()
    {
        userList = findPaneOfTypeByID(LIST_USERS, ScrollingList.class);
        userList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return users.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Permissions.Player player = users.get(index);
                String rank = player.getRank().name();
                rank = Character.toUpperCase(rank.charAt(0)) + rank.toLowerCase().substring(1);
                rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(player.getName());
                rowPane.findPaneOfTypeByID("rank", Label.class).setLabelText(rank);
            }
        });
    }

    /**
     * Fills the citizens list in the GUI.
     */
    private void fillCitizensList()
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return citizens.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final CitizenDataView citizen = citizens.get(index);

                rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(citizen.getName());
            }
        });
    }

    /**
     * Fills the workOrder list inside the townhall GUI.
     */
    private void fillWorkOrderList()
    {
        final ScrollingList workOrderList = findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class);
        workOrderList.enable();
        workOrderList.show();

        //Creates a dataProvider for the unemployed citizenList.
        workOrderList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return workOrders.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final WorkOrderView workOrder = workOrders.get(index);
                String claimingCitizen = "";

                if (index == 0)
                {
                    if (getElementCount() == 1)
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                    }
                    else
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).show();
                    }
                    rowPane.findPaneOfTypeByID(BUTTON_UP, Button.class).hide();
                }
                else if (index == getElementCount() - 1)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                }

                //Searches citizen of id x
                for (@NotNull final CitizenDataView citizen : citizens)
                {
                    if (citizen.getID() == workOrder.getClaimedBy())
                    {
                        claimingCitizen = citizen.getName();
                        break;
                    }
                }

                rowPane.findPaneOfTypeByID(WORK_LABEL, Label.class).setLabelText(workOrder.getValue());
                rowPane.findPaneOfTypeByID(ASSIGNEE_LABEL, Label.class).setLabelText(claimingCitizen);
                rowPane.findPaneOfTypeByID(HIDDEN_WORKORDER_ID, Label.class).setLabelText(Integer.toString(workOrder.getId()));
            }
        });
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @Override
    public String getBuildingName()
    {
        return townHall.getColony().getName();
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     *
     * @param button the pressed button.
     */
    private void toggleHiring(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getLabel().equals(LanguageHandler.format("com.minecolonies.coremod.gui.hiring.off")))
        {
            button.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.hiring.on"));
            toggle = true;
        }
        else
        {
            button.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.hiring.off"));
            toggle = false;
        }
        MineColonies.getNetwork().sendToServer(new ToggleJobMessage(this.building.getColony(), toggle));
    }

    /**
     * Sets the clicked tab.
     *
     * @param button Tab button clicked on.
     */
    private void onTabClicked(@NotNull final Button button)
    {
        final String page = tabsToPages.get(button.getID());
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(page);

        lastTabButton.setEnabled(true);
        button.setEnabled(false);
        lastTabButton = button;
    }

    @Override
    public void onUpdate()
    {
        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
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
        updateWorkOrders();
        window.findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        @NotNull final WindowTownHallNameEntry window = new WindowTownHallNameEntry(townHall.getColony());
        window.open();
    }

    /**
     * Action performed when add player button is clicked.
     */
    private void addPlayerCLicked()
    {
        final TextField input = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(townHall.getColony(), input.getText()));
        input.setText("");
    }

    /**
     * Action performed when remove player button is clicked.
     *
     * @param button Button that holds the user clicked on.
     */
    private void removePlayerClicked(final Button button)
    {
        final int row = userList.getListElementIndexByPane(button);
        if (row >= 0 && row < users.size())
        {
            final Permissions.Player user = users.get(row);
            if (user.getRank() != Permissions.Rank.OWNER)
            {
                MineColonies.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(townHall.getColony(), user.getID()));
            }
        }
    }

    /**
     * Action performed when promote or demote button is clicked.
     *
     * @param button Button that holds the  user clicked on.
     */
    private void promoteDemoteClicked(@NotNull final Button button)
    {
        final int row = userList.getListElementIndexByPane(button);
        if (row >= 0 && row < users.size())
        {
            final Permissions.Player user = users.get(row);

            if (button.getID().equals(BUTTON_PROMOTE))
            {
                MineColonies.getNetwork()
                  .sendToServer(new PermissionsMessage.ChangePlayerRank(townHall.getColony(), user.getID(), PermissionsMessage.ChangePlayerRank.Type.PROMOTE));
            }
            else
            {
                MineColonies.getNetwork()
                  .sendToServer(new PermissionsMessage.ChangePlayerRank(townHall.getColony(), user.getID(), PermissionsMessage.ChangePlayerRank.Type.DEMOTE));
            }
        }
    }

    /**
     * Action when a recall button is clicked.
     */
    private void recallClicked()
    {
        MineColonies.getNetwork().sendToServer(new RecallTownhallMessage(townHall));
    }
}
