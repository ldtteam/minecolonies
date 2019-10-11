package com.minecolonies.coremod.event;

import com.minecolonies.coremod.generation.defaults.DefaultBlockLootTableProvider;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class GatherDataHandler
{
    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     * @param event event sent when you run the "runData" gradle task
     */
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new DefaultBlockLootTableProvider(event.getGenerator()));
    }
}
