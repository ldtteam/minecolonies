package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import org.jetbrains.annotations.NotNull;

/**
 * Window for a hut name entry.
 */
public class WindowHutNameEntry extends Window implements ButtonHandler
{
    private static final String BUTTON_DONE                   = "done";
    private static final String BUTTON_CANCEL                 = "cancel";
    private static final String INPUT_NAME                    = "name";
    private static final String HUT_NAME_RESOURCE_SUFFIX = ":gui/windowhutnameentry.xml";

    private final AbstractBuildingView building;

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b {@link AbstractBuilding}
     */
    public WindowHutNameEntry(final AbstractBuildingView b)
    {
        super(Constants.MOD_ID + HUT_NAME_RESOURCE_SUFFIX);
        this.building = b;
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(INPUT_NAME, TextField.class).setText(building.getCustomName());
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
            if (!name.isEmpty())
            {
                building.setCustomName(name);
            }
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (building != null)
        {
            building.openGui(false);
        }
    }
}
