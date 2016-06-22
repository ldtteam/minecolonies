package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.HireFireMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorker extends Window implements Button.Handler
{
    /**
     * Id of the done button in the GUI.
     */
    private static final String BUTTON_DONE = "done";

    /**
     * Id of the cancel button in the GUI.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * Id of the citizen name in the GUI.
     */
    private static final String CITIZEN_LABEL = "citizen";

    /**
     * Id of the id label in the GUI.
     */
    private static final String ID_LABEL = "id";

    /**
     * Id of the citizen list in the GUI.
     */
    private static final String CITIZEN_LIST = "unemployed";

    /**
     * Id of the attributes label in the GUI..
     */
    private static final String ATTRIBUTES_LABEL = "attributes";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowHireWorker.xml";

    /**
     * Position of the id label of each citizen in the list.
     */
    private static final int CITIZEN_ID_LABEL_POSITION = 3;

    /**
     * Contains all the citizens.
     */
    private List<CitizenData.View> citizens = new ArrayList<>();

    /**
     * The view of the current building.
     */
    private AbstractBuilding.View building;

    /**
     * The colony.
     */
    private                 ColonyView  colony;

    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     * @param c the colony view
     * @param buildingId the building position
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

        //Removes all citizens which already have a job.
        citizens = colony.getCitizens().values().stream()
                .filter(citizen -> citizen.getWorkBuilding()==null)
                .collect(Collectors.toList());
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
        citizenList.enable();
        citizenList.show();
        //Creates a dataProvider for the unemployed citizenList.
        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return citizens.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element
             * @param rowPane the parent Pane for the row, containing the elements to update
             */
            @Override
            public void updateElement(int index, Pane rowPane)
            {
                CitizenData.View citizen = citizens.get(index);

                //Creates the list of attributes for each citizen
                String attributes = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength",citizen.getStrength()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma",citizen.getCharisma()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.dexterity",citizen.getDexterity()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.endurance",citizen.getEndurance()) + " " +
                        LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence",citizen.getIntelligence());

                rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setText(citizen.getName());
                rowPane.findPaneOfTypeByID(ATTRIBUTES_LABEL, Label.class).setText(attributes);

                //Invisible id textContent.
                rowPane.findPaneOfTypeByID(ID_LABEL, Label.class).setText(Integer.toString(citizen.getID()));
            }
        });
    }

    @Override
    public void onUpdate()
    {
        updateCitizens();
        window.findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class).refreshElementPanes();
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
            Label id = (Label)button.getParent().getChildren().get(CITIZEN_ID_LABEL_POSITION);
            MineColonies.getNetwork().sendToServer(new HireFireMessage(this.building,true, Integer.parseInt(id.getText())));
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
