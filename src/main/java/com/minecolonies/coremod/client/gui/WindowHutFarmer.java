package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
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
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_FARMER_RESOURCE_SUFFIX = ":gui/windowhutfarmer.xml";

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
     * Id of the requestFert button inside the GUI.
     */
    private static final String TAG_BUTTON_REQUEST_FERTILIZER = "requestFert";

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
     * List of fields the building seeds.
     */
    private List<BlockPos> fields = new ArrayList<>();

    /**
     * ScrollList with the fields.
     */
    private ScrollingList fieldList;

    /**
     * The world.
     */
    private final ClientWorld world = Minecraft.getInstance().world;

    /**
     * Constructor for the window of the farmer.
     *
     * @param building {@link BuildingFarmer.View}.
     */
    public WindowHutFarmer(final BuildingFarmer.View building)
    {
        super(building, Constants.MOD_ID + HUT_FARMER_RESOURCE_SUFFIX);
        registerButton(TAG_BUTTON_ASSIGNMENT_MODE, this::assignmentModeClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
        registerButton(TAG_BUTTON_REQUEST_FERTILIZER, this::requestFertilizerClicked);
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void assignClicked(@NotNull final Button button)
    {
        final int row = fieldList.getListElementIndexByPane(button);
        final BlockPos field = fields.get(row);
        final TileEntity entity = world.getTileEntity(field);
        if (entity instanceof ScarecrowTileEntity)
        {
            if (button.getTextAsString().equals(RED_X))
            {
                button.setText(APPROVE);
                building.changeFields(field, false, (ScarecrowTileEntity) entity);
            }
            else
            {
                button.setText(RED_X);
                building.changeFields(field, true, (ScarecrowTileEntity) entity);
            }

            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
        }
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
        if (button.getTextAsString().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            building.setAssignFieldManually(true);
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            building.setAssignFieldManually(false);
        }
        window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Fired when the request fertilizer button has been toggled
     *
     * @param button clicked button
     */
    private void requestFertilizerClicked(@NotNull final Button button)
    {
        if (button.getTextAsString().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_REQUESTFERT_OFF)))
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_REQUESTFERT_ON));
            building.setRequestFertilizer(true);
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_REQUESTFERT_OFF));
            building.setRequestFertilizer(false);
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (building.assignFieldManually())
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }
        else
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
        }

        if (building.requestFertilizer())
        {
            findPaneOfTypeByID(TAG_BUTTON_REQUEST_FERTILIZER, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_REQUESTFERT_ON));
        }
        else
        {
            findPaneOfTypeByID(TAG_BUTTON_REQUEST_FERTILIZER, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_REQUESTFERT_OFF));
        }

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
                final BlockPos field = fields.get(index);
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(field, building.getPosition())));
                final String direction = BlockPosUtil.calcDirection(building.getPosition(), field);
                final TileEntity entity = world.getTileEntity(field);
                if (entity instanceof ScarecrowTileEntity)
                {
                    @NotNull final String owner =
                      ((ScarecrowTileEntity) entity).getOwner().isEmpty()
                        ? ("<" + LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_UNUSED) + ">")
                        : ((ScarecrowTileEntity) entity).getOwner();

                    rowPane.findPaneOfTypeByID(TAG_WORKER, Text.class).setText(owner);
                    rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(distance + "m");

                    rowPane.findPaneOfTypeByID(TAG_DIRECTION, Text.class).setText(direction);

                    final Button assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class);

                    assignButton.setEnabled(building.assignFieldManually());

                    if (((ScarecrowTileEntity) entity).isTaken())
                    {
                        assignButton.setText(RED_X);
                    }
                    else
                    {
                        assignButton.setText(APPROVE);
                        if (building.getBuildingLevel() <= building.getAmountOfFields())
                        {
                            assignButton.disable();
                        }
                    }

                    if (((ScarecrowTileEntity) entity).getSeed() != null)
                    {
                        rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(((ScarecrowTileEntity) entity).getSeed());
                    }
                }
            }
        });
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return TILE_MINECOLONIES_BLOCK_HUT_FARMER_NAME;
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
}

