package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

public abstract class WindowWorkerBuilding<BUILDING extends BuildingWorker.View> extends Window implements Button.Handler
{
    private static String BUTTON_INVENTORY = "inventory",
            BUTTON_HIRE = "hire",
            BUTTON_RECALL = "recall",
            BUTTON_BUILD = "build",
            BUTTON_REPAIR = "repair",
            LABEL_BUILDINGNAME = "name",
            LABEL_BUILDINGTYPE = "type",
            LABEL_WORKERNAME = "workerName",
            LABEL_WORKERLEVEL = "workerLevel";

    BUILDING building;

    WindowWorkerBuilding(BUILDING building, String resource)
    {
        super(resource);
        this.building = building;
    }

    public abstract String getBuildingName();

    @Override
    public void onOpened()
    {
        String workerName = "";
        String workerLevel = "";

        if (building.getWorkerId() != null)
        {
            CitizenData.View worker = building.getColony().getCitizen(building.getWorkerId());
            if (worker != null)
            {
                workerName = worker.getName();
                workerLevel = String.format("%d", worker.getLevel());
            }
        }

        try
        {
            findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setLabel(workerName);
            findPaneOfTypeByID(LABEL_WORKERLEVEL, Label.class).setLabel(
                    LanguageHandler.format("com.minecolonies.gui.workerHuts.workerLevel",
                            workerLevel));

            findPaneOfTypeByID(LABEL_BUILDINGNAME, Label.class).setLabel(
                    LanguageHandler.getString(getBuildingName()));
            findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabel("xxxxxxxx");
        }
        catch (NullPointerException exc) {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_INVENTORY))
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(building));
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
        {}
        else if (button.getID().equals(BUTTON_BUILD))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
        }
        else if (button.getID().equals(BUTTON_REPAIR))
        {
            MineColonies.network.sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
        }
    }
}
