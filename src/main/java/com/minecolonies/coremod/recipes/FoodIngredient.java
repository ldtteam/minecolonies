package com.minecolonies.coremod.recipes;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "food");

    public static final String MIN_HEALING_PROP = "min-healing";
    public static final String MAX_HEALING_PROP = "max-healing";
    public static final String MIN_SATURATION_PROP = "min-saturation";
    public static final String MAX_SATURATION_PROP = "max-saturation";

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

    /**
     * True if this stack is a standard food item (has at least some healing and some saturation, not purely for effects).
     */
    public static final Predicate<ItemStack> ISFOOD
            = stack -> ItemStackUtils.isNotEmpty(stack) && stack.getItem().isFood() && stack.getItem().getFood().getHealing() > 0 && stack.getItem().getFood().getSaturation() > 0;

    private static Stream<IItemList> buildItemLists(final Builder builder)
    {
        return ForgeRegistries.ITEMS.getValues().stream()
                .map(ItemStack::new)
                .filter(ISFOOD)
                .filter(builder::matchesFood)
                .map(SingleItemList::new);
    }

    @NotNull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.getInstance();
    }

    public static class Builder
    {
        private Optional<Integer> minHealing = Optional.empty();
        private Optional<Integer> maxHealing = Optional.empty();
        private Optional<Float> minSaturation = Optional.empty();
        private Optional<Float> maxSaturation = Optional.empty();

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
            @NotNull final Food food = Objects.requireNonNull(stack.getItem().getFood());
            return minHealing.map(healing -> food.getHealing() >= healing).orElse(true) &&
                    maxHealing.map(healing -> food.getHealing() < healing).orElse(true) &&
                    minSaturation.map(saturation -> food.getSaturation() >= saturation).orElse(true) &&
                    maxSaturation.map(saturation -> food.getSaturation() < saturation).orElse(true);
        }
    }

    public static class Serializer implements IIngredientSerializer<FoodIngredient>
    {
        private static final Serializer INSTANCE = new Serializer();

        public static Serializer getInstance() { return INSTANCE; }

        private Serializer() { }

        @NotNull
        @Override
        public FoodIngredient parse(@NotNull final JsonObject json)
        {
            final Builder builder = new Builder();

            if (json.has(MIN_HEALING_PROP)) builder.minHealing(JSONUtils.getInt(json, MIN_HEALING_PROP));
            if (json.has(MAX_HEALING_PROP)) builder.maxHealing(JSONUtils.getInt(json, MAX_HEALING_PROP));
            if (json.has(MIN_SATURATION_PROP)) builder.minSaturation(JSONUtils.getFloat(json, MIN_SATURATION_PROP));
            if (json.has(MAX_SATURATION_PROP)) builder.maxSaturation(JSONUtils.getFloat(json, MAX_SATURATION_PROP));

            return builder.build();
        }

        @NotNull
        @Override
        public FoodIngredient parse(@NotNull final PacketBuffer buffer)
        {
            final Builder builder = new Builder();
            final int flags = buffer.readVarInt();

            if ((flags & 1) != 0) builder.minHealing(buffer.readVarInt());
            if ((flags & 2) != 0) builder.maxHealing(buffer.readVarInt());
            if ((flags & 4) != 0) builder.minSaturation(buffer.readFloat());
            if ((flags & 8) != 0) builder.maxSaturation(buffer.readFloat());

            return builder.build();
        }

        @Override
        public void write(@NotNull final PacketBuffer buffer, @NotNull final FoodIngredient ingredient)
        {
            buffer.writeVarInt((ingredient.minHealing.isPresent() ? 1 : 0) |
                    (ingredient.maxHealing.isPresent() ? 2 : 0) |
                    (ingredient.minSaturation.isPresent() ? 4 : 0) |
                    (ingredient.maxSaturation.isPresent() ? 8 : 0));
            ingredient.minHealing.ifPresent(buffer::writeVarInt);
            ingredient.maxHealing.ifPresent(buffer::writeVarInt);
            ingredient.minSaturation.ifPresent(buffer::writeFloat);
            ingredient.maxSaturation.ifPresent(buffer::writeFloat);
        }
    }
}
