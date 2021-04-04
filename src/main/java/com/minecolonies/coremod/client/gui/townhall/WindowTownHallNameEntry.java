package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.ColonyView;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for a town hall name entry.
 */
public class WindowTownHallNameEntry extends Window implements ButtonHandler
{
    private static final String TOWNHALL_NAME_RESOURCE_SUFFIX = ":gui/townhall/windowtownhallnameentry.xml";

    private final IColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param c {@link ColonyView}
     */
    public WindowTownHallNameEntry(final IColonyView c)
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
