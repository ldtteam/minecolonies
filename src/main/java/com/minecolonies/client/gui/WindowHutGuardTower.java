package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.colony.buildings.BuildingGuardTower;
import com.minecolonies.items.ItemScepterGuard;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Window for the guardTower hut
 */
public class WindowHutGuardTower extends AbstractWindowWorkerBuilding<BuildingGuardTower.View>
{
    /**
     * Id of the list of the patrol points in the GUI.
     */
    private static final String LIST_LEVELS    = "positions";

    /**
     * Id of the actions page in the GUI.
     */
    private static final String PAGE_ACTIONS    = "levelActions";

    /**
     * Id of the previous page button in the GUI
     */
    private static final String BUTTON_PREVPAGE = "prevPage";

    /**
     * Id of the next page button in the GUI.
     */
    private static final String BUTTON_NEXTPAGE = "nextPage";

    /**
     * Id of the switch job button in the GUI.
     */
    private static final String BUTTON_JOB = "job";

    /**
     * Id of the switch assignment mode button in the GUI - (Manually / Automatically).
     */
    private static final String BUTTON_ASSIGNMENT_MODE = "assign";

    /**
     * Id of the switch patrolling mode button in the GUI - (Manually / Automatically).
     */
    private static final String BUTTON_PATROL_MODE = "patrol";

    /**
     * Id of the switch retrieval mode button in the GUI - (Off / 10% / 20%).
     */
    private static final String BUTTON_RETRIEVAL_MODE = "retrieve";

    /**
     * Id of the switch the task button in the GUI - (Patrol).
     */
    private static final String BUTTON_TASK_PATROL= "patrolling";

    /**
     * Id of the switch the task button in the GUI - (Follow).
     */
    private static final String BUTTON_TASK_FOLLOW = "following";

    /**
     * Id of the switch the task button in the GUI - (Guard).
     */
    private static final String BUTTON_TASK_GUARD = "guarding";




    private static final String VIEW_PAGES                      = "pages";
    private static final String HUT_GUARD_TOWER_RESOURCE_SUFFIX = ":gui/windowHutGuardTower.xml";
    private Button                  buttonPrevPage;
    private Button                  buttonNextPage;

    /**
     * Assign the job manually, knight or ranger.
     */
    private boolean assignManually = false;

    /**
     * Retrieve the guard on low health.
     */
    private boolean retrieveOnLowHealth = false;

    /**
     * Patrol manually or automatically.
     */
    private boolean patrolManually = false;

    /**
     * The task of the guard, following the Task enum.
     */
    private BuildingGuardTower.Task task = BuildingGuardTower.Task.GUARD;

    /**
     * The job of the guard, following the GuarJob enum.
     */
    private BuildingGuardTower.GuardJob job = null;

    /**
     * The list of manual patrol targets.
     */
    private ArrayList<BlockPos> patrolTargets = new ArrayList<>();


    private ScrollingList           patrolList;

    private BuildingGuardTower.View guardTower;

    private final Button buttonTaskPatrol;
    private final Button buttonTaskFollow;
    private final Button buttonTaskGuard;


    //todo create message for all the data
    //todo create list with the patrol targets
    //todo if click on follow guard will start following player who clicked follow
    //todo if clicked on patrol and patrolling is on manual give the player the rod to choose a spot
    //todo if clicked on guard give the player the rod to choose a spot

    //todo create variable for guard spot which might be null if null guard spot is his hut chest.

    /**
     * Constructor for the window of the guardTower hut
     *
     * @param building {@link BuildingGuardTower.View}
     */
    public WindowHutGuardTower(BuildingGuardTower.View building)
    {
        super(building, Constants.MOD_ID + HUT_GUARD_TOWER_RESOURCE_SUFFIX);

        pullInfoFromHut();

        registerButton(BUTTON_JOB, this::switchJob);
        registerButton(BUTTON_ASSIGNMENT_MODE, this::switchAssignmentMode);
        registerButton(BUTTON_PATROL_MODE, this::switchPatrolMode);
        registerButton(BUTTON_RETRIEVAL_MODE, this::switchRetrievalMode);

        registerButton(BUTTON_TASK_PATROL, this::switchTask);
        registerButton(BUTTON_TASK_FOLLOW, this::switchTask);
        registerButton(BUTTON_TASK_GUARD, this::switchTask);

        Button buttonJob = this.findPaneOfTypeByID(BUTTON_JOB, Button.class);
        buttonJob.setEnabled(assignManually);

        buttonTaskPatrol = this.findPaneOfTypeByID(BUTTON_TASK_PATROL, Button.class);
        buttonTaskFollow = this.findPaneOfTypeByID(BUTTON_TASK_FOLLOW, Button.class);
        buttonTaskGuard = this.findPaneOfTypeByID(BUTTON_TASK_GUARD, Button.class);

        if(task.equals(BuildingGuardTower.Task.PATROL))
        {
            buttonTaskPatrol.setEnabled(false);
        }
        else if(task.equals(BuildingGuardTower.Task.FOLLOW))
        {
            buttonTaskFollow.setEnabled(false);
        }
        else
        {
            buttonTaskGuard.setEnabled(false);
        }
    }

    private void switchTask(final Button button)
    {
        if(button.getLabel().contains("Patrol"))
        {
            building.task = BuildingGuardTower.Task.PATROL;

            buttonTaskPatrol.setEnabled(false);

            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(true);
        }
        else if(button.getLabel().contains("Follow"))
        {
            building.task = BuildingGuardTower.Task.FOLLOW;

            buttonTaskFollow.setEnabled(false);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskGuard.setEnabled(true);
        }
        else
        {
            building.task = BuildingGuardTower.Task.GUARD;

            buttonTaskGuard.setEnabled(false);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(true);
        }

        //todo send message to player which tells him what he has to do:
        //todo if guard -> right click one block to mark it as guard position
        //todo if follow -> don't give stick
        //todo if patrol + manual -> "Right click blocks to mark them as patrol targets left click to terminate.
        //todo if patrol + auto -> don't give

        //todo send message to server with task
        //todo at server in the tool recognize which task he should follow, following that message.
        EntityPlayerSP player = this.mc.thePlayer;

        //todo put the itemStack into an empty slot. If no empty slot, tell player to make space
        ItemStack item = player.inventory.getStackInSlot(player.inventory.currentItem);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(ModItems.scepterGuard));
    }

    private void switchRetrievalMode(final Button button)
    {

    }

    private void switchPatrolMode(final Button button)
    {

    }

    private void switchJob(final Button button)
    {

    }

    private void switchAssignmentMode(final Button button)
    {

    }

    /**
     * Retrieve positions from the building to display in GUI
     */
    private void pullInfoFromHut()
    {
        this.guardTower = building;
        this.assignManually = building.assignManually;
        this.patrolManually = building.patrolManually;
        this.retrieveOnLowHealth = building.retrieveOnLowHealth;
        this.task = building.task;
        this.job = building.job;
        this.patrolTargets = building.patrolTargets;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);

        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);

        patrolList = findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class);
        patrolList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
               return 0;
            }

            @Override
            public void updateElement(int index, @NotNull Pane rowPane)
            {

                /*if (index == guardTower.current)
                {
                    rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.RED.getRGB());
                }
                else
                {
                    rowPane.findPaneOfTypeByID("lvl", Label.class).setColor(Color.BLACK.getRGB());
                }

                rowPane.findPaneOfTypeByID("lvl", Label.class).setLabelText(Integer.toString(index));
                rowPane.findPaneOfTypeByID("nONodes", Label.class).setLabelText(LanguageHandler.getString("com.minecolonies.gui.workerHuts.minerNode") + ": " + positions[index]);*/
            }
        });
    }

    @Override
    public void onButtonClicked(@NotNull Button button)
    {
        switch (button.getID())
        {
            case BUTTON_PREVPAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
                buttonPrevPage.setEnabled(false);
                buttonNextPage.setEnabled(true);
                break;
            case BUTTON_NEXTPAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
                buttonPrevPage.setEnabled(true);
                buttonNextPage.setEnabled(false);
                break;
            default:
                super.onButtonClicked(button);
                break;
        }
    }

    @Override
    public void onUpdate()
    {
        pullInfoFromHut();

        String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_ACTIONS))
        {
            pullInfoFromHut();
            window.findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class).refreshElementPanes();
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.minerHut";
    }
}

