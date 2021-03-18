package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the sifter hut.
 */
public class WindowHutSifterModule extends AbstractWindowWorkerModuleBuilding<BuildingSifter.View>
{
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
    private static final String SIFTER_RESOURCE_SUFFIX = ":gui/windowhutsifter.xml";


    /**
     * Constructor for the window of the sifter hut.
     *
     * @param building {@link BuildingSifter.View}.
     */
    public WindowHutSifterModule(final BuildingSifter.View building)
    {
        super(building, Constants.MOD_ID + SIFTER_RESOURCE_SUFFIX);
        final TextField sifterSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);

        registerButton(BUTTON_SAVE, this::save);

        final ButtonImage saveButton = findPaneOfTypeByID(BUTTON_SAVE, ButtonImage.class);
        saveButton.setVisible(false);

        final Text label = findPaneOfTypeByID("maxSifted", Text.class);
        if (building.getMaxDailyQuantity() == Integer.MAX_VALUE)
        {
            label.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo.unlimited"));
        }
        else
        {
            label.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo", building.getMaxDailyQuantity()));
        }

        sifterSettingsInput.setText(String.valueOf(building.getCurrentDailyQuantity()));
        sifterSettingsInput.setEnabled(false);
    }

    /**
     * Save the sifting mode.
     */
    private void save()
    {
        final TextField crushingSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        try
        {
            final int qty = Integer.parseInt(crushingSettingsInput.getText());
            building.save(qty);
        }
        catch (final NumberFormatException ex)
        {
            Log.getLogger().warn("Wrong input!");
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.Sifter";
    }
}

