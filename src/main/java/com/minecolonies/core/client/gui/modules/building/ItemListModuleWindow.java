package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.modules.IItemListModuleView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * BOWindow for all the filterable lists.
 */
public class ItemListModuleWindow extends AbstractModuleWindow<IItemListModuleView>
{
    /**
     * Resource scrolling list.
     */
    protected final ScrollingList resourceList;

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
     * @param moduleView the assigned module view.
     * @param resource   window resource location.
     */
    public ItemListModuleWindow(final IItemListModuleView moduleView, final ResourceLocation resource)
    {
        super(moduleView, resource);
        this.isInverted = moduleView.isInverted();
        this.id = moduleView.getId();

        registerButton(BUTTON_SWITCH, this::switchClicked);
        registerButton(BUTTON_RESET_DEFAULT, this::reset);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        groupedItemList = new ArrayList<>(moduleView.getAllItems().apply(buildingView));
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

        if (add)
        {
            moduleView.addItem(item);
        }
        else
        {
            moduleView.removeItem(item);
        }

        resourceList.refreshElementPanes();
    }

    /**
     * Fired when reset to default has been clicked.
     */
    private void reset()
    {
        moduleView.clearItems();
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
            boolean o1Allowed = moduleView.isAllowedItem(o1);
            boolean o2Allowed = moduleView.isAllowedItem(o2);

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
                final boolean isAllowedItem = moduleView.isAllowedItem(new ItemStorage(resource));
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

