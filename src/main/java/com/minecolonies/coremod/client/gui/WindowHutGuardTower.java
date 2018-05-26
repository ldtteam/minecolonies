package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuardsNew;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuardsNew.GuardJob;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuardsNew.GuardTask;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.network.messages.GuardRecalculateMessage;
import com.minecolonies.coremod.network.messages.GuardScepterMessage;
import com.minecolonies.coremod.network.messages.GuardTaskMessage;
import com.minecolonies.coremod.network.messages.MobEntryChangeMessage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Our building hut view.
 *
 * @author Asherslab
 */
public class WindowHutGuardTowerNew extends AbstractWindowWorkerBuilding<AbstractBuildingGuardsNew.View>
{
    //// ---- GUI Constants ---- \\\\
    //GUI Lists
    private static final String  GUI_ELEMENT_LIST_LEVELS    = "positions";
    private static final String  GUI_ELEMENT_LIST_MOBS      = "mobs";
    //GUI List Elements
    private static final String  GUI_LIST_ELEMENT_NAME      = "name";
    private static final String  GUI_LIST_BUTTON_SWITCH     = "switch";
    private static final String  GUI_LIST_BUTTON_UP         = "prioUp";
    private static final String  GUI_LIST_BUTTON_DOWN       = "prioDown";
    //GUI Buttons
    private static final String  GUI_BUTTON_JOB             = "job";
    private static final String  GUI_BUTTON_ASSIGNMENT_MODE = "assign";
    private static final String  GUI_BUTTON_PATROL_MODE     = "patrol";
    private static final String  GUI_BUTTON_RETRIEVAL_MODE  = "retrieve";
    private static final String  GUI_BUTTON_SET_TARGET      = "setTarget";
    private static final String  GUI_BUTTON_NEXT_PAGE       = "nextPage";
    private static final String  GUI_BUTTON_PREV_PAGE       = "prevPage";
    private static final String  GUI_BUTTON_RECALCULATE     = "recalculate";
    //GUI Switches
    private static final String  GUI_SWITCH_VIEW_PAGES      = "pages";
    private static final String  GUI_SWITCH_TASK_PATROL     = "patrolling";
    private static final String  GUI_SWITCH_TASK_FOLLOW     = "following";
    private static final String  GUI_SWITCH_TASK_GUARD      = "guarding";
    private static final String  GUI_SWITCH_AUTO            = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_MODE_AUTO);
    private static final String  GUI_SWITCH_MANUAL          = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_MODE_MANUAL);
    private static final String  GUI_SWITCH_ON              = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON);
    private static final String  GUI_SWITCH_OFF             = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF);
    //GUI Pages
    private static final String  GUI_PAGE_PAGE_ACTIONS      = "pageActions";
    private static final String  GUI_PAGE_LEVEL_ACTIONS     = "levelActions";
    private static final String  GUI_PAGE_MOB_ACTIONS       = "mobActions";
    //GUI Resource
    private static final String  GUI_RESOURCE               = ":gui/windowHutGuardTower.xml";
    //GUI Other
    private static final Integer GUI_LIST_ELEMENT_NAME_POS  = 2;
    //// ---- GUI Constants ---- \\\\

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
    private ScrollingList patrolList;

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
     * The Map of mobs we are allowed to attack.
     */
    private List<MobEntryView> mobsToAttack = new ArrayList<>();

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingGuardsNew.View}.
     */
    public WindowHutGuardTowerNew(final AbstractBuildingGuardsNew.View building)
    {
        super(building, Constants.MOD_ID + GUI_RESOURCE);

        registerButton(GUI_BUTTON_JOB, this::switchJob);
        registerButton(GUI_BUTTON_ASSIGNMENT_MODE, this::switchAssignmentMode);
        registerButton(GUI_BUTTON_PATROL_MODE, this::switchPatrolMode);
        registerButton(GUI_BUTTON_RETRIEVAL_MODE, this::switchRetrievalMode);
        registerButton(GUI_BUTTON_SET_TARGET, this::setTarget);
        registerButton(GUI_BUTTON_RECALCULATE, this::recalculate);

        registerButton(GUI_SWITCH_TASK_PATROL, this::switchTask);
        registerButton(GUI_SWITCH_TASK_FOLLOW, this::switchTask);
        registerButton(GUI_SWITCH_TASK_GUARD, this::switchTask);

        registerButton(GUI_LIST_BUTTON_SWITCH, this::switchAttackMode);
        registerButton(GUI_LIST_BUTTON_UP, this::updatePriority);
        registerButton(GUI_LIST_BUTTON_DOWN, this::updatePriority);

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

        patrolList = findPaneOfTypeByID(GUI_ELEMENT_LIST_LEVELS, ScrollingList.class);
        if (task.equals(GuardTask.PATROL))
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
        else if (task.equals(GuardTask.GUARD))
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
                    final BlockPos pos = building.getGuardPos();
                    rowPane.findPaneOfTypeByID("position", Label.class).setLabelText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            });
        }

        ScrollingList mobsList = findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class);
        mobsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return mobsToAttack.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final String name = mobsToAttack.get(index).getName();

                rowPane.findPaneOfTypeByID(GUI_LIST_ELEMENT_NAME, Label.class).setLabelText(name);

                final Button switchButton = rowPane.findPaneOfTypeByID(GUI_LIST_BUTTON_SWITCH, Button.class);

                if (mobsToAttack.get(index).getAttack())
                {
                    switchButton.setLabel(GUI_SWITCH_ON);
                }
                else
                {
                    switchButton.setLabel(GUI_SWITCH_OFF);
                }
            }
        });
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        final Pane currentPane = findPaneOfTypeByID(GUI_SWITCH_VIEW_PAGES, SwitchView.class).getCurrentView();
        if (currentPane != null)
        {
            Button buttonNextPage = findPaneOfTypeByID(GUI_BUTTON_NEXT_PAGE, Button.class);
            Button buttonPrevPage = findPaneOfTypeByID(GUI_BUTTON_PREV_PAGE, Button.class);
            final String currentPage = currentPane.getID();
            switch (button.getID())
            {
                case GUI_BUTTON_NEXT_PAGE:
                    findPaneOfTypeByID(GUI_SWITCH_VIEW_PAGES, SwitchView.class).nextView();
                    buttonPrevPage.setEnabled(true);
                    buttonPrevPage.show();

                    if (currentPage.equals(GUI_PAGE_MOB_ACTIONS))
                    {
                        buttonNextPage.setEnabled(false);
                        buttonNextPage.hide();
                    }
                    break;
                case GUI_BUTTON_PREV_PAGE:
                    findPaneOfTypeByID(GUI_SWITCH_VIEW_PAGES, SwitchView.class).previousView();
                    buttonNextPage.setEnabled(true);
                    buttonNextPage.show();

                    if (currentPage.equals(GUI_PAGE_PAGE_ACTIONS))
                    {
                        buttonPrevPage.setEnabled(false);
                        buttonPrevPage.hide();
                    }
                    break;
                default:
                    super.onButtonClicked(button);
            }
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        pullInfoFromHut();
        handleButtons();

        if (!task.equals(GuardTask.PATROL))
        {
            patrolList.hide();
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
            else if (currentPage.equals(GUI_PAGE_MOB_ACTIONS))
            {
                pullInfoFromHut();
                sortMobsToAttack();
                window.findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class).refreshElementPanes();
            }
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.GuardTower";
    }

    /**
     * Re-sorts the WorkOrders list according to the priorities inside the list.
     */
    private void sortMobsToAttack()
    {
        mobsToAttack.sort(Comparator.comparing(MobEntryView::getPriority, Comparator.reverseOrder()));
    }

    /**
     * Retrieve all attributes from the building to display in GUI.
     */
    private void pullInfoFromHut()
    {
        this.assignManually = building.isAssignManually();
        this.patrolManually = building.isPatrolManually();
        this.retrieveOnLowHealth = building.isRetrieveOnLowHealth();
        this.task = building.getTask();
        this.job = building.getJob();
        this.patrolTargets = building.getPatrolTargets();
        this.mobsToAttack = building.getMobsToAttack();
    }

    /**
     * Sends the changes to the server.
     */
    private void sendChangesToServer()
    {
        final int ordinal = building.getJob() == null ? -1 : job.ordinal();
        MineColonies.getNetwork().sendToServer(new GuardTaskMessage(building, ordinal, assignManually, patrolManually, retrieveOnLowHealth, task.ordinal()));
    }

    /**
     * Switches whether or not to attack a mob.
     *
     * @param button The Switch button clicked
     */
    private void switchAttackMode(@NotNull final Button button)
    {
        Label idLabel = (Label) button.getParent().getChildren().get(GUI_LIST_ELEMENT_NAME_POS);

        if (idLabel != null)
        {
            for (final MobEntryView entry : mobsToAttack)
            {
                if (entry.getName().equals(idLabel.getLabelText()))
                {
                    entry.setAttack(!entry.getAttack());
                }
            }
            MineColonies.getNetwork().sendToServer(new MobEntryChangeMessage(building, this.mobsToAttack));
            window.findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class).refreshElementPanes();
        }
    }

    /**
     * On Button click update the priority.
     *
     * @param button the clicked button.
     */
    private void updatePriority(@NotNull final Button button)
    {
        @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(GUI_LIST_ELEMENT_NAME_POS);
        final String buttonLabel = button.getID();

        for (final MobEntryView mobEntry : this.mobsToAttack)
        {
            if (mobEntry.getName().equals(idLabel.getLabelText()))
            {
                if (buttonLabel.equals(GUI_LIST_BUTTON_UP) && mobEntry.getPriority() < mobsToAttack.size())
                {
                    for (final MobEntryView mobEntryView : this.mobsToAttack)
                    {
                        if (mobEntryView.getPriority() == mobEntry.getPriority() + 1)
                        {
                            mobEntry.setPriority(mobEntry.getPriority() + 1);
                            mobEntryView.setPriority(mobEntryView.getPriority() - 1);
                            break;
                        }
                    }
                    sortMobsToAttack();
                    MineColonies.getNetwork().sendToServer(new MobEntryChangeMessage(building, this.mobsToAttack));
                    window.findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class).refreshElementPanes();
                    return;
                }
                else if (buttonLabel.equals(GUI_LIST_BUTTON_DOWN) && mobEntry.getPriority() > 1)
                {
                    for (final MobEntryView mobEntryView : this.mobsToAttack)
                    {
                        if (mobEntryView.getPriority() == mobEntry.getPriority() - 1)
                        {
                            mobEntry.setPriority(mobEntry.getPriority() - 1);
                            mobEntryView.setPriority(mobEntryView.getPriority() + 1);
                            break;
                        }
                    }
                    sortMobsToAttack();
                    MineColonies.getNetwork().sendToServer(new MobEntryChangeMessage(building, this.mobsToAttack));
                    window.findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class).refreshElementPanes();
                    return;
                }
            }
        }
    }

    /**
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        final Button buttonJob = this.findPaneOfTypeByID(GUI_BUTTON_JOB, Button.class);

        if (job != null)
        {
            buttonJob.setLabel(LanguageHandler.format(job.buttonName));
        }

        buttonJob.setEnabled(assignManually);

        this.findPaneOfTypeByID(GUI_BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(assignManually ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);
        this.findPaneOfTypeByID(GUI_BUTTON_PATROL_MODE, Button.class).setLabel(patrolManually ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);
        this.findPaneOfTypeByID(GUI_BUTTON_RETRIEVAL_MODE, Button.class).setLabel(retrieveOnLowHealth ? GUI_SWITCH_MANUAL : GUI_SWITCH_AUTO);

        if (task.equals(GuardTask.PATROL))
        {
            buttonSetTarget.setEnabled(patrolManually);

            if (patrolManually)
            {
                buttonSetTarget.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.targetPatrol"));
            }
            buttonTaskPatrol.setEnabled(false);
        }
        else if (task.equals(GuardTask.FOLLOW))
        {
            buttonTaskFollow.setEnabled(false);
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

            buttonSetTarget.hide();
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
        handleButtons();
    }

    /**
     * Sets the target for patrolling or guarding of the guard.
     */
    private void setTarget()
    {
        final EntityPlayerSP player = this.mc.player;
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
        MineColonies.getNetwork().sendToServer(new GuardRecalculateMessage(building.getColony().getID(), building.getID()));
        pullInfoFromHut();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     * @param localTask the task to execute with the scepter.
     */
    private void givePlayerScepter(final GuardTask localTask)
    {
        MineColonies.getNetwork().sendToServer(new GuardScepterMessage(localTask.ordinal(), building.getID()));
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
        handleButtons();
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
     * Switch the job.
     */
    private void switchJob()
    {
        if (building.getJob() == null)
        {
            building.setJob(GuardJob.RANGER);
        }
        else
        {
            if (building.getJob().equals(GuardJob.KNIGHT))
            {
                building.setJob(GuardJob.RANGER);
            }
            else
            {
                building.setJob(GuardJob.KNIGHT);
            }
        }
        pullInfoFromHut();
        sendChangesToServer();
    }
}
