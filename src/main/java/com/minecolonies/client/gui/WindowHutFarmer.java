package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.ItemIcon;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.entity.ai.citizen.farmer.FieldView;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.AssignFieldMessage;
import com.minecolonies.network.messages.AssignmentModeMessage;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.*;
import java.util.List;

/**
 * Window for the farmer hut
 */
public class WindowHutFarmer extends AbstractWindowWorkerBuilding<BuildingFarmer.View>
{

    /**
     * Button leading the player to the next page.
     */
    private static final String BUTTON_PREV_PAGE = "prevPage";

    /**
     * Button leading the player to the previous page.
     */
    private static final String BUTTON_NEXT_PAGE = "nextPage";

    /**
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES       = "pages";

    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_FARMER_RESOURCE_SUFFIX = ":gui/windowHutFarmer.xml";

    /**
     * Id of the the fields page inside the GUI.
     */
    private static final String PAGE_FIELDS                = "pageFields";

    /**
     * Id of the the fields list inside the GUI.
     */
    private static final String LIST_FIELDS                = "fields";

    /**
     * Id of the the worker label inside the GUI.
     */
    private static final String TAG_WORKER                 = "worker";

    /**
     * Id of the the distance label inside the GUI.
     */
    private static final String TAG_DISTANCE               = "dist";

    /**
     * Id of the the direction label inside the GUI.
     */
    private static final String TAG_DIRECTION              = "dir";

    /**
     * Id of the the assign button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGN       = "assignFarm";

    /**
     * Id of the the assignmentMode button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGNMENT_MODE = "assignmentMode";

    /**
     * String which displays the release of a field.
     */
    private static final String RED_X = "§n§4X";

    /**
     * String which displays adding a field.
     */
    private static final String APPROVE = "✓";

    /**
     * Id of the icon inside the GUI.
     */
    private static final String TAG_ICON         = "icon";

    /**
     * Button leading to the previous page.
     */
    private Button buttonPrevPage;

    /**
     * Button leading to the next page.
     */
    private Button buttonNextPage;

    /**
     * List of fields the building seeds.
     */
    private List<FieldView> fields = new ArrayList<>();

    /**
     * ScrollList with the fields.
     */
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
        registerButton(TAG_BUTTON_ASSIGNMENT_MODE, this::assignmentModeClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
    }

    /**
     * Fired when assign has been clicked in the field list.
     * @param button clicked button.
     */
    private void assignClicked(Button button)
    {
        final int row = fieldList.getListElementIndexByPane(button);
        final FieldView field = fields.get(row);

        if (button.getLabel().equals(RED_X))
        {
            button.setLabel(APPROVE);
            MineColonies.getNetwork().sendToServer(new AssignFieldMessage(building, false, field.getId()));
            fields.get(row).setTaken(false);
            field.setOwner("");
            building.reduceAmountOfFields(1);
        }
        else
        {
            button.setLabel(RED_X);
            MineColonies.getNetwork().sendToServer(new AssignFieldMessage(building, true, field.getId()));
            field.setTaken(true);
            field.setOwner(building.getWorkerName());
        }

        window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Fired when the assignment mode has been toggled.
     * @param button clicked button.
     */
    private void assignmentModeClicked(Button button)
    {
        if(button.getLabel().equals(LanguageHandler.format("com.minecolonies.gui.hiring.off")))
        {
            button.setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.on"));
            MineColonies.getNetwork().sendToServer(new AssignmentModeMessage(building, true));
        }
        else
        {
            button.setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.off"));
            MineColonies.getNetwork().sendToServer(new AssignmentModeMessage(building, false));
        }
        window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
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

        if(building.assignFieldManually())
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.on"));
        }
        else
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(LanguageHandler.format("com.minecolonies.gui.hiring.off"));
        }

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
                final FieldView field = fields.get(index);
                final String distance = Integer.toString((int)Math.sqrt(BlockPosUtil.getDistanceSquared(field.getId(), building.getLocation())));
                final String direction = calcDirection(building.getLocation(), field.getId());
                final String owner = field.getOwner().isEmpty() ? ("<" + LanguageHandler.format("com.minecolonies.gui.workerHuts.farmerHut.unused") + ">") : field.getOwner();

                rowPane.findPaneOfTypeByID(TAG_WORKER, Label.class).setLabelText(owner);
                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Label.class).setLabelText(distance  + "m");

                rowPane.findPaneOfTypeByID(TAG_DIRECTION, Label.class).setLabelText(direction);

                if(!building.assignFieldManually())
                {
                    rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class).disable();
                }

                if (field.isTaken())
                {
                    rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class).setLabel(RED_X);
                }
                else
                {
                    rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class).setLabel(APPROVE);
                    if(building.getBuildingLevel() <= building.getAmountOfFields())
                    {
                        rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class).disable();
                    }
                }

                if(field.getItem() != null)
                {
                    rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(new ItemStack(field.getItem(),1));
                }
            }
        });
    }

    /**
     * Calculates the direction the field is from the building.
     * @param building the building
     * @param field the field.
     * @return a string describing the direction.
     */
    private String calcDirection(BlockPos building, BlockPos field)
    {
        String dist = "";

        if(field.getZ() > building.getZ()+1)
        {
            dist = LanguageHandler.format("com.minecolonies.gui.workerHuts.farmerHut.South");
        }
        else if(field.getZ() < building.getZ()-1)
        {
            dist = LanguageHandler.format("com.minecolonies.gui.workerHuts.farmerHut.North");
        }

        if(field.getX() > building.getX()+1)
        {
            dist += LanguageHandler.format("com.minecolonies.gui.workerHuts.farmerHut.East");
        }
        else if(field.getX() < building.getX()-1)
        {
            dist += LanguageHandler.format("com.minecolonies.gui.workerHuts.farmerHut.West");
        }

        return dist;
    }

    @Override
    public void onUpdate()
    {
        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
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
}

