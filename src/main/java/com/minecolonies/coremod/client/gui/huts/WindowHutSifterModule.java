package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockui.controls.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the sifter hut.
 */
public class WindowHutSifterModule extends AbstractWindowWorkerModuleBuilding<BuildingSifter.View>
{
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

        final Text label = findPaneOfTypeByID("maxSifted", Text.class);
        if (building.getMaxDailyQuantity() == Integer.MAX_VALUE)
        {
            label.setText(new TranslatableComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo.unlimited"));
        }
        else
        {
            label.setText(new TranslatableComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo", building.getMaxDailyQuantity()));
        }

        sifterSettingsInput.setText(String.valueOf(building.getCurrentDailyQuantity()));
        sifterSettingsInput.setEnabled(false);
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.Sifter";
    }
}

