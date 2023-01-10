package com.minecolonies.coremod.event;

import com.minecolonies.coremod.generation.DatagenLootTableManager;
import com.minecolonies.coremod.generation.defaults.*;
import com.minecolonies.coremod.generation.defaults.workers.*;
import com.minecolonies.coremod.util.SchemFixerUtil;
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
        generator.addProvider(true, new DefaultBlockLootTableProvider(generator));
        generator.addProvider(true, new DefaultEntityLootProvider(generator));
        generator.addProvider(true, new DefaultSupplyLootProvider(generator));
        generator.addProvider(true, new DefaultAdvancementsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultSoundProvider(generator));
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new DefaultItemTagsProvider(generator, blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultEntityTypeTagsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultResearchProvider(generator));
        generator.addProvider(true, new DefaultRecipeProvider(generator));

        // workers
        generator.addProvider(true, new DefaultRecipeLootProvider(generator));
        generator.addProvider(true, new DefaultAlchemistCraftingProvider(generator));
        generator.addProvider(true, new DefaultBakerCraftingProvider(generator));
        generator.addProvider(true, new DefaultBlacksmithCraftingProvider(generator));
        generator.addProvider(true, new DefaultConcreteMixerCraftingProvider(generator));
        generator.addProvider(true, new DefaultCookAssistantCraftingProvider(generator));
        generator.addProvider(true, new DefaultCrusherCraftingProvider(generator));
        generator.addProvider(true, new DefaultDyerCraftingProvider(generator));
        generator.addProvider(true, new DefaultEnchanterCraftingProvider(generator));
        generator.addProvider(true, new DefaultFarmerCraftingProvider(generator));
        generator.addProvider(true, new DefaultFishermanLootProvider(generator));
        generator.addProvider(true, new DefaultFletcherCraftingProvider(generator));
        generator.addProvider(true, new DefaultGlassblowerCraftingProvider(generator));
        generator.addProvider(true, new DefaultLumberjackCraftingProvider(generator));
        generator.addProvider(true, new DefaultMechanicCraftingProvider(generator));
        generator.addProvider(true, new DefaultNetherWorkerLootProvider(generator, lootTableManager));
        generator.addProvider(true, new DefaultPlanterCraftingProvider(generator));
        generator.addProvider(true, new DefaultSawmillCraftingProvider(generator));
        generator.addProvider(true, new DefaultSifterCraftingProvider(generator, lootTableManager));
        generator.addProvider(true, new DefaultStonemasonCraftingProvider(generator));
        generator.addProvider(true, new DefaultStoneSmelteryCraftingProvider(generator));
    }
}
