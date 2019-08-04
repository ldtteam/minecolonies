package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.block.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all recipes the bakery can use.
 */
public final class BakerRecipes
{
    /**
     * The grid size the bakery can use.
     */
    private static final int GRID_SIZE = 9;

    /**
     * Amount of wheat required for recipe.
     */
    private static final int REQUIRED_WHEAT = 3;

    /**
     * Amount of sugar required for a recipe.
     */
    private static final int REQUIRED_SUGAR = 2;

    /**
     * Amount of egg required for a recipe.
     */
    private static final int REQUIRED_EGGS = 1;

    /**
     * Amount of cocoa required for a recipe.
     */
    private static final int REQUIRED_COCOA = 1;

    /**
     * Amount of wheat required for a cookie recipe.
     */
    private static final int REQUIRED_WHEAT_COOKIES = 2;

    /**
     * List of recipes the Baker can know.
     */
    private static final ImmutableList<IRecipeStorage> recipes;

    /**
     * Amount of cookies, for more cookies, increase this value!
     */
    private static final int COOKIES = 8;
    static
    {
        final List<ItemStack> inputPumpkinPie = new ArrayList<>();
        inputPumpkinPie.add(new ItemStack(Blocks.PUMPKIN.getItemDropped(null, null, 0), 1));
//        inputPumpkinPie.add(new ItemStack(Items.PUMPKIN_SEEDS, 4));
        inputPumpkinPie.add(new ItemStack(Items.SUGAR, 1));
        inputPumpkinPie.add(new ItemStack(Items.EGG, 1));

    	
    	
    	final List<ItemStack> inputCake = new ArrayList<>();
        inputCake.add(new ItemStack(Items.WHEAT, REQUIRED_WHEAT));
        inputCake.add(new ItemStack(Items.MILK_BUCKET, 1));
        inputCake.add(new ItemStack(Items.MILK_BUCKET, 1));
        inputCake.add(new ItemStack(Items.MILK_BUCKET, 1));
        inputCake.add(new ItemStack(Items.SUGAR, REQUIRED_SUGAR));
        inputCake.add(new ItemStack(Items.EGG, REQUIRED_EGGS));

        final List<ItemStack> inputCookie = new ArrayList<>();
        inputCookie.add(new ItemStack(Items.WHEAT, REQUIRED_WHEAT_COOKIES));
        inputCookie.add(new ItemStack(Items.DYE, REQUIRED_COCOA, 0x3));

        final List<ItemStack> inputBread = new ArrayList<>();
        inputBread.add(new ItemStack(Items.WHEAT, REQUIRED_WHEAT));
        final StandardFactoryController sfc = StandardFactoryController.getInstance();
        recipes = new ImmutableList.Builder<IRecipeStorage>()
                    .add(sfc.getNewInstance(TypeConstants.RECIPE, sfc.getNewInstance(TypeConstants.ITOKEN), inputCookie, GRID_SIZE, new ItemStack(Items.COOKIE, COOKIES)))
                    .add(sfc.getNewInstance(TypeConstants.RECIPE, sfc.getNewInstance(TypeConstants.ITOKEN), inputCake, GRID_SIZE, new ItemStack(Items.CAKE)))
                    .add(sfc.getNewInstance(TypeConstants.RECIPE, sfc.getNewInstance(TypeConstants.ITOKEN), inputBread, GRID_SIZE, new ItemStack(Items.BREAD)))
                    .add(sfc.getNewInstance(TypeConstants.RECIPE, sfc.getNewInstance(TypeConstants.ITOKEN), inputPumpkinPie, GRID_SIZE, new ItemStack(Items.PUMPKIN_PIE)))
                    .build();
    }
    /**
     * Private constructor to hide implicit one.
     */
    private BakerRecipes()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Get the list of recipes from the class.
     *
     * @return a copy of the recipes.
     */
    public static List<IRecipeStorage> getRecipes()
    {
        return recipes;
    }
}
