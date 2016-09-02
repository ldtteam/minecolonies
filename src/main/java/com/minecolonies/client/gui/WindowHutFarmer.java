package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.ItemIcon;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.entity.ai.citizen.farmer.FieldView;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Window for the farmer hut
 */
public class WindowHutFarmer extends AbstractWindowWorkerBuilding<BuildingFarmer.View>
{

    private static final String BUTTON_PREV_PAGE = "prevPage";
    private static final String BUTTON_NEXT_PAGE = "nextPage";
    private static final String VIEW_PAGES       = "pages";

    private static final String HUT_FARMER_RESOURCE_SUFFIX = ":gui/windowHutFarmer.xml";
    private static final String PAGE_FIELDS                = "pageFields";
    private static final String LIST_FIELDS                = "fields";
    private static final String TAG_WORKER                 = "worker";
    private static final String TAG_DISTANCE                  = "distance";
    private static final String TAG_DIRECTION                  = "direction";

    private Button buttonPrevPage;
    private Button buttonNextPage;

    private final BuildingFarmer.View farmerBuilding;
    private List<FieldView> fields = new ArrayList<>();
    private ScrollingList   fieldList;

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
        farmerBuilding = building;
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.farmer";
    }

    /**
     * Retrieve levels from the building to display in GUI
     */
    private void pullLevelsFromHut()
    {
        fields = building.getFields();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXT_PAGE, Button.class);

        fieldList = findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class);
        fieldList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return fields.size();
            }

            @Override
            public void updateElement(int index, Pane rowPane)
            {
                FieldView field = fields.get(index);
                String distance = Integer.toString(BlockPosUtil.getDistanceSquared(field.getId(), building.getLocation()));
                String taken = field.isTaken()? "Release" : "Assign";
                rowPane.findPaneOfTypeByID(TAG_WORKER, Label.class).setLabelText(field.getOwner());
                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Label.class).setLabelText(distance);

                //todo calculate direction (North, south, west, east, north-west ....)
                rowPane.findPaneOfTypeByID(TAG_DIRECTION, Label.class).setLabelText("North");

                rowPane.findPaneOfTypeByID(TAG_WORKER, Button.class).setLabel(taken);

                //rowPane.findPaneOfTypeByID(TAG_WORKER, ItemIcon.class).setLabelText(fields.get(index).getOwner());
            }
        });
    }

    @Override
    public void onUpdate()
    {
        String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_FIELDS))
        {
            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
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
    }
}

