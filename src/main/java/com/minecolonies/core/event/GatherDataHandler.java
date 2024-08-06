package com.minecolonies.core.event;

import com.minecolonies.core.generation.DatagenLootTableManager;
import com.minecolonies.core.generation.ItemNbtCalculator;
import com.minecolonies.core.generation.defaults.*;
import com.minecolonies.core.generation.defaults.workers.*;
import com.minecolonies.core.util.SchemFixerUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        final DatagenLootTableManager lootTableManager = new DatagenLootTableManager(event.getExistingFileHelper());
        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());

        generator.addProvider(event.includeClient(), new DefaultSoundProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new DefaultItemModelProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new DefaultEntityIconProvider(generator));
        generator.addProvider(event.includeClient(), new DefaultStoriesProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient() && event.includeServer(), new QuestTranslationProvider(generator.getPackOutput()));

        generator.addProvider(event.includeServer(), new DefaultDamageTypeProvider(generator.getPackOutput(), event.getExistingFileHelper(), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new DefaultAdvancementsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DefaultItemTagsProvider(generator.getPackOutput(), event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultEntityTypeTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultDamageTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultResearchProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultRecipeProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new DefaultBiomeTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));

        // workers
        generator.addProvider(event.includeServer(), new DefaultAlchemistCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultBakerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultBlacksmithCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultConcreteMixerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultChefCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultCrusherCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultDyerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultEnchanterCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultFarmerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new LootTableProviders(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultFletcherCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultGlassblowerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultLumberjackCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultMechanicCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultNetherWorkerLootProvider(generator.getPackOutput(), lootTableManager));
        generator.addProvider(event.includeServer(), new DefaultPlanterCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultSawmillCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultSifterCraftingProvider(generator.getPackOutput(), lootTableManager));
        generator.addProvider(event.includeServer(), new DefaultStonemasonCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultStoneSmelteryCraftingProvider(generator.getPackOutput()));

        generator.addProvider(event.includeClient() && event.includeServer(), new ItemNbtCalculator(generator.getPackOutput(), event.getLookupProvider()));

        SchemFixerUtil.fixSchematics(event.getLookupProvider().join());
    }

    private static final class LootTableProviders extends LootTableProvider
    {
        public LootTableProviders(final PackOutput packOutput)
        {
            super(packOutput, Set.of(), List.of(
                new SubProviderEntry(DefaultFishermanLootProvider::new, LootContextParamSets.FISHING),
                new SubProviderEntry(DefaultRecipeLootProvider::new, LootContextParamSets.ALL_PARAMS),
                new SubProviderEntry(DefaultSupplyLootProvider::new, LootContextParamSets.CHEST),
                new SubProviderEntry(DefaultEntityLootProvider::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(DefaultBlockLootTableProvider::new, LootContextParamSets.BLOCK)
            ));
        }

        @Override
        protected void validate(final Map<ResourceLocation, LootTable> map, final ValidationContext validationcontext)
        {
            final ValidationContext newTracker =
                new ValidationContext(validationcontext.reporter, validationcontext.params, new LootDataResolver()
                {
                    public <T> T getElement(final LootDataId<T> id)
                    {
                        if (id.location().equals(BuiltInLootTables.FISHING_FISH) ||
                            id.location().equals(BuiltInLootTables.FISHING_JUNK) ||
                            id.location().equals(BuiltInLootTables.FISHING_TREASURE))
                        {
                            return id.type() == LootDataType.TABLE ? (T) map.getOrDefault(id.location(), LootTable.EMPTY) : null;
                        }
                        return validationcontext.resolver.getElement(id);
                    }
                }, validationcontext.visitedElements);

            super.validate(map, newTracker);
        }
    }
}
