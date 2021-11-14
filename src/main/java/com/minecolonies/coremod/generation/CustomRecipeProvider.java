package com.minecolonies.coremod.generation;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public abstract class CustomRecipeProvider implements IDataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    protected final DataGenerator generator;

    public CustomRecipeProvider(final DataGenerator generatorIn)
    {
        this.generator = generatorIn;
    }

    @Override
    public void run(final DirectoryCache cache) throws IOException
    {
        final Path path = this.generator.getOutputFolder();
        final Set<ResourceLocation> set = Sets.newHashSet();
        registerRecipes((recipe) ->
        {
            if (!set.add(recipe.getId()))
            {
                throw new IllegalStateException("Duplicate recipe " + recipe.getId());
            }
            else
            {
                saveRecipe(cache, recipe.serializeRecipe(), path.resolve("data/" + recipe.getId().getNamespace() + "/crafterrecipes/" + recipe.getId().getPath() + ".json"));
            }
        });
    }

    private static void saveRecipe(final DirectoryCache cache, final JsonObject jsonObject, final Path recipeJson)
    {
        try
        {
            final String json = GSON.toJson(jsonObject);
            final String hash = SHA1.hashUnencodedChars(json).toString();
            if (!Objects.equals(cache.getHash(recipeJson), hash) || !Files.exists(recipeJson))
            {
                Files.createDirectories(recipeJson.getParent());

                try (final BufferedWriter bufferedwriter = Files.newBufferedWriter(recipeJson))
                {
                    bufferedwriter.write(json);
                }
            }

            cache.putNew(recipeJson, hash);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save recipe {}", recipeJson, ioexception);
        }
    }

    protected abstract void registerRecipes(final Consumer<IFinishedRecipe> consumer);

    public static class CustomRecipeBuilder
    {
        private final JsonObject json = new JsonObject();
        private final ResourceLocation id;
        private Block intermediate = Blocks.AIR;

        private CustomRecipeBuilder(final String crafter, final String id)
        {
            this.json.addProperty(CustomRecipe.RECIPE_TYPE_PROP, CustomRecipe.RECIPE_TYPE_RECIPE);
            this.json.addProperty(CustomRecipe.RECIPE_CRAFTER_PROP, crafter);
            this.id = new ResourceLocation(Constants.MOD_ID, crafter + "/" + id);
        }

        @NotNull
        public static CustomRecipeBuilder create(final String crafter, final String id)
        {
            return new CustomRecipeBuilder(crafter, id);
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
            this.json.addProperty(CustomRecipe.RECIPE_RESULT_PROP, result.getItem().getRegistryName().toString());
            if (result.getCount() != 1)
            {
                this.json.addProperty(COUNT_PROP, result.getCount());
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

        @NotNull
        public CustomRecipeBuilder minResearchId(@NotNull final ResourceLocation researchId)
        {
            this.json.addProperty(CustomRecipe.RECIPE_RESEARCHID_PROP, researchId.toString());
            return this;
        }

        @NotNull
        public CustomRecipeBuilder maxResearchId(@NotNull final ResourceLocation researchId)
        {
            this.json.addProperty(CustomRecipe.RECIPE_EXCLUDED_RESEARCHID_PROP, researchId.toString());
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

        public void build(@NotNull final Consumer<IFinishedRecipe> consumer)
        {
            this.json.addProperty(CustomRecipe.RECIPE_INTERMEDIATE_PROP, this.intermediate.getRegistryName().toString());
            consumer.accept(new Result(this.json, this.id));
        }

        @NotNull
        private JsonArray stackAsJson(final List<ItemStack> itemStacks)
        {
            final JsonArray jsonItemStacks = new JsonArray();
            for (final ItemStack itemStack : itemStacks)
            {
                final JsonObject jsonItemStack = new JsonObject();
                String name = itemStack.getItem().getRegistryName().toString();
                // this could be incorrect for items with both damage and other NBT,
                // but that should be rare, and this avoids some annoyance.
                if (itemStack.hasTag() && !itemStack.isDamageableItem())
                {
                    name += itemStack.getTag().toString();
                }
                jsonItemStack.addProperty(ITEM_PROP, name);
                if (itemStack.getCount() != 1)
                {
                    jsonItemStack.addProperty(COUNT_PROP, itemStack.getCount());
                }
                jsonItemStacks.add(jsonItemStack);
            }
            return jsonItemStacks;
        }

        @NotNull
        private JsonArray storageAsJson(final List<ItemStorage> itemStorages)
        {
            final JsonArray jsonItemStorages = new JsonArray();
            for (final ItemStorage itemStorage : itemStorages)
            {
                final ItemStack stack = itemStorage.getItemStack();
                final JsonObject jsonItemStorage = new JsonObject();
                String name = stack.getItem().getRegistryName().toString();
                // this could be incorrect for items with both damage and other NBT,
                // but that should be rare, and this avoids some annoyance.
                if (stack.hasTag() && !stack.isDamageableItem())
                {
                    name += stack.getTag().toString();
                }
                jsonItemStorage.addProperty(ITEM_PROP, name);
                if (itemStorage.ignoreNBT())
                {
                    jsonItemStorage.addProperty(MATCHTYPE_PROP, MATCH_NBTIGNORE);
                }
                if (itemStorage.getAmount() != 1)
                {
                    jsonItemStorage.addProperty(COUNT_PROP, itemStorage.getAmount());
                }
                jsonItemStorages.add(jsonItemStorage);
            }
            return jsonItemStorages;
        }

        private static class Result implements IFinishedRecipe
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
            public IRecipeSerializer<?> getType()
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
