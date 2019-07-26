package com.minecolonies.coremod.event;

import com.ldtteam.blockout.Log;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BarbarianSpawnEventHandler
{

    @SubscribeEvent
    public void onEntityJoinWorld(final EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityBarbarian)
        {
            Log.getLogger().warn("Spawning barbarian at: " + event.getEntity().getPosition());
        }
    }
}
