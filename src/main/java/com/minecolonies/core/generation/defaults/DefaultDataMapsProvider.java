package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModFoodItems;
import com.minecolonies.api.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen for data maps.
 */
public class DefaultDataMapsProvider extends DataMapProvider
{
    public DefaultDataMapsProvider(@NotNull final PackOutput packOutput,
                                   @NotNull final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather()
    {
        generateCompostables();
    }

    private void generateCompostables()
    {
        final Builder<Compostable, Item> builder = builder(NeoForgeDataMaps.COMPOSTABLES);

        // these items aren't registered in "getAllFoods"
        registerCompostItemFromNutrition(builder, ModFoodItems.milkyBread.asItem(), 6f);
        registerCompostItemFromNutrition(builder, ModFoodItems.sugaryBread.asItem(), 6f);
        registerCompostItemFromNutrition(builder, ModFoodItems.goldenBread.asItem(), 6f);
        registerCompostItemFromNutrition(builder, ModFoodItems.chorusBread.asItem(), 6f);

        for (final Item item : ModFoodItems.INGREDIENTS)
        {
            registerCompostItemFromNutrition(builder, item, 10f);
        }
        for (final Item item : ModFoodItems.FOODS)
        {
            registerCompostItemFromNutrition(builder, item, 6f);
        }

        builder.add(ModItems.mistletoe.builtInRegistryHolder(), new Compostable(0.5f), false);

        for (final Block block : ModBlocks.CROPS)
        {
            builder.add(block.asItem().builtInRegistryHolder(), new Compostable(0.5f), false);
        }
        builder.add(ModBlocks.blockCompostedDirt.asItem().builtInRegistryHolder(), new Compostable(1.0f), false);
    }

    private static void registerCompostItemFromNutrition(final Builder<Compostable, Item> builder, final Item item, final float factor)
    {
        final FoodProperties food = item.getFoodProperties(new ItemStack(item), null);
        if (food != null)
        {
            final float strength = Math.min(1.0f, food.nutrition() / factor);
            if (strength > 0)
            {
                builder.add(item.builtInRegistryHolder(), new Compostable(strength), false);
            }
        }
    }
}
