package com.minecolonies.core.network;

import net.minecraft.server.level.ServerPlayer;

public interface IRangeRestrictedPacket<T>
{
    boolean isInRange(ServerPlayer player, T target);
}
