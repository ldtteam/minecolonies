package com.minecolonies.core.generation;

import com.minecolonies.api.crafting.CompostRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Datagen for {@link CompostRecipe}
 */
public class CompostRecipeBuilder
{
    private final List<Ingredient> inputs = new ArrayList<>();
    private final int strength;

    private CompostRecipeBuilder(final int strength)
    {
        this.strength = strength;
    }

    public static CompostRecipeBuilder strength(final int strength)
    {
        return new CompostRecipeBuilder(strength);
    }

    public CompostRecipeBuilder input(@NotNull final Ingredient ingredient)
    {
        this.inputs.add(ingredient);
        return this;
    }

    public void save(@NotNull final RecipeOutput consumer,
                     @NotNull final ResourceLocation id)
    {
        consumer.accept(id, new CompostRecipe(CompoundIngredient.of(inputs.toArray(Ingredient[]::new)), strength), null);
    }
}