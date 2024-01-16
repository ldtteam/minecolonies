package com.minecolonies.core.event;

import com.minecolonies.core.generation.DatagenLootTableManager;
import com.minecolonies.core.generation.ItemNbtCalculator;
import com.minecolonies.core.generation.defaults.*;
import com.minecolonies.core.generation.defaults.workers.*;
import com.minecolonies.core.util.SchemFixerUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.world.level.storage.loot.LootTables;
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
        final LootTables lootTableManager = new DatagenLootTableManager(event.getExistingFileHelper());
        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator, event.getExistingFileHelper());

        generator.addProvider(event.includeClient(), new DefaultSoundProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new DefaultItemModelProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new DefaultEntityIconProvider(generator));
        generator.addProvider(event.includeClient() && event.includeServer(), new QuestTranslationProvider(generator));

        generator.addProvider(event.includeServer(), new DefaultBlockLootTableProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultEntityLootProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultSupplyLootProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultAdvancementsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DefaultItemTagsProvider(generator, blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultEntityTypeTagsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DefaultResearchProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultRecipeProvider(generator));

        // workers
        generator.addProvider(event.includeServer(), new DefaultRecipeLootProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultAlchemistCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultBakerCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultBlacksmithCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultConcreteMixerCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultCookAssistantCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultCrusherCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultDyerCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultEnchanterCraftingProvider(generator, lootTableManager));
        generator.addProvider(event.includeServer(), new DefaultFarmerCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultFishermanLootProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultFletcherCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultGlassblowerCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultLumberjackCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultMechanicCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultNetherWorkerLootProvider(generator, lootTableManager));
        generator.addProvider(event.includeServer(), new DefaultPlanterCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultSawmillCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultSifterCraftingProvider(generator, lootTableManager));
        generator.addProvider(event.includeServer(), new DefaultStonemasonCraftingProvider(generator));
        generator.addProvider(event.includeServer(), new DefaultStoneSmelteryCraftingProvider(generator));

        generator.addProvider(event.includeClient() && event.includeServer(), new ItemNbtCalculator(generator));
    }
}
