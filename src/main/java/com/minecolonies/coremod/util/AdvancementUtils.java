package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdvancementUtils
{

    public static void TriggerAdvancementPlayersForColony(final IColony colony, Consumer<ServerPlayerEntity> playerConsumer)
    {
        MinecraftServer minecraftServer = colony.getWorld().getServer();
        if (minecraftServer != null)
        {
            final Predicate<Rank> predicate =
              MineColonies.getConfig().getServer().officersReceiveAdvancements.get() ? Rank::isColonyManager : rank -> rank.getId() == IPermissions.OWNER_RANK_ID;

            for (final Player player : colony.getPermissions().getFilteredPlayers(predicate))
            {
                final ServerPlayerEntity playerEntity = minecraftServer.getPlayerList().getPlayer(player.getID());
                if (playerEntity != null)
                {
                    playerConsumer.accept(playerEntity);
                }
            }
        }
    }
}
