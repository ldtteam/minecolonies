package com.minecolonies.core.generation.defaults;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.items.ItemFood;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
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
    protected void gather(@NotNull final HolderLookup.Provider provider)
    {
        generateCompostables(provider);
        bindEmptyItemPrototypesForDatagen();
    }
    private void generateCompostables(@NotNull final HolderLookup.Provider provider)
    {
        final Builder<Compostable, Item> builder = builder(NeoForgeDataMaps.COMPOSTABLES);
        // these items aren't registered in "getAllFoods"
        registerCompostItemFromNutrition(builder, ModItems.milkyBread.asItem(), 6f);
        registerCompostItemFromNutrition(builder, ModItems.sugaryBread.asItem(), 6f);
        registerCompostItemFromNutrition(builder, ModItems.goldenBread.asItem(), 6f);
        registerCompostItemFromNutrition(builder, ModItems.chorusBread.asItem(), 6f);
        for (final Item item : ModItems.getAllIngredients())
        {
            registerCompostItemFromNutrition(builder, item, 10f);
        }
        for (final Item item : ModItems.getAllFoods())
        {
            registerCompostItemFromNutrition(builder, item, 6f);
        }
        builder.add(ModItems.mistletoe.builtInRegistryHolder(), new Compostable(0.5f), false);
        for (final Block block : ModBlocks.getCrops())
        {
            builder.add(block.asItem().builtInRegistryHolder(), new Compostable(0.5f), false);
        }
        builder.add(ModBlocks.blockCompostedDirt.asItem().builtInRegistryHolder(), new Compostable(1.0f), false);
    }
    private static void registerCompostItemFromNutrition(final Builder<Compostable, Item> builder, final Item item, final float factor)
    {
        // Item component maps are not bound until after data-map gathering in 26.2,
        // so reading FOOD from an ItemStack is invalid here.
        if (item instanceof ItemFood food)
        {
            final float strength = Math.min(1.0f, food.getFoodNutrition() / factor);
            if (strength > 0)
            {
                builder.add(item.builtInRegistryHolder(), new Compostable(strength), false);
            }
        }
    }

    private static void bindEmptyItemPrototypesForDatagen()
    {
        // ItemStack's 26.2 constructors require a bound prototype component map.
        // Datagen runs before the normal server reload applies those maps, while
        // the custom recipe providers only serialize explicit item/count/patch
        // data. Bind an empty prototype for this isolated generator process so
        // those providers can continue to use ItemStack without changing runtime
        // component initialization.
        BuiltInRegistries.ITEM.listElements().forEach(holder -> holder.bindComponents(DataComponentMap.EMPTY));
    }
}
