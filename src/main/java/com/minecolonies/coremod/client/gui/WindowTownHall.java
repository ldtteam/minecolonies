package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.DropDownList;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.commands.colonycommands.ListColoniesCommand.TELEPORT_COMMAND;

/**
 * Window for the town hall.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public class WindowTownHall extends AbstractWindowBuilding<ITownHallView>
{
    /**
     * Black color.
     */
    public static final int BLACK = Color.getByName("black", 0);

    /**
     * List of workOrders.
     */
    private final List<WorkOrderView> workOrders = new ArrayList<>();

    /**
     * The view of the current building.
     */
    private final ITownHallView townHall;

    /**
     * List of added users.
     */
    @NotNull
    private final List<Player> users = new ArrayList<>();

    /**
     * List of citizens.
     */
    @NotNull
    private final List<IColonyView> allies = new ArrayList<>();

    /**
     * List of citizens.
     */
    @NotNull
    private final List<IColonyView> feuds = new ArrayList<>();

    /**
     * List of citizens.
     */
    @NotNull
    private final List<ICitizenDataView> citizens = new ArrayList<>();

    /**
     * Map of the pages.
     */
    @NotNull
    private final Map<String, String> tabsToPages = new HashMap<>();

    /**
     * Drop down list for style.
     */
    private DropDownList colorDropDownList;

    /**
     * The button f the last tab -> will be filled later on.
     */
    private Button lastTabButton;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList permEventList;

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
     * The ScrollingList of all allies.
     */
    private final        ScrollingList alliesList;

    /**
     * The ScrollingList of all feuds.
     */
    private final        ScrollingList feudsList;

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowTownHall(final BuildingTownHall.View townHall)
    {
        super(townHall, Constants.MOD_ID + TOWNHALL_RESOURCE_SUFFIX);
        this.townHall = townHall;

        alliesList = findPaneOfTypeByID(LIST_ALLIES, ScrollingList.class);
        feudsList = findPaneOfTypeByID(LIST_FEUDS, ScrollingList.class);

        initColorPicker();
        updateUsers();
        updateCitizens();
        updateWorkOrders();
        updateAllies();
        updateFeuds();

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFO, PAGE_INFO);
        tabsToPages.put(BUTTON_SETTINGS, PAGE_SETTINGS);
        tabsToPages.put(BUTTON_PERMISSIONS, PAGE_PERMISSIONS);
        tabsToPages.put(BUTTON_CITIZENS, PAGE_CITIZENS);
        tabsToPages.put(BUTTON_WORKORDER, PAGE_WORKORDER);
        tabsToPages.put(BUTTON_HAPPINESS, PAGE_HAPPINESS);

        tabsToPages.keySet().forEach(key -> registerButton(key, this::onTabClicked));
        registerButton(BUTTON_ADD_PLAYER, this::addPlayerCLicked);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_MERCENARY, this::mercenaryClicked);
        registerButton(BUTTON_REMOVE_PLAYER, this::removePlayerClicked);
        registerButton(BUTTON_PROMOTE, this::promoteDemoteClicked);
        registerButton(BUTTON_DEMOTE, this::promoteDemoteClicked);
        registerButton(BUTTON_RECALL, this::recallClicked);
        registerButton(BUTTON_HIRE, this::hireClicked);
        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_TOGGLE_JOB, this::toggleHiring);
        registerButton(BUTTON_TOGGLE_HOUSING, this::toggleHousing);
        registerButton(BUTTON_TOGGLE_MOVE_IN, this::toggleMoveIn);
        registerButton(BUTTON_TOGGLE_PRINT_PROGRESS, this::togglePrintProgress);

        registerButton(NAME_LABEL, this::fillCitizenInfo);
        registerButton(RECALL_ONE, this::recallOneClicked);

        registerButton(BUTTON_MANAGE_OFFICER, this::editOfficer);
        registerButton(BUTTON_MANAGE_FRIEND, this::editFriend);
        registerButton(BUTTON_MANAGE_NEUTRAL, this::editNeutral);
        registerButton(BUTTON_MANAGE_HOSTILE, this::editHostile);
        registerButton(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, this::addPlayerToColonyClicked);
        registerButton(BUTTON_TP, this::teleportToColony);
        registerButton(BUTTON_UP, this::updatePriority);
        registerButton(BUTTON_DOWN, this::updatePriority);
        registerButton(BUTTON_DELETE, this::deleteWorkOrder);

        registerButton(BUTTON_TRIGGER, this::trigger);
        registerButton(BUTTON_ADD_BLOCK, this::addBlock);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
        findPaneOfTypeByID(BUTTON_MANAGE_OFFICER, Button.class).setEnabled(false);
        colorDropDownList.setSelectedIndex(townHall.getColony().getTeamColonyColor().ordinal());
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initColorPicker()
    {
        registerButton(BUTTON_PREVIOUS_COLOR_ID, this::previousStyle);
        registerButton(BUTTON_NEXT_COLOR_ID, this::nextStyle);
        findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class).setEnabled(enabled);
        colorDropDownList = findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class);

        colorDropDownList.setHandler(this::onDropDownListChanged);

        final List<TextFormatting> textColors = Arrays.stream(TextFormatting.values()).filter(TextFormatting::isColor).collect(Collectors.toList());

        colorDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return textColors.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < textColors.size())
                {
                    return textColors.get(index).getFriendlyName();
                }
                return "";
            }
        });
    }

    /**
     * Called when the dropdownList changed.
     * @param dropDownList the list.
     */
    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        MineColonies.getNetwork().sendToServer(new TeamColonyColorChangeMessage(dropDownList.getSelectedIndex(), townHall));
    }

    /**
     * Change to the next style.
     */
    private void nextStyle()
    {
        colorDropDownList.selectNext();
    }

    /**
     * Change to the previous style.
     */
    private void previousStyle()
    {
        colorDropDownList.selectPrevious();
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
     * Clears and resets all allies.
     */
    private void updateAllies()
    {
        allies.clear();
        final IColony colony = building.getColony();

        for (final Player player : colony.getPermissions().getPlayersByRank(Rank.OFFICER))
        {
            final IColony col = IColonyManager.getInstance().getIColonyByOwner(Minecraft.getMinecraft().world, player.getID());
            if (col instanceof ColonyView)
            {
                for (final Player owner : colony.getPermissions().getPlayersByRank(Rank.OWNER))
                {
                    if (col.getPermissions().getRank(owner.getID()) == Rank.OFFICER)
                    {
                        allies.add((IColonyView) col);
                    }
                }
            }
        }
    }

    /**
     * Clears and resets all feuds.
     */
    private void updateFeuds()
    {
        feuds.clear();
        final IColony colony = building.getColony();
        for (final Player player : colony.getPermissions().getPlayersByRank(Rank.HOSTILE))
        {
            final IColony col = IColonyManager.getInstance().getIColonyByOwner(Minecraft.getMinecraft().world, player.getID());
            if (col instanceof ColonyView)
            {
                for (final Player owner : colony.getPermissions().getPlayersByRank(Rank.OWNER))
                {
                    if (col.getPermissions().getRank(owner.getID()) == Rank.HOSTILE)
                    {
                        feuds.add((IColonyView) col);
                    }
                }
            }
        }
    }

    /**
     * On Button click teleport to the colony..
     *
     * @param button the clicked button.
     */
    private void teleportToColony(@NotNull final Button button)
    {
        final int row = alliesList.getListElementIndexByPane(button);
        final IColonyView ally = allies.get(row);
        final ITextComponent teleport = new TextComponentString(LanguageHandler.format(DO_REALLY_WANNA_TP, ally.getName()))
                                          .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, TELEPORT_COMMAND + ally.getID())
                                          ));

        Minecraft.getMinecraft().player.sendMessage(teleport);
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened.
     * Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        if (lastTabButton != null)
        {
            return;
        }

        createAndSetStatistics();

        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);

        lastTabButton = findPaneOfTypeByID(BUTTON_ACTIONS, Button.class);
        lastTabButton.off();
        findPaneOfTypeByID(lastTabButton.getID() + "0", Image.class).hide();
        findPaneOfTypeByID(lastTabButton.getID() + "1", ButtonImage.class).show();

        fillUserList();
        fillCitizensList();
        fillWorkOrderList();
        fillFreeBlockList();
        fillAlliesAndFeudsList();
        fillPermEventsList();
        updateHappiness();

        if (townHall.getColony().isManualHiring())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_JOB, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (!townHall.getColony().isPrintingProgress())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_PRINT_PROGRESS, Button.class).setLabel(LanguageHandler.format(OFF_STRING));
        }

        if (townHall.getColony().isManualHousing())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_HOUSING, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (townHall.getColony().canMoveIn())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_MOVE_IN, Button.class).setLabel(LanguageHandler.format(ON_STRING));
        }

        if (townHall.getColony().getMercenaryUseTime() != 0
              && townHall.getColony().getWorld().getTotalWorldTime() - townHall.getColony().getMercenaryUseTime() < TICKS_FOURTY_MIN)
        {
            findPaneOfTypeByID(BUTTON_MERCENARY, Button.class).disable();
        }
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
        sortWorkOrders();
    }

    /**
     * Re-sorts the WorkOrders list according to the priorities inside the list.
     */
    private void sortWorkOrders()
    {
        workOrders.sort(Comparator.comparing(WorkOrderView::getPriority, Comparator.reverseOrder()));
    }

    private void removeBlock(final Button button)
    {
        final int row = freeBlocksList.getListElementIndexByPane(button);
        if (row >= 0)
        {
            @NotNull final List<Block> freeBlocks = townHall.getColony().getFreeBlocks();
            @NotNull final List<BlockPos> freePositions = townHall.getColony().getFreePositions();

            if (row < freeBlocks.size())
            {
                MineColonies.getNetwork().sendToServer(
                  new ChangeFreeToInteractBlockMessage(townHall.getColony(), freeBlocks.get(row), ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
                townHall.getColony().removeFreeBlock(freeBlocks.get(row));
            }
            else if (row < freeBlocks.size() + freePositions.size())
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
                if (index < freeBlocks.size())
                {
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(freeBlocks.get(index).getRegistryName().toString());
                }
                else
                {
                    final BlockPos pos = freePositions.get(index - freeBlocks.size());
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            }
        });
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

        if (block != null)
        {
            townHall.getColony().addFreeBlock(block);
            MineColonies.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(townHall.getColony(), block, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
        }

        final BlockPos pos = BlockPosUtil.getBlockPosOfString(inputText);

        if (pos != null)
        {
            MineColonies.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(townHall.getColony(), pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
            townHall.getColony().addFreePosition(pos);
        }

        fillFreeBlockList();
        input.setText("");
    }

    /**
     * Called when the permission button has been triggered.
     *
     * @param button the triggered button.
     */
    private void trigger(@NotNull final Button button)
    {
        @NotNull final Pane pane = button.getParent().getChildren().get(2);
        int index = 0;
        if (pane instanceof Label)
        {
            index = Integer.valueOf(((Label) pane).getLabelText());
        }
        final boolean trigger = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON).equals(button.getLabel());
        final Action action = Action.values()[index];
        final Rank rank = Rank.valueOf(actionsList.getParent().getID().toUpperCase(Locale.ENGLISH));

        MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(townHall.getColony(), PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
        townHall.getColony().getPermissions().togglePermission(rank, action);

        if (trigger)
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
        }
        else
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON));
        }
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
                final String name = LanguageHandler.format(KEY_TO_PERMISSIONS + action.toString().toLowerCase(Locale.US));

                if (name.contains(KEY_TO_PERMISSIONS))
                {
                    Log.getLogger().warn("Didn't work for:" + name);
                    return;
                }

                rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(name);
                final boolean isTriggered = townHall.getColony().getPermissions().hasPermission(Rank.valueOf(actionsList.getParent().getID().toUpperCase(Locale.ENGLISH)), action);
                rowPane.findPaneOfTypeByID("trigger", Button.class)
                  .setLabel(isTriggered ? LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON)
                              : LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
                rowPane.findPaneOfTypeByID("index", Label.class).setLabelText(Integer.toString(actionIndex));
            }
        });
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
     * Creates several statistics and sets them in the townHall GUI.
     */
    private void createAndSetStatistics()
    {
        final int citizensSize = townHall.getColony().getCitizens().size();

        final Map<String, Integer> jobCountMap = new HashMap<>();
        for (@NotNull final ICitizenDataView citizen : citizens)
        {
            if (citizen.isChild())
            {
                jobCountMap.put("child", jobCountMap.get("child") == null ? 1 : (jobCountMap.get("child") + 1));
            }
            else
            {
                final String[] splitString = citizen.getJob().split("\\.");
                final int length = splitString.length;
                final String job = splitString[length - 1].toLowerCase(Locale.ENGLISH);
                jobCountMap.put(job, jobCountMap.get(job) == null ? 1 : (jobCountMap.get(job) + 1));
            }
		}

        final Map<String, Integer> jobMaxCountMap = new HashMap<>();
        for (@NotNull final IBuildingView building : townHall.getColony().getBuildings())
        {
            if (!building.isBuilding() && building instanceof AbstractBuildingWorker.View)
            {
                final String buildingName = building.getSchematicName().toLowerCase();
                if (jobCountMap.get(buildingName) == null)
                {
                    jobCountMap.put(buildingName, 0);
                }
                jobMaxCountMap.put(buildingName, jobMaxCountMap.get(buildingName) == null ? 1 : (jobMaxCountMap.get(buildingName) + 1));
            }
        }

        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        final String roundedHappiness = df.format(building.getColony().getOverallHappiness());

        findPaneOfTypeByID(HAPPINESS_LABEL, Label.class).setLabelText(roundedHappiness);

        final ScrollingList list = findPaneOfTypeByID("citizen-stats", ScrollingList.class);
        if (list == null)
        {
            return;
        }

        final String numberOfCitizens =
            LanguageHandler.format("com.minecolonies.coremod.gui.townHall.population.totalCitizens",
                citizensSize, townHall.getColony().getCitizenCount());
        findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Label.class).setLabelText(numberOfCitizens);

        final Integer unemployed = jobCountMap.get("") == null ? 0 : jobCountMap.get("");
        final String numberOfUnemployed = LanguageHandler.format(
            "com.minecolonies.coremod.gui.townHall.population.unemployed", unemployed);
        jobCountMap.remove("");

        final Integer maxJobs = jobCountMap.size();
        final Integer preJobsHeaders = 1;

        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return maxJobs + preJobsHeaders;
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                if (jobCountMap.isEmpty())
                {
                    return;
                }

                final Label label = rowPane.findPaneOfTypeByID(CITIZENS_AMOUNT_LABEL, Label.class);
                // preJobsHeaders = number of all unemployed citizens
                if (index == 0)
                {
                    label.setLabelText(numberOfUnemployed);
                }
                if (index < preJobsHeaders)
                {
                    return;
                }

                final Map.Entry<String, Integer> entry = jobCountMap.entrySet().iterator().next();
                final String job = entry.getKey();
                final String labelJobKey = job.endsWith("man") ? job.replace("man", "men") : (job + "s");
                final String numberOfWorkers = LanguageHandler.format(
					"com.minecolonies.coremod.gui.townHall.population." + labelJobKey, entry.getValue(), jobMaxCountMap.get(job));
                label.setLabelText(numberOfWorkers);
                jobCountMap.remove(entry.getKey());
            }
        });
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
                rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(player.getName());
                rowPane.findPaneOfTypeByID("rank", Label.class).setLabelText(rank);
            }
        });
    }

    /**
     * Fills the allies and feuds lists.
     */
    private void fillAlliesAndFeudsList()
    {
        alliesList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return allies.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IColonyView IColonyView = allies.get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(IColonyView.getName());
                final long distance = BlockPosUtil.getDistance2D(IColonyView.getCenter(), building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Label.class).setLabelText((int) distance + "b");
                final Button button = rowPane.findPaneOfTypeByID(BUTTON_TP, Button.class);
                if (townHall.getBuildingLevel() < Configurations.gameplay.minThLevelToTeleport || !townHall.canPlayerUseTP())
                {
                    button.setLabel(LanguageHandler.format(TH_TOO_LOW));
                    button.disable();
                }
                else
                {
                    button.enable();
                }
            }
        });

        feudsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return feuds.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IColonyView IColonyView = feuds.get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(IColonyView.getName());
                final long distance = BlockPosUtil.getDistance2D(IColonyView.getCenter(), building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Label.class).setLabelText(String.valueOf((int) distance));
            }
        });
    }

    private void fillPermEventsList()
    {
        permEventList = findPaneOfTypeByID(LIST_PERM_EVENT, ScrollingList.class);
        permEventList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getPermissionEvents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final PermissionEvent event = building.getPermissionEvents().get(index);

                rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(event.getName() + (event.getId() == null ? " <fake>" : ""));
                rowPane.findPaneOfTypeByID(POS_LABEL, Label.class).setLabelText(event.getPosition().getX() + " " + event.getPosition().getY() + " " + event.getPosition().getZ());

                if (event.getId() == null)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).hide();
                }

                final String name = LanguageHandler.format(KEY_TO_PERMISSIONS + event.getAction().toString().toLowerCase(Locale.US));

                if (name.contains(KEY_TO_PERMISSIONS))
                {
                    Log.getLogger().warn("Didn't work for:" + name);
                    return;
                }
                rowPane.findPaneOfTypeByID(ACTION_LABEL, Label.class).setLabelText(name);
            }
        });
    }

    /**
     * Update the display for the happiness
     */
    private void updateHappiness()
    {
        final HappinessData happiness = building.getColony().getHappinessData();
        final String[] imagesIds = new String[] {GUARD_HAPPINESS_LEVEL, HOUSE_HAPPINESS_LEVEL, SATURATION_HAPPINESS_LEVEL};
        final int[] levels = new int[] {happiness.getGuards(), happiness.getHousing(), happiness.getSaturation()};
        for (int i = 0; i < imagesIds.length; i++)
        {
            final Image image = findPaneOfTypeByID(imagesIds[i], Image.class);
            switch (levels[i])
            {
                case HappinessData.INCREASE:
                    image.setImage(GREEN_ICON);
                    break;
                case HappinessData.STABLE:
                    image.setImage(YELLOW_ICON);
                    break;
                case HappinessData.DECREASE:
                    image.setImage(RED_ICON);
                    break;
                default:
                    throw new IllegalStateException(imagesIds[i] + "isn't in [" + HappinessData.INCREASE + "," + HappinessData.STABLE + "," + HappinessData.DECREASE + "] range.");
            }
        }
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
     * Executed when fill citizen is clicked.
     *
     * @param button the clicked button.
     */
    private void fillCitizenInfo(final Button button)
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        for (final Pane pane : citizenList.getContainer().getChildren())
        {
            pane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class).enable();
        }
        final int row = citizenList.getListElementIndexByPane(button);
        findPaneByID(CITIZEN_INFO).show();
        button.disable();
        final ICitizenDataView view = citizens.get(row);
        WindowCitizen.createXpBar(view, this);
        WindowCitizen.createHappinessBar(view, this); 
        WindowCitizen.createSkillContent(view, this);
        findPaneOfTypeByID(JOB_LABEL, Label.class).setLabelText("§l" + LanguageHandler.format(view.getJob().trim().isEmpty() ? GUI_TOWNHALL_CITIZEN_JOB_UNEMPLOYED : view.getJob()));
        findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Label.class).setLabelText(String.valueOf(view.getId()));
    }

    /**
     * Executed when the recall one button has been clicked.
     * Recalls one specific citizen.
     *
     * @param button the clicked button.
     */
    private void recallOneClicked(final Button button)
    {
        final String citizenidLabel = button.getParent().findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Label.class).getLabelText();
        final int citizenid = Integer.parseInt(citizenidLabel);
        MineColonies.getNetwork().sendToServer(new RecallSingleCitizenMessage(townHall, citizenid));
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
                final ICitizenDataView citizen = citizens.get(index);

                rowPane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class).setLabel(citizen.getName());
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

                final int numElements = getElementCount();

                if (index == 0)
                {
                    if (numElements == 1)
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                    }
                    else
                    {
                        rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).show();
                    }
                    rowPane.findPaneOfTypeByID(BUTTON_UP, Button.class).hide();
                }
                else if (index == numElements - 1)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_DOWN, Button.class).hide();
                }

                //Searches citizen of id x
                for (@NotNull final IBuildingView buildingView : building.getColony().getBuildings())
                {
                    if (buildingView.getPosition().equals(workOrder.getClaimedBy()) && buildingView instanceof AbstractBuildingBuilderView)
                    {
                        claimingCitizen = ((AbstractBuildingBuilderView) buildingView).getWorkerName();
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
     * Toggles citizens moving in. Off means citizens stop moving in.
     *
     * @param button the pressed button.
     */
    private void toggleMoveIn(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getLabel().equals(LanguageHandler.format(OFF_STRING)))
        {
            button.setLabel(LanguageHandler.format(ON_STRING));
            toggle = true;
        }
        else
        {
            button.setLabel(LanguageHandler.format(OFF_STRING));
            toggle = false;
        }
        MineColonies.getNetwork().sendToServer(new ToggleMoveInMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles printing progress.
     */
    private void togglePrintProgress(@NotNull final Button button)
    {
        if (button.getLabel().equals(LanguageHandler.format(OFF_STRING)))
        {
            button.setLabel(LanguageHandler.format(ON_STRING));
        }
        else
        {
            button.setLabel(LanguageHandler.format(OFF_STRING));
        }
        MineColonies.getNetwork().sendToServer(new ToggleHelpMessage(this.building.getColony()));
    }

    /**
     * Sets the clicked tab.
     *
     * @param button Tab button clicked on.
     */
    private void onTabClicked(@NotNull final Button button)
    {
        final String oldId = lastTabButton.getID();
        final String newId = button.getID();
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(tabsToPages.get(newId));
        findPaneOfTypeByID(oldId + "0", Image.class).show();
        findPaneOfTypeByID(oldId + "1", ButtonImage.class).hide();
        findPaneOfTypeByID(newId + "0", Image.class).hide();
        findPaneOfTypeByID(newId + "1", ButtonImage.class).show();

        lastTabButton.on();
        button.off();
        lastTabButton = button;
        setPage("");
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

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        switch (currentPage)
        {
            case PAGE_PERMISSIONS:
                updateUsers();
                window.findPaneOfTypeByID(LIST_USERS, ScrollingList.class).refreshElementPanes();
                break;
            case PAGE_CITIZENS:
                updateCitizens();
                window.findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class).refreshElementPanes();
                break;
            case PAGE_HAPPINESS:
                updateHappiness();
                break;
            case PAGE_WORKORDER:
                updateWorkOrders();
                window.findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class).refreshElementPanes();
                break;
        }
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
     * Action performed when mercenary button is clicked.
     */
    private void mercenaryClicked()
    {
        @NotNull final WindowTownHallMercenary window = new WindowTownHallMercenary(townHall.getColony());
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
     * Action performed when remove player button is clicked.
     *
     * @param button Button that holds the user clicked on.
     */
    private void addPlayerToColonyClicked(@NotNull final Button button)
    {
        final int row = permEventList.getListElementIndexByPane(button);
        if (row >= 0 && row < building.getPermissionEvents().size())
        {
            final PermissionEvent user = building.getPermissionEvents().get(row);
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayerOrFakePlayer(townHall.getColony(), user.getName(), user.getId()));
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

    /**
     * Action when the hire button is clicked
     */
    private void hireClicked()
    {
        @NotNull final WindowTownHallHireCitizen window = new WindowTownHallHireCitizen(townHall.getColony());
        window.open();
    }

    /**
     * For switches inside of tabs
     */
    @Override
    public void setPage(@NotNull final String button)
    {
        final String curSwitch = (lastTabButton == null) ? findPaneOfTypeByID(BUTTON_ACTIONS, Button.class).getID() : lastTabButton.getID();
        super.switchView = findPaneOfTypeByID(GUI_LIST_BUTTON_SWITCH + tabsToPages.get(curSwitch), SwitchView.class);
        super.pageNum.on();
        super.setPage(button);

        // Additional handlers
        if (switchView.getCurrentView().getID().equals(PERMISSION_VIEW))
        {
            editOfficer();
        }
    }
}
