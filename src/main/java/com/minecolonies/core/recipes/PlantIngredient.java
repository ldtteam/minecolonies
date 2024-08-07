package com.minecolonies.core.recipes;

import com.minecolonies.apiimp.initializer.ModIngredientTypeInitializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * An ingredient that can be used in a vanilla recipe to match plantable items.
 *
 * // any plant item
 * {
 *     "type": "minecolonies:plant"
 * }
 */
public class PlantIngredient implements ICustomIngredient
{
    private static final Lazy<PlantIngredient> INSTANCE = Lazy.of(PlantIngredient::new);

    public static final MapCodec<PlantIngredient> CODEC = MapCodec.unit(INSTANCE);

    private final List<ItemStack> items;

    private PlantIngredient()
    {
        items = BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof final BlockItem block &&
                        (block.getBlock() instanceof CropBlock || block.getBlock() instanceof StemBlock))
                .map(ItemStack::new)
                .toList();
    }

    @NotNull
    public static Ingredient of()
    {
        return INSTANCE.get().toVanilla();
    }

    @Override
    public boolean test(@Nullable final ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        return getItems().anyMatch(s -> stack.is(s.getItem()));
    }

    @NotNull
    @Override
    public Stream<ItemStack> getItems()
    {
        return items.stream();
    }

    @Override
    public boolean isSimple()
    {
        return true;
    }

    @NotNull
    @Override
    public IngredientType<?> getType()
    {
        return ModIngredientTypeInitializer.PLANT_INGREDIENT_TYPE.get();
    }
}
