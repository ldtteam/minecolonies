package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A vanilla recipe class describing the operation of the compost barrel.  Primarily this is just used to
 * assign different "strength" ratings to different input items; currently the actual outputs of the barrel
 * are fixed.
 */
public class CompostRecipe implements Recipe<SingleRecipeInput>
{
    public static final MapCodec<CompostRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
        .group(Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(CompostRecipe::getInput),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf("strength", 1).forGetter(CompostRecipe::getStrength))
        .apply(builder, CompostRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompostRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, CompostRecipe::getInput,
                    ByteBufCodecs.VAR_INT, CompostRecipe::getStrength,
                    CompostRecipe::new);

    private static final int FERMENT_TIME = 24000;
    private static final int COMPOST_RESULT = 6;

    private final Ingredient input;
    private final ItemStack output;
    private final int strength;

    public CompostRecipe(@NotNull final Ingredient ingredient, final int strength)
    {
        this.input = ingredient;
        this.strength = strength;
        this.output = new ItemStack(ModItems.compost, COMPOST_RESULT);
    }

    @NotNull
    @Override
    public RecipeType<?> getType() { return ModRecipeSerializer.CompostRecipeType.get(); }

    /**
     * Get the input ingredient for this recipe (this is multiple alternative item types).
     * Use {@link #getIngredients()} instead to also calculate the amount of items needed for one composting operation.
     *
     * @return The input.
     */
    public Ingredient getInput() { return this.input; }

    /**
     * Get the strength of this recipe.  A higher strength means fewer input items are required to craft the same
     * quantity of output compost/dirt.
     *
     * @return The strength; typically 2, 4, or 8, but other values are possible.
     */
    public int getStrength() { return this.strength; }

    /**
     * The number of ticks that this recipe should take to ferment into compost.
     * This is currently just for informational purposes and is not configurable (nor actually used).
     *
     * @return A number of ticks.
     */
    public int getFermentTime() { return FERMENT_TIME; }

    @Override
    public boolean matches(@NotNull final SingleRecipeInput input, @NotNull final Level worldIn)
    {
        return false;
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull final SingleRecipeInput input, @NotNull final HolderLookup.Provider provider)
    {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@Nullable final HolderLookup.Provider provider)
    {
        return this.output;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializer.CompostRecipeSerializer.get();
    }

    /**
     * Gets the list of ingredients for this recipe; there will only be one, but it is a multi-alternative stack
     * and includes the specific count of items that are required.
     *
     * @return The list of input ingredients.
     */
    @NotNull
    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(CountedIngredient.of(this.input, calculateIngredientCount()));
        return ingredients;
    }

    private int calculateIngredientCount()
    {
        return AbstractTileEntityBarrel.MAX_ITEMS / this.strength;
    }

    // JEI looks better if we render these as many individual recipes rather than as
    // a few recipes with a massive number of alternative ingredients.
    @NotNull
    public static CompostRecipe individualize(@NotNull final Item item, @NotNull final RecipeHolder<CompostRecipe> recipe)
    {
        return new CompostRecipe(Ingredient.of(item), recipe.value().getStrength());
    }

    public static class Serializer implements RecipeSerializer<CompostRecipe>
    {
        @NotNull
        @Override
        public MapCodec<CompostRecipe> codec()
        {
            return CompostRecipe.CODEC;
        }

        @NotNull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompostRecipe> streamCodec()
        {
            return CompostRecipe.STREAM_CODEC;
        }
    }
}
