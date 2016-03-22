package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.TextField;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.Constants;

public class WindowTownhallNameEntry extends Window implements Button.Handler
{
    private static final String BUTTON_DONE = "done";
    private static final String BUTTON_CANCEL = "cancel";
    private static final String INPUT_NAME = "name";
    private static final String TOWNHALL_NAME_RESOURCE_SUFFIX = ":gui/windowTownhallNameEntry.xml";

    ColonyView colony;

    public WindowTownhallNameEntry(ColonyView c)
    {
        super(Constants.MOD_ID + TOWNHALL_NAME_RESOURCE_SUFFIX);
        this.colony = c;
    }

    @Override
    public void onOpened()
    {
        try
        {
            findPaneOfTypeByID(INPUT_NAME, TextField.class).setText(colony.getName());
        }
        catch (NullPointerException exc)
        {
            MineColonies.logger.error("findPane error, report to mod authors");
        }
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
