package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.ChangeDeliveryPriorityMessage;
import com.minecolonies.core.network.messages.server.colony.building.ForcePickupMessage;
import com.minecolonies.core.network.messages.server.colony.building.worker.RecallCitizenMessage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.TEXT_PICKUP_PRIORITY;
import static com.minecolonies.api.util.constant.TranslationConstants.TEXT_PICKUP_PRIORITY_NEVER;

/**
 * Abstract class for window for worker building.
 *
 * @param <B> Class extending {@link AbstractBuildingView}
 */
public abstract class AbstractWindowWorkerModuleBuilding<B extends IBuildingView> extends AbstractWindowModuleBuilding<B>
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
     * Id of the priority value label in the GUI.
     */
    private static final String LABEL_PRIO_VALUE = "prioValue";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME = "workerName";

    /**
     * Button to increase delivery prio.
     */
    private static final String BUTTON_DP_UP = "deliveryPrioUp";

    /**
     * Button to decrease delivery prio.
     */
    private static final String BUTTON_DP_DOWN = "deliveryPrioDown";

    /**
     * Button to force a pickup
     */
    private static final String BUTTON_FORCE_PICKUP = "forcePickup";

    /**
     * Current pickup priority of the building.
     */
    private int prio = building.getBuildingDmPrio();

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingView}.
     * @param resource Resource of the window.
     */
    protected AbstractWindowWorkerModuleBuilding(final B building, final String resource)
    {
        super(building, resource);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
        super.registerButton(BUTTON_DP_UP, this::deliveryPriorityUp);
        super.registerButton(BUTTON_DP_DOWN, this::deliveryPriorityDown);
        super.registerButton(BUTTON_FORCE_PICKUP, this::forcePickup);
    }

    private void updatePriorityLabel()
    {
        Component component;
        if (prio == 0)
        {
            component = Component.translatable(TEXT_PICKUP_PRIORITY)
              .append(Component.translatable(TEXT_PICKUP_PRIORITY_NEVER));
        }
        else
        {
            component = Component.translatable(TEXT_PICKUP_PRIORITY)
              .append(Component.literal(prio + "/10"));
        }
        findPaneOfTypeByID(LABEL_PRIO_VALUE, Text.class).setText(component);
    }

    private void deliveryPriorityUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, true));
        updatePriorityLabel();
    }

    private void deliveryPriorityDown()
    {
        if (prio != 0)
        {
            prio--;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, false));
        updatePriorityLabel();
    }

    private void forcePickup()
    {
        Network.getNetwork().sendToServer(new ForcePickupMessage(building));
    }

    /**
     * Action when a hire button is clicked. If there is no worker (worker.Id == 0) then Contract someone. Else then Fire the current worker.
     *
     * @param button the clicked button.
     */
    protected void hireClicked(@NotNull final Button button)
    {
        if (!building.allowsAssignment())
        {
            new WindowPreferredJob(building.getColony(), building.getPosition()).open();
            return;
        }

        new WindowHireWorker(building.getColony(), building.getPosition()).open();
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
        final List<Tuple<String, Integer>> workers = new ArrayList<>();

        for (final WorkerBuildingModuleView module : buildingView.getModuleViews(WorkerBuildingModuleView.class))
        {
            for (final int worker : module.getAssignedCitizens())
            {
                workers.add(new Tuple<>(Component.translatable(module.getJobEntry().getTranslationKey()).getString(), worker));
            }
        }

        if (findPaneByID(LIST_WORKERS) != null)
        {
            ScrollingList workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
            workerList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return workers.size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {

                    final ICitizenDataView worker = building.getColony().getCitizen(workers.get(index).getB());
                    if (worker != null)
                    {
                        rowPane.findPaneOfTypeByID(LABEL_WORKERNAME, Text.class)
                          .setText(Component.literal(Component.translatable(workers.get(index).getA()).getString() + ": " + worker.getName()));
                    }
                }
            });
        }

        updatePriorityLabel();
    }
}
