package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
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
        resourceList = findPaneOfTypeByID("resourcesstock", ScrollingList.class);

        registerButton(BUTTON_SAVE, this::save);

        final ButtonImage saveButton = findPaneOfTypeByID(BUTTON_SAVE, ButtonImage.class);
        saveButton.setVisible(false);

        final Text label = findPaneOfTypeByID("maxSifted", Text.class);
        if (building.getMaxDailyQuantity() == Integer.MAX_VALUE)
        {
            label.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo.unlimited"));
        }
        else
        {
            label.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.workerhuts.sifterinfo", building.getMaxDailyQuantity()));
        }

        sifterSettingsInput.setText(String.valueOf(building.getCurrentDailyQuantity()));
        sifterSettingsInput.setEnabled(false);

        registerButton(STOCK_ADD, this::addStock);
        if (building.hasReachedLimit())
        {
            final ButtonImage button = findPaneOfTypeByID(STOCK_ADD, ButtonImage.class);
            button.setText(LanguageHandler.format(LABEL_LIMIT_REACHED));
            button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium_dark.png"));
        }

        registerButton(STOCK_REMOVE, this::removeStock);

    }

    /**
     * Save the sifting mode.
     */
    private void save()
    {
        final TextField crushingSettingsInput = findPaneOfTypeByID(QTY_INPUT, TextField.class);
        try
        {
            final int qty = Integer.parseInt(crushingSettingsInput.getText());
            building.save(qty);
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

                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(resource.getDisplayName());
                rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Text.class).setText(String.valueOf(tempRes.get(index).getB()));
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

