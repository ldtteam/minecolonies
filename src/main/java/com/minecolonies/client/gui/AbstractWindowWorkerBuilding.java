package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.network.messages.HireFireMessage;
import com.minecolonies.network.messages.RecallCitizenMessage;
import com.minecolonies.util.LanguageHandler;

/**
 * Abstract class for window for worker building
 *
 * @param <B> Class extending {@link AbstractBuildingWorker.View}
 */
public abstract class AbstractWindowWorkerBuilding<B extends AbstractBuildingWorker.View> extends AbstractWindowBuilding<B>
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_HIRE        = "hire";

    /**
     * Id of the recall button in the GUI.
     */
    private static final String BUTTON_RECALL      = "recall";

    /**
     * Id of the type label in the GUI.
     */
    private static final String LABEL_BUILDINGTYPE = "type";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME   = "workerName";

    /**
     * Id of the level label in the GUI.
     */
    private static final String LABEL_WORKERLEVEL  = "workerLevel";

    /**
     * Constructor for the window of the worker building
     *
     * @param building class extending {@link AbstractBuildingWorker.View}
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
     * If there is no worker (worker.Id == 0) => Contract someone.
     * Else => Fire the current worker.
     *
     * @param button the clicked button.
     */
    private void hireClicked(Button button)
    {
        if(building.getColony().isManualHiring())
        {
            if (building.getWorkerId() == 0)
            {
                WindowHireWorker window = new WindowHireWorker(building.getColony(), building.getLocation());
                window.open();
            }
            else
            {
                MineColonies.getNetwork().sendToServer(new HireFireMessage(building, false, 0));
                button.setLabel(LanguageHandler.format("com.minecolonies.gui.workerHuts.hire"));
            }
        }
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

    /**
     * Called when the GUI has been opened.
     */
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
            findPaneOfTypeByID(BUTTON_HIRE, Button.class).setLabel(LanguageHandler.format("com.minecolonies.gui.workerHuts.fire"));
        }
        else
        {
            findPaneOfTypeByID(BUTTON_HIRE, Button.class).setLabel(LanguageHandler.format("com.minecolonies.gui.workerHuts.hire"));
        }

        findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setText(workerName);
        findPaneOfTypeByID(LABEL_WORKERLEVEL, Label.class)
                .setText(LanguageHandler.format("com.minecolonies.gui.workerHuts.workerLevel", workerLevel));

        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setText("xxxxxxxx");
    }
}
