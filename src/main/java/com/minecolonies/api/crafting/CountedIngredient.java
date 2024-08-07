package com.minecolonies.api.crafting;

import com.minecolonies.apiimp.initializer.ModIngredientTypeInitializer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * An ingredient that can be used in a vanilla recipe to require more than one item in a particular input slot.
 *
 * {
 *     "type": "minecolonies:counted",
 *     "item": {
 *         "item": "minecraft:cobblestone"  // could be a tag or something else
 *     },
 *     "count": 16
 * }
 *
 * @param child the underlying ingredient.
 * @param count the number of items required.
 */
public record CountedIngredient(@NotNull Ingredient child, int count) implements ICustomIngredient
{
    public static final MapCodec<CountedIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
        .group(Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(CountedIngredient::child),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count))
        .apply(builder, CountedIngredient::new));

    public CountedIngredient
    {
        if (child == Ingredient.EMPTY || count <= 0) throw new IllegalArgumentException("Counted ingredient must have a child");
    }

    /**
     * Creates a counted ingredient.
     * @param child the underlying ingredient.
     * @param count the number of items required.
     * @return the counted ingredient.
     */
    public static Ingredient of(@NotNull final Ingredient child, final int count)
    {
        return new CountedIngredient(child, count).toVanilla();
    }

    @Override
    public boolean test(@Nullable final ItemStack stack)
    {
        return child.test(stack);
    }

    @Override
    public boolean isSimple()
    {
        return child.isSimple();
    }

    @NotNull
    @Override
    public IngredientType<?> getType()
    {
        return ModIngredientTypeInitializer.COUNTED_INGREDIENT_TYPE.get();
    }

    @NotNull
    @Override
    public Stream<ItemStack> getItems()
    {
        return Arrays.stream(child.getItems())
                .map(ItemStack::copy)
                .peek(s -> s.setCount(this.count));
    }
}
