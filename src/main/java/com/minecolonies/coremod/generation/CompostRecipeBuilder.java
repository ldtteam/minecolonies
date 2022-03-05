package com.minecolonies.coremod.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.CompostRecipe;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Datagen for {@link CompostRecipe}
 */
public class CompostRecipeBuilder
{
    private final List<Ingredient> inputs = new ArrayList<>();
    private final int strength;

    public CompostRecipeBuilder(final int strength)
    {
        this.strength = strength;
    }

    public static CompostRecipeBuilder strength(final int strength)
    {
        return new CompostRecipeBuilder(strength);
    }

    public CompostRecipeBuilder input(@NotNull final Ingredient ingredient)
    {
        this.inputs.add(ingredient);
        return this;
    }

    public void save(@NotNull final Consumer<IFinishedRecipe> consumer,
                     @NotNull final ResourceLocation id)
    {
        // vanilla Ingredient.merge does not take custom ingredient types into account in an alternatives array
        final JsonArray inputsJson = new JsonArray();
        for (final Ingredient input : this.inputs)
        {
            inputsJson.add(input.toJson());
        }
        final Ingredient merged = CraftingHelper.getIngredient(inputsJson);
        consumer.accept(new Result(id, merged, this.strength));
    }

    public static class Result implements IFinishedRecipe
    {
        private final ResourceLocation id;
        private final Ingredient input;
        private final int strength;

        public Result(@NotNull final ResourceLocation id,
                      @NotNull final Ingredient input,
                      final int strength)
        {
            this.id = id;
            this.input = input;
            this.strength = strength;
        }

        public void serializeRecipeData(@NotNull final JsonObject json)
        {
            json.add("input", this.input.toJson());
            json.addProperty("strength", this.strength);
        }

        @NotNull
        public IRecipeSerializer<?> getType() {
            return CompostRecipe.Serializer.getInstance();
        }

        @NotNull
        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return new ResourceLocation("");
        }
    }
}