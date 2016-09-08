package com.minecolonies.event;

import com.minecolonies.entity.pathfinding.Pathfinding;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import javax.annotation.Nonnull;

public class ClientEventHandler
{
    @SubscribeEvent
    public void renderWorldLastEvent(@Nonnull RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.getPartialTicks());
    }
}
