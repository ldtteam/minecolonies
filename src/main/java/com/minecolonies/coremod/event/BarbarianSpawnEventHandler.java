package com.minecolonies.coremod.event;

import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.api.util.Log;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BarbarianSpawnEventHandler
{
    @SubscribeEvent
    public static void onEntityJoinWorld(final EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityBarbarian)
        {
            Log.getLogger().warn("Spawning barbarian at: " + event.getEntity().getPosition());
        }
    }
}
