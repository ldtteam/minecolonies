package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModuleView;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * Window for all the filterable lists.
 */
public class ItemListModuleWindow extends AbstractModuleWindow<ItemListModuleView>
{
    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

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
    private final boolean isInverted;

    /**
     * Grouped list that can be further filtered.
     */
    private List<ItemStorage> groupedItemList = new ArrayList<>();

    /**
     * Grouped list after applying the current temporary filter.
     */
    private final List<ItemStorage> currentDisplayedList = new ArrayList<>();

    /**
     * @param building   the building it belongs to.
     * @param id         the id of this window (page order of filterable lists).
     * @param desc       the description on the top of the page.
     * @param allItems   all items to display in this item list.
     * @param isInverted if the list is inverted.
     */
    public ItemListModuleWindow(
      final String res,
      final IBuildingView building,
      final String id,
      final String desc,
      final Function<IBuildingView, Set<ItemStorage>> allItems,
      final boolean isInverted)
    {
        super(building, res);
        this.id = id;

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(desc);
        this.building = building;
        this.isInverted = isInverted;

        groupedItemList = new ArrayList<>(allItems.apply(building));
    }


    @Override
    public Pane getIcon()
    {
        final ItemIcon icon = new ItemIcon();
        icon.setItem(representativeItem);
        return icon;
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);
        if (Objects.equals(button.getID(), BUTTON_SWITCH))
        {
            switchClicked(button);
        }
    }

    @Override
    public void onOpened()
    {
        updateResources();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        filter = window.findPaneOfTypeByID(INPUT_FILTER, TextField.class).getText();
        updateResources();
        return result;
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void switchClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        if (button.getTextAsString().equals(ON))
        {
            button.setText(OFF);
            if (isInverted)
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.addItem(currentDisplayedList.get(row)));
            }
            else
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.removeItem(currentDisplayedList.get(row)));
            }
        }
        else
        {
            button.setText(ON);
            if (isInverted)
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.removeItem(currentDisplayedList.get(row)));
            }
            else
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.addItem(currentDisplayedList.get(row)));
            }
        }
        resourceList.refreshElementPanes();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final Predicate<ItemStack> filterPredicate = stack -> filter.isEmpty()
                                                                || stack.getTranslationKey().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                || stack.getDisplayName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
        currentDisplayedList.clear();
        for (final ItemStorage storage : groupedItemList)
        {
            if (filterPredicate.test(storage.getItemStack()))
            {
                currentDisplayedList.add(storage);
            }
        }

        currentDisplayedList.sort((o1, o2) -> {

            boolean o1Allowed = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id))
                                  .map(m -> m.isAllowedItem(o1)).orElse(false);

            boolean o2Allowed = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id))
                                  .map(m -> m.isAllowedItem(o2)).orElse(false);

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

        updateResourceList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
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
                resourceLabel.setText(resource.getDisplayName());
                resourceLabel.setColors(WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
                final boolean isAllowedItem  = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).map(m -> m.isAllowedItem(new ItemStorage(resource))).orElse(!isInverted);
                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);

                if ((isInverted && !isAllowedItem) || (!isInverted && isAllowedItem))
                {
                    switchButton.setText(ON);
                }
                else
                {
                    switchButton.setText(OFF);
                }
            }
        });
    }
}

