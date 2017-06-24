package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.network.messages.AssignUnassignMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowAssignCitizen extends Window implements ButtonHandler
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
     * Id of the citizen list in the GUI.
     */
    private static final String CITIZEN_LIST = "unassigned";

    /**
     * Assign button of the gui.
     */
    private static final String CITIZEN_DONE = "done";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowAssignCitizen.xml";

    /**
     * Id of the job label in the GUI.
     */
    private static final String CITIZEN_JOB = "job";

    /**
     * Contains all the citizens.
     */
    private List<CitizenDataView> citizens = new ArrayList<>();
    /**
     * The view of the current building.
     */
    private final AbstractBuilding.View building;

    /**
     * List of citizens which can be assigned.
     */
    private final ScrollingList citizenList;

    /**
     * The colony.
     */
    private final ColonyView colony;

    /**
     * Constructor for the window when the player wants to assign a worker for a certain home building.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowAssignCitizen(final ColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.colony = c;
        building = colony.getBuilding(buildingId);
        citizenList = findPaneOfTypeByID(CITIZEN_LIST, ScrollingList.class);
        updateCitizens();
    }

    /**
     * Clears and resets/updates all citizens.
     */
    private void updateCitizens()
    {
        citizens.clear();
        citizens.addAll(colony.getCitizens().values());

        //Removes all citizens which already have a job.
        citizens = colony.getCitizens().values().stream()
                     .filter(citizen -> citizen.getHomeBuilding() == null)
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
        citizenList.enable();
        citizenList.show();
        //Creates a dataProvider for the homeless citizenList.
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
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                @NotNull final CitizenDataView citizen = citizens.get(index);

                if (building instanceof BuildingHome.View)
                {
                    rowPane.findPaneOfTypeByID(CITIZEN_LABEL, Label.class).setLabelText(citizen.getName());
                    rowPane.findPaneOfTypeByID(CITIZEN_JOB, Label.class).setLabelText(LanguageHandler.format(citizen.getJob()));

                    final Button done = rowPane.findPaneOfTypeByID(CITIZEN_DONE, Button.class);
                    if(colony.isManualHousing())
                    {
                        done.enable();
                    }
                    else
                    {
                        done.disable();
                    }
                }
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
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final int row = citizenList.getListElementIndexByPane(button);
            final CitizenDataView data = citizens.get(row);
            if (building instanceof BuildingHome.View)
            {
                ((BuildingHome.View) building).addResident(data.getID());
            }
            MineColonies.getNetwork().sendToServer(new AssignUnassignMessage(this.building, true, data.getID()));
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
