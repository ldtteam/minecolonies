package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.CompactColonyReference;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.colonyEvents.descriptions.IBuildingEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSchool;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenDiedEvent;
import com.minecolonies.coremod.commands.ClickEventWithExecutable;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.server.colony.*;
import com.minecolonies.coremod.network.messages.server.colony.citizen.RecallSingleCitizenMessage;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.CITIZEN_CAP;
import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.huts.WindowHutBuilderModule.BLACK;

/**
 * Window for the town hall.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public class WindowTownHall extends AbstractWindowModuleBuilding<ITownHallView>
{
    /**
     * Citizen name comparator.
     */
    private static final Comparator<ICitizenDataView> COMPARE_BY_NAME = Comparator.comparing(ICitizen::getName);

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
    private ScrollingList eventList;

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
    private final ScrollingList alliesList;

    /**
     * The ScrollingList of all feuds.
     */
    private final ScrollingList feudsList;

    /**
     * Whether the event list should display permission events, or colony events.
     */
    private boolean permissionEvents;

    /**
     * Color constants for builder list.
     */
    public static final int RED       = Color.getByName("red", 0);
    public static final int DARKGREEN = Color.getByName("darkgreen", 0);
    public static final int ORANGE    = Color.getByName("orange", 0);

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

        tabsToPages.put(BUTTON_ACTIONS, PAGE_ACTIONS);
        tabsToPages.put(BUTTON_INFOPAGE, PAGE_INFO);
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
        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_TOGGLE_JOB, this::toggleHiring);
        registerButton(BUTTON_TOGGLE_HOUSING, this::toggleHousing);
        registerButton(BUTTON_TOGGLE_MOVE_IN, this::toggleMoveIn);
        registerButton(BUTTON_TOGGLE_PRINT_PROGRESS, this::togglePrintProgress);
        registerButton("bannerPicker", this::openBannerPicker);

        registerButton(NAME_LABEL, this::fillCitizenInfo);
        registerButton(RECALL_ONE, this::recallOneClicked);

        registerButton(BUTTON_MANAGE_OFFICER, this::editOfficer);
        registerButton(BUTTON_MANAGE_FRIEND, this::editFriend);
        registerButton(BUTTON_MANAGE_NEUTRAL, this::editNeutral);
        registerButton(BUTTON_MANAGE_HOSTILE, this::editHostile);
        registerButton(BUTTON_PERMISSION_EVENTS, this::permissionEventsClicked);
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
     *
     * @param dropDownList the list.
     */
    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        Network.getNetwork().sendToServer(new TeamColonyColorChangeMessage(dropDownList.getSelectedIndex(), townHall));
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
     * On Button click teleport to the colony..
     *
     * @param button the clicked button.
     */
    private void teleportToColony(@NotNull final Button button)
    {
        final int row = alliesList.getListElementIndexByPane(button);
        final CompactColonyReference ally = building.getColony().getAllies().get(row);
        final ITextComponent teleport = new StringTextComponent(LanguageHandler.format(DO_REALLY_WANNA_TP, ally.name))
                                          .setStyle(Style.EMPTY.setBold(true).setFormatting(TextFormatting.GOLD).setClickEvent(
                                            new ClickEventWithExecutable(ClickEvent.Action.RUN_COMMAND, "",
                                              () -> Network.getNetwork().sendToServer(new TeleportToColonyMessage(
                                                ally.dimension, ally.id)))));

        Minecraft.getInstance().player.sendMessage(teleport, Minecraft.getInstance().player.getUniqueID());
        this.close();
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
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
        fillEventsList();
        updateHappiness();

        if (townHall.getColony().isManualHiring())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_JOB, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (!townHall.getColony().isPrintingProgress())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_PRINT_PROGRESS, Button.class).setText(LanguageHandler.format(OFF_STRING));
        }

        if (townHall.getColony().isManualHousing())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_HOUSING, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (townHall.getColony().canMoveIn())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_MOVE_IN, Button.class).setText(LanguageHandler.format(ON_STRING));
        }

        if (townHall.getColony().getMercenaryUseTime() != 0
              && townHall.getColony().getWorld().getGameTime() - townHall.getColony().getMercenaryUseTime() < TICKS_FOURTY_MIN)
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
        citizens.sort(COMPARE_BY_NAME);
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
                Network.getNetwork().sendToServer(
                  new ChangeFreeToInteractBlockMessage(townHall.getColony(), freeBlocks.get(row), ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
                townHall.getColony().removeFreeBlock(freeBlocks.get(row));
            }
            else if (row < freeBlocks.size() + freePositions.size())
            {
                final BlockPos freePos = freePositions.get(row - freeBlocks.size());
                Network.getNetwork().sendToServer(
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
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(freeBlocks.get(index).getRegistryName().toString());
                }
                else
                {
                    final BlockPos pos = freePositions.get(index - freeBlocks.size());
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            }
        });
    }

    /**
     * Called when the "addBlock" button has been triggered. Tries to add the content of the input field as block or position to the colony.
     */
    private void addBlock()
    {
        final TextField input = findPaneOfTypeByID(INPUT_BLOCK_NAME, TextField.class);
        final String inputText = input.getText();

        try
        {
            final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(inputText));

            if (block != null)
            {
                townHall.getColony().addFreeBlock(block);
                Network.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(townHall.getColony(), block, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
            }
        }
        catch (final ResourceLocationException e)
        {
            // Do nothing.
        }

        final BlockPos pos = BlockPosUtil.getBlockPosOfString(inputText);

        if (pos != null)
        {
            Network.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(townHall.getColony(), pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
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
        final int index = Integer.valueOf(button.getParent().findPaneOfTypeByID("index", Text.class).getTextAsString());
        final boolean trigger = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON).equals(button.getTextAsString());
        final Action action = Action.values()[index];
        final Rank rank = Rank.valueOf(actionsList.getParent().getID().toUpperCase(Locale.ENGLISH));

        Network.getNetwork().sendToServer(new PermissionsMessage.Permission(townHall.getColony(), PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
        townHall.getColony().getPermissions().togglePermission(rank, action);

        if (trigger)
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON));
        }
    }

    /**
     * Fills the permission list in the GUI.
     *
     * @param category the category to fill.
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

                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(name);
                final boolean isTriggered = townHall.getColony().getPermissions().hasPermission(Rank.valueOf(actionsList.getParent().getID().toUpperCase(Locale.ENGLISH)), action);
                rowPane.findPaneOfTypeByID("trigger", Button.class)
                  .setText(isTriggered ? LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON)
                              : LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
                rowPane.findPaneOfTypeByID("index", Text.class).setText(Integer.toString(actionIndex));
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
        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        final String roundedHappiness = df.format(building.getColony().getOverallHappiness());

        findPaneOfTypeByID(HAPPINESS_LABEL, Text.class).setText(roundedHappiness);
        final int citizensSize = townHall.getColony().getCitizens().size();
        final int citizensCap;

        if(MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(CITIZEN_CAP))
        {
            citizensCap = (int) (Math.min(MineColonies.getConfig().getServer().maxCitizenPerColony.get(),
              25 + this.building.getColony().getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_CAP)));
        }
        else
        {
              citizensCap = MineColonies.getConfig().getServer().maxCitizenPerColony.get();
        }

        final Text totalCitizenLabel = findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Text.class);
        totalCitizenLabel.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT,
            citizensSize,
            Math.max(citizensSize, townHall.getColony().getCitizenCountLimit())));
        List<IFormattableTextComponent> hoverText = new ArrayList<>();
        if(citizensSize < (citizensCap * 0.9) && citizensSize < (townHall.getColony().getCitizenCountLimit() * 0.9))
        {
            totalCitizenLabel.setColors(DARKGREEN);
        }
        else if(citizensSize < citizensCap)
        {
            hoverText.add(new TranslationTextComponent("com.minecolonies.coremod.gui.townhall.population.totalcitizens.houselimited", this.building.getColony().getName()));
            totalCitizenLabel.setColors(ORANGE);
        }
        else
        {
            if(citizensCap < MineColonies.getConfig().getServer().maxCitizenPerColony.get())
            {
                hoverText.add(new TranslationTextComponent("com.minecolonies.coremod.gui.townhall.population.totalcitizens.researchlimited", this.building.getColony().getName()));
            }
            else
            {
                hoverText.add(new TranslationTextComponent( "com.minecolonies.coremod.gui.townhall.population.totalcitizens.configlimited", this.building.getColony().getName()));
            }
            totalCitizenLabel.setText(
                LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT, citizensSize, citizensCap));
            totalCitizenLabel.setColors(RED);
        }
        PaneBuilders.tooltipBuilder().hoverPane(totalCitizenLabel).build().setText(hoverText);

        int children = 0;
        final Map<String, Tuple<Integer, Integer>> jobMaxCountMap = new HashMap<>();
        for (@NotNull final IBuildingView building : townHall.getColony().getBuildings())
        {
            if (building instanceof AbstractBuildingWorker.View)
            {
                int max = ((AbstractBuildingWorker.View) building).getMaxInhabitants();
                int workers = ((AbstractBuildingWorker.View) building).getWorkerId().size();

                String jobName = ((AbstractBuildingWorker.View) building).getJobDisplayName().toLowerCase(Locale.ENGLISH);
                if (building instanceof AbstractBuildingGuards.View)
                {
                    jobName = ((AbstractBuildingGuards.View) building).getGuardType().getJobTranslationKey();
                }

                if (building instanceof BuildingSchool.View)
                {
                    // For schools, getJobDisplayName will always be the teacher's key, as it's derived from createJob(null),
                    // while getJobName will always be the pupil, as it's derived from the hardcoded job name for the school.
                    final String teacherJobName = jobName;
                    final String pupilJobName = ((BuildingSchool.View) building).getJobName();

                    int maxTeachers = 1;
                    max = max - 1;
                    int teachers = workers = 0;
                    for (@NotNull final Integer workerId : ((BuildingSchool.View) building).getWorkerId())
                    {
                        final ICitizenDataView view = townHall.getColony().getCitizen(workerId);
                        if (view != null && view.isChild())
                        {
                            workers += 1;
                        }
                        else
                        {
                            teachers += 1;
                        }
                    }
                    final Tuple<Integer, Integer> teacherTuple = jobMaxCountMap.getOrDefault(teacherJobName, new Tuple<>(0, 0));
                    jobMaxCountMap.put(teacherJobName, new Tuple<>(teacherTuple.getA() + teachers, teacherTuple.getB() + maxTeachers));
                    final Tuple<Integer, Integer> pupilTuple = jobMaxCountMap.getOrDefault(pupilJobName, new Tuple<>(0, 0));
                    jobMaxCountMap.put(pupilJobName, new Tuple<>(pupilTuple.getA() + workers, pupilTuple.getB() + max));
                }
                else
                {
                    final Tuple<Integer, Integer> tuple = jobMaxCountMap.getOrDefault(jobName, new Tuple<>(0, 0));
                    jobMaxCountMap.put(jobName, new Tuple<>(tuple.getA() + workers, tuple.getB() + max));
                }
            }
        }


        //calculate number of children
        int unemployedCount = 0;
        for (ICitizenDataView iCitizenDataView : townHall.getColony().getCitizens().values())
        {
            if (iCitizenDataView.isChild())
            {
                children++;
            }
            else if (iCitizenDataView.getJobView() == null)
            {
                unemployedCount++;
            }
        }
        final String numberOfUnemployed = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_UNEMPLOYED, unemployedCount);
        final String numberOfKids = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_CHILDS, children);

        final ScrollingList list = findPaneOfTypeByID("citizen-stats", ScrollingList.class);
        if (list == null)
        {
            return;
        }

        final int maxJobs = jobMaxCountMap.size();
        final List<Map.Entry<String, Tuple<Integer, Integer>>> theList = new ArrayList<>(jobMaxCountMap.entrySet());

        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return maxJobs + 2;
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text label = rowPane.findPaneOfTypeByID(CITIZENS_AMOUNT_LABEL, Text.class);
                // preJobsHeaders = number of all unemployed citizens

                if (index < theList.size())
                {
                    final Map.Entry<String, Tuple<Integer, Integer>> entry = theList.get(index);
                    final String job = LanguageHandler.format(entry.getKey());
                    final String numberOfWorkers =
                      LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_POPULATION_EACH, job, entry.getValue().getA(), entry.getValue().getB());
                    label.setText(numberOfWorkers);
                }
                else
                {
                    if (index == maxJobs + 1)
                    {
                        label.setText(numberOfUnemployed);
                    }
                    else
                    {
                        label.setText(numberOfKids);
                    }
                }
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
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(player.getName());
                rowPane.findPaneOfTypeByID("rank", Text.class).setText(rank);
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
                return building.getColony().getAllies().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final CompactColonyReference colonyReference = building.getColony().getAllies().get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(colonyReference.name);
                final long distance = BlockPosUtil.getDistance2D(colonyReference.center, building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Text.class).setText((int) distance + "b");
                final Button button = rowPane.findPaneOfTypeByID(BUTTON_TP, Button.class);
                if (colonyReference.hasTownHall && (townHall.getBuildingLevel() < MineColonies.getConfig().getServer().minThLevelToTeleport.get() || !townHall.canPlayerUseTP()))
                {
                    button.setText(LanguageHandler.format(TH_TOO_LOW));
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
                return building.getColony().getFeuds().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final CompactColonyReference colonyReference = building.getColony().getFeuds().get(index);
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(colonyReference.name);
                final long distance = BlockPosUtil.getDistance2D(colonyReference.center, building.getPosition());
                rowPane.findPaneOfTypeByID(DIST_LABEL, Text.class).setText(String.valueOf((int) distance));
            }
        });
    }

    private void fillEventsList()
    {
        eventList = findPaneOfTypeByID(EVENTS_LIST, ScrollingList.class);
        eventList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return permissionEvents ? building.getPermissionEvents().size() : building.getColonyEvents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text nameLabel = rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class);
                final Text actionLabel = rowPane.findPaneOfTypeByID(ACTION_LABEL, Text.class);
                if (permissionEvents)
                {
                    final PermissionEvent event = building.getPermissionEvents().get(index);

                    nameLabel.setText(event.getName() + (event.getId() == null ? " <fake>" : ""));
                    rowPane.findPaneOfTypeByID(POS_LABEL, Text.class).setText(event.getPosition().getX() + " " + event.getPosition().getY() + " " + event.getPosition().getZ());

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
                    actionLabel.setText(name);
                }
                else
                {
                    final IColonyEventDescription event = building.getColonyEvents().get(index);
                    if (event instanceof CitizenDiedEvent)
                    {
                        actionLabel.setText(((CitizenDiedEvent) event).getDeathCause());
                    }
                    else
                    {
                        actionLabel.setText(event.getName());
                    }
                    if (event instanceof ICitizenEventDescription)
                    {
                        nameLabel.setText(((ICitizenEventDescription) event).getCitizenName());
                    }
                    else if (event instanceof IBuildingEventDescription)
                    {
                        IBuildingEventDescription buildEvent = (IBuildingEventDescription) event;
                        nameLabel.setText(buildEvent.getBuildingName() + " " + buildEvent.getLevel());
                    }
                    rowPane.findPaneOfTypeByID(POS_LABEL, Text.class).setText(event.getEventPos().getX() + " " + event.getEventPos().getY() + " " + event.getEventPos().getZ());
                    rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).hide();
                }
            }
        });
    }

    /**
     * Update the display for the happiness
     */
    private void updateHappiness()
    {
        final Map<String, Double> happinessMap = new HashMap<>();

        for (final ICitizenDataView data : building.getColony().getCitizens().values())
        {
            for (final String modifier : data.getHappinessHandler().getModifiers())
            {
                happinessMap.put(modifier, happinessMap.getOrDefault(modifier, 0.0) + data.getHappinessHandler().getModifier(modifier).getFactor());
            }
        }

        final View pane = findPaneOfTypeByID("happinesspage", View.class);
        int yPos = 62;
        for (final Map.Entry<String, Double> entry : happinessMap.entrySet())
        {
            final double value = entry.getValue() / citizens.size();
            final Image image = new Image();
            image.setSize(11, 11);
            image.setPosition(25, yPos);

            final Text label = new Text();
            label.setSize(136, 11);
            label.setPosition(50, yPos);
            label.setColors(BLACK);
            label.setText(LanguageHandler.format("com.minecolonies.coremod.gui.townhall.happiness." + entry.getKey()));

            if (value > 1.0)
            {
                image.setImage(GREEN_ICON);
            }
            else if (value == 1)
            {
                image.setImage(BLUE_ICON);
            }
            else if (value > 0.75)
            {
                image.setImage(YELLOW_ICON);
            }
            else
            {
                image.setImage(RED_ICON);
            }
            pane.addChild(image);
            pane.addChild(label);
            PaneBuilders.tooltipBuilder().hoverPane(label).append(new TranslationTextComponent("com.minecolonies.coremod.gui.townhall.happiness.desc." + entry.getKey())).build();

            yPos += 12;
        }
    }

    /**
     * On Button click update the priority.
     *
     * @param button the clicked button.
     */
    private void updatePriority(@NotNull final Button button)
    {
        final int id = Integer.parseInt(button.getParent().findPaneOfTypeByID("hiddenId", Text.class).getTextAsString());
        final String buttonLabel = button.getID();

        for (int i = 0; i < workOrders.size(); i++)
        {
            final WorkOrderView workOrder = workOrders.get(i);
            if (workOrder.getId() == id)
            {
                if (buttonLabel.equals(BUTTON_UP) && i > 0)
                {
                    workOrder.setPriority(workOrders.get(i - 1).getPriority() + 1);
                    Network.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()));
                }
                else if (buttonLabel.equals(BUTTON_DOWN) && i <= workOrders.size())
                {
                    workOrder.setPriority(workOrders.get(i + 1).getPriority() - 1);
                    Network.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, false, workOrder.getPriority()));
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
        final int id = Integer.parseInt(button.getParent().findPaneOfTypeByID("hiddenId", Text.class).getTextAsString());
        for (int i = 0; i < workOrders.size(); i++)
        {
            if (workOrders.get(i).getId() == id)
            {
                workOrders.remove(i);
                break;
            }
        }
        Network.getNetwork().sendToServer(new WorkOrderChangeMessage(this.building, id, true, 0));
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
        WindowCitizen.createHappinessBar(view, this);
        WindowCitizen.createSkillContent(view, this);
        findPaneOfTypeByID(JOB_LABEL, Text.class).setText(
          "§l" + LanguageHandler.format(view.getJob().trim().isEmpty() ? COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED : view.getJob()));
        findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Text.class).setText(String.valueOf(view.getId()));
    }

    /**
     * Executed when the recall one button has been clicked. Recalls one specific citizen.
     *
     * @param button the clicked button.
     */
    private void recallOneClicked(final Button button)
    {
        final int citizenid = Integer.parseInt(button.getParent().findPaneOfTypeByID(HIDDEN_CITIZEN_ID, Text.class).getTextAsString());
        Network.getNetwork().sendToServer(new RecallSingleCitizenMessage(townHall, citizenid));
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

                rowPane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class).setText(citizen.getName());
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

                final String[] split = workOrder.get().split("/");

                rowPane.findPaneOfTypeByID(WORK_LABEL, Text.class).setText(split[split.length - 1]);
                rowPane.findPaneOfTypeByID(ASSIGNEE_LABEL, Text.class).setText(claimingCitizen);
                rowPane.findPaneOfTypeByID(HIDDEN_WORKORDER_ID, Text.class).setText(Integer.toString(workOrder.getId()));
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
        if (button.getTextAsString().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            toggle = false;
        }
        Network.getNetwork().sendToServer(new ToggleJobMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     *
     * @param button the pressed button.
     */
    private void toggleHousing(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getTextAsString().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            toggle = false;
        }
        Network.getNetwork().sendToServer(new ToggleHousingMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles citizens moving in. Off means citizens stop moving in.
     *
     * @param button the pressed button.
     */
    private void toggleMoveIn(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getTextAsString().equals(LanguageHandler.format(OFF_STRING)))
        {
            button.setText(LanguageHandler.format(ON_STRING));
            toggle = true;
        }
        else
        {
            button.setText(LanguageHandler.format(OFF_STRING));
            toggle = false;
        }
        Network.getNetwork().sendToServer(new ToggleMoveInMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles printing progress.
     *
     * @param button the button to toggle.
     */
    private void togglePrintProgress(@NotNull final Button button)
    {
        if (button.getTextAsString().equals(LanguageHandler.format(OFF_STRING)))
        {
            button.setText(LanguageHandler.format(ON_STRING));
        }
        else
        {
            button.setText(LanguageHandler.format(OFF_STRING));
        }
        Network.getNetwork().sendToServer(new ToggleHelpMessage(this.building.getColony()));
    }

    /**
     * Opens the banner picker window. Window does not use BlockOut, so is started manually.
     * @param button the trigger button
     */
    private void openBannerPicker(@NotNull final Button button)
    {
        Screen window = new WindowBannerPicker(townHall.getColony(), this);
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().displayGuiScreen(window));
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
        setPage(false, 0);
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
        Network.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(townHall.getColony(), input.getText()));
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
                Network.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(townHall.getColony(), user.getID()));
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
        final int row = eventList.getListElementIndexByPane(button);
        if (row >= 0 && row < building.getPermissionEvents().size())
        {
            final PermissionEvent user = building.getPermissionEvents().get(row);
            Network.getNetwork().sendToServer(new PermissionsMessage.AddPlayerOrFakePlayer(townHall.getColony(), user.getName(), user.getId()));
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
                Network.getNetwork()
                  .sendToServer(new PermissionsMessage.ChangePlayerRank(townHall.getColony(), user.getID(), PermissionsMessage.ChangePlayerRank.Type.PROMOTE));
            }
            else
            {
                Network.getNetwork()
                  .sendToServer(new PermissionsMessage.ChangePlayerRank(townHall.getColony(), user.getID(), PermissionsMessage.ChangePlayerRank.Type.DEMOTE));
            }
        }
    }

    /**
     * For switches inside of tabs
     */
    @Override
    public void setPage(final boolean relative, final int page)
    {
        final String curSwitch = (lastTabButton == null) ? findPaneOfTypeByID(BUTTON_ACTIONS, Button.class).getID() : lastTabButton.getID();
        super.switchView = findPaneOfTypeByID(GUI_LIST_BUTTON_SWITCH + tabsToPages.get(curSwitch), SwitchView.class);
        super.pageNum.on();
        super.setPage(relative, page);

        // Additional handlers
        if (switchView.getCurrentView().getID().equals(PERMISSION_VIEW))
        {
            editOfficer();
        }
    }

    /**
     * Switching between permission and colony events.
     * 
     * @param button the clicked button.
     */
    public void permissionEventsClicked(@NotNull final Button button)
    {
        permissionEvents = !permissionEvents;
        button.setText(LanguageHandler.format(permissionEvents ? TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_COLONYEVENTS : TranslationConstants.COM_MINECOLONIES_CIREMOD_GUI_TOWNHALL_PERMISSIONEVENTS));
    }
}
