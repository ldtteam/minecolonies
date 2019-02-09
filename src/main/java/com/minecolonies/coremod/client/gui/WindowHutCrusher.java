package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import net.minecraft.init.Blocks;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the crusher hut.
 */
public class WindowHutCrusher extends AbstractWindowWorkerBuilding<BuildingCrusher.View>
{
    /**
     * The mode button id.
     */
    private static final String BLOCK_BUTTON = "block";

    /**
     * The save button id.
     */
    private static final String BUTTON_SAVE = "save";

    /**
     * The id of the input field.
     */
    private static final String QTY_INPUT = "qty";

    /**
     * The id of the gui.
     */
    private static final String CRUSHER_RESOURCE_SUFFIX = ":gui/windowhutcrusher.xml";

    /**
     * The current crusher mode.
     */
    private BuildingCrusher.CrusherMode mode;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link BuildingMiner.View}.
     */
    public WindowHutCrusher(final BuildingCrusher.View building)
    {
        super(building, Constants.MOD_ID + CRUSHER_RESOURCE_SUFFIX);
        final Button crushingSettingsButton = findPaneOfTypeByID(BLOCK_BUTTON, Button.class);
        final TextField crushingSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);

        registerButton(BLOCK_BUTTON, this::switchCrushingMode);
        registerButton(BUTTON_SAVE, this::saveCrushingMode);
        mode = building.getCrusherMode().getFirst();
        crushingSettingsInput.setText(building.getCrusherMode().getSecond().toString());
        setupSettings(crushingSettingsButton);
    }

    /**
     * Save the crushing mode.
     */
    private void saveCrushingMode()
    {
        final TextField crushingSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        try
        {
            final int qty = Integer.parseInt(crushingSettingsInput.getText());
            building.setCrusherMode(mode, qty);
        }
        catch (final NumberFormatException ex)
        {
            Log.getLogger().warn("Wrong input!");
        }
    }

    /**
     * Switch the mode after clicking the button.
     *
     * @param crushingSettingsButton the clicked button.
     */
    private void switchCrushingMode(final Button crushingSettingsButton)
    {
        if (crushingSettingsButton.getLabel().equals(Blocks.GRAVEL.getLocalizedName()))
        {
            this.mode = BuildingCrusher.CrusherMode.GRAVEL;
        }
        else if (crushingSettingsButton.getLabel().equals(Blocks.SAND.getLocalizedName()))
        {
            this.mode = BuildingCrusher.CrusherMode.SAND;
        }
        else
        {
            this.mode = BuildingCrusher.CrusherMode.CLAY;
        }
        setupSettings(crushingSettingsButton);
    }

    /**
     * Setup the settings.
     *
     * @param crushingSettingsButton the buttons to setup.
     */
    private void setupSettings(final Button crushingSettingsButton)
    {
        if (this.mode == BuildingCrusher.CrusherMode.GRAVEL)
        {
            crushingSettingsButton.setLabel(Blocks.GRAVEL.getLocalizedName());
        }
        else if (this.mode == BuildingCrusher.CrusherMode.SAND)
        {
            crushingSettingsButton.setLabel(Blocks.SAND.getLocalizedName());
        }
        else
        {
            crushingSettingsButton.setLabel(Blocks.CLAY.getLocalizedName());
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.Crusher";
    }
}

