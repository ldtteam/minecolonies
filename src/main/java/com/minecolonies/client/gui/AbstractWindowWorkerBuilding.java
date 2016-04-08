package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.network.messages.RecallCitizenMessage;
import com.minecolonies.util.LanguageHandler;

public abstract class AbstractWindowWorkerBuilding<B extends BuildingWorker.View> extends Window implements Button.Handler
{
    private static final String      BUTTON_INVENTORY      = "inventory";
    private static final String      BUTTON_HIRE           = "hire";
    private static final String      BUTTON_RECALL         = "recall";
    private static final String      BUTTON_BUILD          = "build";
    private static final String      BUTTON_REPAIR         = "repair";
    private static final String      LABEL_BUILDINGNAME    = "name";
    private static final String      LABEL_BUILDINGTYPE    = "type";
    private static final String      LABEL_WORKERNAME      = "workerName";
    private static final String      LABEL_WORKERLEVEL     = "workerLevel";

    protected B building;

    AbstractWindowWorkerBuilding(B building, String resource)
    {
        super(resource);
        this.building = building;
    }

    /**
     * Returns the name of a building
     *
     * @return      Name of a building
     */
    public abstract String getBuildingName();

    @Override
    public void onOpened()
    {
        String workerName = "";
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

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_INVENTORY))
        {
            MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(building));
        }
        else if (button.getID().equals(BUTTON_HIRE))
        {
//            if(guiButton.displayString.equals(LanguageHandler.format("com.minecolonies.gui.workerHuts.hire")))
//            {
//                //TODO: hire worker
//                guiButton.displayString = LanguageHandler.format("com.minecolonies.gui.workerHuts.fire");
//            }
//            else
//            {
//                //TODO: fire worker
//                guiButton.displayString = LanguageHandler.format("com.minecolonies.gui.workerHuts.hire");
//            }
        }
        else if (button.getID().equals(BUTTON_RECALL))
        {
            MineColonies.getNetwork().sendToServer(new RecallCitizenMessage(building));
        }
        else if (button.getID().equals(BUTTON_BUILD))
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
        }
        else if (button.getID().equals(BUTTON_REPAIR))
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
        }
    }
}
