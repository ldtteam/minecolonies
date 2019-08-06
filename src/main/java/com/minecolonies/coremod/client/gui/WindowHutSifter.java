package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.Log;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.LIST_RESOURCES;
import static com.minecolonies.api.util.constant.WindowConstants.RESOURCE_ICON;
import static com.minecolonies.api.util.constant.WindowConstants.RESOURCE_NAME;
import static org.jline.utils.AttributedStyle.GREEN;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * Window for the sifter hut.
 */
public class WindowHutSifter extends AbstractWindowWorkerBuilding<BuildingSifter.View>
{
    /**
     * The mode button id.
     */
    private static final String BLOCK_BUTTON = "block";

    /**
     * The mode button id.
     */
    private static final String MESH_BUTTON = "buyMesh";

    /**
     * The save button id.
     */
    private static final String BUTTON_SAVE = "save";

    /**
     * The id of the input field.
     */
    private static final String QTY_INPUT = "qty";

    /**
     * The id of the gui.
     */
    private static final String SIFTER_RESOURCE_SUFFIX = ":gui/windowhutsifter.xml";

    /**
     * The list of meshes.
     */
    private final ScrollingList meshList;

    /**
     * The current sifter mesh.
     */
    private ItemStorage block;

    /**
     * The current sifter mesh.
     */
    private ItemStorage mesh;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link BuildingMiner.View}.
     */
    public WindowHutSifter(final BuildingSifter.View building)
    {
        super(building, Constants.MOD_ID + SIFTER_RESOURCE_SUFFIX);
        final Button crushingSettingsButton = findPaneOfTypeByID(BLOCK_BUTTON, Button.class);
        final TextField sifterSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        meshList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);

        registerButton(MESH_BUTTON, this::switchMesh);
        registerButton(BLOCK_BUTTON, this::switchSievableBlock);
        registerButton(BUTTON_SAVE, this::save);
        block = building.getSifterBlock();
        mesh = building.getMesh();

        sifterSettingsInput.setText(String.valueOf(building.getDailyQuantity()));
        setupSettings(crushingSettingsButton);

        updateResourceList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        meshList.enable();
        meshList.show();

        final int size = building.getBuildingLevel() - building.getMeshes().size() + 3;
        
        //Creates a dataProvider for the unemployed resourceList.
        meshList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return Math.max(Math.min(size, building.getMeshes().size()), 1);
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = building.getMeshes().get(index).getItemStack();
                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);

                boolean isSet = false;
                if (resource.isItemEqual(mesh.getItemStack()))
                {
                    resourceLabel.setColor(GREEN, GREEN);
                    resourceLabel.setLabelText("§2" + resource.getDisplayName() + "§r");
                    isSet = true;
                }
                else
                {
                    resourceLabel.setColor(WHITE, WHITE);
                    resourceLabel.setLabelText(resource.getDisplayName().getFormattedText());
                }

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);

                final Button switchButton = rowPane.findPaneOfTypeByID(MESH_BUTTON, Button.class);

                if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(Minecraft.getInstance().player.inventory), stack -> stack.isItemEqual(resource)) || isSet)
                {
                    switchButton.hide();
                }
                else
                {
                    switchButton.show();
                }
            }
        });
    }

    /**
     * Switch the mesh to a new one.
     * @param button the clicked button.
     */
    private void switchMesh(final Button button)
    {
        final int row = meshList.getListElementIndexByPane(button);
        mesh = building.getMeshes().get(row);

        final TextField crushingSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        try
        {
            final int qty = Integer.parseInt(crushingSettingsInput.getText());
            building.save(block, mesh, qty, true);
        }
        catch (final NumberFormatException ex)
        {
            Log.getLogger().warn("Wrong input!");
        }
    }

    /**
     * Save the crushing mode.
     */
    private void save()
    {
        final TextField crushingSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        try
        {
            final int qty = Integer.parseInt(crushingSettingsInput.getText());
            building.save(block, mesh, qty, false);
        }
        catch (final NumberFormatException ex)
        {
            Log.getLogger().warn("Wrong input!");
        }
    }

    /**
     * Switch the mode after clicking the button.
     *
     * @param crushingSettingsButton the clicked button.
     */
    private void switchSievableBlock(final Button crushingSettingsButton)
    {
        final List<ItemStorage> modes = building.getSievableBlocks();
        int index = modes.indexOf(this.block) + 1;

        if (index >= modes.size())
        {
            index = 0;
        }

        this.block = modes.get(index);
        setupSettings(crushingSettingsButton);
    }

    /**
     * Setup the settings.
     *
     * @param crushingSettingsButton the buttons to setup.
     */
    private void setupSettings(final Button crushingSettingsButton)
    {
        crushingSettingsButton.setLabel(this.block.getItemStack().getDisplayName().getFormattedText());
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.Sifter";
    }
}

