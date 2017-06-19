package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.BuildingGuardTower;
import com.minecolonies.coremod.network.messages.GuardScepterMessage;
import com.minecolonies.coremod.network.messages.GuardTaskMessage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON;

/**
 * Window for the guardTower hut.
 */
public class WindowHutGuardTower extends AbstractWindowWorkerBuilding<BuildingGuardTower.View>
{
    /**
     * Id of the list of the patrol points in the GUI.
     */
    private static final String LIST_LEVELS = "positions";

    /**
     * Id of the actions page in the GUI.
     */
    private static final String PAGE_ACTIONS = "levelActions";
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
    private static final String BUTTON_TASK_PATROL = "patrolling";

    /**
     * Id of the switch the task button in the GUI - (Follow).
     */
    private static final String BUTTON_TASK_FOLLOW = "following";

    /**
     * Id of the switch the task button in the GUI - (Guard).
     */
    private static final String BUTTON_TASK_GUARD = "guarding";

    /**
     * Id of the settarget button in the GUI - Depending ON task sets guard position or patrol.
     */
    private static final String BUTTON_SET_TARGET = "setTarget";

    private static final String VIEW_PAGES                      = "pages";
    private static final String HUT_GUARD_TOWER_RESOURCE_SUFFIX = ":gui/windowHutGuardTower.xml";
    private static final String AUTO   = LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.modeA");
    private static final String MANUAL = LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.modeM");
    private static final String ON  = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON);
    private static final String OFF = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF);
    /**
     * Buttons used in the application.
     */
    private final Button buttonTaskPatrol;
    private final Button buttonTaskFollow;
    private final Button buttonTaskGuard;
    private final Button buttonSetTarget;
    /**
     * Assign the job manually, knight or ranger.
     */
    private boolean assignManually = false;
    /**
     * Retrieve the guard ON low health.
     */
    private boolean                     retrieveOnLowHealth = false;
    /**
     * Patrol manually or automatically.
     */
    private boolean                     patrolManually      = false;
    /**
     * The task of the guard, following the Task enum.
     */
    private BuildingGuardTower.Task     task                = BuildingGuardTower.Task.GUARD;
    /**
     * The job of the guard, following the GuardJob enum.
     */
    private BuildingGuardTower.GuardJob job                 = null;
    /**
     * The list of MANUAL patrol targets.
     */
    private List<BlockPos>              patrolTargets       = new ArrayList<>();
    /**
     * The patrol list.
     */
    private ScrollingList patrolList;

    /**
     * Constructor for the window of the guardTower hut.
     *
     * @param building {@link BuildingGuardTower.View}.
     */
    public WindowHutGuardTower(final BuildingGuardTower.View building)
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
        registerButton(BUTTON_SET_TARGET, this::setTarget);

        buttonTaskPatrol = this.findPaneOfTypeByID(BUTTON_TASK_PATROL, Button.class);
        buttonTaskFollow = this.findPaneOfTypeByID(BUTTON_TASK_FOLLOW, Button.class);
        buttonTaskGuard = this.findPaneOfTypeByID(BUTTON_TASK_GUARD, Button.class);

        buttonSetTarget = this.findPaneOfTypeByID(BUTTON_SET_TARGET, Button.class);
        handleButtons();
    }

    /**
     * Retrieve positions from the building to display in GUI.
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

    /**
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        final Button buttonJob = this.findPaneOfTypeByID(BUTTON_JOB, Button.class);

        if (job != null)
        {
            if (job.equals(BuildingGuardTower.GuardJob.KNIGHT))
            {
                buttonJob.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.knight"));
            }
            else
            {
                buttonJob.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.ranger"));
            }
        }

        buttonJob.setEnabled(assignManually);

        this.findPaneOfTypeByID(BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(assignManually ? MANUAL : AUTO);
        this.findPaneOfTypeByID(BUTTON_PATROL_MODE, Button.class).setLabel(patrolManually ? MANUAL : AUTO);
        this.findPaneOfTypeByID(BUTTON_RETRIEVAL_MODE, Button.class).setLabel(retrieveOnLowHealth ? ON : OFF);

        if (task.equals(BuildingGuardTower.Task.PATROL))
        {
            if (patrolManually)
            {
                buttonSetTarget.setEnabled(true);
                buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.targetPatrol"));
            }
            else
            {
                buttonSetTarget.setEnabled(false);
            }
            buttonTaskPatrol.setEnabled(false);
        }
        else if (task.equals(BuildingGuardTower.Task.FOLLOW))
        {
            buttonTaskFollow.setEnabled(false);
            buttonSetTarget.hide();
        }
        else if (task.equals(BuildingGuardTower.Task.GUARD))
        {
            buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.targetGuard"));
            buttonTaskGuard.setEnabled(false);
        }
    }

    /**
     * Switch between the different task (Patrol, Follow, Guard).
     *
     * @param button the button clicked to switch the task.
     */
    private void switchTask(final Button button)
    {
        if (button.getID().contains("patrol"))
        {
            building.task = BuildingGuardTower.Task.PATROL;

            buttonTaskPatrol.setEnabled(false);
            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(true);

            buttonSetTarget.show();
        }
        else if (button.getID().contains("follow"))
        {
            building.task = BuildingGuardTower.Task.FOLLOW;

            buttonTaskFollow.setEnabled(false);
            buttonTaskPatrol.setEnabled(true);
            buttonTaskGuard.setEnabled(true);

            buttonSetTarget.hide();
        }
        else
        {
            building.task = BuildingGuardTower.Task.GUARD;

            buttonTaskGuard.setEnabled(false);
            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(true);

            buttonSetTarget.show();
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Sends changes to the server.
     */
    private void sendChangesToServer()
    {
        final int ordinal = building.job == null ? -1 : job.ordinal();
        MineColonies.getNetwork().sendToServer(new GuardTaskMessage(building, ordinal, assignManually, patrolManually, retrieveOnLowHealth, task.ordinal()));
    }

    /**
     * Sets the target for patrolling or guarding of the guard.
     */
    private void setTarget()
    {
        final EntityPlayerSP player = this.mc.thePlayer;
        final int emptySlot = player.inventory.getFirstEmptyStack();
        pullInfoFromHut();

        if (emptySlot == -1)
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.workerHuts.noSpace");
        }

        if (patrolManually && task.equals(BuildingGuardTower.Task.PATROL))
        {
            givePlayerScepter(BuildingGuardTower.Task.PATROL);
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.job.guard.tool.taskPatrol");
        }
        else if (task.equals(BuildingGuardTower.Task.GUARD))
        {
            givePlayerScepter(BuildingGuardTower.Task.GUARD);
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.job.guard.tool.taskGuard");
        }
        window.close();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     * @param localTask the task to execute with the scepter.
     */
    private void givePlayerScepter(final BuildingGuardTower.Task localTask)
    {
        MineColonies.getNetwork().sendToServer(new GuardScepterMessage(localTask.ordinal(), building.getID()));
    }

    /**
     * Switch the retrieval mode.
     */
    private void switchRetrievalMode()
    {
        building.retrieveOnLowHealth = !building.retrieveOnLowHealth;
        pullInfoFromHut();
        sendChangesToServer();
        this.findPaneOfTypeByID(BUTTON_RETRIEVAL_MODE, Button.class).setLabel(retrieveOnLowHealth ? ON : OFF);
    }

    /**
     * Switch the patrol mode.
     */
    private void switchPatrolMode()
    {
        building.patrolManually = !building.patrolManually;
        pullInfoFromHut();
        sendChangesToServer();
        handleButtons();
    }

    /**
     * Switch the job.
     */
    private void switchJob()
    {
        if (building.job == null)
        {
            building.job = BuildingGuardTower.GuardJob.RANGER;
        }
        else
        {
            if (building.job.equals(BuildingGuardTower.GuardJob.KNIGHT))
            {
                building.job = BuildingGuardTower.GuardJob.RANGER;
            }
            else
            {
                building.job = BuildingGuardTower.GuardJob.KNIGHT;
            }
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Switch the assignment mode.
     */
    private void switchAssignmentMode()
    {
        building.assignManually = !building.assignManually;
        pullInfoFromHut();
        sendChangesToServer();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        patrolList = findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class);
        if (task.equals(BuildingGuardTower.Task.PATROL))
        {
            patrolList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return patrolTargets.size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    final BlockPos pos = patrolTargets.get(index);
                    rowPane.findPaneOfTypeByID("position", Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            });
        }
        else if (task.equals(BuildingGuardTower.Task.GUARD))
        {
            patrolList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 1;
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    final BlockPos pos = building.guardPos;
                    rowPane.findPaneOfTypeByID("position", Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            });
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        pullInfoFromHut();
        handleButtons();

        if (!task.equals(BuildingGuardTower.Task.PATROL))
        {
            patrolList.hide();
        }

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
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
        return "com.minecolonies.coremod.gui.workerHuts.GuardTower";
    }
}

