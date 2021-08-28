package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.EnchanterStationsModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Enchanter window class.
 */
public class EnchanterStationModuleWindow extends AbstractModuleWindow
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutenchanter.xml";

    /**
     * Tag of the list of workers.
     */
    private static final String LIST_WORKERS = "gatherWorkers";

    /**
     * The label of the worker.
     */
    private static final String WORKER_NAME = "workerName";

    /**
     * The stations module view.
     */
    private final EnchanterStationsModuleView module;

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
    public EnchanterStationModuleWindow(final IBuildingView building, final EnchanterStationsModuleView module)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        super.registerButton(BUTTON_SWITCH, this::switchClicked);
        this.module = module;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        selectedBuildings = module.getBuildingsToGatherFrom();
        allBuildings = buildingView.getColony().getBuildings().stream()
                         .filter(b -> b instanceof AbstractBuildingWorkerView && !(b instanceof BuildingEnchanter.View))
                         .sorted((b1, b2) -> (int) (BlockPosUtil.getDistance2D(buildingView.getPosition(), b1.getPosition()) - BlockPosUtil.getDistance2D(buildingView.getPosition(),
                           b2.getPosition())))
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
                IBuildingView bView = allBuildings.get(index);
                String text = "";
                if (bView instanceof AbstractBuildingWorkerView)
                {
                    text += bView.getCustomName().isEmpty() ? bView.getSchematicName() : bView.getCustomName();
                    text += " " + BlockPosUtil.getDistance2D(buildingView.getPosition(), bView.getPosition()) + "m";
                    rowPane.findPaneOfTypeByID(WORKER_NAME, Text.class).setText(text);
                    final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);
                    if (selectedBuildings.contains(bView.getID()))
                    {
                        switchButton.setText(new TranslatableComponent(ON));
                    }
                    else
                    {
                        switchButton.setText(new TranslatableComponent(OFF));
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
        if (button.getTextAsString().equals(LanguageHandler.format(OFF)))
        {
            button.setText(new TranslatableComponent(ON));
            module.addWorker(allBuildings.get(row).getID());
        }
        else
        {
            button.setText(new TranslatableComponent(OFF));
            module.removeWorker(allBuildings.get(row).getID());
        }
        selectedBuildings = module.getBuildingsToGatherFrom();
        workerList.refreshElementPanes();
    }
}
