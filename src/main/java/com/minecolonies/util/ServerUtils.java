package com.minecolonies.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

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
     * Returns the online EntityPlayer with the given UUID
     *
     * @param world world the player is in
     * @param id    the player's UUID
     * @return the Player
     */
    public static EntityPlayer getPlayerFromUUID(World world, UUID id)
    {
        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            if (id.equals(((EntityPlayer) world.playerEntities.get(i)).getGameProfile().getId()))
            {
                return (EntityPlayer) world.playerEntities.get(i);
            }
        }
        return null;
    }

    /**
     * Found on <a href="http://jabelarminecraft.blogspot.de/p/minecraft-forge-172-finding-block.html">jabelarminecraft</a>
     *
     * @param parUUID
     * @return The player
     */
    public static EntityPlayer getPlayerFromUUID(UUID parUUID)
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
