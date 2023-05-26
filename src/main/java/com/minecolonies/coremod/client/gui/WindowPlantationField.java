package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.tileentities.AbstractTileEntityPlantationField;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.network.messages.server.PlantationFieldBuildRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The window shown when clicking on plantation fields blocks.
 */
public class WindowPlantationField extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowplantationfield.xml";

    /**
     * The ID for the "not in colony" text.
     */
    private static final String NOT_IN_COLONY_TEXT_ID = "not-in-colony";

    /**
     * The ID of the select seed button.
     */
    private static final String PLANTS_HEADER_TEXT_ID = "plants-header";

    /**
     * ID of the plants list inside the GUI.
     */
    private static final String LIST_PLANTS_ID = "plants";

    /**
     * ID of the plants list icon items inside the GUI.
     */
    private static final String LIST_PLANTS_ICON_ID = "icon";

    /**
     * ID of the button to repair the schematic.
     */
    private static final String BUTTON_REPAIR_ID = "repair";

    /**
     * The amount of columns every row in the list of plants has.
     */
    private static final double LIST_PLANTS_COLUMN_COUNT = 4;

    /**
     * The plantation field tile entity which is at this location.
     */
    private final AbstractTileEntityPlantationField tileEntityPlantationField;

    /**
     * All the plants configured on the plantation field.
     */
    private final List<ItemStack> plants;

    /**
     * Constructor for the plantation field window.
     *
     * @param tileEntityPlantationField the tile entity which is at this location.
     */
    public WindowPlantationField(AbstractTileEntityPlantationField tileEntityPlantationField)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.tileEntityPlantationField = tileEntityPlantationField;
        this.plants = tileEntityPlantationField.getPlantationFieldTypes().stream()
                        .map(fieldType -> new ItemStack(fieldType.getModule().getItem()))
                        .toList();

        registerButton(BUTTON_REPAIR_ID, this::repairField);

        updateElementStates();
    }

    private void repairField()
    {
        close();
        new WindowBuildDecoration(tileEntityPlantationField.getBlockPos(),
          tileEntityPlantationField.getPackName(),
          tileEntityPlantationField.getBlueprintPath(),
          tileEntityPlantationField.getRotation(),
          tileEntityPlantationField.getMirror(),
          builder -> new PlantationFieldBuildRequestMessage(WorkOrderType.REPAIR,
            tileEntityPlantationField.getBlockPos(),
            tileEntityPlantationField.getPackName(),
            tileEntityPlantationField.getBlueprintPath(),
            Minecraft.getInstance().level.dimension(),
            tileEntityPlantationField.getRotation(),
            tileEntityPlantationField.getMirror(),
            builder)).open();
    }

    /**
     * Updates the states of certain additional elements, determining whether they should be enabled/visible.
     */
    private void updateElementStates()
    {
        IColonyView colonyView = getCurrentColony();

        findPaneOfTypeByID(NOT_IN_COLONY_TEXT_ID, Text.class).setVisible(colonyView == null);
        findPaneOfTypeByID(PLANTS_HEADER_TEXT_ID, Text.class).setVisible(colonyView != null);
        findPaneOfTypeByID(LIST_PLANTS_ID, ScrollingList.class).setVisible(colonyView != null);
        findPaneOfTypeByID(BUTTON_REPAIR_ID, ButtonImage.class).setVisible(colonyView != null);
    }

    /**
     * Get the current colony, if any, from the tile entity.
     *
     * @return the colony view, if exists.
     */
    @Nullable
    private IColonyView getCurrentColony()
    {
        if (tileEntityPlantationField.getCurrentColony() instanceof IColonyView colonyView)
        {
            return colonyView;
        }
        return null;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateElementStates();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        ScrollingList fieldList = findPaneOfTypeByID(LIST_PLANTS_ID, ScrollingList.class);
        fieldList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return (int) Math.ceil(plants.size() / LIST_PLANTS_COLUMN_COUNT);
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                for (int id = 1; id <= LIST_PLANTS_COLUMN_COUNT; id++)
                {
                    ItemIcon pane = rowPane.findPaneOfTypeByID(LIST_PLANTS_ICON_ID + id, ItemIcon.class);
                    final Box parent = (Box) pane.getParent();
                    parent.setLineWidth(0);

                    // Row index + item index
                    int itemIndex = ((index + 1) * id) - 1;
                    if (itemIndex < plants.size())
                    {
                        pane.setItem(plants.get(itemIndex));
                        parent.setLineWidth(1);
                    }
                }
            }
        });
    }
}