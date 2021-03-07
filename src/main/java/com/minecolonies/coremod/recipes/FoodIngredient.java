package com.minecolonies.coremod.recipes;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class FoodIngredient extends Ingredient
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "food");

    public static final Predicate<ItemStack> ISFOOD
            = stack -> ItemStackUtils.isNotEmpty(stack) && stack.getItem().isFood() && stack.getItem().getFood().getHealing() > 0 && stack.getItem().getFood().getSaturation() > 0;

    private static final Lazy<FoodIngredient> INSTANCE
            = Lazy.of(() -> new FoodIngredient(ForgeRegistries.ITEMS.getValues().stream()
                .map(ItemStack::new)
                .filter(ISFOOD)
                .map(SingleItemList::new)));

    protected FoodIngredient(final Stream<? extends IItemList> itemLists)
    {
        super(itemLists);
    }

    @NotNull
    public static FoodIngredient getInstance()
    {
        return INSTANCE.get();
    }

    @NotNull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<FoodIngredient>
    {
        public static final Serializer INSTANCE = new Serializer();

        @NotNull
        @Override
        public FoodIngredient parse(@NotNull final JsonObject json)
        {
            return FoodIngredient.getInstance();
        }

        @NotNull
        @Override
        public FoodIngredient parse(@NotNull final PacketBuffer buffer)
        {
            return FoodIngredient.getInstance();
        }

        @Override
        public void write(@NotNull final PacketBuffer buffer, @NotNull final FoodIngredient ingredient)
        {
        }
    }
}
