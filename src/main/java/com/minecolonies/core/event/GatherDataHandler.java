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
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
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
    public static void dataGeneratorSetupServer(final GatherDataEvent.Server event)
    {
        final DataGenerator generator = event.getGenerator();
        RegistrySetBuilder enchRegBuilder = new RegistrySetBuilder().add(Registries.ENCHANTMENT, DefaultEnchantmentProvider::bootstrap);
        DatapackBuiltinEntriesProvider enchRegProvider = new DatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), enchRegBuilder, Set.of(Constants.MOD_ID, "minecraft"));
        generator.addProvider(true, enchRegProvider);
        final CompletableFuture<HolderLookup.Provider> provider = enchRegProvider.getRegistryProvider()
            .thenApply(p -> new DatagenLootTableManager(p, event.getResourceManager(PackType.SERVER_DATA)));

        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator.getPackOutput(), provider, provider);

        generator.addProvider(true, new DefaultSoundProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultItemModelProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultEntityIconProvider(generator));
        generator.addProvider(true, new DefaultStoriesProvider(generator.getPackOutput()));
        generator.addProvider(true, new QuestTranslationProvider(generator.getPackOutput()));

        generator.addProvider(true, new DefaultDamageTypeProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultAdvancementsProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new DefaultItemTagsProvider(generator.getPackOutput(), provider, blockTagsProvider, provider));
        generator.addProvider(true, new DefaultEntityTypeTagsProvider(generator.getPackOutput(), provider, provider));
        generator.addProvider(true, new DefaultDamageTagsProvider(generator.getPackOutput(), provider, provider));
        generator.addProvider(true, new DefaultResearchProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultBiomeTagsProvider(generator.getPackOutput(), provider, provider));
        generator.addProvider(true, new DefaultRecipeProvider.Runner(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultLootModifiersProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultDataMapsProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultRecruitmentItemsProvider(generator.getPackOutput()));

        // workers
        generator.addProvider(true, new DefaultAlchemistCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultBakerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultBlacksmithCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultConcreteMixerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultChefCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultCrusherCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultDyerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultEnchanterCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultFarmerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new LootTableProviders(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultFletcherCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultGlassblowerCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultLumberjackCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultMechanicCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultNetherWorkerLootProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultPlanterCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultSawmillCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultSifterCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultStonemasonCraftingProvider(generator.getPackOutput(), provider));
        generator.addProvider(true, new DefaultStoneSmelteryCraftingProvider(generator.getPackOutput(), provider));

        generator.addProvider(true, new ItemNbtCalculator(generator.getPackOutput(), provider));

        SchemFixerUtil.fixSchematics(provider);
    }

    public static void dataGeneratorSetupClient(final GatherDataEvent.Client event)
    {
        // MineColonies currently generates the complete pack from the server data run; retain the client hook so
        // either NeoForge datagen target can be invoked without registering against the abstract base event.
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
                new SubProviderEntry(DefaultCropsLootProvider::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(DefaultEntityLootProvider::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(DefaultBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(DefaultLuckyOreLootProvider::new, LootContextParamSets.BLOCK)
            ), provider);
        }

        @Override
        protected void validate(
          final WritableRegistry<LootTable> writableregistry,
          final ValidationContextSource validationcontext,
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
//                          if (id.identifier().equals(BuiltInLootTables.FISHING_FISH) ||
//                                id.identifier().equals(BuiltInLootTables.FISHING_JUNK) ||
//                                id.identifier().equals(BuiltInLootTables.FISHING_TREASURE))
//                          {
//                              return id.type() == LootDataType.TABLE ? (T) map.getOrDefault(id.identifier(), LootTable.EMPTY) : null;
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
