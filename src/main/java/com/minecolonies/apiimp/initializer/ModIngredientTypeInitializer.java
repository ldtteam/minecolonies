package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.CountedIngredient;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.recipes.FoodIngredient;
import com.minecolonies.core.recipes.PlantIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModIngredientTypeInitializer
{
    public final static DeferredRegister<IngredientType<?>> DEFERRED_REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Constants.MOD_ID);

    private ModIngredientTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModIngredientTypeInitializer but this is a Utility class.");
    }

    public static final DeferredHolder<IngredientType<?>, IngredientType<CountedIngredient>> COUNTED_INGREDIENT_TYPE =
        DEFERRED_REGISTER.register("counted", () -> new IngredientType<>(CountedIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<PlantIngredient>> PLANT_INGREDIENT_TYPE =
        DEFERRED_REGISTER.register("plant", () -> new IngredientType<>(PlantIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<FoodIngredient>> FOOD_INGREDIENT_TYPE =
        DEFERRED_REGISTER.register("food", () -> new IngredientType<>(FoodIngredient.CODEC));
}
