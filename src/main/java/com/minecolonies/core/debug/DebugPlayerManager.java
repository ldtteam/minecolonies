package com.minecolonies.core.debug;

import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Debug manager to keep track of players having debug mode enabled
 */
public class DebugPlayerManager
{
    /**
     * Set of players currently added to debug info
     */
    private static Set<UUID> debugPlayers = new HashSet<>();

    /**
     * Check if debugging is enabled for the player
     *
     * @param player
     * @return
     */
    public static boolean hasDebugEnabled(final Player player)
    {
        return !FMLEnvironment.production || debugPlayers.contains(player.getUUID());
    }

    /**
     * Toggles debugging for the given player
     *
     * @param player
     * @return
     */
    public static boolean toggleDebugModeFor(final UUID player)
    {
        if (debugPlayers.contains(player))
        {
            debugPlayers.remove(player);
            PathfindingUtils.trackingMap.remove(player);
            return false;
        }
        else
        {
            debugPlayers.add(player);
            return true;
        }
    }

    /**
     * Toggles debugging for the given player
     *
     * @param player
     * @return
     */
    public static void setDebugModeFor(final UUID player, boolean enable)
    {
        if (!enable)
        {
            debugPlayers.remove(player);
        }
        else
        {
            debugPlayers.add(player);
        }
    }
}
