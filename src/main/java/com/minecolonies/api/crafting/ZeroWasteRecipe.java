package com.minecolonies.api.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A shapeless recipe that discards any remaining items.  Mainly intended for mixing things into bottles or bowls
 * without leaving extra empties behind, but can be used for other things too.
 */
public class ZeroWasteRecipe extends ShapelessRecipe
{
    public ZeroWasteRecipe(@NotNull final ResourceLocation id,
                           @NotNull final ItemStack output,
                           @NotNull final NonNullList<Ingredient> inputs)
    {
        super(id, "", CraftingBookCategory.MISC, output, inputs);
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull final CraftingContainer container)
    {
        final NonNullList<ItemStack> remainingItems = super.getRemainingItems(container);
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
        @NotNull
        @Override
        public ZeroWasteRecipe fromJson(@NotNull final ResourceLocation id,
                                        @NotNull final JsonObject json)
        {
            final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            final JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            final NonNullList<Ingredient> inputs = NonNullList.create();
            for (int i = 0; i < array.size(); ++i)
            {
                inputs.add(Ingredient.fromJson(array.get(i), false));
            }

            return new ZeroWasteRecipe(id, output, inputs);
        }

        @Nullable
        @Override
        public ZeroWasteRecipe fromNetwork(@NotNull final ResourceLocation id,
                                           @NotNull final FriendlyByteBuf buf)
        {
            final int count = buf.readVarInt();
            final NonNullList<Ingredient> inputs = NonNullList.withSize(count, Ingredient.EMPTY);
            for (int i = 0; i < count; ++i)
            {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }
            final ItemStack output = buf.readItem();

            return new ZeroWasteRecipe(id, output, inputs);
        }

        @Override
        public void toNetwork(@NotNull final FriendlyByteBuf buf,
                              @NotNull final ZeroWasteRecipe recipe)
        {
            buf.writeVarInt(recipe.getIngredients().size());
            for (final Ingredient input : recipe.getIngredients())
            {
                input.toNetwork(buf);
            }
            buf.writeItem(recipe.getResultItem(null));
        }
    }

    public static Builder build(@NotNull final RecipeCategory category,
                                @NotNull final ItemLike output,
                                final int count)
    {
        return new Builder(category, output, count);
    }

    public static class Builder extends CraftingRecipeBuilder implements RecipeBuilder
    {
        private final RecipeCategory category;
        private final Item output;
        private final int count;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

        public Builder(@NotNull final RecipeCategory category,
                       @NotNull final ItemLike output,
                       final int count)
        {
            this.category = category;
            this.output = output.asItem();
            this.count = count;
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
        public Builder unlockedBy(@NotNull final String name, @NotNull final CriterionTriggerInstance criterion)
        {
            this.advancement.addCriterion(name, criterion);
            return this;
        }

        @NotNull
        public Item getResult()
        {
            return this.output.asItem();
        }

        @NotNull
        @Override
        public RecipeBuilder group(@Nullable String group)
        {
            return this;
        }

        public void save(@NotNull final Consumer<FinishedRecipe> consumer, @NotNull final ResourceLocation id)
        {
            this.ensureValid(id);
            this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
            consumer.accept(new Result(id, this.output, this.count, this.ingredients, this.advancement, id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
        }

        private void ensureValid(@NotNull final ResourceLocation id)
        {
            if (this.advancement.getCriteria().isEmpty())
            {
                throw new IllegalStateException("No way of obtaining recipe " + id);
            }
        }

        public static class Result implements FinishedRecipe
        {
            private final ResourceLocation id;
            private final Item output;
            private final int count;
            private final List<Ingredient> inputs;
            private final Advancement.Builder advancement;
            private final ResourceLocation advancementId;

            public Result(@NotNull final ResourceLocation id,
                          @NotNull final Item output,
                          final int count,
                          @NotNull final List<Ingredient> inputs,
                          @NotNull final Advancement.Builder advancement,
                          @NotNull final ResourceLocation advancementId)
            {
                this.id = id;
                this.output = output;
                this.count = count;
                this.inputs = inputs;
                this.advancement = advancement;
                this.advancementId = advancementId;
            }

            public void serializeRecipeData(@NotNull final JsonObject json)
            {
                final JsonArray jsonInputs = new JsonArray();
                for (final Ingredient ingredient : this.inputs)
                {
                    jsonInputs.add(ingredient.toJson());
                }
                json.add("ingredients", jsonInputs);

                final JsonObject result = new JsonObject();
                result.addProperty("item", BuiltInRegistries.ITEM.getKey(this.output).toString());
                if (this.count > 1)
                {
                    result.addProperty("count", this.count);
                }
                json.add("result", result);
            }

            @NotNull
            public RecipeSerializer<?> getType()
            {
                return ModRecipeSerializer.ZeroWasteRecipeSerializer.get();
            }

            @NotNull
            public ResourceLocation getId()
            {
                return this.id;
            }

            @Nullable
            public JsonObject serializeAdvancement()
            {
                return this.advancement.serializeToJson();
            }

            @Nullable
            public ResourceLocation getAdvancementId()
            {
                return this.advancementId;
            }
        }
    }
}
