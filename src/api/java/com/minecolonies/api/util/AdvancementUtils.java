package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.configuration.Configurations;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.function.Consumer;

public class AdvancementUtils
{

    public static void TriggerAdvancementPlayersForColony(final IColony colony, Consumer<EntityPlayerMP> playerConsumer)
    {
        MinecraftServer minecraftServer = colony.getWorld().getMinecraftServer();
        if (minecraftServer != null)
        {
            final List<Rank> ranks = Configurations.gameplay.officersReceiveAdvancements ? Arrays.asList(Rank.OWNER, Rank.OFFICER) : Collections.singletonList(Rank.OWNER);

            for (Player player : colony.getPermissions().getPlayersByRank(new HashSet<>(ranks)))
            {
                final EntityPlayerMP playerEntity = minecraftServer.getPlayerList().getPlayerByUUID(player.getID());
                if (playerEntity != null)
                {
                    playerConsumer.accept(playerEntity);
                }
            }
        }
    }

}
