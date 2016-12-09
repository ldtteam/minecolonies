package com.minecolonies.coremod.event;

import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used to handle client events.
 */
public class ClientEventHandler
{
    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     * @param event the catched event.
     */
    @SubscribeEvent
    public void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.getPartialTicks());
    }
}
