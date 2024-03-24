package com.minecolonies.api.crafting.registry;

import com.minecolonies.api.crafting.CompostRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Holds ref to the mod recipe serializers and recipe types.
 */
public class ModRecipeSerializer
{
    public static DeferredHolder<RecipeSerializer<?>, CompostRecipe.Serializer> CompostRecipeSerializer;
    public static DeferredHolder<RecipeType<?>, RecipeType<CompostRecipe>> CompostRecipeType;
}
