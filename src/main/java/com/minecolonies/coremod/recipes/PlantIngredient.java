package com.minecolonies.coremod.recipes;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import net.minecraft.item.crafting.Ingredient.IItemList;
import net.minecraft.item.crafting.Ingredient.SingleItemList;

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
            = Lazy.of(() -> new PlantIngredient(ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item instanceof BlockItem &&
                        (((BlockItem) item).getBlock() instanceof CropsBlock ||
                         ((BlockItem) item).getBlock() instanceof StemBlock))
                    .map(item -> new SingleItemList(new ItemStack(item)))));

    protected PlantIngredient(final Stream<? extends IItemList> itemLists)
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

        @NotNull
        @Override
        public PlantIngredient parse(@NotNull final PacketBuffer buffer)
        {
            return PlantIngredient.getInstance();
        }

        @Override
        public void write(@NotNull final PacketBuffer buffer, @NotNull final PlantIngredient ingredient)
        {
        }
    }
}
