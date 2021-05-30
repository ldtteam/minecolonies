package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.network.messages.server.colony.GuardScepterMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardTaskMessage;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Our building hut view.
 *
 * @author Asherslab
 */
public class WindowHutGuardTowerModule extends AbstractWindowWorkerModuleBuilding<AbstractBuildingGuards.View>
{

    /**
     * GUI Buttons.
     */
    private final Button buttonTaskPatrol;
    private final Button buttonTaskFollow;
    private final Button buttonTaskGuard;
    private final Button buttonTaskMine;
    private final Button buttonSetTarget;

    /**
     * GUI Lists.
     */
    private ScrollingList listOfPoints;

    /**
     * Whether to assign the job manually.
     */
    private boolean assignManually = false;

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
     * Whether to hire from training facilities
     */
    private boolean hireTrainees = true;

    /**
     * The GuardTask of the guard.
     */
    private GuardTask task = GuardTask.GUARD;

    /**
     * The GuardJob of the guard.
     */
    private GuardType job = null;

    /**
     * The list of manual patrol targets.
     */
    private List<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingGuards.View}.
     */
    public WindowHutGuardTowerModule(final AbstractBuildingGuards.View building)
    {
        super(building, Constants.MOD_ID + GUI_RESOURCE);

        registerButton(GUI_BUTTON_JOB, this::switchJob);
        registerButton(GUI_BUTTON_ASSIGNMENT_MODE, this::switchAssignmentMode);
        registerButton(GUI_BUTTON_PATROL_MODE, this::switchPatrolMode);
        registerButton(GUI_BUTTON_RETRIEVAL_MODE, this::switchRetrievalMode);
        registerButton(GUI_BUTTON_TRAINEE_MODE, this::switchTraineeMode);
        registerButton(GUI_BUTTON_SET_TARGET, this::setTarget);

        registerButton(GUI_SWITCH_TASK_PATROL, this::switchTask);
        registerButton(GUI_SWITCH_TASK_FOLLOW, this::switchTask);
        registerButton(GUI_SWITCH_TASK_GUARD, this::switchTask);
        registerButton(GUI_SWITCH_TASK_MINE, this::switchTask);

        buttonTaskPatrol = this.findPaneOfTypeByID(GUI_SWITCH_TASK_PATROL, Button.class);
        buttonTaskFollow = this.findPaneOfTypeByID(GUI_SWITCH_TASK_FOLLOW, Button.class);
        buttonTaskGuard = this.findPaneOfTypeByID(GUI_SWITCH_TASK_GUARD, Button.class);
        buttonTaskMine = this.findPaneOfTypeByID(GUI_SWITCH_TASK_MINE, Button.class);
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
        else if (task.equals(GuardTask.MINE))
        {
            setMinePosLabel();
        }


        if (!building.canGuardMine())
        {
            buttonTaskMine.hide();
        }
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
        final Pane currentPane = findPaneOfTypeByID(GUI_SWITCH_VIEW_PAGES, SwitchView.class).getCurrentView();
        if (currentPane != null)
        {
            final String currentPage = currentPane.getID();
            if (currentPage.equals(GUI_PAGE_LEVEL_ACTIONS))
            {
                pullInfoFromHut();
                window.findPaneOfTypeByID(GUI_ELEMENT_LIST_LEVELS, ScrollingList.class).refreshElementPanes();
            }
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.GuardTower";
    }

    /**
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        final Button buttonJob = this.findPaneOfTypeByID(GUI_BUTTON_JOB, Button.class);

        if (job != null)
        {
            buttonJob.setText(LanguageHandler.format(job.getButtonTranslationKey()));
        }

        buttonJob.setEnabled(assignManually);

        this.findPaneOfTypeByID(GUI_BUTTON_ASSIGNMENT_MODE, Button.class).setText(assignManually ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);
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
        else if (task.equals(GuardTask.MINE) && building.canGuardMine())
        {
            buttonTaskMine.setEnabled(false);
            buttonSetTarget.hide();
        }
    }

    /**
     * Switch the job.
     */
    private void switchJob()
    {
        if (building.getGuardType() == null)
        {
            final List<GuardType> guardTypes = new ArrayList<>(IGuardTypeRegistry.getInstance().getValues());
            job = guardTypes.get(new Random().nextInt(guardTypes.size()));
        }
        else
        {
            final GuardType guardType = building.getGuardType();
            final List<GuardType> possibleGuardTypes = new ArrayList<>(IGuardTypeRegistry.getInstance().getValues());
            final int currentGuardTypeIndex = possibleGuardTypes.indexOf(guardType);
            final GuardType nextGuardType = possibleGuardTypes.get(currentGuardTypeIndex == possibleGuardTypes.size() - 1 ? 0 : currentGuardTypeIndex + 1);

            building.setGuardType(nextGuardType);
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Retrieve all attributes from the building to display in GUI.
     */
    private void pullInfoFromHut()
    {
        this.assignManually = building.isAssignManually();
        this.patrolManually = building.isPatrolManually();
        this.retrieveOnLowHealth = building.isRetrieveOnLowHealth();
        this.tightGrouping = building.isTightGrouping();
        this.hireTrainees = building.isHireTrainees();
        this.task = building.getTask();
        this.job = building.getGuardType();
        this.patrolTargets = building.getPatrolTargets();
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
            buttonTaskMine.setEnabled(true);

            buttonSetTarget.show();
        }
        else if (button.getID().contains(GUI_SWITCH_TASK_FOLLOW))
        {
            building.setTask(GuardTask.FOLLOW);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(false);
            buttonTaskGuard.setEnabled(true);
            buttonSetTarget.setEnabled(true);
            buttonTaskMine.setEnabled(true);
            buttonSetTarget.show();
        }
        else if (button.getID().contains(GUI_SWITCH_TASK_MINE) && building.canGuardMine())
        {
            building.setTask(GuardTask.MINE);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(true);
            buttonTaskMine.setEnabled(false);
            buttonSetTarget.hide();

        }
        else
        {
            building.setTask(GuardTask.GUARD);

            buttonTaskPatrol.setEnabled(true);
            buttonTaskFollow.setEnabled(true);
            buttonTaskGuard.setEnabled(false);
            buttonTaskMine.setEnabled(true);

            buttonSetTarget.show();
        }
        pullInfoFromHut();
        sendChangesToServer();
        setMinePosLabel();
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

    /**
     * Switch the assignment mode.
     */
    private void switchAssignmentMode()
    {
        building.setAssignManually(!building.isAssignManually());
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Sends the changes to the server.
     */
    private void sendChangesToServer()
    {
        final ResourceLocation resourceName = building.getGuardType() == null ? new ResourceLocation("") : building.getGuardType().getRegistryName();
        Network.getNetwork().sendToServer(new GuardTaskMessage(building, resourceName, assignManually, patrolManually, retrieveOnLowHealth, task.ordinal(), tightGrouping, hireTrainees));
    }

    /**
     * Sets the label for mine position if task is set to patrol mine
     * Set to info text if mine position is null
     */
    private void setMinePosLabel()
    {
        final Text minePosLabel = window.findPaneOfTypeByID("minePos", Text.class);
        if (task.equals(GuardTask.MINE))
        {
            if (building.getMinePos() != null)
            {
                minePosLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.worherhuts.patrollingmine", building.getMinePos().getCoordinatesAsString()));
            }
            else
            {
                minePosLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.job.guard.assignmine"));
            }
        }
        else
        {
            minePosLabel.clearText();
        }
    }
}
