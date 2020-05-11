package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.PickUpPriorityState;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangeDeliveryPriorityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangePickUpPriorityStateMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.OpenCraftingGUIMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.RecallCitizenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.colony.buildings.PickUpPriorityState.*;

/**
 * Abstract class for window for worker building.
 *
 * @param <B> Class extending {@link AbstractBuildingWorker.View}
 */
public abstract class AbstractWindowWorkerBuilding<B extends AbstractBuildingWorker.View> extends AbstractWindowBuilding<B>

{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_HIRE = "hire";

    /**
     * Id of the scroll view
     */
    private static final String LIST_WORKERS = "workers";

    /**
     * Id of the recall button in the GUI.
     */
    private static final String BUTTON_RECALL = "recall";

    /**
     * Id of the priority label in the GUI.
     */
    private static final String LABEL_PRIO_LABEL = "prioLabel";

    /**
     * Id of the priority value label in the GUI.
     */
    private static final String LABEL_PRIO_VALUE = "prioValue";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME = "workerName";

    /**
     * Id of the level label in the GUI.
     */
    private static final String LABEL_WORKERLEVEL = "workerLevel";

    /**
     * Name string of the builder hut.
     */
    private static final String BUILDER_HUT_NAME = "com.minecolonies.coremod.gui.workerhuts.buildersHut";

    /**
     * Button to access the crafting grid.
     */
    private static final String BUTTON_CRAFTING = "crafting";

    /**
     * Button to access the recipe list.
     */
    private static final String BUTTON_RECIPES_LIST = "recipelist";

    /**
     * Button to increase delivery prio.
     */
    private static final String BUTTON_DP_UP = "deliveryPrioUp";

    /**
     * Button to decrease delivery prio.
     */
    private static final String BUTTON_DP_DOWN = "deliveryPrioDown";

    /**
     * Button to set delivery prio state
     */
    private static final String BUTTON_DP_STATE = "deliveryPrioState";

    /**
     * Current pickup priority of the building.
     */
    private int prio = building.getBuildingDmPrio();

    /**
     * PickUp priority state of the building.
     * Can be AUTOMATIC, STATIC, or NEVER.
     */
    private PickUpPriorityState state = building.getBuildingDmPrioState();

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View}.
     * @param resource Resource of the window.
     */
    AbstractWindowWorkerBuilding(final B building, final String resource)
    {
        super(building, resource);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
        super.registerButton(BUTTON_CRAFTING, this::craftingClicked);
        super.registerButton(BUTTON_RECIPES_LIST, this::recipeListClicked);
        super.registerButton(BUTTON_DP_UP, this::deliveryPrioUp);
        super.registerButton(BUTTON_DP_DOWN, this::deliveryPrioDown);
        super.registerButton(BUTTON_DP_STATE, this::changeDPState);

        updatePickUpButtons();
    }

    private void deliveryPrioUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, true));
        findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setLabelText(prio + "/10");
    }

    private void deliveryPrioDown()
    {
        if (prio != 1)
        {
            prio--;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, false));
        findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setLabelText(prio + "/10");
    }

    private void changeDPState()
    {
        switch (state)
        {
            case AUTOMATIC:
                state = NEVER;
                break;
            case STATIC:
                state = AUTOMATIC;
                break;
            case NEVER:
                state = STATIC;
                break;
        }

        Network.getNetwork().sendToServer(new ChangePickUpPriorityStateMessage(building, state));
        findPaneOfTypeByID(BUTTON_DP_STATE, Button.class).setLabel(LanguageHandler.format(state.toString()));

        updatePickUpButtons();
    }

    /**
     * Hides and realigns the buttons based on the selected pickup priority state
     */
    private void updatePickUpButtons()
    {
        if (state == NEVER)
        {
            findPaneOfTypeByID(LABEL_PRIO_LABEL, Label.class).setVisible(false);
            findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setVisible(false);
            findPaneOfTypeByID(BUTTON_DP_DOWN, ButtonImage.class).setVisible(false);
            findPaneOfTypeByID(BUTTON_DP_UP, ButtonImage.class).setVisible(false);
        }
        else
        {
            findPaneOfTypeByID(LABEL_PRIO_LABEL, Label.class).setVisible(true);
            findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setVisible(true);
            findPaneOfTypeByID(BUTTON_DP_DOWN, ButtonImage.class).setVisible(true);
            findPaneOfTypeByID(BUTTON_DP_UP, ButtonImage.class).setVisible(true);
        }
    }

    private void recipeListClicked()
    {
        @NotNull final WindowListRecipes window = new WindowListRecipes(building.getColony(), building.getPosition());
        window.open();
    }

    /**
     * If crafting is clicked this happens. Override if needed.
     */
    public void craftingClicked()
    {
        final BlockPos pos = building.getPosition();
        Minecraft.getInstance().player.openContainer((INamedContainerProvider) Minecraft.getInstance().world.getTileEntity(pos));
        Network.getNetwork().sendToServer(new OpenCraftingGUIMessage(building));
    }

    /**
     * Action when a hire button is clicked.
     * If there is no worker (worker.Id == 0) then Contract someone.
     * Else then Fire the current worker.
     *
     * @param button the clicked button.
     */
    protected void hireClicked(@NotNull final Button button)
    {
        if (building.getBuildingLevel() == 0 && !BUILDER_HUT_NAME.equals(getBuildingName()))
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "com.minecolonies.coremod.gui.workerhuts.level0");
            return;
        }

        @NotNull final WindowHireWorker window = new WindowHireWorker(building.getColony(), building.getPosition());
        window.open();
    }

    /**
     * Action when a recall button is clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenMessage(building));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (findPaneByID(LIST_WORKERS) != null)
        {
            ScrollingList workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
            workerList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return building.getWorkerId().size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    if (!building.getWorkerId().isEmpty())
                    {
                        final ICitizenDataView worker = building.getColony().getCitizen(building.getWorkerId().get(index));
                        if (worker != null)
                        {
                            rowPane.findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setLabelText(worker.getName());
                            rowPane.findPaneOfTypeByID(LABEL_WORKERLEVEL, Label.class)
                              .setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.workerLevel",
                                worker.getCitizenSkillHandler().getJobModifier(building)));
                        }
                    }
                }
            });
        }

        findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setLabelText(building.getBuildingDmPrio() + "/10");
        findPaneOfTypeByID(BUTTON_DP_STATE, Button.class).setLabel(LanguageHandler.format(state.toString()));
    }
}
