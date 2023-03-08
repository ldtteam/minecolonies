package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.tileentities.AbstractTileEntityPlantationField;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import com.minecolonies.coremod.network.messages.server.PlantationFieldRepairMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
     * ID of the button to repair the schematic.
     */
    private static final String BUTTON_REPAIR = "repair";

    /**
     * ID of the plants list inside the GUI.
     */
    private static final String LIST_PLANTS = "plants";

    /**
     * ID of the plants list icon items inside the GUI.
     */
    private static final String LIST_PLANTS_ICON = "icon";

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
                        .map(PlantationModuleRegistry::getPlantationModule)
                        .filter(Objects::nonNull)
                        .map(module -> new ItemStack(module.getItem()))
                        .toList();

        registerButton(BUTTON_REPAIR, this::repairField);
    }

    private void repairField()
    {
        ResourceKey<Level> dimension = tileEntityPlantationField.getDimension();
        if (dimension != null)
        {
            PlantationFieldRepairMessage message = new PlantationFieldRepairMessage(
              tileEntityPlantationField.getBlockPos(),
              tileEntityPlantationField.getSchematicName()
                .substring(tileEntityPlantationField.getSchematicName().lastIndexOf("/") + 1)
                .replaceAll("\\d$", ""),
              tileEntityPlantationField.getSchematicName(),
              tileEntityPlantationField.getDimension());
            Network.getNetwork().sendToServer(message);
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        ScrollingList fieldList = findPaneOfTypeByID(LIST_PLANTS, ScrollingList.class);
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
                    ItemIcon pane = rowPane.findPaneOfTypeByID(LIST_PLANTS_ICON + id, ItemIcon.class);
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
