package com.minecolonies.util;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * todo: explain this class
 */
public final class PermissionUtils
{

    private PermissionUtils()
    {
    }

    @NotNull
    public static List<Permissions.Player> getPlayersWithAtleastRank(@NotNull Colony colony, @NotNull Permissions.Rank rank)
    {
        @NotNull List<Permissions.Player> playersWithAtLeastRank = new ArrayList<Permissions.Player>();

        final Permissions permissions = colony.getPermissions();
        final Map<UUID, Permissions.Player> players = permissions.getPlayers();

        for (@NotNull Permissions.Player player : players.values())
        {
            if (player.getRank().ordinal() <= rank.ordinal())
            {
                playersWithAtLeastRank.add(player);
            }
        }

        return playersWithAtLeastRank;
    }
}
