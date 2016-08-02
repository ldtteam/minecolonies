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
        registerButton(BUTTON_WHEAT, this::wheatClicked);
        registerButton(BUTTON_POTATO, this::potatoClicked);
        registerButton(BUTTON_MELON, this::melonClicked);
        registerButton(BUTTON_PUMPKIN, this::pumpkinClicked);
        registerButton(BUTTON_CARROT, this::carrotClicked);
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
    }

    /**
     * Remove one of the materials, described by s
     * Possible inputs are:
     * - potato
     * - wheat
     * - carrot
     * - melon
     * - pumpkin
     *
     * @param s String presentation of the material to remove
     */
    private void removeOthers(String s)
    {
        while (sum() > MAX_AMOUNT)
        {
            final int numberOfProducts = 5;
            int rand = random.nextInt() * numberOfProducts;

            if (building.potato != 0 && !s.equals(POTATO) && rand == 0)
            {
                building.potato--;
            }
            else if (building.wheat != 0 && !s.equals(WHEAT) && rand == 1)
            {
                building.wheat--;
            }
            else if (building.carrot != 0 && !s.equals(CARROT) && rand == 2)
            {
                building.carrot--;
            }
            else if (building.melon != 0 && !s.equals(MELON) && rand == 3)
            {
                building.melon--;
            }
            else if (building.pumpkin != 0 && !s.equals(PUMPKIN) && rand == 4)
            {
                building.pumpkin--;
            }
        }

        if (sum() < MAX_AMOUNT)
        {
            building.wheat = building.wheat + MAX_AMOUNT - sum();
        }

        MineColonies.getNetwork().sendToServer(new FarmerCropTypeMessage(building));
    }

    /**
     * Returns the sum of all materials at the hut
     *
     * @return The sum of wheat, carrots, melons, potatoes and pumpkins.
     */
    private int sum()
    {
        return building.wheat + building.carrot + building.melon + building.potato + building.pumpkin;
    }

    /**
     * Action performed when wheat button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void wheatClicked(Button ignored)
    {
        if (building.wheat >= MAX_AMOUNT)
        {
            return;
        }
        building.wheat++;
        removeOthers(WHEAT);
    }

    /**
     * Action performed when potato button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void potatoClicked(Button ignored)
    {
        if (building.potato >= MAX_AMOUNT)
        {
            return;
        }
        building.potato++;
        removeOthers(POTATO);
    }

    /**
     * Action performed when melon button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void melonClicked(Button ignored)
    {
        if (building.melon >= MAX_AMOUNT)
        {
            return;
        }
        building.melon++;
        removeOthers(MELON);
    }


    /**
     * Action performed when pumpkin button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void pumpkinClicked(Button ignored)
    {
        if (building.pumpkin >= MAX_AMOUNT)
        {
            return;
        }
        building.pumpkin++;
        removeOthers(PUMPKIN);
    }


    /**
     * Action performed when carrot button is clicked
     *
     * @param ignored   Parameter is ignored, since some actions require a button.
     *                  This method does not
     */
    private void carrotClicked(Button ignored)
    {
        if (building.carrot >= MAX_AMOUNT)
        {
            return;
        }
        building.carrot++;
        removeOthers(CARROT);
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
        try
        {
            findPaneOfTypeByID(BUTTON_WHEAT, ButtonVanilla.class).setLabel(Integer.toString(building.wheat));
            findPaneOfTypeByID(BUTTON_POTATO, ButtonVanilla.class).setLabel(Integer.toString(building.potato));
            findPaneOfTypeByID(BUTTON_CARROT, ButtonVanilla.class).setLabel(Integer.toString(building.carrot));
            findPaneOfTypeByID(BUTTON_MELON, ButtonVanilla.class).setLabel(Integer.toString(building.melon));
            findPaneOfTypeByID(BUTTON_PUMPKIN, ButtonVanilla.class).setLabel(Integer.toString(building.pumpkin));

        }
        catch (RuntimeException exc)
        {
            Log.logger.error("findPane error, report to mod authors", exc);
        }
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

