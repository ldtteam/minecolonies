package com.minecolonies.api.crafting;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.item.crafting.Ingredient.SingleItemList;

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
        super(Arrays.stream(child.getItems()).map(SingleItemList::new));

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
            final int count = JSONUtils.getAsInt(json, "count", 1);
            return new CountedIngredient(child, count);
        }

        @NotNull
        @Override
        public CountedIngredient parse(@NotNull final PacketBuffer buffer)
        {
            final int count = buffer.readVarInt();
            final Ingredient child = Ingredient.fromNetwork(buffer);
            return new CountedIngredient(child, count);
        }

        @Override
        public void write(@NotNull final PacketBuffer buffer, @NotNull final CountedIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.getCount());
            CraftingHelper.write(buffer, ingredient.getChild());
        }
    }
}
