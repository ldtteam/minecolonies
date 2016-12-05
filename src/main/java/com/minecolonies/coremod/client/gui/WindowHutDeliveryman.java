package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.buildings.BuildingDeliveryman;
import com.minecolonies.coremod.lib.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the farmer hut.
 */
public class WindowHutDeliveryman extends AbstractWindowWorkerBuilding<BuildingDeliveryman.View> implements Button.Handler
{
    private static final String BUTTON_PREV_PAGE = "prevPage";
    private static final String BUTTON_NEXT_PAGE = "nextPage";
    private static final String VIEW_PAGES       = "pages";

    private static final String HUT_WAREHOUSE_RESOURCE_SUFFIX = ":gui/windowHutWarehouse.xml";

    private Button buttonPrevPage;
    private Button buttonNextPage;

    /**
     * Constructor for the window of the warehouse hut.
     *
     * @param building {@link BuildingDeliveryman.View}
     */
    public WindowHutDeliveryman(@NotNull final BuildingDeliveryman.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.warehouse";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class).setEnabled(false);

        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXT_PAGE, Button.class);
    }

    /**
     * Action performed when previous button is clicked.
     */
    private void prevClicked()
    {
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
        buttonPrevPage.setEnabled(false);
        buttonNextPage.setEnabled(true);
    }

    /**
     * Action performed when next button is clicked.
     */
    private void nextClicked()
    {
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
        buttonPrevPage.setEnabled(true);
        buttonNextPage.setEnabled(false);
    }
}
