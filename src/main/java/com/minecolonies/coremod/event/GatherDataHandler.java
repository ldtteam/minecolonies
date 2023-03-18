package com.minecolonies.coremod.event;

import com.minecolonies.coremod.generation.DatagenLootTableManager;
import com.minecolonies.coremod.generation.defaults.*;
import com.minecolonies.coremod.generation.defaults.workers.*;
import com.minecolonies.coremod.util.SchemFixerUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.LootTables;
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
        final LootTables lootTableManager = new DatagenLootTableManager(event.getExistingFileHelper());
        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
        generator.addProvider(true, new DefaultBlockLootTableProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultEntityLootProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultSupplyLootProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultAdvancementsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultSoundProvider(generator.getPackOutput()));
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new DefaultItemTagsProvider(generator.getPackOutput(), event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultEntityTypeTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultResearchProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultRecipeProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultDamageTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));

        // workers
        generator.addProvider(true, new DefaultRecipeLootProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultAlchemistCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultBakerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultBlacksmithCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultConcreteMixerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultCookAssistantCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultCrusherCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultDyerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultEnchanterCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultFarmerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultFishermanLootProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultFletcherCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultGlassblowerCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultLumberjackCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultMechanicCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultNetherWorkerLootProvider(generator.getPackOutput(), lootTableManager));
        generator.addProvider(true, new DefaultPlanterCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultSawmillCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultSifterCraftingProvider(generator.getPackOutput(), lootTableManager));
        generator.addProvider(true, new DefaultStonemasonCraftingProvider(generator.getPackOutput()));
        generator.addProvider(true, new DefaultStoneSmelteryCraftingProvider(generator.getPackOutput()));
    }
}
