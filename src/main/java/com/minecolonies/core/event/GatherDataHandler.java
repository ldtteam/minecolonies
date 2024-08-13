package com.minecolonies.core.event;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.generation.DatagenLootTableManager;
import com.minecolonies.core.generation.ItemNbtCalculator;
import com.minecolonies.core.generation.defaults.*;
import com.minecolonies.core.generation.defaults.workers.*;
import com.minecolonies.core.util.SchemFixerUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GatherDataHandler
{
    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     *
     * @param event event sent when you run the "runData" gradle task
     */
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        final DataGenerator generator = event.getGenerator();
        final CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider().thenApply(p -> new DatagenLootTableManager(p, event.getExistingFileHelper()));

        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator.getPackOutput(), provider, event.getExistingFileHelper());

        // todo: is this needed?  the provider already contains enchantments without needing to do anything special
        RegistrySetBuilder enchRegBuilder = new RegistrySetBuilder().add(Registries.ENCHANTMENT, DefaultEnchantmentProvider::bootstrap).add(Registries.LOOT_TABLE, (context) -> {});
        DatapackBuiltinEntriesProvider enchRegProvider = new DatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(), provider, enchRegBuilder, Set.of(Constants.MOD_ID, "minecraft"));
        generator.addProvider(true, enchRegProvider);

        DatapackBuiltinEntriesProvider datapackEntries = new DatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(), provider, enchRegBuilder, Set.of(Constants.MOD_ID));

        generator.addProvider(event.includeClient(), new DefaultSoundProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new DefaultItemModelProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new DefaultEntityIconProvider(generator));
        generator.addProvider(event.includeClient(), new DefaultStoriesProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient() && event.includeServer(), new QuestTranslationProvider(generator.getPackOutput()));

        generator.addProvider(event.includeServer(), new DefaultDamageTypeProvider(generator.getPackOutput(), event.getExistingFileHelper(), provider));
        generator.addProvider(event.includeServer(), new DefaultAdvancementsProvider(generator.getPackOutput(), provider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DefaultItemTagsProvider(generator.getPackOutput(), provider, blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultEntityTypeTagsProvider(generator.getPackOutput(), provider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultDamageTagsProvider(generator.getPackOutput(), provider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultResearchProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultRecipeProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultBiomeTagsProvider(generator.getPackOutput(), provider, event.getExistingFileHelper()));

        // workers
        generator.addProvider(event.includeServer(), new DefaultAlchemistCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultBakerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultBlacksmithCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultConcreteMixerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultChefCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultCrusherCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultDyerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultEnchanterCraftingProvider(generator.getPackOutput(), datapackEntries.getRegistryProvider()));
        generator.addProvider(event.includeServer(), new DefaultFarmerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new LootTableProviders(generator.getPackOutput(), datapackEntries.getRegistryProvider()));
        generator.addProvider(event.includeServer(), new DefaultFletcherCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultGlassblowerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultLumberjackCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultMechanicCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultNetherWorkerLootProvider(generator.getPackOutput(), datapackEntries.getRegistryProvider()));
        generator.addProvider(event.includeServer(), new DefaultPlanterCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultSawmillCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultSifterCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultStonemasonCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(event.includeServer(), new DefaultStoneSmelteryCraftingProvider(generator.getPackOutput(), provider));

        generator.addProvider(event.includeServer(), new ItemNbtCalculator(generator.getPackOutput(), provider));

        SchemFixerUtil.fixSchematics(provider);
    }

    // todo: move this back to SimpleLootTableProvider?
    private static final class LootTableProviders extends LootTableProvider
    {
        public LootTableProviders(final PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider)
        {
            super(packOutput, Set.of(), List.of(
                new SubProviderEntry(DefaultFishermanLootProvider::new, LootContextParamSets.FISHING),
                new SubProviderEntry(DefaultRecipeLootProvider::new, LootContextParamSets.ALL_PARAMS),
                new SubProviderEntry(DefaultSupplyLootProvider::new, LootContextParamSets.CHEST),
                new SubProviderEntry(DefaultEntityLootProvider::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(DefaultBlockLootTableProvider::new, LootContextParamSets.BLOCK)
            ), provider);
        }

        @Override
        protected void validate(
          final WritableRegistry<LootTable> writableregistry,
          final ValidationContext validationcontext,
          final ProblemReporter.Collector problemreporter$collector)
        {
            // todo this might be a bit aggressive, someone should adjust this.
        }

        // NOTE: I'm reasonably sure this code will not be required due to usage of the DatagenLootTableManager, but keeping for now just in case
//        @Override
//        protected void validate(WritableRegistry<LootTable> writableregistry, ValidationContext validationcontext, ProblemReporter.Collector collector)
//        {
//            {
//                final ValidationContext newTracker =
//                  new ValidationContext(validationcontext.reporter, validationcontext.params, new LootDataResolver()
//                  {
//                      public <T> T getElement(final LootDataId<T> id)
//                      {
//                          if (id.location().equals(BuiltInLootTables.FISHING_FISH) ||
//                                id.location().equals(BuiltInLootTables.FISHING_JUNK) ||
//                                id.location().equals(BuiltInLootTables.FISHING_TREASURE))
//                          {
//                              return id.type() == LootDataType.TABLE ? (T) map.getOrDefault(id.location(), LootTable.EMPTY) : null;
//                          }
//                          return validationcontext.resolver.getElement(id);
//                      }
//                  }, validationcontext.visitedElements);
//
//                super.validate(map, newTracker);
//            }
//        }
    }
}
