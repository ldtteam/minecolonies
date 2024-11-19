package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.FoodUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.RestaurantMenuModuleView;
import com.minecolonies.core.network.messages.server.colony.building.AlterRestaurantMenuItemMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.FOOD_QUALITY_TOOLTIP;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * Restaurant menu window.
 */
public class RestaurantMenuModuleWindow extends AbstractModuleWindow
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutfoodstock.xml";

    /**
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * Resource scrolling list.
     */
    private final ScrollingList menuList;

    /**
     * The matching module view to the window.
     */
    private final RestaurantMenuModuleView moduleView;

    /**
     * Resource scrolling list.
     */
    protected final ScrollingList resourceList;

    /**
     * The filter for the resource list.
     */
    private String filter = "";


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
    private int                    tick;

    /**
     * The currently selected menu.
     */
    private List<ItemStorage> menu;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param building class extending
     * @param moduleView the module view.
     */
    public RestaurantMenuModuleWindow(final IBuildingView building, final RestaurantMenuModuleView moduleView)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        menuList = this.window.findPaneOfTypeByID("resourcesstock", ScrollingList.class);
        this.moduleView = moduleView;

        registerButton(BUTTON_SWITCH, this::switchClicked);
        registerButton(STOCK_REMOVE, this::removeStock);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);

        groupedItemList = new ArrayList<>(IColonyManager.getInstance().getCompatibilityManager().getEdibles(building.getBuildingLevel() - 1));

        window.findPaneOfTypeByID(INPUT_FILTER, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });
    }

    /**
     * Remove the stock.
     *
     * @param button the button.
     */
    private void removeStock(final Button button)
    {
        final int row = menuList.getListElementIndexByPane(button);
        final ItemStorage storage = menu.get(row);
        moduleView.getMenu().remove(storage);
        Network.getNetwork().sendToServer(AlterRestaurantMenuItemMessage.removeMenuItem(buildingView, storage.getItemStack(), moduleView.getProducer().getRuntimeID()));
        updateStockList();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStockList();
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
        if (!moduleView.hasReachedLimit())
        {
            final int row = resourceList.getListElementIndexByPane(button);
            final ItemStorage storage = currentDisplayedList.get(row);

            Network.getNetwork().sendToServer(AlterRestaurantMenuItemMessage.addMenuItem(buildingView, storage.getItemStack(), moduleView.getProducer().getRuntimeID()));
            moduleView.getMenu().add(storage);
            updateStockList();

            resourceList.refreshElementPanes();
        }
    }


    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateStockList()
    {
        menu = new ArrayList<>(moduleView.getMenu());
        applySorting(menu);

        if (menu.isEmpty())
        {
            findPaneByID("warning").show();
        }
        else
        {
            findPaneByID("warning").hide();
        }

        menuList.enable();
        menuList.show();

        //Creates a dataProvider for the unemployed resourceList.
        menuList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return menu.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = menu.get(index).getItemStack().copy();

                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(resource.getHoverName());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);

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

            }
        });
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

    /**
     * Apply sorting to display list based on the scores.
     * @param displayedList list to apply sorting to.
     */
    protected void applySorting(final List<ItemStorage> displayedList)
    {
        displayedList.sort((o1, o2) -> {
            int score = o1.getItem() instanceof IMinecoloniesFoodItem foodItem ? foodItem.getTier()* -100 : -o1.getItemStack().getFoodProperties(null).getNutrition();
            int score2 = o2.getItem() instanceof IMinecoloniesFoodItem foodItem2 ? foodItem2.getTier()* -100 : -o2.getItemStack().getFoodProperties(null).getNutrition();
            return score - score2 + o1.getItemStack().getDisplayName().getString().toLowerCase(Locale.US).compareTo(o2.getItemStack().getDisplayName().getString().toLowerCase(Locale.US));
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
                resourceLabel.setText(resource.getItem().getName(resource).plainCopy());
                resourceLabel.setColors(WHITE);
                final ItemIcon itemIcon = rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class);
                itemIcon.setItem(resource);
                final boolean isInMenu  = moduleView.getMenu().contains(new ItemStorage(resource));
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


                if (moduleView.hasReachedLimit())
                {
                    switchButton.disable();
                    PaneBuilders.tooltipBuilder()
                      .append(Component.translatable(LABEL_LIMIT_REACHED))
                      .hoverPane(switchButton)
                      .build();

                }
                if (isInMenu)
                {
                    switchButton.disable();
                }
                else
                {
                    switchButton.enable();
                }
            }
        });
    }
}
