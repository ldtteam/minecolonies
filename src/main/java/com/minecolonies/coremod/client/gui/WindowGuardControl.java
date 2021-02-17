package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.network.messages.server.colony.GuardScepterMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardRecalculateMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardTaskMessage;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
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
    private boolean       retrieveOnLowHealth = false;

    /**
     * Whether to patrol manually or not.
     */
    private boolean patrolManually = false;

    /**
     * Whether tight grouping is used on type of Follow.
     */
    private boolean tightGrouping = true;

    /**
     * Whether to hire from training facility
     */
    private boolean hireTrainees = true;

    /**
     * The GuardTask of the guard.
     */
    private GuardTask task = GuardTask.GUARD;

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
        registerButton(GUI_BUTTON_TRAINEE_MODE, this::switchTraineeMode);
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
                    rowPane.findPaneOfTypeByID("position", Text.class).setText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
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
                    rowPane.findPaneOfTypeByID("position", Text.class).setText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
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
                final ICitizenDataView citizenDataView = building.getColony().getCitizen((building.getGuards().get(index)));
                if (citizenDataView != null)
                {
                    final BlockPos pos = citizenDataView.getPosition();
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(citizenDataView.getName());
                    rowPane.findPaneOfTypeByID(POSITION_LABEL, Text.class).setText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                    rowPane.findPaneOfTypeByID(LEVEL_LABEL, Text.class).setText("Level: " + citizenDataView.getCitizenSkillHandler().getJobModifier(building));
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
        this.hireTrainees = building.isHireTrainees();
        this.tightGrouping = building.isTightGrouping();
        this.task = building.getTask();
        this.patrolTargets = building.getPatrolTargets();
    }

    /**
     * Sends the changes to the server.
     */
    private void sendChangesToServer()
    {
        final ResourceLocation resourceName = building.getGuardType() == null ? new ResourceLocation("") : building.getGuardType().getRegistryName();
        Network.getNetwork()
          .sendToServer(new GuardTaskMessage(building, resourceName, building.isAssignManually(), patrolManually, retrieveOnLowHealth, task.ordinal(), tightGrouping, hireTrainees));
    }

    /**
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        this.findPaneOfTypeByID(GUI_BUTTON_PATROL_MODE, Button.class).setText(patrolManually ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);
        this.findPaneOfTypeByID(GUI_BUTTON_RETRIEVAL_MODE, Button.class).setText(retrieveOnLowHealth ? GUI_SWITCH_ON : GUI_SWITCH_OFF);

        if (task.equals(GuardTask.PATROL))
        {
            buttonSetTarget.setEnabled(patrolManually);

            if (patrolManually)
            {
                buttonSetTarget.setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.targetPatrol"));
            }
            else
            {
                buttonSetTarget.clearText();
            }
            buttonTaskPatrol.setEnabled(false);
        }
        else if (task.equals(GuardTask.FOLLOW))
        {
            buttonTaskFollow.setEnabled(false);
            if (tightGrouping)
            {
                buttonSetTarget.setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.followTight"));
            }
            else
            {
                buttonSetTarget.setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.followLoose"));
            }
        }
        else if (task.equals(GuardTask.GUARD))
        {
            buttonSetTarget.setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.targetGuard"));
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
        final ClientPlayerEntity player = this.mc.player;
        final int emptySlot = player.inventory.getFirstEmptyStack();
        pullInfoFromHut();

        if (emptySlot == -1)
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.workerhuts.noSpace");
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
        Network.getNetwork().sendToServer(new GuardRecalculateMessage(building));
        pullInfoFromHut();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     * @param localTask the task to execute with the scepter.
     */
    private void givePlayerScepter(final GuardTask localTask)
    {
        Network.getNetwork().sendToServer(new GuardScepterMessage(building, localTask.ordinal()));
    }

    /**
     * Switch the retrieval mode.
     */
    private void switchRetrievalMode()
    {
        building.setRetrieveOnLowHealth(!building.isRetrieveOnLowHealth());
        pullInfoFromHut();
        sendChangesToServer();
        this.findPaneOfTypeByID(GUI_BUTTON_RETRIEVAL_MODE, Button.class).setText(retrieveOnLowHealth ? GUI_SWITCH_ON : GUI_SWITCH_OFF);
    }

    /**
     * Switch the trainee mode.
     */
    private void switchTraineeMode()
    {
        building.setHireTrainees(!building.isHireTrainees());
        pullInfoFromHut();
        sendChangesToServer();
        this.findPaneOfTypeByID(GUI_BUTTON_TRAINEE_MODE, Button.class).setText(hireTrainees ? GUI_SWITCH_ON : GUI_SWITCH_OFF);
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
