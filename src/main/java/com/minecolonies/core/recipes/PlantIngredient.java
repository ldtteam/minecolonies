package com.minecolonies.core.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.IIngredientSerializer;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An ingredient that can be used in a vanilla recipe to match plantable items.
 *
 * // any plant item
 * {
 *     "type": "minecolonies:plant"
 * }
 */
public class PlantIngredient extends Ingredient
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "plant");

    private static final Lazy<PlantIngredient> INSTANCE
            = Lazy.of(() -> new PlantIngredient(BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof BlockItem &&
                        (((BlockItem) item).getBlock() instanceof CropBlock ||
                         ((BlockItem) item).getBlock() instanceof StemBlock))
                    .map(item -> new ItemValue(new ItemStack(item)))));

    protected PlantIngredient(final Stream<? extends Value> itemLists)
    {
        super(itemLists);
    }

    @NotNull
    public static PlantIngredient getInstance()
    {
        return INSTANCE.get();
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

    public static class Serializer implements IIngredientSerializer<PlantIngredient>
    {
        private static final Serializer INSTANCE = new Serializer();

        public static Serializer getInstance() { return INSTANCE; }

        private Serializer() { }

        @NotNull
        @Override
        public PlantIngredient parse(@NotNull final JsonObject json)
        {
            return PlantIngredient.getInstance();
        }

        public void write(@NotNull final JsonObject json, @NotNull final PlantIngredient ingredient)
        {
            json.addProperty("type", (Objects.requireNonNull(CraftingHelper.getID(this))).toString());
        }

        @NotNull
        @Override
        public PlantIngredient parse(@NotNull final FriendlyByteBuf buffer)
        {
            return PlantIngredient.getInstance();
        }

        @Override
        public void write(@NotNull final FriendlyByteBuf buffer, @NotNull final PlantIngredient ingredient)
        {
        }
    }
}
