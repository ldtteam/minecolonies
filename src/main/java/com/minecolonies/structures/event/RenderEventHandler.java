package com.minecolonies.structures.event;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.structures.client.StructureClientHandler;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import com.sun.jna.StructureReadContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
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
            StructureClientHandler.renderStructure(structure, event.getPartialTicks());
        }
    }
}
