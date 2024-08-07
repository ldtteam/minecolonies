package com.minecolonies.core.recipes;

import com.minecolonies.apiimp.initializer.ModIngredientTypeInitializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;

/**
 * An ingredient that can be used in a vanilla recipe to match food items.
 * Only items with at least *some* healing and saturation are counted, and
 * further restrictions can be imposed if desired.
 *
 * // any food item at all
 * {
 *     "type": "minecolonies:food"
 * }
 *
 * // any food with at least 4 healing points (inclusive)
 * {
 *     "type": "minecolonies:food",
 *     "min-healing": 4
 * }
 *
 * // any food with less than 1.0 saturation points (not including 1.0 itself)
 * {
 *     "type": "minecolonies:food",
 *     "max-saturation": 1.0
 * }
 *
 * Conditions can also be combined.
 * Min bounds are inclusive and max bounds are exclusive.
 *
 * @param minHealing minimum healing value
 * @param maxHealing maximum healing value
 * @param minSaturation minimum saturation value
 * @param maxSaturation maximum saturation value
 */
public record FoodIngredient(@NotNull Optional<Integer> minHealing,
                             @NotNull Optional<Integer> maxHealing,
                             @NotNull Optional<Float> minSaturation,
                             @NotNull Optional<Float> maxSaturation) implements ICustomIngredient
{
    public static final MapCodec<FoodIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
        .group(Codec.INT.optionalFieldOf("min-healing").forGetter(FoodIngredient::minHealing),
               Codec.INT.optionalFieldOf("max-healing").forGetter(FoodIngredient::maxHealing),
               Codec.FLOAT.optionalFieldOf("min-saturation").forGetter(FoodIngredient::minSaturation),
               Codec.FLOAT.optionalFieldOf("max-saturation").forGetter(FoodIngredient::maxSaturation))
        .apply(builder, FoodIngredient::new));

    private boolean matchesFood(@NotNull final ItemStack stack)
    {
        @NotNull final FoodProperties food = Objects.requireNonNull(stack.getItem().getFoodProperties(stack, null));
        return minHealing.map(healing -> food.nutrition() >= healing).orElse(true) &&
               maxHealing.map(healing -> food.nutrition() < healing).orElse(true) &&
               minSaturation.map(saturation -> food.saturation() >= saturation).orElse(true) &&
               maxSaturation.map(saturation -> food.saturation() < saturation).orElse(true);
    }

    @Override
    public boolean test(@Nullable final ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        return ISFOOD.test(stack) && matchesFood(stack);
    }

    @NotNull
    @Override
    public Stream<ItemStack> getItems()
    {
        return BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(this::test);
    }

    @Override
    public boolean isSimple()
    {
        return true;
    }

    @NotNull
    @Override
    public IngredientType<?> getType()
    {
        return ModIngredientTypeInitializer.FOOD_INGREDIENT_TYPE.get();
    }
}
