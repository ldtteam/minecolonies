package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.ZeroWasteRecipe;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializerInitializer
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);
    public static final DeferredRegister<RecipeType<?>>       RECIPE_TYPES      = DeferredRegister.create(Registries.RECIPE_TYPE, Constants.MOD_ID);

    static
    {
        ModRecipeSerializer.CompostRecipeSerializer = RECIPE_SERIALIZER.register("composting", () -> new RecipeSerializer<>(CompostRecipe.CODEC, CompostRecipe.STREAM_CODEC));
        ModRecipeSerializer.CompostRecipeType = RECIPE_TYPES.register("composting", () -> RecipeType.<CompostRecipe>simple(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "composting")));

        ModRecipeSerializer.ZeroWasteRecipeSerializer = RECIPE_SERIALIZER.register("zero_waste", () -> new RecipeSerializer<>(ZeroWasteRecipe.CODEC, ZeroWasteRecipe.STREAM_CODEC));
    }

    private ModRecipeSerializerInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeSerializerInitializer but this is a Utility class.");
    }
}
