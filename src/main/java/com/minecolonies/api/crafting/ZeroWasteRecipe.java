package com.minecolonies.api.crafting;

import com.google.common.collect.Lists;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A shapeless recipe that discards any remaining items.  Mainly intended for mixing things into bottles or bowls
 * without leaving extra empties behind, but can be used for other things too.
 */
public class ZeroWasteRecipe extends NormalCraftingRecipe
{
    public static final MapCodec<ZeroWasteRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
        CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
        Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
    ).apply(builder, ZeroWasteRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ZeroWasteRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC,
        recipe -> recipe.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
        recipe -> recipe.bookInfo,
        ItemStackTemplate.STREAM_CODEC,
        recipe -> recipe.result,
        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
        recipe -> recipe.ingredients,
        ZeroWasteRecipe::new
    );

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final boolean simple;

    public ZeroWasteRecipe(@NotNull final ItemStack output, @NotNull final NonNullList<Ingredient> inputs)
    {
        this(
            new Recipe.CommonInfo(true),
            new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
            ItemStackTemplate.fromNonEmptyStack(output),
            List.copyOf(inputs)
        );
    }

    public ZeroWasteRecipe(
        @NotNull final Recipe.CommonInfo commonInfo,
        @NotNull final CraftingRecipe.CraftingBookInfo bookInfo,
        @NotNull final ItemStackTemplate result,
        @NotNull final List<Ingredient> inputs)
    {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = inputs;
        this.simple = this.ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public boolean matches(@NotNull final CraftingInput input, @NotNull final net.minecraft.world.level.Level level)
    {
        if (input.ingredientCount() != this.ingredients.size())
        {
            return false;
        }

        if (!this.simple)
        {
            final List<ItemStack> nonEmptyItems = new ArrayList<>(input.ingredientCount());
            for (final ItemStack stack : input.items())
            {
                if (!stack.isEmpty())
                {
                    nonEmptyItems.add(stack);
                }
            }
            return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        }

        return input.size() == 1 && this.ingredients.size() == 1
            ? this.ingredients.getFirst().test(input.getItem(0))
            : input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(@NotNull final CraftingInput input)
    {
        return this.result.create();
    }

    @Override
    protected PlacementInfo createPlacementInfo()
    {
        return PlacementInfo.create(this.ingredients);
    }

    @NotNull
    @Override
    public List<net.minecraft.world.item.crafting.display.RecipeDisplay> display()
    {
        return List.of(new net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay(
            this.ingredients.stream().map(Ingredient::display).toList(),
            new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.result),
            new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.CRAFTING_TABLE)
        ));
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
    public RecipeSerializer<? extends NormalCraftingRecipe> getSerializer()
    {
        return ModRecipeSerializer.ZeroWasteRecipeSerializer.get();
    }

    @NotNull
    public List<Ingredient> ingredients()
    {
        return this.ingredients;
    }

    public static Builder build(@NotNull final RecipeCategory category, @NotNull final ItemLike output, final int count)
    {
        return new Builder(category, new ItemStackTemplate(output.asItem(), count));
    }

    public static Builder build(@NotNull final RecipeCategory category, @NotNull final ItemStack output)
    {
        return new Builder(category, output);
    }

    public static class Builder implements RecipeBuilder
    {
        private final RecipeCategory category;
        private final ItemStackTemplate output;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();

        public Builder(@NotNull final RecipeCategory category, @NotNull final ItemStack output)
        {
            this(category, ItemStackTemplate.fromNonEmptyStack(output));
        }

        private Builder(@NotNull final RecipeCategory category, @NotNull final ItemStackTemplate output)
        {
            this.category = category;
            this.output = output;
        }

        public Builder requires(@NotNull final TagKey<Item> tag)
        {
            return this.requires(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag)));
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
            this.advancementBuilder.unlockedBy(name, criterion);
            return this;
        }

        @NotNull
        public Item getResult()
        {
            return this.output.item().value();
        }

        @NotNull
        @Override
        public RecipeBuilder group(final String group)
        {
            return this;
        }

        @NotNull
        @Override
        public ResourceKey<Recipe<?>> defaultId()
        {
            return RecipeBuilder.getDefaultRecipeId(this.output);
        }

        @Override
        public void save(@NotNull final RecipeOutput consumer, @NotNull final ResourceKey<Recipe<?>> id)
        {
            final ZeroWasteRecipe recipe = new ZeroWasteRecipe(
                RecipeBuilder.createCraftingCommonInfo(true),
                RecipeBuilder.createCraftingBookInfo(this.category, null),
                this.output,
                List.copyOf(this.ingredients)
            );
            consumer.accept(id, recipe, this.advancementBuilder.build(consumer, id, this.category));
        }

        public void save(@NotNull final RecipeOutput consumer, @NotNull final Identifier id)
        {
            this.save(consumer, ResourceKey.create(Registries.RECIPE, id));
        }
    }
}
