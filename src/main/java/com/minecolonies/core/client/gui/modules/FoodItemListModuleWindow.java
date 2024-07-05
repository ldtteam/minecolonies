package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.modules.IItemListModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.core.colony.buildings.moduleviews.ItemListModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * BOWindow for all the filterable lists.
 */
public class FoodItemListModuleWindow extends ItemListModuleWindow
{
    /**
     * @param building   the building it belongs to.
     * @param res   the building res id.
     * @param moduleView   the assigned module view.
     */
    public FoodItemListModuleWindow(
      final String res,
      final IBuildingView building,
      final IItemListModuleView moduleView)
    {
        super(res, building, moduleView);
        groupedItemList.removeIf(c -> c.getItemStack().is(ModTags.excludedFood));
    }

    @Override
    protected void applySorting(final List<ItemStorage> displayedList)
    {
        displayedList.sort((o1, o2) -> {

            int score = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(o1) ? 10 : -10;
            int score2 = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(o2) ? 10 : -10;
            score += o1.getItem() instanceof IMinecoloniesFoodItem ? -10 : 10;
            score2 += o2.getItem() instanceof IMinecoloniesFoodItem ? -10 : 10;

            return score - score2;
        });
    }

    @Override
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
                resourceLabel.setText(resource.getItem().getName(resource).plainCopy());
                resourceLabel.setColors(WHITE);
                final ItemIcon itemIcon = rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class);
                itemIcon.setItem(resource);
                final boolean isAllowedItem  = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(new ItemStorage(resource));
                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);

                if (resource.getItem() instanceof IMinecoloniesFoodItem)
                {
                    itemIcon.setPosition(16, itemIcon.getY());
                    resourceLabel.setPosition(36, resourceLabel.getY());
                    rowPane.findPaneOfTypeByID(STAR_IMAGE, Image.class).show();
                }
                else
                {
                    itemIcon.setPosition(0, itemIcon.getY());
                    rowPane.findPaneOfTypeByID(STAR_IMAGE, Image.class).hide();
                }

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
