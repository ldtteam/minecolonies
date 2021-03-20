package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.OldRank;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class AdvancementUtils
{

    public static void TriggerAdvancementPlayersForColony(final IColony colony, Consumer<ServerPlayerEntity> playerConsumer)
    {
        MinecraftServer minecraftServer = colony.getWorld().getServer();
        if (minecraftServer != null)
        {
            final List<OldRank> oldRanks =
              MineColonies.getConfig().getServer().officersReceiveAdvancements.get() ? Arrays.asList(OldRank.OWNER, OldRank.OFFICER) : Collections.singletonList(OldRank.OWNER);

            for (final Player player : colony.getPermissions().getPlayersByRank(new HashSet<>(oldRanks)))
            {
                final ServerPlayerEntity playerEntity = minecraftServer.getPlayerList().getPlayerByUUID(player.getID());
                if (playerEntity != null)
                {
                    playerConsumer.accept(playerEntity);
                }
            }
        }
    }
}
