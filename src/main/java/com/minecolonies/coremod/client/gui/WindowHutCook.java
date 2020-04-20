package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.views.View;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.network.messages.RemoveMinimumStockFromBuildingMessage;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.RESOURCE_ICON;

/**
 * Cook window class. Specifies the extras the composter has for its list.
 */
public class WindowHutCook extends AbstractHutFilterableLists
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutcook.xml";

    /**
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "fuel";

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutCook(final BuildingCook.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        final ViewFilterableList win = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format(COM_MINECOLONIES_REQUESTS_BURNABLE),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, win);
        resourceList = this.window.findPaneOfTypeByID("resourcesstock", ScrollingList.class);

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
     * The classic block list.
     * @param filterPredicate the predicate filter.
     * @param id the id of the specific predicate.
     * @return the list of itemStorages.
     */
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return ImmutableList.copyOf(IColonyManager.getInstance().getCompatibilityManager().getFuel().stream().filter(item -> filterPredicate.test(item.getItemStack())).collect(Collectors.toList()));
    }

    /**
     * Remove the stock.
     * @param button the button.
     */
    private void removeStock(final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final Tuple<ItemStorage, Integer> tuple = ((BuildingCook.View) building).getStock().get(row);
        ((BuildingCook.View) building).getStock().remove(row);
        Network.getNetwork().sendToServer(new RemoveMinimumStockFromBuildingMessage(tuple.getA().getItemStack(), building.getColony().getID(), building.getID()));
        updateStockList();
    }

    /**
     * Add the stock.
     */
    private void addStock()
    {
        if (!((BuildingCook.View) building).hasReachedLimit())
        {
            new WindowSelectRes(this, building.getColony().getID(), building.getID(),
              itemStack -> ItemStackUtils.CAN_EAT.test(itemStack) || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance().getSmeltingResult(itemStack))).open();
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
        final List<Tuple<ItemStorage, Integer> > tempRes = new ArrayList<>(((BuildingCook.View) building).getStock());

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
                resourceLabel.setLabelText(resource.getDisplayName().getFormattedText());

                final Label quantityLabel = rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Label.class);
                quantityLabel.setLabelText(String.valueOf(tempRes.get(index).getB()));

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }


    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.cook";
    }
}
