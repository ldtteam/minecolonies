package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.FoodUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.RestaurantMenuModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.items.ItemLargeBottle;
import com.minecolonies.core.network.messages.server.colony.building.AlterRestaurantMenuItemMessage;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.research.util.ResearchConstants.SATURATION;
import static com.minecolonies.api.util.FoodUtils.computeSaturationConsumptionFactor;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * Restaurant menu window.
 */
public class RestaurantMenuModuleWindow extends AbstractModuleWindow<RestaurantMenuModuleView>
{
    /**
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * Resource scrolling list.
     */
    private final ScrollingList menuList;

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
    private int tick;

    /**
     * The currently selected menu.
     */
    private List<ItemStorage> menu;

    /**
     * Data about the module consumers.
     */
    private double avgCustomerConsumption = 0;
    private int    numerOfCustomers      = 0;

    /**
     * Recipe mapping cache from itemstorage to ingredients.
     */
    private static Map<ItemStorage, List<ItemStorage>> recipeMapping = new HashMap<>();

    /**
     * Constructor for the minimum stock window view.
     *
     * @param moduleView the module view.
     */
    public RestaurantMenuModuleWindow(final RestaurantMenuModuleView moduleView)
    {
        super(moduleView, new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layoutfoodstock.xml"));

        menuList = this.window.findPaneOfTypeByID("resourcesstock", ScrollingList.class);

        registerButton(BUTTON_SWITCH, this::switchClicked);
        registerButton(STOCK_REMOVE, this::removeStock);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);

        groupedItemList = new ArrayList<>(IColonyManager.getInstance().getCompatibilityManager().getEdibles(moduleView.getBuildingView().getBuildingLevel() - 1));

        window.findPaneOfTypeByID(INPUT_FILTER, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });


        if (moduleView.getBuildingView() instanceof BuildingCook.View buildingCookView)
        {
            double sum = 0;
            for (final int i : buildingCookView.getCustomers())
            {
                final ICitizenDataView citizenDataView = buildingCookView.getColony().getCitizen(i);
                if (citizenDataView != null)
                {
                    final int buildingLevel = citizenDataView.getHomeBuilding() == null ? 0 :  buildingCookView.getColony().getClientBuildingManager().getBuilding(citizenDataView.getHomeBuilding()).getBuildingLevelEquivalent();
                    sum += computeSaturationConsumptionFactor(buildingLevel);
                }
            }
            this.avgCustomerConsumption = sum/buildingCookView.getCustomers().size();
            this.numerOfCustomers = buildingCookView.getCustomers().size();
        }

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

            boolean hasGoodMinecoloniesFood = false;
            for (ItemStorage menuItem : menu)
            {
                if (menuItem.getItem() instanceof IMinecoloniesFoodItem minecoloniesFoodItem && minecoloniesFoodItem.getTier() >= 2)
                {
                    hasGoodMinecoloniesFood = true;
                    break;
                }
            }

            if (hasGoodMinecoloniesFood)
            {
                findPaneByID("poorwarning").hide();
            }
            else
            {
                findPaneByID("poorwarning").show();
            }
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
                final int tier = FoodUtils.getFoodTier(resource);
                if (tier == 3)
                {
                    gradient.setGradientStart(255, 215, 0, 255);
                    gradient.setGradientEnd(255, 215, 0, 255);
                }
                else if (tier == 2)
                {
                    gradient.setGradientStart(211, 211, 211, 255);
                    gradient.setGradientEnd(211, 211, 211, 255);
                }
                else if (tier == 1)
                {
                    gradient.setGradientStart(205, 127, 50, 255);
                    gradient.setGradientEnd(205, 127, 50, 255);
                }
                else
                {
                    gradient.setGradientStart(0, 0, 0, 0);
                    gradient.setGradientEnd(0, 0, 0, 0);
                }

                if (resource.getItem() instanceof IMinecoloniesFoodItem || tier > 0)
                {
                    PaneBuilders.tooltipBuilder()
                            .append(Component.translatable(FOOD_QUALITY_TOOLTIP, FoodUtils.getBuildingLevelForFood(resource)))
                            .hoverPane(gradient)
                            .build();
                }
                else
                {
                    PaneBuilders.tooltipBuilder()
                            .append(Component.translatable(FOOD_QUALITY_TOOLTIP, FoodUtils.getBuildingLevelForFood(resource)))
                            .appendNL(Component.translatable(VANILLA_FOOD_QUALITY_TOOLTIP))
                            .hoverPane(gradient)
                            .build();
                }
            }
        });

        final Object2DoubleMap<ItemStorage> ingredients = new Object2DoubleArrayMap<>();
        int saturationSum = 0;
        final int researchBonus = (int) buildingView.getColony().getResearchManager().getResearchEffects().getEffectStrength(SATURATION);
        for (final ItemStorage dish : menu)
        {
            final ItemStorage input = new ItemStorage(dish.getItemStack());
            final List<ItemStorage> map = getRecipeFromStack(input, mc.level);
            for (final ItemStorage ingredient : map)
            {
                ingredients.merge(ingredient, (double) ingredient.getAmount() / input.getAmount(), Double::sum);
            }
            final FoodProperties foodProperty = dish.getItem().getFoodProperties(dish.getItemStack(), null);
            saturationSum += FoodUtils.getFoodValue(dish.getItemStack(), foodProperty, researchBonus);
        }
        final double consumption = (avgCustomerConsumption * 10 * numerOfCustomers) / saturationSum;

        final ArrayList<Object2DoubleMap.Entry<ItemStorage>> ingredientList = new ArrayList<>(ingredients.object2DoubleEntrySet());
        ingredientList.sort(Comparator.comparingDouble(i -> -i.getValue()));

        //Creates a dataProvider for the unemployed resourceList.
        this.window.findPaneOfTypeByID("ingredientslist", ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return ingredientList.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Object2DoubleMap.Entry<ItemStorage> resource = ingredientList.get(index);
                final ItemStack ingredient = resource.getKey().getItemStack().copy();
                ingredient.setCount((int) resource.getDoubleValue());
                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(ingredient.getHoverName());
                final ItemIcon itemIcon = rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class);
                itemIcon.setItem(ingredient);
                PaneBuilders.tooltipBuilder()
                    .hoverPane(itemIcon)
                        .append(Component.translatable(FOOD_CONSUMPTION_TOOLTIP, (int) consumption*resource.getValue(), (int) consumption*resource.getValue()*1.5))
                        .build();
            }
        });
    }

    public static List<ItemStorage> getRecipeFromStack(final ItemStorage storage, final Level level)
    {
        if (recipeMapping.containsKey(storage))
        {
            return recipeMapping.get(storage);
        }
        else
        {
            final List<ItemStorage> set = new ArrayList<>();
            processRecipe(storage, set, 0, level);
            recipeMapping.put(storage, set);
            return set;
        }
    }

    private static boolean processRecipe(final ItemStorage dish, final List<ItemStorage> ingredients, final int depth, final Level level)
    {
        //todo find out original recipe size.
        if (depth > 10)
        {
            return false;
        }
        for (final CustomRecipe recipe : CustomRecipeManager.getInstance().getRecipeByOutput(dish))
        {
            if (depth == 0)
            {
                dish.setAmount(recipe.getPrimaryOutput().getCount());
            }
            for (final ItemStorage input : recipe.getInputs())
            {
                if (input.getItem() == Items.BOWL || input.getItem() instanceof BottleItem)
                {
                    continue;
                }
                else if (input.getItem() instanceof ItemLargeBottle
                    || input.getItem() instanceof HoneyBottleItem
                    || input.getItem() == Items.DRIED_KELP
                    || !processRecipe(input, ingredients, depth + 1, level))
                {
                    ingredients.add(input);
                }
            }
            // For now we're only interested in the first alternative.
            return true;
        }

        final Optional<? extends Recipe<?>> recipe = level.getRecipeManager().byKey(BuiltInRegistries.ITEM.getKey(dish.getItem()));
        if (recipe.isPresent())
        {
            if (depth == 0)
            {
                dish.setAmount(recipe.get().getResultItem(level.registryAccess()).getCount());
            }
            for (final Ingredient ingredient : recipe.get().getIngredients())
            {
                final ItemStack[] inputs = ingredient.getItems();
                if (inputs.length >= 1)
                {
                    final ItemStorage input = new ItemStorage(inputs[0]);
                    if (input.getItem() == Items.BOWL || input.getItem() instanceof BottleItem)
                    {
                        continue;
                    }
                    if (input.getItem() instanceof ItemLargeBottle
                        || input.getItem() instanceof HoneyBottleItem
                        || input.getItem() == Items.DRIED_KELP
                        || !processRecipe(input, ingredients, depth + 1, level))
                    {
                        ingredients.add(input);
                    }
                }
            }
            return true;
        }
        return false;
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
            int score = FoodUtils.getFoodTier(o1.getItemStack()) * -100 - o1.getItemStack().getFoodProperties(null).getNutrition();
            int score2 = FoodUtils.getFoodTier(o2.getItemStack())* -100 - o2.getItemStack().getFoodProperties(null).getNutrition();

            final int scoreComparison = Integer.compare(score, score2);
            if (scoreComparison != 0)
            {
                return scoreComparison;
            }

            return o1.getItemStack().getDisplayName().getString().toLowerCase(Locale.US).compareTo(o2.getItemStack().getDisplayName().getString().toLowerCase(Locale.US));
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
                final int tier = FoodUtils.getFoodTier(resource);
                if (tier == 3)
                {
                    gradient.setGradientStart(255, 215, 0, 255);
                    gradient.setGradientEnd(255, 215, 0, 255);
                }
                else if (tier == 2)
                {
                    gradient.setGradientStart(211, 211, 211, 255);
                    gradient.setGradientEnd(211, 211, 211, 255);
                }
                else if (tier == 1)
                {
                    gradient.setGradientStart(205, 127, 50, 255);
                    gradient.setGradientEnd(205, 127, 50, 255);
                }
                else
                {
                    gradient.setGradientStart(0, 0, 0, 0);
                    gradient.setGradientEnd(0, 0, 0, 0);
                }
                if (resource.getItem() instanceof IMinecoloniesFoodItem || tier > 0)
                {
                    PaneBuilders.tooltipBuilder()
                            .append(Component.translatable(FOOD_QUALITY_TOOLTIP, FoodUtils.getBuildingLevelForFood(resource)))
                            .hoverPane(gradient)
                            .build();
                }
                else
                {
                    PaneBuilders.tooltipBuilder()
                            .append(Component.translatable(FOOD_QUALITY_TOOLTIP, FoodUtils.getBuildingLevelForFood(resource)))
                            .appendNL(Component.translatable(VANILLA_FOOD_QUALITY_TOOLTIP))
                            .hoverPane(gradient)
                            .build();
                }

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
