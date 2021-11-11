package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangeDeliveryPriorityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.ForcePickupMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.RecallCitizenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
     * Name string of the builder hut.
     */
    private static final String BUILDER_HUT_NAME = "com.minecolonies.coremod.gui.workerhuts.buildersHut";

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
        super.registerButton(BUTTON_DP_UP, this::deliveryPrioUp);
        super.registerButton(BUTTON_DP_DOWN, this::deliveryPrioDown);
        super.registerButton(BUTTON_FORCE_PICKUP, this::forcePickup);
    }

    private void updatePriorityLabel()
    {
        if (prio == 0)
        {
            findPaneOfTypeByID(LABEL_PRIO_VALUE, Text.class).setText(
              LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.buildPrio") + LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.deliveryprio.never"));
        }
        else
        {
            findPaneOfTypeByID(LABEL_PRIO_VALUE, Text.class).setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.buildPrio") + prio + "/10");
        }
    }

    private void deliveryPrioUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, true));
        updatePriorityLabel();
    }

    private void deliveryPrioDown()
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
        final List<Tuple<String, Integer>> workers = new ArrayList<>();

        for (final WorkerBuildingModuleView module : buildingView.getModuleViews(WorkerBuildingModuleView.class))
        {
            for (final int worker : module.getWorkerIdList())
            {
                workers.add(new Tuple<>(new TranslationTextComponent(module.getJobEntry().getTranslationKey()).getString(), worker));
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
                          .setText(new TranslationTextComponent(workers.get(index).getA()).getString() + ": " + worker.getName());
                    }
                }
            });
        }

        updatePriorityLabel();
    }
}
