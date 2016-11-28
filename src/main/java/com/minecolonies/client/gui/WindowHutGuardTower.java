package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingGuardTower;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.GuardScepterMessage;
import com.minecolonies.network.messages.GuardTaskMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
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

    /**
     * The patrol list.
     */
    ScrollingList patrolList;

    private final Button buttonTaskPatrol;
    private final Button buttonTaskFollow;
    private final Button buttonTaskGuard;

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

        buttonTaskPatrol = this.findPaneOfTypeByID(BUTTON_TASK_PATROL, Button.class);
        buttonTaskFollow = this.findPaneOfTypeByID(BUTTON_TASK_FOLLOW, Button.class);
        buttonTaskGuard = this.findPaneOfTypeByID(BUTTON_TASK_GUARD, Button.class);
        handleButtons();
    }

    /**
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        Button buttonJob = this.findPaneOfTypeByID(BUTTON_JOB, Button.class);

        if(job.equals(BuildingGuardTower.GuardJob.KNIGHT))
        {
            buttonJob.setLabel(LanguageHandler.format("com.minecolonies.gui.workerHuts.knight"));
        }
        else
        {
            buttonJob.setLabel(LanguageHandler.format("com.minecolonies.gui.workerHuts.ranger"));
        }

        buttonJob.setEnabled(assignManually);

        String auto = LanguageHandler.format("com.minecolonies.gui.workerHuts.modeA");
        String manual = LanguageHandler.format("com.minecolonies.gui.workerHuts.modeM");

        String on = LanguageHandler.format("com.minecolonies.gui.workerHuts.retrieveOn");
        String off = LanguageHandler.format("com.minecolonies.gui.workerHuts.retrieveOn");

        this.findPaneOfTypeByID(BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(assignManually ? manual : auto);
        this.findPaneOfTypeByID(BUTTON_PATROL_MODE, Button.class).setLabel(patrolManually ? manual : auto);
        this.findPaneOfTypeByID(BUTTON_RETRIEVAL_MODE, Button.class).setLabel(retrieveOnLowHealth ? on : off);

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

    /**
     * Switch between the different task (Patrol, Follow, Guard).
     * @param button the button clicked to switch the task.
     */
    private void switchTask(final Button button)
    {
        final EntityPlayerSP player = this.mc.thePlayer;
        final int emptySlot = player.inventory.getFirstEmptyStack();

        if(button.getID().contains("patrol"))
        {
            if(building.patrolManually)
            {
                if(emptySlot == -1)
                {
                    LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.gui.workerHuts.noSpace");
                    return;
                }
                givePlayerScepter(BuildingGuardTower.Task.PATROL);
                LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.job.guard.tool.taskPatrol");
            }

            building.task = BuildingGuardTower.Task.PATROL;

            buttonTaskPatrol.setEnabled(false);

            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(true);
        }
        else if(button.getID().contains("follow"))
        {
            building.task = BuildingGuardTower.Task.FOLLOW;

            buttonTaskFollow.setEnabled(false);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskGuard.setEnabled(true);
        }
        else
        {
            if(emptySlot == -1)
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.gui.workerHuts.noSpace");
                return;
            }
            building.task = BuildingGuardTower.Task.GUARD;
            givePlayerScepter(BuildingGuardTower.Task.GUARD);
            LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.job.guard.tool.taskGuard");

            buttonTaskGuard.setEnabled(false);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(true);
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Send message to player to add scepter to his inventory.
     * @param localTask the task to execute with the scepter.
     */
    private void givePlayerScepter(BuildingGuardTower.Task localTask)
    {
        MineColonies.getNetwork().sendToServer(new GuardScepterMessage(localTask.ordinal(), building.getID()));
    }

    /**
     * Sends changes to the server.
     */
    private void sendChangesToServer()
    {
        MineColonies.getNetwork().sendToServer(new GuardTaskMessage(building, job.ordinal(), assignManually, patrolManually, retrieveOnLowHealth, task.ordinal()));
    }

    /**
     * Switch the retrieval mode.
     * @param button clicked button
     */
    private void switchRetrievalMode(final Button button)
    {
        building.retrieveOnLowHealth = !building.retrieveOnLowHealth;
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Switch the patrol mode.
     * @param button clicked button
     */
    private void switchPatrolMode(final Button button)
    {
        building.patrolManually = !building.patrolManually;
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Switch the job.
     * @param button clicked button
     */
    private void switchJob(final Button button)
    {
        if(building.job.equals(BuildingGuardTower.GuardJob.KNIGHT))
        {
            building.job = BuildingGuardTower.GuardJob.RANGER;
        }
        else
        {
            building.job = BuildingGuardTower.GuardJob.KNIGHT;
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Switch the assignment mode.
     * @param button clicked button
     */
    private void switchAssignmentMode(final Button button)
    {
        building.assignManually = !building.assignManually;
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Retrieve positions from the building to display in GUI
     */

    private void pullInfoFromHut()
    {
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


        if(task.equals(BuildingGuardTower.Task.PATROL))
        {
            patrolList = findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class);
            patrolList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return patrolTargets.size();
                }

                @Override
                public void updateElement(int index, @NotNull Pane rowPane)
                {
                    BlockPos pos = patrolTargets.get(index);
                    rowPane.findPaneOfTypeByID("position", Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            });
        }
        else if(task.equals(BuildingGuardTower.Task.GUARD))
        {
            //print guard position
        }
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
        handleButtons();

        if(!task.equals(BuildingGuardTower.Task.PATROL))
        {
            patrolList.hide();
        }

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

