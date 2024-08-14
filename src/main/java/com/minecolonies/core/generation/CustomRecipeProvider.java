package com.minecolonies.core.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract datagen for crafterrecipes
 */
public abstract class CustomRecipeProvider implements DataProvider
{
    private final PackOutput                               packOutput;
    private final CompletableFuture<HolderLookup.Provider> providerFuture;
    protected HolderLookup.Provider                        provider;

    public CustomRecipeProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> providerFuture)
    {
        this.packOutput = packOutput;
        this.providerFuture = providerFuture;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        return providerFuture.thenCompose(provider ->
        {
            this.provider = provider;

            final PackOutput.PathProvider pathProvider = this.packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "crafterrecipes");
            final List<CompletableFuture<?>> futures = new ArrayList<>();
            final Set<ResourceLocation> dupeKeyCheck = new HashSet<>();

            registerRecipes((recipe) ->
            {
                if (!dupeKeyCheck.add(recipe.id))
                {
                    throw new IllegalStateException("Duplicate recipe " + recipe.id);
                }
                futures.add(DataProvider.saveStable(cache,
                        recipe.json,
                        pathProvider.json(recipe.id)));
            });

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @NotNull
    protected CustomRecipeBuilder recipe(final String crafter, final String module, final String id)
    {
        return new CustomRecipeBuilder(crafter, module, id, provider);
    }

    protected abstract void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer);

    /**
     * Helper to construct custom crafterrecipes for datagen
     */
    public static class CustomRecipeBuilder
    {
        private final HolderLookup.Provider provider;
        private final JsonObject json = new JsonObject();
        private final ResourceLocation id;
        private Block intermediate = Blocks.AIR;

        public CustomRecipeBuilder(final String crafter, final String module, final String id,
                                   @NotNull final HolderLookup.Provider provider)
        {
            this.provider = provider;
            this.json.addProperty(CustomRecipe.RECIPE_TYPE_PROP, CustomRecipe.RECIPE_TYPE_RECIPE);
            this.json.addProperty(CustomRecipe.RECIPE_CRAFTER_PROP, crafter + "_" + module);
            this.id = new ResourceLocation(Constants.MOD_ID, crafter + "/" + id);
        }

        @NotNull
        public static CustomRecipeBuilder create(final String crafter, final String module, final String id,
                                                 @NotNull final HolderLookup.Provider provider)
        {
            return new CustomRecipeBuilder(crafter, module, id, provider);
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

        public void build(@NotNull final Consumer<CustomRecipeBuilder> consumer)
        {
            this.json.addProperty(CustomRecipe.RECIPE_INTERMEDIATE_PROP, BuiltInRegistries.BLOCK.getKey(this.intermediate).toString());
            consumer.accept(this);
        }

        @NotNull
        private JsonObject stackAsJson(final ItemStack stack)
        {
            final JsonObject jsonItemStack = new JsonObject();
            String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (!stack.isComponentsPatchEmpty())
            {
                name += Utils.serializeCodecMessToJson(DataComponentPatch.CODEC, provider, stack.getComponentsPatch());
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
    }
}
