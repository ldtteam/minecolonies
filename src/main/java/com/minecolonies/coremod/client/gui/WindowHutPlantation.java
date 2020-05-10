package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Window for the plantation hut.
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
    private static final String PLANTATION_RESOURCE_SUFFIX = ":gui/windowhutplantation.xml";

    /**
     * Constructor for the window of the planter hut.
     *
     * @param building {@link BuildingPlantation.View}.
     */
    public WindowHutPlantation(final BuildingPlantation.View building)
    {
        super(building, Constants.MOD_ID + PLANTATION_RESOURCE_SUFFIX);
        final Button plantSettingsButton = findPaneOfTypeByID(BLOCK_BUTTON, Button.class);

        registerButton(BLOCK_BUTTON, this::switchPlantingMode);
        setupSettings(plantSettingsButton);
    }

    /**
     * Switch the mode after clicking the button.
     *
     * @param phaseSettingButton the clicked button.
     */
    private void switchPlantingMode(final Button phaseSettingButton)
    {
        final List<Item> phases = building.getPhases();
        int index = building.getPhases().indexOf(building.getCurrentPhase()) + 1;

        if (index >= phases.size())
        {
            index = 0;
        }

        building.setPhase(phases.get(index));
        setupSettings(phaseSettingButton);
    }

    /**
     * Setup the settings.
     *
     * @param plantSettingsButton the buttons to setup.
     */
    private void setupSettings(final Button plantSettingsButton)
    {
        if (building.getCurrentPhase() != null)
        {
            plantSettingsButton.setLabel(new ItemStack(building.getCurrentPhase()).getDisplayName().getFormattedText());
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.plantation";
    }
}

