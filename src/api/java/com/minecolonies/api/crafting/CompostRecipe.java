package com.minecolonies.api.crafting;

import com.google.gson.JsonObject;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A vanilla recipe class describing the operation of the compost barrel.  Primarily this is just used to
 * assign different "strength" ratings to different input items; currently the actual outputs of the barrel
 * are fixed.
 */
public class CompostRecipe implements IRecipe<IInventory>
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "composting");
    public static final IRecipeType<CompostRecipe> TYPE = IRecipeType.register(ID.toString());

    private static final int FERMENT_TIME = 24000;
    private static final int COMPOST_RESULT = 6;

    private final ResourceLocation id;

    private final Ingredient input;
    private final ItemStack output;
    private final int strength;

    public CompostRecipe(@NotNull final ResourceLocation id, @NotNull final Ingredient ingredient, final int strength)
    {
        this.id = id;
        this.input = ingredient;
        this.strength = strength;
        this.output = new ItemStack(ModItems.compost, COMPOST_RESULT);
    }

    @NotNull
    @Override
    public IRecipeType<?> getType() { return TYPE; }

    @NotNull
    @Override
    public ResourceLocation getId() { return this.id; }

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
    public boolean matches(final IInventory inv, final World worldIn)
    {
        return false;
    }

    @NotNull
    @Override
    public ItemStack assemble(final IInventory inv)
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
    public ItemStack getResultItem()
    {
        return this.output;
    }

    @NotNull
    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return Serializer.getInstance();
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
        ingredients.add(new CountedIngredient(this.input, calculateIngredientCount()));
        return ingredients;
    }

    private int calculateIngredientCount()
    {
        return AbstractTileEntityBarrel.MAX_ITEMS / this.strength;
    }

    // JEI looks better if we render these as many individual recipes rather than as
    // a few recipes with a massive number of alternative ingredients.
    @NotNull
    public static CompostRecipe individualize(@NotNull final Item item, @NotNull final CompostRecipe recipe)
    {
        return new CompostRecipe(recipe.getId(), Ingredient.of(item), recipe.getStrength());
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
            implements IRecipeSerializer<CompostRecipe>
    {
        private static final Serializer INSTANCE = new Serializer();

        public static Serializer getInstance() { return INSTANCE; }

        private Serializer() { }

        @NotNull
        @Override
        public CompostRecipe fromJson(@NotNull final ResourceLocation recipeId, @NotNull final JsonObject json)
        {
            final Ingredient ingredient = Ingredient.fromJson(json.get("input"));
            final int strength = JSONUtils.getAsInt(json, "strength", 1);

            return new CompostRecipe(recipeId, ingredient, strength);
        }

        @Nullable
        @Override
        public CompostRecipe fromNetwork(@NotNull final ResourceLocation recipeId, @NotNull final PacketBuffer buffer)
        {
            final Ingredient ingredient = Ingredient.fromNetwork(buffer);
            final int strength = buffer.readVarInt();

            return new CompostRecipe(recipeId, ingredient, strength);
        }

        @Override
        public void toNetwork(@NotNull final PacketBuffer buffer, @NotNull final CompostRecipe recipe)
        {
            recipe.getInput().toNetwork(buffer);
            buffer.writeVarInt(recipe.getStrength());
        }
    }
}
