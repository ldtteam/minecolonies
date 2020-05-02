package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingQuarry;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingQuarryStation;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the home building.
 */
public class WindowHutQuarry extends AbstractWindowWorkerBuilding<BuildingQuarry.View>
{
    /**
     * The station assigned to this quarry;
     */
    private BlockPos assignedStation;

    /**
     * List of stations available in the colony
     */
    private List<BuildingQuarryStation.View> stations = new ArrayList<>();

    /**
     * ScrollList with the stations.
     */
    private ScrollingList stationList;

    /**
     * Constructor for window warehouse hut.
     *
     * @param building {@link BuildingWareHouse.View}.
     */
    public WindowHutQuarry(final BuildingQuarry.View building)
    {
        super(building, Constants.MOD_ID + QUARRY_RESOURCE_SUFFIX);
        this.assignedStation = building.getAssignedStation();
        registerButton(BUTTON_ASSIGN, this::assignClicked);
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.quarry";
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final Pane currentView = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView();
        if (currentView != null)
        {
            updateStations();
            if (currentView.getID().equals(PAGE_STATIONS))
            {
                window.findPaneOfTypeByID(LIST_STATIONS, ScrollingList.class).refreshElementPanes();
            }
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        stationList = findPaneOfTypeByID(LIST_STATIONS, ScrollingList.class);
        stationList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return stations.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final BuildingQuarryStation.View stationView = stations.get(index);
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(stationView.getPosition(), building.getPosition())));
                rowPane.findPaneOfTypeByID(LABEL_DISTANCE, Label.class).setLabelText(LanguageHandler.format(GUI_QUARRY_DISTANCE, distance));
                rowPane.findPaneOfTypeByID(LABEL_POSITION, Label.class).setLabelText(LanguageHandler.format(GUI_QUARRY_POSITION, stationView.getPosition().func_229422_x_()));

                final Button assignButton = rowPane.findPaneOfTypeByID(BUTTON_ASSIGN, Button.class);

                if (stationView.getPosition().equals(assignedStation))
                {
                    assignButton.enable();
                    assignButton.setLabel(LanguageHandler.format(GUI_STATION_ASSIGNED));
                }
                else if (stationView.isFull())
                {
                    assignButton.disable();
                    assignButton.setLabel(LanguageHandler.format(GUI_STATION_FULL));
                }
                else
                {
                    assignButton.enable();
                    assignButton.setLabel(LanguageHandler.format(GUI_STATION_ASSIGN));
                }
            }
        });
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void assignClicked(@NotNull final Button button)
    {
        final int row = stationList.getListElementIndexByPane(button);

        if (assignedStation == stations.get(row).getPosition())
        {
            assignedStation = null;
            building.setAssignedStation(null);
        }
        else
        {
            assignedStation = stations.get(row).getPosition();
            building.setAssignedStation(assignedStation);
        }

        window.findPaneOfTypeByID(LIST_STATIONS, ScrollingList.class).refreshElementPanes();
    }

    public void updateStations()
    {
        this.stations = new ArrayList<>();
        for (final IBuildingView iBuildingView : building.getColony().getBuildings())
        {
            if (iBuildingView instanceof BuildingQuarryStation.View)
            {
                this.stations.add((BuildingQuarryStation.View) iBuildingView);
            }
        }
    }
}
