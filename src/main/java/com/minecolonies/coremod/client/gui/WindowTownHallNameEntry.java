package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.colony.ColonyView;
import org.jetbrains.annotations.NotNull;

/**
 * Window for a town hall name entry.
 */
public class WindowTownHallNameEntry extends Window implements ButtonHandler
{
    private static final String BUTTON_DONE                   = "done";
    private static final String BUTTON_CANCEL                 = "cancel";
    private static final String INPUT_NAME                    = "name";
    private static final String TOWNHALL_NAME_RESOURCE_SUFFIX = ":gui/townhall/windowtownhallnameentry.xml";

    private final ColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param c {@link ColonyView}
     */
    public WindowTownHallNameEntry(final ColonyView c)
    {
        super(Constants.MOD_ID + TOWNHALL_NAME_RESOURCE_SUFFIX);
        this.colony = c;
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(INPUT_NAME, TextField.class).setText(colony.getName());
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
            if (!name.isEmpty())
            {
                colony.setName(name);
            }
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            colony.getTownHall().openGui(false);
        }
    }
}
