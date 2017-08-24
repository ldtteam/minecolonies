package com.minecolonies.coremod.event;

import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.util.RenderUtils;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to handle client events.
 */
public class ClientEventHandler
{
    /**
     * List of all BlockPos in the colony border.
     */
    private static final List<BlockPos> colonyBorder = new ArrayList<>();

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.getPartialTicks());

        final Structure structure = Settings.instance.getActiveStructure();
        if (structure != null)
        {
            final BlockPos position = Settings.instance.getPosition();
            if (Settings.instance.getStructureName().contains(AbstractEntityAIStructure.WAYPOINT_STRING))
            {
                RenderUtils.renderWayPoints(position, Minecraft.getMinecraft().theWorld, event.getPartialTicks());
            }
            else
            {
                RenderUtils.renderColonyBorder(position, Minecraft.getMinecraft().theWorld, event.getPartialTicks(), Minecraft.getMinecraft().thePlayer, colonyBorder);
            }
            return;
        }
        colonyBorder.clear();
    }
}
