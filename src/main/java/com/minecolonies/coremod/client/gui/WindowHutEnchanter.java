package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.ENCHANTER_BUILDING_NAME;
import static com.minecolonies.coremod.client.gui.ViewFilterableList.*;

/**
 * Enchanter window class.
 */
public class WindowHutEnchanter extends AbstractWindowWorkerBuilding<BuildingEnchanter.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutenchanter.xml";

    /**
     * Tag of the list of workers.
     */
    private static final String LIST_WORKERS = "gatherWorkers";

    /**
     * The label of the worker.
     */
    private static final String WORKER_NAME = "workerName";

    /**
     * The actual list element of the workers.
     */
    private ScrollingList workerList;

    /**
     * The list of already selected buildings.
     */
    private List<BlockPos> selectedBuildings;

    /**
     * All buildings in the colony with worker.
     */
    private List<IBuildingView> allBuildings;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutEnchanter(final BuildingEnchanter.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        super.registerButton(BUTTON_SWITCH, this::switchClicked);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        selectedBuildings = building.getBuildingsToGatherFrom();
        allBuildings = building.getColony().getBuildings().stream()
                                                   .filter(b -> b instanceof AbstractBuildingWorker.View && !(b instanceof BuildingEnchanter.View))
                                                   .sorted((b1, b2) -> (int) (BlockPosUtil.getDistance2D(building.getPosition(), b1.getPosition()) - BlockPosUtil.getDistance2D(building.getPosition(), b2.getPosition())))
                                                   .collect(Collectors.toList());
        workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
        workerList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return allBuildings.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                IBuildingView buildingView = allBuildings.get(index);
                String text = "";
                if (buildingView instanceof AbstractBuildingWorker.View)
                {
                    text += buildingView.getCustomName().isEmpty() ? buildingView.getSchematicName() : buildingView.getCustomName();
                    text += " " + BlockPosUtil.getDistance2D(building.getPosition(), buildingView.getPosition()) + "m";
                    rowPane.findPaneOfTypeByID(WORKER_NAME, Label.class).setLabelText(text);
                    final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);
                    if (selectedBuildings.contains(buildingView.getID()))
                    {
                        switchButton.setLabel(ON);
                    }
                    else
                    {
                        switchButton.setLabel(OFF);
                    }
                }
            }
        });
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void switchClicked(@NotNull final Button button)
    {
        final int row = workerList.getListElementIndexByPane(button);
        if (button.getLabel().equals(OFF))
        {
            button.setLabel(ON);
            building.addWorker(allBuildings.get(row).getID());
        }
        else
        {
            button.setLabel(OFF);
            building.removeWorker(allBuildings.get(row).getID());
        }
        selectedBuildings = building.getBuildingsToGatherFrom();
        workerList.refreshElementPanes();
    }

    @Override
    public String getBuildingName()
    {
        return ENCHANTER_BUILDING_NAME;
    }
}
