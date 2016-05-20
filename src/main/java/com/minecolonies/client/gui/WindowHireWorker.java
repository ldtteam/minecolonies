package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.HireFireMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorker extends Window implements Button.Handler
{
    /**
     * Id of the done button in the GUI.
     */
    private static final    String      BUTTON_DONE                     = "done";

    /**
     * Id of the cancel button in the GUI.
     */
    private static final    String      BUTTON_CANCEL                   = "cancel";

    /**
     * Id of the citizen name in the GUI.
     */
    private static final    String      CITIZEN_LABEL                   = "citizen";

    /**
     * Id of the id label in the GUI.
     */
    private static final    String      ID_LABEL                        = "id";

    /**
     * Id of the citizen list in the GUI.
     */
    private static final    String      CITIZEN_LIST                    = "unemployed";

    /**
     * Id of the attributes label in the GUI..
     */
    private static final    String      ATTRIBUTES_LABEL                = "attributes";

    /**
     * Link to the xml file.
     */
    private static final    String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowHireWorker.xml";

    /**
     * Contains all the citizens.
     */
    private List<CitizenData.View> citizens    = new ArrayList<>();

    /**
     * The id of the current building.
     */
    Building.View building;

    /**
     * The colony.
     */
    private                 ColonyView  colony;

    /**
     * Constructor for a town hall rename entry window
     *
     * @param c         {@link ColonyView}
     */
    public WindowHireWorker(ColonyView c, BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.colony = c;
        building = colony.getBuilding(buildingId);
        updateCitizens();
    }

    /**
     * Clears and resets/updates all citizens
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

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        updateCitizens();
        ScrollingList citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
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

                String attributes = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength",citizen.getStrength()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma",citizen.getCharisma()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.dexterity",citizen.getDexterity()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.endurance",citizen.getEndurance()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence",citizen.getIntelligence());

                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabel(citizen.getName());
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Label.class).setLabel(attributes);
                rowPane.findPaneOfTypeByID(ID_LABEL, Label.class).setLabel("" + citizen.getID());
            }
        });
    }

    /**
     * Called when any button has been clicked.
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            Label id = (Label)button.getParent().getChildren().get(3);
            MineColonies.getNetwork().sendToServer(new HireFireMessage(this.building,true, Integer.parseInt(id.getLabel())));
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
