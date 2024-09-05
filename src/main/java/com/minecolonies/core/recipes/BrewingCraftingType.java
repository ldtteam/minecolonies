package com.minecolonies.core.recipes;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.tools.ModToolTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A crafting type for brewing recipes
 */
public class BrewingCraftingType extends CraftingType
{
    public BrewingCraftingType()
    {
        super(ModCraftingTypes.BREWING_ID);
    }

    @Override
    @NotNull
    public List<IGenericRecipe> findRecipes(@NotNull RecipeManager recipeManager, @Nullable Level world)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();
        final ICompatibilityManager compatibilityManager = MinecoloniesAPIProxy.getInstance().getColonyManager().getCompatibilityManager();

        for (final IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
        {
            final List<ItemStack> inputs = compatibilityManager.getListOfAllItems().stream()
                    .filter(recipe::isInput)
                    .collect(Collectors.toList());
            final List<ItemStack> ingredients = compatibilityManager.getListOfAllItems().stream()
                    .filter(recipe::isIngredient)
                    .collect(Collectors.toList());

            for (final ItemStack input : inputs)
            {
                for (final ItemStack ingredient : ingredients)
                {
                    final ItemStack output = recipe.getOutput(input, ingredient);
                    if (!output.isEmpty())
                    {
                        final ItemStack actualInput = input.copy();
                        actualInput.setCount(3);
                        final ItemStack actualOutput = output.copy();
                        actualOutput.setCount(3);

                        recipes.add(new GenericRecipe(null, actualOutput, Collections.emptyList(),
                                Arrays.asList(Collections.singletonList(ingredient), Collections.singletonList(actualInput)),
                                1, Blocks.BREWING_STAND, null, ModToolTypes.none.get(), Collections.emptyList(), -1));
                    }
                }
            }
        }

        return recipes;
    }
}
