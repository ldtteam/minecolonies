package com.minecolonies.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Utility for server related stuff.
 * 
 * @author Isfirs
 * @since 0.2
 */
public final class ServerUtils
{

    /**
     * Private.
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
            if (id.equals((world.playerEntities.get(i)).getGameProfile().getId()))
            {
                return world.playerEntities.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a list of online players whose UUID's match the ones provided.
     *
     * @param world the world the players are in.
     * @param ids   List of UUIDs
     * @return list of EntityPlayers
     */
    public static List<EntityPlayer> getPlayersFromUUID(World world, Collection<UUID> ids)
    {
        final List<EntityPlayer> players = new ArrayList<>();

        for (Object o : world.playerEntities)
        {
            if (o instanceof EntityPlayer)
            {
                final EntityPlayer player = (EntityPlayer) o;
                if (ids.contains(player.getGameProfile().getId()))
                {
                    players.add(player);
                    if (players.size() == ids.size())
                    {
                        return players;
                    }
                }
            }
        }
        return players;
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
        final List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
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
