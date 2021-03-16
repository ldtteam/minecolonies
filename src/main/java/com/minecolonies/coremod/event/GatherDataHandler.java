package com.minecolonies.coremod.event;

import com.minecolonies.coremod.generation.SawmillTimberFrameRecipeProvider;
import com.minecolonies.coremod.generation.SifterCraftingProvider;
import com.minecolonies.coremod.generation.defaults.DefaultBlockLootTableProvider;
import com.minecolonies.coremod.generation.defaults.DefaultResearchProvider;
import com.minecolonies.coremod.generation.defaults.DefaultSoundProvider;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class GatherDataHandler
{
    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     *
     * @param event event sent when you run the "runData" gradle task
     */
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new DefaultBlockLootTableProvider(event.getGenerator()));
        event.getGenerator().addProvider(new DefaultSoundProvider(event.getGenerator()));
        event.getGenerator().addProvider(new DefaultResearchProvider(event.getGenerator()));
        event.getGenerator().addProvider(new SawmillTimberFrameRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(new SifterCraftingProvider(event.getGenerator()));
    }
}
