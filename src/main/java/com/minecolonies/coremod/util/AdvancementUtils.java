package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AdvancementUtils
{

    public static void TriggerAdvancementPlayersForColony(final IColony colony, Consumer<ServerPlayer> playerConsumer)
    {
        MinecraftServer minecraftServer = colony.getWorld().getServer();
        if (minecraftServer != null)
        {
            final Predicate<Rank> predicate =
              MineColonies.getConfig().getServer().officersReceiveAdvancements.get() ? Rank::isColonyManager : rank -> rank.getId() == IPermissions.OWNER_RANK_ID;

            for (final ColonyPlayer player : colony.getPermissions().getFilteredPlayers(predicate))
            {
                final ServerPlayer playerEntity = minecraftServer.getPlayerList().getPlayer(player.getID());
                if (playerEntity != null)
                {
                    playerConsumer.accept(playerEntity);
                }
            }
        }
    }
}
