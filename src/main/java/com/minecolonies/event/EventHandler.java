package com.minecolonies.event;

import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class EventHandler
{
    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if(event.entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.entity;
            if(PlayerProperties.get(player) == null)
            {
                PlayerProperties.register(player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.saveProxyData((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.loadProxyData((EntityPlayer) event.entity);
        }
    }
}
