package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.buildings.BuildingFarmer;
import com.minecolonies.coremod.entity.ai.citizen.farmer.FieldView;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the farmer hut.
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
    private static final String VIEW_PAGES = "pages";

    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_FARMER_RESOURCE_SUFFIX = ":gui/windowHutFarmer.xml";

    /**
     * Id of the the fields page inside the GUI.
     */
    private static final String PAGE_FIELDS = "pageFields";

    /**
     * Id of the the fields list inside the GUI.
     */
    private static final String LIST_FIELDS = "fields";

    /**
     * Id of the the worker label inside the GUI.
     */
    private static final String TAG_WORKER = "worker";

    /**
     * Id of the the distance label inside the GUI.
     */
    private static final String TAG_DISTANCE = "dist";

    /**
     * Id of the the direction label inside the GUI.
     */
    private static final String TAG_DIRECTION = "dir";

    /**
     * Id of the the assign button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGN = "assignFarm";

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
    private static final String TAG_ICON = "icon";

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
    private ScrollingList fieldList;

    /**
     * Constructor for the window of the farmer.
     *
     * @param building {@link com.minecolonies.coremod.colony.buildings.BuildingFarmer.View}.
     */
    public WindowHutFarmer(final BuildingFarmer.View building)
    {
        super(building, Constants.MOD_ID + HUT_FARMER_RESOURCE_SUFFIX);
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
        registerButton(TAG_BUTTON_ASSIGNMENT_MODE, this::assignmentModeClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void assignClicked(@NotNull final Button button)
    {
        final int row = fieldList.getListElementIndexByPane(button);
        final FieldView field = fields.get(row);


        if (button.getLabel().equals(RED_X))
        {
            button.setLabel(APPROVE);
            building.changeFields(field.getId(), false, row);
        }
        else
        {
            button.setLabel(RED_X);
            building.changeFields(field.getId(), true, row);
        }

        pullLevelsFromHut();
        window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullLevelsFromHut()
    {
        fields = building.getFields();
    }

    /**
     * Fired when the assignment mode has been toggled.
     *
     * @param button clicked button.
     */
    private void assignmentModeClicked(@NotNull final Button button)
    {
        if (button.getLabel().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            building.setAssignFieldManually(true);
        }
        else
        {
            button.setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            building.setAssignFieldManually(false);
        }
        window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (building.assignFieldManually())
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }
        else
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
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
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final FieldView field = fields.get(index);
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(field.getId(), building.getLocation())));
                final String direction = calcDirection(building.getLocation(), field.getId());
                @NotNull final String owner =
                  field.getOwner().isEmpty() ? ("<" + LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_UNUSED) + ">") : field.getOwner();

                rowPane.findPaneOfTypeByID(TAG_WORKER, Label.class).setLabelText(owner);
                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Label.class).setLabelText(distance + "m");

                rowPane.findPaneOfTypeByID(TAG_DIRECTION, Label.class).setLabelText(direction);

                final Button assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class);

                assignButton.setEnabled(building.assignFieldManually());

                if (field.isTaken())
                {
                    assignButton.setLabel(RED_X);
                }
                else
                {
                    assignButton.setLabel(APPROVE);
                    if (building.getBuildingLevel() <= building.getAmountOfFields())
                    {
                        assignButton.disable();
                    }
                }

                if (field.getItem() != null)
                {
                    rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(new ItemStack(field.getItem(), 1));
                }
            }
        });
    }

    /**
     * Calculates the direction the field is from the building.
     *
     * @param building the building.
     * @param field    the field.
     * @return a string describing the direction.
     */
    private static String calcDirection(@NotNull final BlockPos building, @NotNull final BlockPos field)
    {
        String dist = "";

        if (field.getZ() > building.getZ() + 1)
        {
            dist = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_SOUTH);
        }
        else if (field.getZ() < building.getZ() - 1)
        {
            dist = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_NORTH);
        }

        if (field.getX() > building.getX() + 1)
        {
            dist += LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_EAST);
        }
        else if (field.getX() < building.getX() - 1)
        {
            dist += LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_WEST);
        }

        return dist;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_FIELDS))
        {
            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return TILE_MINECOLONIES_BLOCK_HUT_FARMER_NAME;
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

