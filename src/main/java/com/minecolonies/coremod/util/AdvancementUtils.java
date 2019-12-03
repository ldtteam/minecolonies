package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
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
            final List<Rank> ranks = MineColonies.getConfig().getCommon().officersReceiveAdvancements.get() ? Arrays.asList(Rank.OWNER, Rank.OFFICER) : Collections.singletonList(Rank.OWNER);

            for (Player player : colony.getPermissions().getPlayersByRank(new HashSet<>(ranks)))
            {
                playerConsumer.accept(minecraftServer.getPlayerList().getPlayerByUUID(player.getID()));
            }
        }
    }

}
