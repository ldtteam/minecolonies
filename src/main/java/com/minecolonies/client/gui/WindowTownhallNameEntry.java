package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.TextField;
import com.blockout.views.Window;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.Constants;

/**
 * Window for a town hall name entry
 */
public class WindowTownhallNameEntry extends Window implements Button.Handler
{
    private static final    String      BUTTON_DONE                     = "done";
    private static final    String      BUTTON_CANCEL                   = "cancel";
    private static final    String      INPUT_NAME                      = "name";
    private static final    String      TOWNHALL_NAME_RESOURCE_SUFFIX   = ":gui/windowTownhallNameEntry.xml";

    private                 ColonyView  colony;

    /**
     * Constructor for a town hall rename entry window
     *
     * @param c         {@link ColonyView}
     */
    public WindowTownhallNameEntry(ColonyView c)
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
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
            if(!name.isEmpty())
            {
                colony.setName(name);
            }
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownhall() != null)
        {
            colony.getTownhall().openGui();
        }
    }
}
