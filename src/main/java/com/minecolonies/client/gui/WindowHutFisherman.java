package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.lib.Constants;

public class WindowHutFisherman extends WindowWorkerBuilding<BuildingFisherman.View>
{
    private static String
            BUTTON_PREVPAGE = "prevPage",
            BUTTON_NEXTPAGE = "nextPage",
            VIEW_PAGES = "pages";

    Button buttonPrevPage, buttonNextPage;


    public WindowHutFisherman(BuildingFisherman.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutFisherman.xml");
    }

    public String getBuildingName() { return "com.minecolonies.gui.workerHuts.fisherman"; }


    @Override
    public void onOpened()
    {
        super.onOpened();

        updateButtonLabels();
        try
        {
            findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);
            buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
            buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        }
        catch (NullPointerException exc) {}
    }

    private void updateButtonLabels()
    {
        try
        {

        }
        catch (NullPointerException exc)
        {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(BUTTON_PREVPAGE))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
            buttonPrevPage.setEnabled(false);
            buttonNextPage.setEnabled(true);
        }
        else if (button.getID().equals(BUTTON_NEXTPAGE))
        {
            findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
            buttonPrevPage.setEnabled(true);
            buttonNextPage.setEnabled(false);
        }
        else
        {
            super.onButtonClicked(button);
        }

        updateButtonLabels();
    }
}

