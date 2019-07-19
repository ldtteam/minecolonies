package com.minecolonies.coremod.client.gui;


import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;

/**
 * GUI window for filterable lists as a list of compostable items, burnables, etc.
 */
public abstract class WindowWorkerMultiPages<B extends AbstractBuildingWorker.View> extends AbstractWindowWorkerBuilding<B>
{
    /**
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Button leading the player to the next page.
     */
    private static final String BUTTON_PREV_PAGE = "prevPage";

    /**
     * Button leading the player to the previous page.
     */
    private static final String BUTTON_NEXT_PAGE = "nextPage";

    /**
     * Button leading to the previous page.
     */
    private Button buttonPrevPage;

    /**
     * Button leading to the next page.
     */
    private Button buttonNextPage;

    /**
     * Public constructor to instantiate this window.
     *
     * @param building  the building to unselect from.
     * @param resource  the resource location of the GUI..
     */
    public WindowWorkerMultiPages(final B building, final String resource)
    {
        super(building, resource);
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);

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
