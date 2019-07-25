package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards.GuardJob;
import com.minecolonies.coremod.network.messages.GuardRecalculateMessage;
import com.minecolonies.coremod.network.messages.GuardScepterMessage;
import com.minecolonies.coremod.network.messages.GuardTaskMessage;
import net.minecraft.client.entity.PlayerEntitySP;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Our guard control window GUI.
 */
public class WindowGuardControl extends AbstractWindowSkeleton
{

    /**
     * GUI Buttons.
     */
    private final Button buttonTaskPatrol;
    private final Button buttonTaskFollow;
    private final Button buttonTaskGuard;
    private final Button buttonSetTarget;

    /**
     * GUI Lists.
     */
    private ScrollingList listOfPoints;
    /**
     * Whether to retrieve the worker on low health.
     */
    private boolean retrieveOnLowHealth = false;

    /**
     * Whether to patrol manually or not.
     */
    private boolean patrolManually = false;

    /**
     * Whether tight grouping is used on type of Follow.
     */
    private boolean tightGrouping = true;

    /**
     * The GuardTask of the guard.
     */
    private GuardTask task = GuardTask.GUARD;

    /**
     * The GuardJob of the guard.
     */
    private GuardJob job = null;

    /**
     * The list of manual patrol targets.
     */
    private List<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * The list of citizen assigned to this hut.
     */
    private ScrollingList workersListPane;

    /**
     * Building associated with the guard control.
     */
    private AbstractBuildingGuards.View building;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingGuards.View}.
     */
    public WindowGuardControl(final AbstractBuildingGuards.View building)
    {
        super(Constants.MOD_ID + GUARD_CONTROL);

        this.building = building;

        registerButton(GUI_BUTTON_PATROL_MODE, this::switchPatrolMode);
        registerButton(GUI_BUTTON_RETRIEVAL_MODE, this::switchRetrievalMode);
        registerButton(GUI_BUTTON_RECALCULATE, this::recalculate);
        registerButton(GUI_BUTTON_SET_TARGET, this::setTarget);

        registerButton(GUI_SWITCH_TASK_PATROL, this::switchTask);
        registerButton(GUI_SWITCH_TASK_FOLLOW, this::switchTask);
        registerButton(GUI_SWITCH_TASK_GUARD, this::switchTask);

        buttonTaskPatrol = this.findPaneOfTypeByID(GUI_SWITCH_TASK_PATROL, Button.class);
        buttonTaskFollow = this.findPaneOfTypeByID(GUI_SWITCH_TASK_FOLLOW, Button.class);
        buttonTaskGuard = this.findPaneOfTypeByID(GUI_SWITCH_TASK_GUARD, Button.class);
        buttonSetTarget = this.findPaneOfTypeByID(GUI_BUTTON_SET_TARGET, Button.class);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        pullInfoFromHut();

        listOfPoints = findPaneOfTypeByID(GUI_ELEMENT_LIST_LEVELS, ScrollingList.class);
        if (task.equals(GuardTask.PATROL))
        {
            listOfPoints.setDataProvider(new ScrollingList.DataProvider()
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
        else if (task.equals(GuardTask.GUARD))
        {
            listOfPoints.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 1;
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    final BlockPos pos = building.getGuardPos();
                    rowPane.findPaneOfTypeByID("position", Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            });
        }

        workersListPane = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
        workersListPane.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getGuards().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final CitizenDataView citizenDataView = building.getColony().getCitizen((building.getGuards().get(index)));
                if (citizenDataView != null)
                {
                    final BlockPos pos = citizenDataView.getPosition();
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Label.class).setLabelText(citizenDataView.getName());
                    rowPane.findPaneOfTypeByID(POSITION_LABEL, Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                    rowPane.findPaneOfTypeByID(LEVEL_LABEL, Label.class).setLabelText("Level: " + Integer.toString(citizenDataView.getLevel()));
                    WindowCitizen.createHealthBar(citizenDataView, rowPane.findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
                }
            }
        });


        handleButtons();
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);
        handleButtons();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        pullInfoFromHut();

        if (!task.equals(GuardTask.PATROL))
        {
            listOfPoints.hide();
        }
        window.findPaneOfTypeByID(GUI_ELEMENT_LIST_LEVELS, ScrollingList.class).refreshElementPanes();
        workersListPane.refreshElementPanes();
    }

    /**
     * Retrieve all attributes from the building to display in GUI.
     */
    private void pullInfoFromHut()
    {
        this.patrolManually = building.isPatrolManually();
        this.retrieveOnLowHealth = building.isRetrieveOnLowHealth();
        this.tightGrouping = building.isTightGrouping();
        this.task = building.getTask();
        this.job = building.getJob();
        this.patrolTargets = building.getPatrolTargets();
    }

    /**
     * Sends the changes to the server.
     */
    private void sendChangesToServer()
    {
        final int ordinal = building.getJob() == null ? -1 : job.ordinal();
        MineColonies.getNetwork().sendToServer(new GuardTaskMessage(building, ordinal, building.isAssignManually(), patrolManually, retrieveOnLowHealth, task.ordinal(), tightGrouping));
    }

    /**
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        this.findPaneOfTypeByID(GUI_BUTTON_PATROL_MODE, Button.class).setLabel(patrolManually ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);
        this.findPaneOfTypeByID(GUI_BUTTON_RETRIEVAL_MODE, Button.class).setLabel(retrieveOnLowHealth ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);

        if (task.equals(GuardTask.PATROL))
        {
            buttonSetTarget.setEnabled(patrolManually);

            if (patrolManually)
            {
                buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.targetPatrol"));
            }
            else
            {
                buttonSetTarget.setLabel("");
            }
            buttonTaskPatrol.setEnabled(false);
        }
        else if (task.equals(GuardTask.FOLLOW))
        {
            buttonTaskFollow.setEnabled(false);
            if (tightGrouping)
            {
                buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.followTight"));
            }
            else
            {
                buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.followLoose"));
            }
        }
        else if (task.equals(GuardTask.GUARD))
        {
            buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.targetGuard"));
            buttonTaskGuard.setEnabled(false);
        }
    }

    /**
     * Switch between the different tasks in {@link GuardTask}
     *
     * @param button the button clicked to switch the task.
     */
    private void switchTask(final Button button)
    {
        if (button.getID().contains(GUI_SWITCH_TASK_PATROL))
        {
            building.setTask(GuardTask.PATROL);

            buttonTaskPatrol.setEnabled(false);
            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(true);

            buttonSetTarget.show();
        }
        else if (button.getID().contains(GUI_SWITCH_TASK_FOLLOW))
        {
            building.setTask(GuardTask.FOLLOW);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(false);
            buttonTaskGuard.setEnabled(true);
            buttonSetTarget.setEnabled(true);
            buttonSetTarget.show();
        }
        else
        {
            building.setTask(GuardTask.GUARD);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(false);

            buttonSetTarget.show();
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Sets the target for patrolling or guarding of the guard.
     */
    private void setTarget()
    {
        final PlayerEntitySP player = this.mc.player;
        final int emptySlot = player.inventory.getFirstEmptyStack();
        pullInfoFromHut();

        if (emptySlot == -1)
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.workerHuts.noSpace");
        }

        if (patrolManually && task.equals(GuardTask.PATROL))
        {
            givePlayerScepter(GuardTask.PATROL);
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.job.guard.tool.taskPatrol");
        }
        else if (task.equals(GuardTask.FOLLOW))
        {
            tightGrouping = !tightGrouping;
            building.setTightGrouping(tightGrouping);
            pullInfoFromHut();
            sendChangesToServer();
            return;
        }
        else if (task.equals(GuardTask.GUARD))
        {
            givePlayerScepter(GuardTask.GUARD);
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.job.guard.tool.taskGuard");
        }
        window.close();
    }

    /**
     * Recalculates the mob list.
     */
    private void recalculate()
    {
        MineColonies.getNetwork().sendToServer(new GuardRecalculateMessage(building.getColony().getID(), building));
        pullInfoFromHut();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     * @param localTask the task to execute with the scepter.
     */
    private void givePlayerScepter(final GuardTask localTask)
    {
        MineColonies.getNetwork().sendToServer(new GuardScepterMessage(localTask.ordinal(), building.getID(), building.getColony().getID()));
    }

    /**
     * Switch the retrieval mode.
     */
    private void switchRetrievalMode()
    {
        building.setRetrieveOnLowHealth(!building.isRetrieveOnLowHealth());
        pullInfoFromHut();
        sendChangesToServer();
        this.findPaneOfTypeByID(GUI_BUTTON_RETRIEVAL_MODE, Button.class).setLabel(retrieveOnLowHealth ? GUI_SWITCH_ON : GUI_SWITCH_OFF);
    }

    /**
     * Switch the patrol mode.
     */
    private void switchPatrolMode()
    {
        building.setPatrolManually(!building.isPatrolManually());
        pullInfoFromHut();
        sendChangesToServer();
    }
}
