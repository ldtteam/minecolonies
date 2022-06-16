package com.minecolonies.coremod.event;

import com.minecolonies.coremod.generation.DatagenLootTableManager;
import com.minecolonies.coremod.generation.defaults.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

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
        final LootTables lootTableManager = new DatagenLootTableManager(event.getExistingFileHelper());
        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator, event.getExistingFileHelper());
        generator.addProvider(true, new DefaultBlockLootTableProvider(generator));
        generator.addProvider(true, new DefaultAdvancementsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultSoundProvider(generator));
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new DefaultItemTagsProvider(generator, blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultEntityTypeTagsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(true, new DefaultResearchProvider(generator));
        generator.addProvider(true, new DefaultRecipeProvider(generator));
        generator.addProvider(true, new DefaultSifterCraftingProvider(generator, lootTableManager));
        generator.addProvider(true, new DefaultEnchanterCraftingProvider(generator));
        generator.addProvider(true, new DefaultFishermanLootProvider(generator));
        generator.addProvider(true, new DefaultConcreteMixerCraftingProvider(generator));
        generator.addProvider(true, new DefaultNetherWorkerLootProvider(generator, lootTableManager));
    }
}
