package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Window for the crusher hut.
 */
public class WindowHutPlantation extends AbstractWindowWorkerBuilding<BuildingPlantation.View>
{
    /**
     * The mode button id.
     */
    private static final String BLOCK_BUTTON = "block";

    /**
     * The id of the gui.
     */
    private static final String CRUSHER_RESOURCE_SUFFIX = ":gui/windowhutplantation.xml";

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link BuildingMiner.View}.
     */
    public WindowHutPlantation(final BuildingPlantation.View building)
    {
        super(building, Constants.MOD_ID + CRUSHER_RESOURCE_SUFFIX);
        final Button crushingSettingsButton = findPaneOfTypeByID(BLOCK_BUTTON, Button.class);

        registerButton(BLOCK_BUTTON, this::switchCrushingMode);
        setupSettings(crushingSettingsButton);
    }

    /**
     * Switch the mode after clicking the button.
     *
     * @param crushingSettingsButton the clicked button.
     */
    private void switchCrushingMode(final Button crushingSettingsButton)
    {
        final List<Item> modes = building.getPhases();
        int index = building.getPhases().indexOf(building.getCurrentPhase()) + 1;

        if (index >= modes.size())
        {
            index = 0;
        }

        building.setPhase(modes.get(index));
        setupSettings(crushingSettingsButton);
    }

    /**
     * Setup the settings.
     *
     * @param crushingSettingsButton the buttons to setup.
     */
    private void setupSettings(final Button crushingSettingsButton)
    {
        if (building.getCurrentPhase() != null)
        {
            crushingSettingsButton.setLabel(new ItemStack(building.getCurrentPhase()).getDisplayName().getFormattedText());
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.plantation";
    }
}

