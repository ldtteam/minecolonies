package com.minecolonies.event;

import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;

public class EventHandler
{
    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if(event.entity instanceof EntityPlayer)
        {
            PlayerProperties playerProperties = new PlayerProperties((EntityPlayer) event.entity);
            if(playerProperties.getPlayerProperties((EntityPlayer) event.entity) == null)
            {
                playerProperties.register((EntityPlayer) event.entity);
            }
            if(event.entity.getExtendedProperties(Constants.PlayerPropertyName) == null)
            {
                event.entity.registerExtendedProperties(Constants.PlayerPropertyName, new PlayerProperties((EntityPlayer) event.entity));
            }
        }
    }
}
