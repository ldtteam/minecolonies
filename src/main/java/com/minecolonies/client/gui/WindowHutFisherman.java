package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.lib.Constants;

public class WindowHutFisherman extends WindowWorkerBuilding<BuildingFisherman.View>
{
    private static String BUTTON_PREVPAGE = "prevPage";
    private static String BUTTON_NEXTPAGE = "nextPage";
    private static String VIEW_PAGES      = "pages";

    Button buttonPrevPage;
    Button buttonNextPage;


    public WindowHutFisherman(BuildingFisherman.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutFisherman.xml");
    }

    public String getBuildingName(){ return "com.minecolonies.gui.workerHuts.fisherman"; }

    /**
     * Called when the Window is displayed.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);

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
    }
}

