package com.minecolonies.core.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract datagen for crafterrecipes
 */
public abstract class CustomRecipeProvider implements DataProvider
{
    private final PackOutput packOutput;

    public CustomRecipeProvider(@NotNull final PackOutput packOutput)
    {
        this.packOutput = packOutput;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final PackOutput.PathProvider pathProvider = this.packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "crafterrecipes");
        final Map<ResourceLocation, CompletableFuture<?>> futures = new HashMap<>();

        registerRecipes((recipe) ->
        {
            if (futures.containsKey(recipe.getId()))
            {
                throw new IllegalStateException("Duplicate recipe " + recipe.getId());
            }

            futures.put(recipe.getId(), DataProvider.saveStable(cache,
                    recipe.serializeRecipe(),
                    pathProvider.json(recipe.getId())));
        });

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]));
    }

    protected abstract void registerRecipes(final Consumer<FinishedRecipe> consumer);

    /**
     * Helper to construct custom crafterrecipes for datagen
     */
    public static class CustomRecipeBuilder
    {
        private final JsonObject json = new JsonObject();
        private final ResourceLocation id;
        private Block intermediate = Blocks.AIR;

        private CustomRecipeBuilder(final String crafter, final String module, final String id)
        {
            this.json.addProperty(CustomRecipe.RECIPE_TYPE_PROP, CustomRecipe.RECIPE_TYPE_RECIPE);
            this.json.addProperty(CustomRecipe.RECIPE_CRAFTER_PROP, crafter + "_" + module);
            this.id = new ResourceLocation(Constants.MOD_ID, crafter + "/" + id);
        }

        @NotNull
        public static CustomRecipeBuilder create(final String crafter, final String module, final String id)
        {
            return new CustomRecipeBuilder(crafter, module, id);
        }

        @NotNull
        public CustomRecipeBuilder inputs(@NotNull final List<ItemStorage> inputs)
        {
            this.json.add(CustomRecipe.RECIPE_INPUTS_PROP, storageAsJson(inputs));
            return this;
        }

        @NotNull
        public CustomRecipeBuilder result(@NotNull final ItemStack result)
        {
            final JsonObject jsonItemStack = stackAsJson(result);

            this.json.addProperty(CustomRecipe.RECIPE_RESULT_PROP, jsonItemStack.get(ITEM_PROP).getAsString());
            if (jsonItemStack.has(COUNT_PROP))
            {
                this.json.add(COUNT_PROP, jsonItemStack.get(COUNT_PROP));
            }
            return this;
        }

        @NotNull
        public CustomRecipeBuilder lootTable(@NotNull final ResourceLocation lootTable)
        {
            this.json.addProperty(CustomRecipe.RECIPE_LOOTTABLE_PROP, lootTable.toString());
            return this;
        }

        @NotNull
        public CustomRecipeBuilder requiredTool(@NotNull final IToolType toolType)
        {
            if (toolType != ToolType.NONE)
            {
                this.json.addProperty(CustomRecipe.RECIPE_TOOL_PROP, toolType.getName());
            }
            return this;
        }

        @NotNull
        public CustomRecipeBuilder secondaryOutputs(@NotNull final List<ItemStack> secondary)
        {
            this.json.add(CustomRecipe.RECIPE_SECONDARY_PROP, stackAsJson(secondary));
            return this;
        }

        @NotNull
        public CustomRecipeBuilder alternateOutputs(@NotNull final List<ItemStack> alternates)
        {
            this.json.add(CustomRecipe.RECIPE_ALTERNATE_PROP, stackAsJson(alternates));
            return this;
        }

        @NotNull
        public CustomRecipeBuilder intermediate(@NotNull final Block intermediate)
        {
            this.intermediate = intermediate;
            return this;
        }

        /**
         * Sets a research id that is required before this recipe is available.  Can be called multiple times to add
         * additional researches, all of which will be required before this recipe is available.
         * @param researchId the required research id.
         */
        @NotNull
        public CustomRecipeBuilder minResearchId(@NotNull final ResourceLocation researchId)
        {
            JsonElement ids = this.json.get(CustomRecipe.RECIPE_RESEARCHID_PROP);
            if (ids == null)
            {
                this.json.addProperty(CustomRecipe.RECIPE_RESEARCHID_PROP, researchId.toString());
            }
            else if (ids.isJsonArray())
            {
                ids.getAsJsonArray().add(researchId.toString());
            }
            else
            {
                final JsonArray array = new JsonArray();
                array.add(ids.getAsString());
                array.add(researchId.toString());

                this.json.remove(CustomRecipe.RECIPE_RESEARCHID_PROP);
                this.json.add(CustomRecipe.RECIPE_RESEARCHID_PROP, array);
            }
            return this;
        }

        /**
         * Sets a research id that is required before this recipe is no longer available.  Can be called multiple
         * times to add additional researches, all of which will be required before this recipe is removed.
         * @param researchId the excluded research id.
         */
        @NotNull
        public CustomRecipeBuilder maxResearchId(@NotNull final ResourceLocation researchId)
        {
            JsonElement ids = this.json.get(CustomRecipe.RECIPE_EXCLUDED_RESEARCHID_PROP);
            if (ids == null)
            {
                this.json.addProperty(CustomRecipe.RECIPE_EXCLUDED_RESEARCHID_PROP, researchId.toString());
            }
            else if (ids.isJsonArray())
            {
                ids.getAsJsonArray().add(researchId.toString());
            }
            else
            {
                final JsonArray array = new JsonArray();
                array.add(ids.getAsString());
                array.add(researchId.toString());

                this.json.remove(CustomRecipe.RECIPE_EXCLUDED_RESEARCHID_PROP);
                this.json.add(CustomRecipe.RECIPE_EXCLUDED_RESEARCHID_PROP, array);
            }
            return this;
        }

        @NotNull
        public CustomRecipeBuilder minBuildingLevel(final int level)
        {
            this.json.addProperty(CustomRecipe.RECIPE_BUILDING_MIN_LEVEL_PROP, level);
            return this;
        }

        @NotNull
        public CustomRecipeBuilder maxBuildingLevel(final int level)
        {
            this.json.addProperty(CustomRecipe.RECIPE_BUILDING_MAX_LEVEL_PROP, level);
            return this;
        }

        @NotNull
        public CustomRecipeBuilder mustExist(final boolean value)
        {
            this.json.addProperty(CustomRecipe.RECIPE_MUST_EXIST, value);
            return this;
        }

        @NotNull
        public CustomRecipeBuilder showTooltip(final boolean value)
        {
            this.json.addProperty(CustomRecipe.RECIPE_SHOW_TOOLTIP, value);
            return this;
        }

        public void build(@NotNull final Consumer<FinishedRecipe> consumer)
        {
            this.json.addProperty(CustomRecipe.RECIPE_INTERMEDIATE_PROP, BuiltInRegistries.BLOCK.getKey(this.intermediate).toString());
            consumer.accept(new Result(this.json, this.id));
        }

        @NotNull
        private JsonObject stackAsJson(final ItemStack stack)
        {
            final JsonObject jsonItemStack = new JsonObject();
            String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            // this could be incorrect for items with both damage and other NBT,
            // but that should be rare, and this avoids some annoyance.
            if (stack.hasTag() && !stack.isDamageableItem())
            {
                name += stack.getTag().toString();
            }
            jsonItemStack.addProperty(ITEM_PROP, name);
            if (stack.getCount() != 1)
            {
                jsonItemStack.addProperty(COUNT_PROP, stack.getCount());
            }
            return jsonItemStack;
        }

        @NotNull
        private JsonArray stackAsJson(final List<ItemStack> itemStacks)
        {
            final JsonArray jsonItemStacks = new JsonArray();
            for (final ItemStack itemStack : itemStacks)
            {
                jsonItemStacks.add(stackAsJson(itemStack));
            }
            return jsonItemStacks;
        }

        @NotNull
        private JsonArray storageAsJson(final List<ItemStorage> itemStorages)
        {
            final JsonArray jsonItemStorages = new JsonArray();
            for (final ItemStorage itemStorage : itemStorages)
            {
                final JsonObject jsonItemStorage = stackAsJson(itemStorage.getItemStack());
                if (itemStorage.getAmount() == 1)
                {
                    jsonItemStorage.remove(COUNT_PROP);
                }
                else
                {
                    jsonItemStorage.addProperty(COUNT_PROP, itemStorage.getAmount());
                }
                if (itemStorage.ignoreNBT())
                {
                    jsonItemStorage.addProperty(MATCHTYPE_PROP, MATCH_NBTIGNORE);
                }
                jsonItemStorages.add(jsonItemStorage);
            }
            return jsonItemStorages;
        }

        private static class Result implements FinishedRecipe
        {
            final JsonObject json;
            final ResourceLocation id;

            public Result(final JsonObject json, final ResourceLocation id)
            {
                this.json = json;
                this.id = id;
            }

            @NotNull
            @Override
            public JsonObject serializeRecipe()
            {
                return this.json;
            }

            @NotNull
            @Override
            public ResourceLocation getId()
            {
                return this.id;
            }

            @Override
            public void serializeRecipeData(@NotNull final JsonObject json)
            {
            }

            @Override
            public RecipeSerializer<?> getType()
            {
                return null;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement()
            {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId()
            {
                return null;
            }
        }
    }
}
