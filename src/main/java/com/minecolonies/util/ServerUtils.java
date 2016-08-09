package com.minecolonies.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Utility for server related stuff
 * 
 * @author Isfirs
 * @since 0.2
 */
public final class ServerUtils
{
    
    /**
     * Private
     */
    private ServerUtils()
    {
    }
    
    /**
     * Found on <a href="http://jabelarminecraft.blogspot.de/p/minecraft-forge-172-finding-block.html">jabelarminecraft</a>
     * 
     * @param parUUID
     * @return The player
     */
    public static EntityPlayer getPlayerOnServerFromUUID(UUID parUUID) 
    {
        if (parUUID == null) 
        {
            return null;
        }
        List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : allPlayers) 
        {
            if (player.getUniqueID().equals(parUUID)) 
            {
                return player;
            }
        }
        return null;
    }

}
