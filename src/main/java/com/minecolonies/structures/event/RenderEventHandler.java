package com.minecolonies.structures.event;

import com.minecolonies.structures.client.StructureClientHandler;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * EventHandler used to display the schematics on the client.
 */
public class RenderEventHandler
{

    /**
     * Event used to render the schematics. Only render the schematic if there is one in the settings.
     *
     * @param event Object containing event details.
     */
    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent event)
    {
        final Structure structure = Settings.instance.getActiveStructure();

        if (structure != null)
        {
            StructureClientHandler.renderStructure(structure, event.getPartialTicks(), Settings.instance.getPosition());
        }
    }
}
