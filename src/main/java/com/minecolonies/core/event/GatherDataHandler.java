package com.minecolonies.core.event;

import com.minecolonies.core.generation.DatagenLootTableManager;
import com.minecolonies.core.generation.ItemNbtCalculator;
import com.minecolonies.core.generation.defaults.*;
import com.minecolonies.core.generation.defaults.workers.*;
import com.minecolonies.core.util.SchemFixerUtil;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;

public class GatherDataHandler
{
    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     *
     * @param event event sent when you run the "runData" gradle task
     */
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        SchemFixerUtil.fixSchematics();

        final DataGenerator generator = event.getGenerator();
        final DatagenLootTableManager lootTableManager = new DatagenLootTableManager(event.getExistingFileHelper());
        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());

        generator.addProvider(event.includeClient(), new DefaultSoundProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new DefaultItemModelProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new DefaultEntityIconProvider(generator));
        generator.addProvider(event.includeClient(), new DefaultStoriesProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient() && event.includeServer(), new QuestTranslationProvider(generator.getPackOutput()));

        generator.addProvider(event.includeServer(), new DefaultBlockLootTableProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultEntityLootProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultSupplyLootProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultDamageTypeProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultAdvancementsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DefaultItemTagsProvider(generator.getPackOutput(), event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultEntityTypeTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultDamageTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultResearchProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultRecipeProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultBiomeTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultLuckyOreLootProvider(generator.getPackOutput()));

        // workers
        generator.addProvider(event.includeServer(), new DefaultRecipeLootProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultAlchemistCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultBakerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultBlacksmithCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultConcreteMixerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultChefCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultCrusherCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultDyerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultEnchanterCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultFarmerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new DefaultFishermanLootProvider(generator.getPackOutput()));
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
    }
}
