package com.minecolonies.coremod.colony.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a sifting recipe, consisting of information about the mesh to be used,
 * and a list of input items and the results that they produce (via loot table).
 *
 * Note that it is legal for multiple recipes to exist with the same mesh type, but
 * they are expected to have different input items in that case.  The mesh properties
 * will be taken from one of them at random.
 */
@SuppressWarnings({"MethodReturnOfConcreteClass", "ClassWithoutLogger"})
public final class SifterRecipe
{
    public static final String RECIPE_TYPE_SIFTER = "sifting";
    public static final String MESH_PROP = "mesh";
    public static final String POWER_PROP = "power";
    public static final String BREAK_PROP = "break-chance";
    public static final String RECIPES_PROP = "recipes";

    private final ResourceLocation recipeId;

    private final ItemStack mesh;
    private final int power;
    private final double breakChance;

    private final List<CustomRecipe> recipes;

    /** Only constructable via parse() */
    private SifterRecipe(@NotNull final ResourceLocation recipeId,
                         @NotNull final ItemStack mesh,
                         final int power,
                         final double breakChance,
                         @NotNull final List<CustomRecipe> recipes)
    {
        this.recipeId = recipeId;
        this.mesh = mesh;
        this.power = power;
        this.breakChance = breakChance;
        this.recipes = Collections.unmodifiableList(recipes);
    }

    @NotNull
    public static SifterRecipe parse(@NotNull final ResourceLocation id, @NotNull final JsonObject recipeJson)
    {
        ItemStack mesh = ItemStack.EMPTY;

        if (recipeJson.has(MESH_PROP))
        {
            final Ingredient ingredient = Ingredient.deserialize(recipeJson.get(MESH_PROP));
            if (!ingredient.hasNoMatchingItems())
            {
                mesh = ingredient.getMatchingStacks()[0];
            }
        }

        final int power = JSONUtils.getInt(recipeJson, POWER_PROP, 10);
        final float breakChance = JSONUtils.getFloat(recipeJson, BREAK_PROP, 0);

        final List<CustomRecipe> recipes = new ArrayList<>();
        //noinspection ConstantConditions
        for (final Map.Entry<String, JsonElement> entry : JSONUtils.getJsonObject(recipeJson, RECIPES_PROP, new JsonObject()).entrySet())
        {
            final ResourceLocation inputId = new ResourceLocation(id.getNamespace(), id.getPath() + "/" + entry.getKey());
            recipes.add(CustomRecipe.parse(inputId, entry.getValue().getAsJsonObject()));
        }

        return new SifterRecipe(id, mesh, power, breakChance, recipes);
    }

    /** the id of this recipe */
    @NotNull public ResourceLocation getRecipeId() { return this.recipeId; }

    /** the mesh item required */
    @NotNull public ItemStack getMesh() { return this.mesh; }

    /**
     * the relative power of this mesh -- 0 is reserved for string and the higher the number
     * represents producing "better" outputs.  mainly this is just used to sort the meshes in
     * the GUI; it has no effect on actual drop rate.
     */
    public int getPower() { return this.power; }

    /** the percentage chance 0-100 that the mesh will break on any given operation */
    public double getBreakChance() { return this.breakChance; }

    /** the list of possible input items and their results */
    @NotNull public List<CustomRecipe> getInputRecipes() { return this.recipes; }
}
