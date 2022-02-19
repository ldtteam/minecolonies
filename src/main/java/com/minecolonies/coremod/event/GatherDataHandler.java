package com.minecolonies.coremod.event;

import com.minecolonies.coremod.generation.defaults.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
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
        final BlockTagsProvider blockTagsProvider = new DefaultBlockTagsProvider(generator, event.getExistingFileHelper());
        generator.addProvider(new DefaultBlockLootTableProvider(generator));
        generator.addProvider(new DefaultAdvancementsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(new DefaultSoundProvider(generator));
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new DefaultItemTagsProvider(generator, blockTagsProvider, event.getExistingFileHelper()));
        generator.addProvider(new DefaultEntityTypeTagsProvider(generator, event.getExistingFileHelper()));
        generator.addProvider(new DefaultResearchProvider(generator));
        generator.addProvider(new DefaultSifterCraftingProvider(generator));
        generator.addProvider(new DefaultEnchanterCraftingProvider(generator));
        generator.addProvider(new DefaultFishermanLootProvider(generator));
        generator.addProvider(new DefaultConcreteMixerCraftingProvider(generator));
    }
}
