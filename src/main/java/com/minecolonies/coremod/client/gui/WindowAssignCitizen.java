package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.BlockPosUtil;
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
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracksTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.AssignUnassignMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowAssignCitizen extends Window implements ButtonHandler
{
    /**
     *
     * The view of the current building.
     */
    private final AbstractBuildingView building;

    /**
     * List of citizens which can be assigned.
     */
    private final ScrollingList citizenList;

    /**
     * The colony.
     */
    private final ColonyView colony;

    /**
     * Contains all the citizens.
     */
    private List<CitizenDataView> citizens = new ArrayList<>();

    /**
     * Constructor for the window when the player wants to assign a worker for a certain home building.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowAssignCitizen(final ColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + ASSIGN_CITIZEN_RESOURCE_SUFFIX);
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
                     .filter(cit -> cit.getHomeBuilding() == null || !(colony.getBuilding(cit.getHomeBuilding()) instanceof BuildingBarracksTower.View) && !cit.getHomeBuilding().equals(building.getID()))
                     .sorted(Comparator.comparing(CitizenDataView::getName)).collect(Collectors.toList());
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

                    final BlockPos work = citizen.getWorkBuilding();
                    String workString = "";
                    if (work != null)
                    {
                        workString = " " + BlockPosUtil.getDistance2D(work, building.getLocation()) + " blocks";
                    }

                    final BlockPos home = citizen.getHomeBuilding();
                    String homeString = "";
                    if (home != null)
                    {
                        if (work != null)
                        {
                            homeString = LanguageHandler.format("com.minecolonies.coremod.gui.homeHut.currently", BlockPosUtil.getDistance2D(work, home));

                        }
                        else
                        {
                            homeString = LanguageHandler.format("com.minecolonies.coremod.gui.homeHut.current", home.getX(), home.getY(), home.getZ());
                        }
                    }

                    rowPane.findPaneOfTypeByID(CITIZEN_JOB, Label.class).setLabelText(LanguageHandler.format(citizen.getJob()) + workString);
                    rowPane.findPaneOfTypeByID(CITIZEN_LIVING, Label.class).setLabelText(homeString);

                    final Button done = rowPane.findPaneOfTypeByID(CITIZEN_DONE, Button.class);
                    if (colony.isManualHousing())
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
                ((BuildingHome.View) building).addResident(data.getId());
            }
            MineColonies.getNetwork().sendToServer(new AssignUnassignMessage(this.building, true, data.getId()));
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            building.openGui(false);
        }
    }
}
