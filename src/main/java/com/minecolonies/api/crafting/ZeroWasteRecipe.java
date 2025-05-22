package com.minecolonies.api.crafting;

import com.google.common.collect.Lists;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A shapeless recipe that discards any remaining items.  Mainly intended for mixing things into bottles or bowls
 * without leaving extra empties behind, but can be used for other things too.
 */
public class ZeroWasteRecipe extends ShapelessRecipe
{
    public ZeroWasteRecipe(@NotNull final ItemStack output,
                           @NotNull final NonNullList<Ingredient> inputs)
    {
        super("", CraftingBookCategory.MISC, output, inputs);
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull final CraftingInput input)
    {
        final NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);
        Collections.fill(remainingItems, ItemStack.EMPTY);
        return remainingItems;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializer.ZeroWasteRecipeSerializer.get();
    }

    public static class Serializer implements RecipeSerializer<ZeroWasteRecipe>
    {
        private static final MapCodec<ZeroWasteRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                        NonNullList.codecOf(Ingredient.CODEC_NONEMPTY)
                                .fieldOf("ingredients")
                                .forGetter(ShapelessRecipe::getIngredients)
                ).apply(builder, ZeroWasteRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, ZeroWasteRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @NotNull
        @Override
        public MapCodec<ZeroWasteRecipe> codec()
        {
            return CODEC;
        }

        @NotNull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ZeroWasteRecipe> streamCodec()
        {
            return STREAM_CODEC;
        }

        private static ZeroWasteRecipe fromNetwork(@NotNull final RegistryFriendlyByteBuf buf)
        {
            final int count = buf.readVarInt();
            final NonNullList<Ingredient> inputs = NonNullList.withSize(count, Ingredient.EMPTY);
            inputs.replaceAll(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            final ItemStack output = ItemStack.STREAM_CODEC.decode(buf);

            return new ZeroWasteRecipe(output, inputs);
        }

        private static void toNetwork(@NotNull final RegistryFriendlyByteBuf buf,
                                      @NotNull final ZeroWasteRecipe recipe)
        {
            buf.writeVarInt(recipe.getIngredients().size());
            for (final Ingredient input : recipe.getIngredients())
            {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, input);
            }
            ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItem(null));
        }
    }

    public static Builder build(@NotNull final RecipeCategory category,
                                @NotNull final ItemLike output,
                                final int count)
    {
        return new Builder(category, new ItemStack(output, count));
    }

    public static Builder build(@NotNull final RecipeCategory category,
                                @NotNull final ItemStack output)
    {
        return new Builder(category, output);
    }

    public static class Builder implements RecipeBuilder
    {
        private final RecipeCategory category;
        private final ItemStack output;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

        public Builder(@NotNull final RecipeCategory category,
                       @NotNull final ItemStack output)
        {
            this.category = category;
            this.output = output;
        }

        public Builder requires(@NotNull final TagKey<Item> tag)
        {
            return this.requires(Ingredient.of(tag));
        }

        public Builder requires(@NotNull final ItemLike item)
        {
            return this.requires(item, 1);
        }

        public Builder requires(@NotNull final ItemLike item, final int count)
        {
            for (int i = 0; i < count; ++i)
            {
                this.requires(Ingredient.of(item));
            }
            return this;
        }

        public Builder requires(@NotNull final Ingredient ingredient)
        {
            return this.requires(ingredient, 1);
        }

        public Builder requires(@NotNull final Ingredient ingredient, final int count)
        {
            for (int i = 0; i < count; ++i)
            {
                this.ingredients.add(ingredient);
            }
            return this;
        }

        @NotNull
        public Builder unlockedBy(@NotNull final String name, @NotNull final Criterion<?> criterion)
        {
            this.criteria.put(name, criterion);
            return this;
        }

        @NotNull
        public Item getResult()
        {
            return this.output.getItem();
        }

        @NotNull
        @Override
        public RecipeBuilder group(@Nullable String group)
        {
            return this;
        }

        public void save(@NotNull final RecipeOutput consumer, @NotNull final ResourceLocation id)
        {
            this.ensureValid(id);

            final ZeroWasteRecipe recipe = new ZeroWasteRecipe(this.output, NonNullList.copyOf(this.ingredients));

            final Advancement.Builder advancementBuilder = consumer.advancement();
            advancementBuilder
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(advancementBuilder::addCriterion);
            final AdvancementHolder advancement = advancementBuilder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/"));

            consumer.accept(id, recipe, advancement);
        }

        private void ensureValid(@NotNull final ResourceLocation id)
        {
            if (this.criteria.isEmpty())
            {
                throw new IllegalStateException("No way of obtaining recipe " + id);
            }
        }
    }
}
