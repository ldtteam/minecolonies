package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Some utility methods all around permissions.
 */
public final class PermissionUtils
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private PermissionUtils()
    {
    }

    /**
     * Creates a list of players that have the given rank or higher.
     * <p>
     * This is using the enums ordinal method for comparison.
     *
     * @param colony The colony to get the players
     * @param rank   The rank to check
     * @return The list with online players that has the rank or higher
     */
    @NotNull
    public static List<Player> getPlayersWithAtLeastRank(@NotNull final IColony colony, @NotNull final Rank rank)
    {
        @NotNull final List<Player> playersWithAtLeastRank = new ArrayList<>();

        @NotNull final IPermissions permissions = colony.getPermissions();
        @NotNull final Map<UUID, Player> players = permissions.getPlayers();

        for (@NotNull final Player player : players.values())
        {
            if (player.getRank().ordinal() <= rank.ordinal())
            {
                playersWithAtLeastRank.add(player);
            }
        }

        return playersWithAtLeastRank;
    }
}
