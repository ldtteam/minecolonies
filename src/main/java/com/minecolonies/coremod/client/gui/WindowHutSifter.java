package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.network.messages.server.colony.building.RemoveMinimumStockFromBuildingMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

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
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * The list of meshes.
     */
    private final ScrollingList meshList;

    /**
     * The current sifter mesh.
     */
    private ItemStorage mesh;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Constructor for the window of the sifter hut.
     *
     * @param building {@link BuildingSifter.View}.
     */
    public WindowHutSifter(final BuildingSifter.View building)
    {
        super(building, Constants.MOD_ID + SIFTER_RESOURCE_SUFFIX);
        final TextField sifterSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        meshList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        resourceList = findPaneOfTypeByID("resourcesstock", ScrollingList.class);

        registerButton(MESH_BUTTON, this::switchMesh);
        registerButton(BUTTON_SAVE, this::save);
        mesh = building.getMesh();

        final Label label = findPaneOfTypeByID("maxSifted", Label.class);
        label.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo", building.getMaxDailyQuantity()));

        sifterSettingsInput.setText(String.valueOf(building.getDailyQuantity()));

        updateResourceList();

        registerButton(STOCK_ADD, this::addStock);
        if (building.hasReachedLimit())
        {
            final ButtonImage button = findPaneOfTypeByID(STOCK_ADD, ButtonImage.class);
            button.setLabel(LanguageHandler.format(LABEL_LIMIT_REACHED));
            button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium_dark.png"));
        }

        registerButton(STOCK_REMOVE, this::removeStock);

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
                    int green = Color.getByName("green", 0);
                    resourceLabel.setColor(green, green);
                    resourceLabel.setLabelText(resource.getDisplayName().getString());
                    isSet = true;
                }
                else
                {
                    int black = Color.getByName("black", 0);
                    resourceLabel.setColor(black, black);
                    resourceLabel.setLabelText(resource.getDisplayName().getString());
                }

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);

                final Button switchButton = rowPane.findPaneOfTypeByID(MESH_BUTTON, Button.class);

                final boolean isCreative = Minecraft.getInstance().player.isCreative();

                if (isSet || (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(Minecraft.getInstance().player.inventory), stack -> stack.isItemEqual(resource))))
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
     *
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
            building.save(mesh, qty, true);
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
            building.save(mesh, qty, false);
        }
        catch (final NumberFormatException ex)
        {
            Log.getLogger().warn("Wrong input!");
        }
    }

    /**
     * Remove the stock.
     *
     * @param button the button.
     */
    private void removeStock(final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final Tuple<ItemStorage, Integer> tuple = ((BuildingSifter.View) building).getStock().get(row);
        ((BuildingSifter.View) building).getStock().remove(row);
        Network.getNetwork().sendToServer(new RemoveMinimumStockFromBuildingMessage(building, tuple.getA().getItemStack()));
        updateStockList();
    }

    /**
     * Add the stock.
     */
    private void addStock()
    {
        if (!((BuildingSifter.View) building).hasReachedLimit())
        {
            List<ItemStorage> items = building.getSievableBlocks();
            new WindowSelectRes(this, building,
              itemStack -> items.stream().anyMatch(i -> i.equals(new ItemStorage(itemStack))) ).open();
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStockList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateStockList()
    {
        resourceList.enable();
        resourceList.show();
        final List<Tuple<ItemStorage, Integer>> tempRes = new ArrayList<>(((BuildingSifter.View) building).getStock());

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempRes.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = tempRes.get(index).getA().getItemStack().copy();
                resource.setCount(resource.getMaxStackSize());

                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                resourceLabel.setLabelText(resource.getDisplayName().getString());

                final Label quantityLabel = rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Label.class);
                quantityLabel.setLabelText(String.valueOf(tempRes.get(index).getB()));

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.Sifter";
    }
}

