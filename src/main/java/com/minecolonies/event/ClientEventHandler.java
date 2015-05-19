package com.minecolonies.event;

import com.minecolonies.entity.pathfinding.Pathfinding;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class ClientEventHandler
{
    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.partialTicks);
    }
}
