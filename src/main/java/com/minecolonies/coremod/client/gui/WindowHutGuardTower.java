package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildings.IGuardType;
import com.minecolonies.api.colony.buildings.registry.IGuardTypeRegistry;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.network.messages.GuardRecalculateMessage;
import com.minecolonies.coremod.network.messages.GuardScepterMessage;
import com.minecolonies.coremod.network.messages.GuardTaskMessage;
import com.minecolonies.coremod.network.messages.MobEntryChangeMessage;
import net.minecraft.client.entity.PlayerEntitySP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Our building hut view.
 *
 * @author Asherslab
 */
public class WindowHutGuardTower extends AbstractWindowWorkerBuilding<AbstractBuildingGuards.View>
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
     * The GuardTask of the guard.
     */
    private GuardTask task = GuardTask.GUARD;

    /**
     * The GuardJob of the guard.
     */
    private IGuardType job = null;

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
     * @param building class extending {@link AbstractBuildingGuards.View}.
     */
    public WindowHutGuardTower(final AbstractBuildingGuards.View building)
    {
        super(building, Constants.MOD_ID + GUI_RESOURCE);

        registerButton(GUI_BUTTON_JOB, this::switchJob);
        registerButton(GUI_BUTTON_ASSIGNMENT_MODE, this::switchAssignmentMode);
        registerButton(GUI_BUTTON_PATROL_MODE, this::switchPatrolMode);
        registerButton(GUI_BUTTON_RETRIEVAL_MODE, this::switchRetrievalMode);
        registerButton(GUI_BUTTON_SET_TARGET, this::setTarget);
        registerButton(GUI_BUTTON_RECALCULATE, this::recalculate);
        registerButton(BUTTON_GET_TOOL, this::getTool);

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

    /**
     * Give the player directly the tool.
     */
    private void getTool()
    {
        givePlayerScepter(building.getTask());
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

        final ScrollingList mobsList = findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class);
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

                if (mobsToAttack.get(index).hasAttack())
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
     * Handle the task buttons correctly.
     */
    private void handleButtons()
    {
        final Button buttonJob = this.findPaneOfTypeByID(GUI_BUTTON_JOB, Button.class);

        if (job != null)
        {
            buttonJob.setLabel(LanguageHandler.format(job.getButtonName()));
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
     * Switch the job.
     */
    private void switchJob()
    {
        if (building.getGuardType() == null)
        {
            final List<IGuardType> guardTypes = new ArrayList<>(IGuardTypeRegistry.getInstance().getRegisteredTypes().values());
            job = guardTypes.get(new Random().nextInt(guardTypes.size()));
        }
        else
        {
            final IGuardType guardType = building.getGuardType();
            final List<IGuardType> possibleGuardTypes = new ArrayList<>(IMinecoloniesAPI.getInstance().getGuardTypeRegistry().getRegisteredTypes().values());
            final int currentGuardTypeIndex = possibleGuardTypes.indexOf(guardType);
            final IGuardType nextGuardType = possibleGuardTypes.get(currentGuardTypeIndex == possibleGuardTypes.size() - 1 ? 0 : currentGuardTypeIndex + 1);

            building.setGuardType(nextGuardType);
        }
        pullInfoFromHut();
        sendChangesToServer();
    }

    /**
     * Switches whether or not to attack a mob.
     *
     * @param button The Switch button clicked
     */
    private void switchAttackMode(@NotNull final Button button)
    {
        final Label idLabel = (Label) button.getParent().getChildren().get(GUI_LIST_ELEMENT_NAME_POS);

        if (idLabel != null)
        {
            for (final MobEntryView entry : mobsToAttack)
            {
                if (entry.getName().equals(idLabel.getLabelText()))
                {
                    entry.setAttack(!entry.hasAttack());
                }
            }
            Network.getNetwork().sendToServer(new MobEntryChangeMessage(building, this.mobsToAttack));
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
                    Network.getNetwork().sendToServer(new MobEntryChangeMessage(building, this.mobsToAttack));
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
                    Network.getNetwork().sendToServer(new MobEntryChangeMessage(building, this.mobsToAttack));
                    window.findPaneOfTypeByID(GUI_ELEMENT_LIST_MOBS, ScrollingList.class).refreshElementPanes();
                    return;
                }
            }
        }
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
        this.task = building.getTask();
        this.job = building.getGuardType();
        this.patrolTargets = building.getPatrolTargets();
        this.mobsToAttack = building.getMobsToAttack();
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
        Network.getNetwork().sendToServer(new GuardRecalculateMessage(building.getColony().getID(), building));
        pullInfoFromHut();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     * @param localTask the task to execute with the scepter.
     */
    private void givePlayerScepter(final GuardTask localTask)
    {
        Network.getNetwork().sendToServer(new GuardScepterMessage(localTask.ordinal(), building.getID(), building.getColony().getID()));
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
        Network.getNetwork().sendToServer(new GuardTaskMessage(building, resourceName, assignManually, patrolManually, retrieveOnLowHealth, task.ordinal(), tightGrouping));
    }
}
