package com.minecolonies.core.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * Interface for range restricted packet interactions
 *
 * @param <T>
 */
public interface IRangeRestrictedPacket<T>
{
    /**
     * Range check to implement
     * @param player
     * @param target
     * @return
     */
    boolean isInRange(ServerPlayer player, T target);
}
