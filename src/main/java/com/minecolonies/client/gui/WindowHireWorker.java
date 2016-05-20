package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.controls.TextField;
import com.blockout.views.ScrollingList;
import com.blockout.views.Window;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.lib.Constants;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Window for a town hall name entry
 */
public class WindowHireWorker extends Window implements Button.Handler
{
    private static final    String      BUTTON_DONE                     = "done";
    private static final    String      BUTTON_CANCEL                   = "cancel";
    private static final    String      CITIZEN_LABEL                   = "citizen";
    private static final    String      CITIZEN_LIST                    = "unemployed";

    private static final    String      TOWNHALL_NAME_RESOURCE_SUFFIX   = ":gui/windowHireWorker.xml";
    private ScrollingList citizenList;
    private List<CitizenData.View> citizens    = new ArrayList<>();
    Building.View building;

    //todo documentation
    private                 ColonyView  colony;

    /**
     * Constructor for a town hall rename entry window
     *
     * @param c         {@link ColonyView}
     */
    public WindowHireWorker(ColonyView c, BlockPos buildingId)
    {
        super(Constants.MOD_ID + TOWNHALL_NAME_RESOURCE_SUFFIX);
        this.colony = c;
        building = colony.getBuilding(buildingId);
        updateCitizens();
    }

    /**
     * Clears and resets all citizens
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(colony.getCitizens().values());
        ArrayList<CitizenData.View> list = new ArrayList<>(citizens);
        for(CitizenData.View citizen: list)
        {
            if(citizen.getWorkBuilding()!=null)
            {
                citizens.remove(citizen);
            }
        }
    }

    @Override
    public void onOpened()
    {
        //todo show attributes in the list.
        updateCitizens();
        citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return citizens.size();
            }

            @Override
            public void updateElement(int index, Pane rowPane)
            {

                CitizenData.View citizen = citizens.get(index);

                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabel(citizen.getName());

            }
        });
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            //todo check which citizen has been chosen and assign him to the job.
            Label lname = (Label)button.getParent().getChildren().get(0);

            //todo send message to server
            //setWorker(joblessCitizen);
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            building.openGui();
        }
    }
}
