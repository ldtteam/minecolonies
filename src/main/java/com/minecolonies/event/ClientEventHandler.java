package com.minecolonies.event;

import com.minecolonies.entity.pathfinding.Pathfinding;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler
{
    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.partialTicks);
    }
}
