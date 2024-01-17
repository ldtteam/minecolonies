package com.minecolonies.core.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
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

    private static Stream<Value> buildItemLists(final Builder builder)
    {
        return ForgeRegistries.ITEMS.getValues().stream()
                .map(ItemStack::new)
                .filter(ISFOOD)
                .filter(builder::matchesFood)
                .map(ItemValue::new);
    }

    @NotNull
    @Override
    public JsonElement toJson()
    {
        JsonObject json = new JsonObject();
        Serializer.getInstance().write(json, this);
        return json;
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
            @NotNull final FoodProperties food = Objects.requireNonNull(stack.getItem().getFoodProperties(stack, null));
            return minHealing.map(healing -> food.getNutrition() >= healing).orElse(true) &&
                    maxHealing.map(healing -> food.getNutrition() < healing).orElse(true) &&
                    minSaturation.map(saturation -> food.getSaturationModifier() >= saturation).orElse(true) &&
                    maxSaturation.map(saturation -> food.getSaturationModifier() < saturation).orElse(true);
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

            if (json.has(MIN_HEALING_PROP)) builder.minHealing(GsonHelper.getAsInt(json, MIN_HEALING_PROP));
            if (json.has(MAX_HEALING_PROP)) builder.maxHealing(GsonHelper.getAsInt(json, MAX_HEALING_PROP));
            if (json.has(MIN_SATURATION_PROP)) builder.minSaturation(GsonHelper.getAsFloat(json, MIN_SATURATION_PROP));
            if (json.has(MAX_SATURATION_PROP)) builder.maxSaturation(GsonHelper.getAsFloat(json, MAX_SATURATION_PROP));

            return builder.build();
        }

        public void write(@NotNull final JsonObject json, @NotNull final FoodIngredient ingredient)
        {
            json.addProperty("type", (Objects.requireNonNull(CraftingHelper.getID(this))).toString());

            ingredient.minHealing.ifPresent(value -> json.addProperty(MIN_HEALING_PROP, value));
            ingredient.maxHealing.ifPresent(value -> json.addProperty(MAX_HEALING_PROP, value));
            ingredient.minSaturation.ifPresent(value -> json.addProperty(MIN_SATURATION_PROP, value));
            ingredient.maxSaturation.ifPresent(value -> json.addProperty(MAX_SATURATION_PROP, value));
        }

        @NotNull
        @Override
        public FoodIngredient parse(@NotNull final FriendlyByteBuf buffer)
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
        public void write(@NotNull final FriendlyByteBuf buffer, @NotNull final FoodIngredient ingredient)
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
