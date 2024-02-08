package com.minecolonies.core.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

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
 */
public class FoodIngredient extends Ingredient
{
    public static final Codec<FoodIngredient> CODEC = RecordCodecBuilder.create(builder -> builder
        .group(Codec.INT.optionalFieldOf("min-healing").forGetter(FoodIngredient::getMinHealing),
            Codec.INT.optionalFieldOf("max-healing").forGetter(FoodIngredient::getMaxHealing),
            Codec.FLOAT.optionalFieldOf("min-saturation").forGetter(FoodIngredient::getMinSaturation),
            Codec.FLOAT.optionalFieldOf("max-saturation").forGetter(FoodIngredient::getMaxSaturation))
        .apply(builder, (minH, maxH, minS, maxS) -> new FoodIngredient(new Builder(minH, maxH, minS, maxS))));

    private final Optional<Integer> minHealing;
    private final Optional<Integer> maxHealing;
    private final Optional<Float> minSaturation;
    private final Optional<Float> maxSaturation;

    private FoodIngredient(final Builder builder)
    {
        super(buildItemLists(builder));

        this.minHealing = builder.minHealing;
        this.maxHealing = builder.maxHealing;
        this.minSaturation = builder.minSaturation;
        this.maxSaturation = builder.maxSaturation;
    }

    private static Stream<Value> buildItemLists(final Builder builder)
    {
        return BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(ISFOOD)
                .filter(builder::matchesFood)
                .map(ItemValue::new);
    }

    @Override
    public boolean synchronizeWithContents()
    {
        // must be false so network sync forcefully uses codec
        return false;
    }

    public Optional<Integer> getMinHealing()
    {
        return minHealing;
    }

    public Optional<Integer> getMaxHealing()
    {
        return maxHealing;
    }

    public Optional<Float> getMinSaturation()
    {
        return minSaturation;
    }

    public Optional<Float> getMaxSaturation()
    {
        return maxSaturation;
    }

    public static class Builder
    {
        private Optional<Integer> minHealing = Optional.empty();
        private Optional<Integer> maxHealing = Optional.empty();
        private Optional<Float> minSaturation = Optional.empty();
        private Optional<Float> maxSaturation = Optional.empty();

        private Builder(final Optional<Integer> minHealing,
            final Optional<Integer> maxHealing,
            final Optional<Float> minSaturation,
            final Optional<Float> maxSaturation)
        {
            this.minHealing = minHealing;
            this.maxHealing = maxHealing;
            this.minSaturation = minSaturation;
            this.maxSaturation = maxSaturation;
        }

        public Builder()
        {}

        public Builder minHealing(final int healing) { minHealing = Optional.of(healing); return this; }
        public Builder maxHealing(final int healing) { maxHealing = Optional.of(healing); return this; }
        public Builder minSaturation(final float saturation) { minSaturation = Optional.of(saturation); return this; }
        public Builder maxSaturation(final float saturation) { maxSaturation = Optional.of(saturation); return this; }

        public FoodIngredient build()
        {
            return new FoodIngredient(this);
        }

        private boolean matchesFood(@NotNull final ItemStack stack)
        {
            @NotNull final FoodProperties food = Objects.requireNonNull(stack.getItem().getFoodProperties(stack, null));
            return minHealing.map(healing -> food.getNutrition() >= healing).orElse(true) &&
                    maxHealing.map(healing -> food.getNutrition() < healing).orElse(true) &&
                    minSaturation.map(saturation -> food.getSaturationModifier() >= saturation).orElse(true) &&
                    maxSaturation.map(saturation -> food.getSaturationModifier() < saturation).orElse(true);
        }
    }
}
