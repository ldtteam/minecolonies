package com.minecolonies.core.recipes;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

        final List<ItemStack> containers = compatibilityManager.getListOfAllItems().stream()
                .filter(world.potionBrewing()::isInput)
                .toList();
        final List<ItemStack> ingredients = compatibilityManager.getListOfAllItems().stream()
                .filter(world.potionBrewing()::isIngredient)
                .toList();

        for (final ItemStack container : containers)
        {
            for (final ItemStack ingredient : ingredients)
            {
                final ItemStack output = world.potionBrewing().mix(ingredient, container);
                if (!output.isEmpty() && output != container)
                {
                    recipes.add(new GenericRecipe(null, output.copyWithCount(3), List.of(),
                            List.of(List.of(ingredient), List.of(container.copyWithCount(3))),
                            1, Blocks.BREWING_STAND, null, ModEquipmentTypes.none.get(), List.of(), -1));
                }
            }
        }

        return recipes;
    }
}
