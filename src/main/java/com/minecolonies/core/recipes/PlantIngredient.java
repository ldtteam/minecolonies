package com.minecolonies.core.recipes;

import com.minecolonies.apiimp.initializer.ModIngredientTypeInitializer;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

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
    private static final Lazy<PlantIngredient> INSTANCE
            = Lazy.of(() -> new PlantIngredient(BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof BlockItem &&
                        (((BlockItem) item).getBlock() instanceof CropBlock ||
                         ((BlockItem) item).getBlock() instanceof StemBlock))
                    .map(item -> new ItemValue(new ItemStack(item)))));

    public static final Codec<PlantIngredient> CODEC = Codec.unit(INSTANCE::get);

    private PlantIngredient(final Stream<? extends Value> itemLists)
    {
        super(itemLists, ModIngredientTypeInitializer.PLANT_INGREDIENT_TYPE);
    }

    @NotNull
    public static PlantIngredient getInstance()
    {
        return INSTANCE.get();
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj == getInstance();
    }

    @Override
    public boolean synchronizeWithContents()
    {
        // must be false so network sync forcefully uses codec
        return false;
    }
}
