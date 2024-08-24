package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.modules.IItemListModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.FoodUtils;
import com.minecolonies.core.colony.buildings.moduleviews.ItemListModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.FOOD_QUALITY_TOOLTIP;
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
        groupedItemList.removeIf(c -> c.getItemStack().is(ModTags.excludedFood) || !FoodUtils.canEat(c.getItemStack(), building.getBuildingLevel() - 1));
    }

    @Override
    protected void applySorting(final List<ItemStorage> displayedList)
    {
        displayedList.sort((o1, o2) -> {
            int score = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(o1) ? 500 : -500;
            int score2 = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).isAllowedItem(o2) ? 500 : -500;
            score += o1.getItem() instanceof IMinecoloniesFoodItem foodItem ? foodItem.getTier()* -100 : -o1.getItemStack().getFoodProperties(null).getNutrition();
            score2 += o2.getItem() instanceof IMinecoloniesFoodItem foodItem2 ? foodItem2.getTier()* -100 : -o2.getItemStack().getFoodProperties(null).getNutrition();
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
                final Gradient gradient = rowPane.findPaneOfTypeByID("gradient", Gradient.class);
                if (resource.getItem() instanceof IMinecoloniesFoodItem foodItem)
                {
                   if (foodItem.getTier() == 3)
                   {
                       gradient.setGradientStart(255, 215, 0, 255);
                       gradient.setGradientEnd(255, 215, 0, 255);
                   }
                   else if (foodItem.getTier() == 2)
                   {
                       gradient.setGradientStart(211, 211, 211, 255);
                       gradient.setGradientEnd(211, 211, 211, 255);
                   }
                   else if (foodItem.getTier() == 1)
                   {
                       gradient.setGradientStart(205, 127, 50, 255);
                       gradient.setGradientEnd(205, 127, 50, 255);
                   }
                }
                else
                {
                    gradient.setGradientStart(0, 0, 0, 0);
                    gradient.setGradientEnd(0, 0, 0, 0);
                }

                PaneBuilders.tooltipBuilder()
                  .append(Component.translatable(FOOD_QUALITY_TOOLTIP, FoodUtils.getBuildingLevelForFood(resource)))
                  .hoverPane(gradient)
                  .build();


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
