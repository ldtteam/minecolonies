package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.modules.IItemListModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.ItemListModuleView;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * BOWindow for all the filterable lists.
 */
public class ItemListModuleWindow extends AbstractModuleWindow
{
    /**
     * Resource scrolling list.
     */
    protected final ScrollingList resourceList;

    /**
     * The building this belongs to.
     */
    protected final IBuildingView building;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Check for inversion of the list.
     */
    protected final boolean isInverted;

    /**
     * Grouped list that can be further filtered.
     */
    protected List<ItemStorage> groupedItemList;

    /**
     * Grouped list after applying the current temporary filter.
     */
    protected final List<ItemStorage> currentDisplayedList = new ArrayList<>();

    /**
     * Update delay.
     */
    private int tick;

    /**
     * @param building   the building it belongs to.
     * @param res   the building res id.
     * @param moduleView   the assigned module view.
     */
    public ItemListModuleWindow(
      final String res,
      final IBuildingView building,
      final IItemListModuleView moduleView)
    {
        super(building, res);

        registerButton(BUTTON_SWITCH, this::switchClicked);
        registerButton(BUTTON_RESET_DEFAULT, this::reset);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(Component.translatable(moduleView.getDesc().toLowerCase(Locale.US)));
        this.building = building;
        this.isInverted = moduleView.isInverted();
        this.id = moduleView.getId();

        groupedItemList = new ArrayList<>(moduleView.getAllItems().apply(building));

        window.findPaneOfTypeByID(INPUT_FILTER, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });
    }


    @Override
    public void onOpened()
    {
        updateResources();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (tick > 0 && --tick == 0)
        {
            updateResources();
        }
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void switchClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final ItemStorage item = currentDisplayedList.get(row);
        final boolean on = button.getText().equals(Component.translatable(ON));
        final boolean add = (on && isInverted) || (!on && !isInverted);
        final IItemListModuleView module = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id));

        if (add)
        {
            module.addItem(item);
        }
        else
        {
            module.removeItem(item);
        }

        resourceList.refreshElementPanes();
    }

    /**
     * Fired when reset to default has been clicked.
     */
    private void reset()
    {
        final IItemListModuleView module = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id));

        module.clearItems();

        resourceList.refreshElementPanes();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final Predicate<ItemStack> filterPredicate = stack -> filter.isEmpty()
                                                                || stack.getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                || stack.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
        currentDisplayedList.clear();
        for (final ItemStorage storage : groupedItemList)
        {
            if (filterPredicate.test(storage.getItemStack()))
            {
                currentDisplayedList.add(storage);
            }
        }

        applySorting(currentDisplayedList);

        updateResourceList();
    }

    protected void applySorting(final List<ItemStorage> displayedList)
    {
        displayedList.sort((o1, o2) -> {

            boolean o1Allowed = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(o1);

            boolean o2Allowed = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(o2);

            if(!o1Allowed && o2Allowed)
            {
                return isInverted ? -1 : 1;
            }
            else if(o1Allowed && !o2Allowed)
            {
                return isInverted ? 1 : -1;
            }
            else
            {
                return 0;
            }
        });
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    protected void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();

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
                return currentDisplayedList.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = currentDisplayedList.get(index).getItemStack();
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                resourceLabel.setText(resource.getHoverName());
                resourceLabel.setColors(WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
                final boolean isAllowedItem  = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(new ItemStorage(resource));
                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);

                if ((isInverted && !isAllowedItem) || (!isInverted && isAllowedItem))
                {
                    switchButton.setText(Component.translatable(ON));
                }
                else
                {
                    switchButton.setText(Component.translatable(OFF));
                }
            }
        });
    }
}

