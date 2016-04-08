package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.network.messages.RecallCitizenMessage;
import com.minecolonies.util.LanguageHandler;

/**
 * Abstract class for window for worker building
 *
 * @param <B> Class extending {@link com.minecolonies.colony.buildings.BuildingWorker.View}
 */
public abstract class AbstractWindowWorkerBuilding<B extends BuildingWorker.View> extends AbstractWindowSkeleton<B> implements Button.Handler
{
    private static final String BUTTON_INVENTORY   = "inventory";
    private static final String BUTTON_HIRE        = "hire";
    private static final String BUTTON_RECALL      = "recall";
    private static final String BUTTON_BUILD       = "build";
    private static final String BUTTON_REPAIR      = "repair";
    private static final String LABEL_BUILDINGNAME = "name";
    private static final String LABEL_BUILDINGTYPE = "type";
    private static final String LABEL_WORKERNAME   = "workerName";
    private static final String LABEL_WORKERLEVEL  = "workerLevel";

    /**
     * Type B is a class that extends {@link com.minecolonies.colony.buildings.BuildingWorker.View}
     */
    protected B building;

    /**
     * Constructor for the window of the worker building
     *
     * @param building class extending {@link com.minecolonies.colony.buildings.BuildingWorker.View}
     * @param resource Resource of the window
     */
    AbstractWindowWorkerBuilding(B building, String resource)
    {
        super(building, resource);
        this.building = building;
        super.registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        super.registerButton(BUTTON_HIRE, this::doNothing);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
        super.registerButton(BUTTON_BUILD, this::buildClicked);
        super.registerButton(BUTTON_REPAIR, this::repairClicked);
    }

    private void inventoryClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(building));
    }

    private void recallClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new RecallCitizenMessage(building));
    }
    private void buildClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
    }
    private void repairClicked(Button ignored)
    {
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
    }
    @Override
    public void onOpened()
    {
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

        findPaneOfTypeByID(LABEL_BUILDINGNAME, Label.class).setLabel(
                LanguageHandler.getString(getBuildingName()));
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabel("xxxxxxxx");

        if (building.getBuildingLevel() == 0)
        {
            findPaneOfTypeByID(BUTTON_BUILD, Button.class).setLabel(
                    LanguageHandler.getString("com.minecolonies.gui.workerHuts.build"));
            findPaneByID(BUTTON_REPAIR).disable();
        }
        else if (building.isBuildingMaxLevel())
        {
            Button button = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
            button.setLabel(LanguageHandler.getString("com.minecolonies.gui.workerHuts.upgradeUnavailable"));
            button.disable();
        }
    }

    /**
     * Returns the name of a building
     *
     * @return Name of a building
     */
    public abstract String getBuildingName();
}
