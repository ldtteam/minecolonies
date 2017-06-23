package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
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
import com.minecolonies.coremod.network.messages.*;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
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
     * Id of the toggle job button in the GUI.
     */
    private static final String BUTTON_TOGGLE_HOUSING = "toggleHousing";

    /**
     * Id of the remove player button in the GUI..
     */
    private static final String BUTTON_REMOVE_PLAYER = "removePlayer";

    /**
     * Id of the promote player button in the GUI..
     */
    private static final String BUTTON_PROMOTE = "promote";

    /**
     * TAG to retrieve the string for on.
     */
    private static final String ON = "com.minecolonies.coremod.gui.workerHuts.retrieveOn";

    /**
     * TAG to retrieve the string for off.
     */
    private static final String OFF = "com.minecolonies.coremod.gui.workerHuts.retrieveOff";

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
    private static final String LIST_WORKORDER  = "workOrderList";
    /**
     * Id of the current specializations label in the GUI.
     */
    private static final String HAPPINESS_LABEL = "happiness";

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
     * Id of the total miners label in the GUI.
     */
    private static final String MINERS_LABEL = "miners";

    /**
     * Id of the total fishermen label in the GUI.
     */
    private static final String FISHERMEN_LABEL = "fishermen";

    /**
     * Id of the total guards label in the GUI.
     */
    private static final String GUARDS_LABEL = "Guards";

    /**
     * Id of the total lumberjacks label in the GUI.
     */
    private static final String LUMBERJACKS_LABEL = "lumberjacks";

    /**
     * Id of the total farmers label in the GUI.
     */
    private static final String FARMERS_LABEL = "farmers";

    /**
     * Id of the total bakers label in the GUI.
     */
    private static final String BAKERS_LABEL = "bakers";

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
    private static final String TOWNHALL_RESOURCE_SUFFIX = ":gui/windowTownHall.xml";

    /**
     * The button to go to the previous permission settings page.
     */
    private static final String BUTTON_PREV_PAGE_PERM = "prevPagePerm";

    /**
     * The button to go to the next permission settings page.
     */
    private static final String BUTTON_NEXT_PAGE_PERM = "nextPagePerm";

    /**
     * The button to go to the officer permission settings page.
     */
    private static final String BUTTON_MANAGE_OFFICER = "officerPage";

    /**
     * The button to go to the friend permission settings page.
     */
    private static final String BUTTON_MANAGE_FRIEND = "friendPage";

    /**
     * The button to go to the neutral permission settings page.
     */
    private static final String BUTTON_MANAGE_NEUTRAL = "neutralPage";

    /**
     * The button to go to the hostile permission settings page.
     */
    private static final String BUTTON_MANAGE_HOSTILE = "hostilePage";

    /**
     * Id of the switch view of the perm pages.
     */
    private static final String VIEW_PERM_PAGES = "permPages";

    /**
     * Id of the switch view of the different groups.
     */
    private static final String VIEW_PERM_GROUPS = "userGroups";

    /**
     * The view of the permission management.
     */
    private static final String PERMISSION_VIEW = "managePermissions";

    /**
     * Button to trigger the permission changes.
     */
    private static final String BUTTON_TRIGGER = "trigger";

    /**
     * The id of the officer permission view.
     */
    private static final String VIEW_OFFICER = "officer";

    /**
     * The id of the officer permission view.
     */
    private static final String VIEW_FRIEND = "friend";

    /**
     * The id of the officer permission view.
     */
    private static final String VIEW_NEUTRAL = "neutral";

    /**
     * The id of the officer permission view.
     */
    private static final String VIEW_HOSTILE = "hostile";

    /**
     * The list of actions for a certain permission group.
     */
    private static final String LIST_ACTIONS = "actions";

    /**
     * The list of free blocks to be interacted with.
     */
    private static final String LIST_FREE_BLOCKS = "blocks";

    /**
     * Key to get readable permission values.
     */
    private static final String KEY_TO_PERMISSIONS = "com.minecolonies.coremod.permission.";

    /**
     * Ignored index starts at this line, ignore this amount after this index.
     */
    private static final int IGNORE_INDEX     = 3;

    /**
     * Button clicked to add a block to the colony to be freely interacted with.
     */
    private static final String BUTTON_ADD_BLOCK = "addBlock";

    /**
     * Input field with the blockName or position to add.
     */
    private static final String INPUT_BLOCK_NAME = "addBlockName";

    /**
     * Button to remove a block or position of the list.
     */
    private static final String BUTTON_REMOVE_BLOCK = "removeBlock";

    /**
     * List of workOrders.
     */
    private final List<WorkOrderView> workOrders = new ArrayList<>();

    /**
     * The view of the current building.
     */
    private final BuildingTownHall.View townHall;

    /**
     * List of added users.
     */
    @NotNull
    private final List<Player> users = new ArrayList<>();

    /**
     * List of citizens.
     */
    @NotNull
    private final List<CitizenDataView> citizens = new ArrayList<>();

    /**
     * Map of the pages.
     */
    @NotNull
    private final Map<String, String> tabsToPages = new HashMap<>();

    /**
     * The button f the last tab -> will be filled later on.
     */
    private Button lastTabButton;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList userList;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList actionsList;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList freeBlocksList;

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
        registerButton(BUTTON_TOGGLE_HOUSING, this::toggleHousing);


        registerButton(BUTTON_PREV_PAGE_PERM, this::switchPage);
        registerButton(BUTTON_NEXT_PAGE_PERM, this::switchPage);

        registerButton(BUTTON_MANAGE_OFFICER, this::editOfficer);
        registerButton(BUTTON_MANAGE_FRIEND, this::editFriend);
        registerButton(BUTTON_MANAGE_NEUTRAL, this::editNeutral);
        registerButton(BUTTON_MANAGE_HOSTILE, this::editHostile);


        registerButton(BUTTON_UP, this::updatePriority);
        registerButton(BUTTON_DOWN, this::updatePriority);
        registerButton(BUTTON_DELETE, this::deleteWorkOrder);

        findPaneOfTypeByID(BUTTON_PREV_PAGE_PERM, Button.class).setEnabled(false);
        findPaneOfTypeByID(BUTTON_MANAGE_OFFICER, Button.class).setEnabled(false);

        registerButton(BUTTON_TRIGGER, this::trigger);
        registerButton(BUTTON_ADD_BLOCK, this::addBlock);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
    }

    /**
     * Clears and resets all users.
     */
    private void updateUsers()
    {
        users.clear();
        users.addAll(townHall.getColony().getPlayers().values());
        users.sort(Comparator.comparing(Player::getRank, Rank::compareTo));
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
     * Re-sorts the WorkOrders list according to the priorities inside the list.
     */
    private void sortWorkOrders()
    {
        workOrders.sort(Comparator.comparing(WorkOrderView::getPriority, Comparator.reverseOrder()));
    }

    /**
     * Clears and resets all citizens.
     */
    private void updateWorkOrders()
    {
        workOrders.clear();
        workOrders.addAll(townHall.getColony().getWorkOrders());
        sortWorkOrders();
    }

    private void removeBlock(final Button button)
    {
        final int row = freeBlocksList.getListElementIndexByPane(button);
        if (row >= 0)
        {
            @NotNull final List<Block> freeBlocks = townHall.getColony().getFreeBlocks();
            @NotNull final List<BlockPos> freePositions = townHall.getColony().getFreePositions();

            if(row < freeBlocks.size())
            {
                MineColonies.getNetwork().sendToServer(
                        new ChangeFreeToInteractBlockMessage(townHall.getColony(), freeBlocks.get(row), ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
                townHall.getColony().removeFreeBlock(freeBlocks.get(row));
            }
            else if(row < freeBlocks.size() + freePositions.size())
            {
                final BlockPos freePos = freePositions.get(row - freeBlocks.size());
                MineColonies.getNetwork().sendToServer(
                        new ChangeFreeToInteractBlockMessage(townHall.getColony(), freePos, ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
                townHall.getColony().removeFreePosition(freePos);
            }
            fillFreeBlockList();
        }
    }

    /**
     * Called when the "addBlock" button has been triggered.
     * Tries to add the content of the input field as block or position to the colony.
     */
    private void addBlock()
    {
        final TextField input = findPaneOfTypeByID(INPUT_BLOCK_NAME, TextField.class);
        final String inputText = input.getText();

        final Block block = Block.getBlockFromName(inputText);

        if(block != null)
        {
            townHall.getColony().addFreeBlock(block);
            MineColonies.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(townHall.getColony(), block, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
        }

        final BlockPos pos = BlockPosUtil.getBlockPosOfString(inputText);

        if(pos != null)
        {
            MineColonies.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(townHall.getColony(), pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
            townHall.getColony().addFreePosition(pos);
        }

        fillFreeBlockList();
        input.setText("");
    }

    /**
     * Called when the permission button has been triggered.
     * @param button the triggered button.
     */
    private void trigger(@NotNull final Button button)
    {
        @NotNull final Pane pane = button.getParent().getChildren().get(2);
        int index = 0;
        if(pane instanceof Label)
        {
            index = Integer.valueOf(((Label) pane).getLabelText());
        }
        final boolean trigger = LanguageHandler.format(ON).equals(button.getLabel());
        final Action action = Action.values()[index];
        final Rank rank = Rank.valueOf(actionsList.getParent().getID().toUpperCase(Locale.ENGLISH));

        MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(townHall.getColony(), PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
        townHall.getColony().getPermissions().togglePermission(rank, action);

        if(trigger)
        {
            button.setLabel(LanguageHandler.format(OFF));
        }
        else
        {
            button.setLabel(LanguageHandler.format(ON));
        }
    }

    /**
     * Switch between previous and next page.
     */
    private void switchPage(@NotNull final Button button)
    {
        if(button.getID().equals(BUTTON_PREV_PAGE_PERM))
        {
            findPaneOfTypeByID(VIEW_PERM_PAGES, SwitchView.class).previousView();

            findPaneOfTypeByID(BUTTON_PREV_PAGE_PERM, Button.class).setEnabled(false);
            findPaneOfTypeByID(BUTTON_NEXT_PAGE_PERM, Button.class).setEnabled(true);
        }
        else
        {
            findPaneOfTypeByID(VIEW_PERM_PAGES, SwitchView.class).nextView();

            findPaneOfTypeByID(BUTTON_PREV_PAGE_PERM, Button.class).setEnabled(true);
            findPaneOfTypeByID(BUTTON_NEXT_PAGE_PERM, Button.class).setEnabled(false);
        }

        if(findPaneOfTypeByID(VIEW_PERM_PAGES, SwitchView.class).getCurrentView().getID().equals(PERMISSION_VIEW))
        {
            findPaneOfTypeByID(BUTTON_PREV_PAGE_PERM, Button.class).setEnabled(true);
            findPaneOfTypeByID(BUTTON_NEXT_PAGE_PERM, Button.class).setEnabled(true);

            fillPermissionList(VIEW_OFFICER);
        }
    }

    private void editOfficer()
    {
        findPaneOfTypeByID(VIEW_PERM_GROUPS, SwitchView.class).setView(VIEW_OFFICER);
        findPaneOfTypeByID(BUTTON_MANAGE_OFFICER, Button.class).setEnabled(false);
        findPaneOfTypeByID(BUTTON_MANAGE_FRIEND, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_NEUTRAL, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_HOSTILE, Button.class).setEnabled(true);

        fillPermissionList(VIEW_OFFICER);
    }

    private void editFriend()
    {
        findPaneOfTypeByID(VIEW_PERM_GROUPS, SwitchView.class).setView(VIEW_FRIEND);
        findPaneOfTypeByID(BUTTON_MANAGE_OFFICER, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_FRIEND, Button.class).setEnabled(false);
        findPaneOfTypeByID(BUTTON_MANAGE_NEUTRAL, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_HOSTILE, Button.class).setEnabled(true);

        fillPermissionList(VIEW_FRIEND);
    }

    private void editNeutral()
    {
        findPaneOfTypeByID(VIEW_PERM_GROUPS, SwitchView.class).setView(VIEW_NEUTRAL);
        findPaneOfTypeByID(BUTTON_MANAGE_OFFICER, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_FRIEND, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_NEUTRAL, Button.class).setEnabled(false);
        findPaneOfTypeByID(BUTTON_MANAGE_HOSTILE, Button.class).setEnabled(true);

        fillPermissionList(VIEW_NEUTRAL);
    }

    private void editHostile()
    {
        findPaneOfTypeByID(VIEW_PERM_GROUPS, SwitchView.class).setView(VIEW_HOSTILE);
        findPaneOfTypeByID(BUTTON_MANAGE_OFFICER, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_FRIEND, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_NEUTRAL, Button.class).setEnabled(true);
        findPaneOfTypeByID(BUTTON_MANAGE_HOSTILE, Button.class).setEnabled(false);

        fillPermissionList(VIEW_HOSTILE);
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

                sortWorkOrders();
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
        fillFreeBlockList();

        if (townHall.getColony().isManualHiring())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_JOB, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (townHall.getColony().isManualHousing())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_HOUSING, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
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
        int miners = 0;
        int fishermen = 0;
        int guards = 0;
        int lumberjacks = 0;
        int farmers = 0;
        int bakers = 0;

        for (@NotNull final CitizenDataView citizen : citizens)
        {
            switch (citizen.getJob())
            {
                case COM_MINECOLONIES_COREMOD_JOB_BUILDER:
                    builders++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN:
                    deliverymen++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_MINER:
                    miners++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_FISHERMAN:
                    fishermen++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_LUMBERJACK:
                    lumberjacks++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_FARMER:
                    farmers++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_GUARD:
                    guards++;
                    break;
                case COM_MINECOLONIES_COREMOD_JOB_BAKER:
                    bakers++;
                    break;
                case "":
                    break;
                default:
                    workers++;
            }
        }

        workers += deliverymen + builders + miners + fishermen + lumberjacks + farmers + guards + bakers;

        final String numberOfCitizens =
          LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.totalCitizens", citizensSize, townHall.getColony().getMaxCitizens());
        final String numberOfUnemployed = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.unemployed", citizensSize - workers);
        final String numberOfBuilders = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.builders", builders);
        final String numberOfDeliverymen = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.deliverymen", deliverymen);
        final String numberOfMiners = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.miners", miners);
        final String numberOfFishermen = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.fishermen", fishermen);
        final String numberOfGuards = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.Guards", guards);
        final String numberOfLumberjacks = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.lumberjacks", lumberjacks);
        final String numberOfFarmers = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.farmers", farmers);
        final String numberOfBakers = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.bakers", bakers);

        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        final String roundedHappiness = df.format(building.getColony().getOverallHappiness());

        findPaneOfTypeByID(HAPPINESS_LABEL, Label.class).setLabelText(roundedHappiness);
        findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Label.class).setLabelText(numberOfCitizens);
        findPaneOfTypeByID(UNEMP_CITIZENS_LABEL, Label.class).setLabelText(numberOfUnemployed);
        findPaneOfTypeByID(BUILDERS_LABEL, Label.class).setLabelText(numberOfBuilders);
        findPaneOfTypeByID(DELIVERY_MAN_LABEL, Label.class).setLabelText(numberOfDeliverymen);
        findPaneOfTypeByID(MINERS_LABEL, Label.class).setLabelText(numberOfMiners);
        findPaneOfTypeByID(FISHERMEN_LABEL, Label.class).setLabelText(numberOfFishermen);
        findPaneOfTypeByID(GUARDS_LABEL, Label.class).setLabelText(numberOfGuards);
        findPaneOfTypeByID(LUMBERJACKS_LABEL, Label.class).setLabelText(numberOfLumberjacks);
        findPaneOfTypeByID(FARMERS_LABEL, Label.class).setLabelText(numberOfFarmers);
        findPaneOfTypeByID(BAKERS_LABEL, Label.class).setLabelText(numberOfBakers);
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
                final Player player = users.get(index);
                String rank = player.getRank().name();
                rank = Character.toUpperCase(rank.charAt(0)) + rank.toLowerCase(Locale.ENGLISH).substring(1);
                rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(player.getName());
                rowPane.findPaneOfTypeByID("rank", Label.class).setLabelText(rank);
            }
        });
    }

    /**
     * Fills the permission list in the GUI.
     */
    private void fillPermissionList(@NotNull final String category)
    {
        actionsList = findPaneOfTypeByID(LIST_ACTIONS + category, ScrollingList.class);
        actionsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return Action.values().length - IGNORE_INDEX;
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final int actionIndex = index <= IGNORE_INDEX ? index : (index + IGNORE_INDEX);
                final Action action = Action.values()[actionIndex];
                final String name = LanguageHandler.format(KEY_TO_PERMISSIONS + action.toString().toLowerCase());

                if(name.contains(KEY_TO_PERMISSIONS))
                {
                    return;
                }

                rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(name);
                final boolean isTriggered = townHall.getColony().getPermissions().hasPermission(Rank.valueOf(actionsList.getParent().getID().toUpperCase()), action);
                rowPane.findPaneOfTypeByID("trigger", Button.class)
                        .setLabel(isTriggered ? LanguageHandler.format(ON)
                                : LanguageHandler.format(OFF));
                rowPane.findPaneOfTypeByID("index", Label.class).setLabelText(Integer.toString(actionIndex));
            }
        });
    }

    /**
     * Fills the free blocks list in the GUI.
     */
    private void fillFreeBlockList()
    {
        @NotNull final List<Block> freeBlocks = townHall.getColony().getFreeBlocks();
        @NotNull final List<BlockPos> freePositions = townHall.getColony().getFreePositions();

        freeBlocksList = findPaneOfTypeByID(LIST_FREE_BLOCKS, ScrollingList.class);
        freeBlocksList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return freeBlocks.size() + freePositions.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                if(index < freeBlocks.size())
                {
                    rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(freeBlocks.get(index).getRegistryName().toString());
                }
                else
                {
                    final BlockPos pos = freePositions.get(index - freeBlocks.size());
                    rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
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
        if (button.getLabel().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            toggle = false;
        }
        MineColonies.getNetwork().sendToServer(new ToggleJobMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     *
     * @param button the pressed button.
     */
    private void toggleHousing(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getLabel().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            toggle = false;
        }
        MineColonies.getNetwork().sendToServer(new ToggleHousingMessage(this.building.getColony(), toggle));
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
        super.onUpdate();

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
            final Player user = users.get(row);
            if (user.getRank() != Rank.OWNER)
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
            final Player user = users.get(row);

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
