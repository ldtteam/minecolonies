package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.network.messages.RecallCitizenMessage;
import com.minecolonies.util.LanguageHandler;

/**
 * Abstract class for window for worker building
 *
 * @param <B> Class extending {@link com.minecolonies.colony.buildings.BuildingWorker.View}
 */
public abstract class AbstractWindowWorkerBuilding<B extends BuildingWorker.View> extends AbstractWindowSkeleton<B> implements Button.Handler
{

    private static final String BUTTON_HIRE        = "hire";
    private static final String BUTTON_RECALL      = "recall";
    private static final String LABEL_BUILDINGTYPE = "type";
    private static final String LABEL_WORKERNAME   = "workerName";
    private static final String LABEL_WORKERLEVEL  = "workerLevel";


    /**
     * Constructor for the window of the worker building
     *
     * @param building class extending {@link com.minecolonies.colony.buildings.BuildingWorker.View}
     * @param resource Resource of the window
     */
    AbstractWindowWorkerBuilding(B building, String resource)
    {
        super(building, resource);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);

    }

    /**
     * Action when a hire button is clicked.
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void hireClicked(Button ignored)
    {
        WindowHireWorker window = new WindowHireWorker(building.getColony(),building.getLocation());
        window.open();
    }

    /**
     * Action when a recall button is clicked.
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void recallClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new RecallCitizenMessage(building));
    }


    @Override
    public void onOpened()
    {
        super.onOpened();
        String workerName  = "";
        String workerLevel = "";

        if (building.getWorkerId() != 0)
        {
            CitizenData.View worker = building.getColony().getCitizen(building.getWorkerId());
            if (worker != null)
            {
                workerName = worker.getName();
                workerLevel = String.format("%d", worker.getLevel());
            }
        }

        findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setLabel(workerName);
        findPaneOfTypeByID(LABEL_WORKERLEVEL, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.workerHuts.workerLevel",
                                       workerLevel));


        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabel("xxxxxxxx");


    }
}
