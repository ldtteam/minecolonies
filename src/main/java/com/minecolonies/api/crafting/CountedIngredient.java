package com.minecolonies.api.crafting;

import com.minecolonies.apiimp.initializer.ModIngredientTypeInitializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
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
 */
public class CountedIngredient extends Ingredient
{
    public static final Codec<CountedIngredient> CODEC = RecordCodecBuilder.create(builder -> builder
        .group(Ingredient.CODEC.fieldOf("item").forGetter(CountedIngredient::getChild),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::getCount))
        .apply(builder, CountedIngredient::new));

    public static final Codec<CountedIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(builder -> builder
        .group(Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(CountedIngredient::getChild),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::getCount))
        .apply(builder, CountedIngredient::new));

    @NotNull
    private final Ingredient child;
    private final int count;
    private ItemStack[] array = null;

    public CountedIngredient(@NotNull final Ingredient child, final int count)
    {
        super(Arrays.stream(child.values), ModIngredientTypeInitializer.COUNTED_INGREDIENT_TYPE);

        this.child = child;
        this.count = count;
    }

    /** The underlying ingredient. */
    @NotNull
    public Ingredient getChild() { return this.child; }

    /** The number of items required. */
    public int getCount() { return this.count; }

    @NotNull
    @Override
    public ItemStack[] getItems()
    {
        if (this.array == null)
        {
            this.array = Arrays.stream(this.child.getItems())
                .map(ItemStack::copy)
                .peek(s -> s.setCount(this.count))
                .toArray(ItemStack[]::new);
        }
        return this.array;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof final CountedIngredient other))
        {
            return false;
        }
        return Objects.equals(other.child, this.child) && other.count == this.count;
    }

    @Override
    public boolean isEmpty()
    {
        return child.isEmpty() || count <= 0;
    }

    @Override
    public boolean synchronizeWithContents()
    {
        // must be false so network sync forcefully uses codec
        return false;
    }
}
