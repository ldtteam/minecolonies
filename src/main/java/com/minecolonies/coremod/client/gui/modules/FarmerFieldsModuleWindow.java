package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.FarmerFieldModuleView;
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
public class FarmerFieldsModuleWindow extends AbstractModuleWindow
{
    /**
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_FARMER_RESOURCE_SUFFIX = ":gui/layouthuts/layoutfarmerfields.xml";

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
     * The farmer module view.
     */
    private final FarmerFieldModuleView moduleView;

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
    private final ClientWorld world = Minecraft.getInstance().level;

    /**
     * Constructor for the window of the farmer.
     *
     * @param moduleView {@link FarmerFieldModuleView}.
     */
    public FarmerFieldsModuleWindow(final IBuildingView building, final FarmerFieldModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_FARMER_RESOURCE_SUFFIX);
        registerButton(TAG_BUTTON_ASSIGNMENT_MODE, this::assignmentModeClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
        this.moduleView = moduleView;
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
        final TileEntity entity = world.getBlockEntity(field);
        if (entity instanceof ScarecrowTileEntity)
        {
            if (button.getTextAsString().equals(RED_X))
            {
                button.setText(APPROVE);
                moduleView.changeFields(field, false, (ScarecrowTileEntity) entity);
            }
            else
            {
                button.setText(RED_X);
                moduleView.changeFields(field, true, (ScarecrowTileEntity) entity);
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
        fields = moduleView.getFields();
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
            moduleView.setAssignFieldManually(true);
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            moduleView.setAssignFieldManually(false);
        }
        window.findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class).refreshElementPanes();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (moduleView.assignFieldManually())
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }
        else
        {
            findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
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
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(field, buildingView.getPosition())));
                final String direction = BlockPosUtil.calcDirection(buildingView.getPosition(), field);
                final TileEntity entity = world.getBlockEntity(field);
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

                    assignButton.setEnabled(moduleView.assignFieldManually());

                    if (((ScarecrowTileEntity) entity).isTaken())
                    {
                        assignButton.setText(RED_X);
                    }
                    else
                    {
                        assignButton.setText(APPROVE);
                        if (buildingView.getBuildingLevel() <= moduleView.getAmountOfFields())
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

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        pullLevelsFromHut();
    }
}

