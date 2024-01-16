package com.minecolonies.api.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * An ingredient that can be used in a vanilla recipe to require more than one item in a particular input slot.
 *
 * {
 *     "type": "minecolonies:counted",
 *     "item": {
 *         "item": "minecraft:cobblestone"  // could be a tag or something else
 *     },
 *     "count": 16
 * }
 */
public class CountedIngredient extends Ingredient
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "counted");

    @NotNull
    private final Ingredient child;
    private final int count;
    private ItemStack[] array = null;

    public CountedIngredient(@NotNull final Ingredient child, final int count)
    {
        super(Arrays.stream(child.getItems()).map(ItemValue::new));

        this.child = child;
        this.count = count;
    }

    /** The underlying ingredient. */
    @NotNull
    public Ingredient getChild() { return this.child; }

    /** The number of items required. */
    public int getCount() { return this.count; }

    @NotNull
    @Override
    public ItemStack[] getItems()
    {
        if (this.array == null)
        {
            final List<ItemStack> matchingStacks = Arrays.stream(this.child.getItems())
                    .map(ItemStack::copy).collect(Collectors.toList());
            matchingStacks.forEach(s -> s.setCount(this.count));
            this.array = matchingStacks.toArray(new ItemStack[matchingStacks.size()]);
        }
        return this.array;
    }

    @NotNull
    @Override
    public JsonElement toJson()
    {
        JsonObject json = new JsonObject();
        Serializer.getInstance().write(json, this);
        return json;
    }

    @NotNull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.getInstance();
    }

    public static class Serializer implements IIngredientSerializer<CountedIngredient>
    {
        private static final Serializer INSTANCE = new Serializer();

        public static Serializer getInstance() { return INSTANCE; }

        private Serializer() { }

        @NotNull
        @Override
        public CountedIngredient parse(@NotNull final JsonObject json)
        {
            final Ingredient child = Ingredient.fromJson(json.get("item"));
            final int count = GsonHelper.getAsInt(json, "count", 1);
            return new CountedIngredient(child, count);
        }

        public void write(@NotNull final JsonObject json, @NotNull final CountedIngredient ingredient)
        {
            json.addProperty("type", (Objects.requireNonNull(CraftingHelper.getID(this))).toString());

            json.add("item", ingredient.child.toJson());
            if (ingredient.getCount() > 1)
            {
                json.addProperty("count", ingredient.getCount());
            }
        }

        @NotNull
        @Override
        public CountedIngredient parse(@NotNull final FriendlyByteBuf buffer)
        {
            final int count = buffer.readVarInt();
            final Ingredient child = Ingredient.fromNetwork(buffer);
            return new CountedIngredient(child, count);
        }

        @Override
        public void write(@NotNull final FriendlyByteBuf buffer, @NotNull final CountedIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.getCount());
            CraftingHelper.write(buffer, ingredient.getChild());
        }
    }
}
