package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.controls.ButtonVanilla;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.FarmerCropTypeMessage;
import com.minecolonies.util.Log;

import java.util.Random;

/**
 * Window for the farmer hut
 */
public class WindowHutFarmer extends AbstractWindowWorkerBuilding<BuildingFarmer.View>
{
    private static final String BUTTON_WHEAT     = "wheat";
    private static final String BUTTON_POTATO    = "potato";
    private static final String BUTTON_CARROT    = "carrot";
    private static final String BUTTON_MELON     = "melon";
    private static final String BUTTON_PUMPKIN   = "pumpkin";
    private static final String WHEAT            = "wheat";
    private static final String POTATO           = "potato";
    private static final String CARROT           = "carrot";
    private static final String MELON            = "melon";
    private static final String PUMPKIN          = "pumpkin";
    private static final String BUTTON_PREV_PAGE = "prevPage";
    private static final String BUTTON_NEXT_PAGE = "nextPage";
    private static final String VIEW_PAGES       = "pages";
    private static final int    MAX_AMOUNT       = 100;

    private static final String HUT_FARMER_RESOURCE_SUFFIX = ":gui/windowHutFarmer.xml";

    private Button buttonPrevPage;
    private Button buttonNextPage;

    private Random random = new Random();

    /**
     * Constructor for the window of the farmer
     *
     * @param building {@link com.minecolonies.colony.buildings.BuildingFarmer.View}
     */
    public WindowHutFarmer(BuildingFarmer.View building)
    {
        super(building, Constants.MOD_ID + HUT_FARMER_RESOURCE_SUFFIX);
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.farmer";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        updateButtonLabels();
        findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXT_PAGE, Button.class);
    }

    /**
     * Updates the labels on the buttons
     */
    private void updateButtonLabels()
    {

    }


    /**
     * Action performed when previous button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void prevClicked(Button ignored)
    {
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
        buttonPrevPage.setEnabled(false);
        buttonNextPage.setEnabled(true);
    }


    /**
     * Action performed when next button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void nextClicked(Button ignored)
    {
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
        buttonPrevPage.setEnabled(true);
        buttonNextPage.setEnabled(false);
    }

    @Override
    public void onButtonClicked(Button button)
    {
        super.onButtonClicked(button);
        updateButtonLabels();
    }
}

