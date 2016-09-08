package com.minecolonies.util;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import javax.annotation.Nonnull;

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
    @Nonnull
    public static List<Permissions.Player> getPlayersWithAtLeastRank(@Nonnull Colony colony, @Nonnull Permissions.Rank rank)
    {
        @Nonnull final List<Permissions.Player> playersWithAtLeastRank = new ArrayList<>();

        @Nonnull final Permissions permissions = colony.getPermissions();
        @Nonnull final Map<UUID, Permissions.Player> players = permissions.getPlayers();

        for (@Nonnull final Permissions.Player player : players.values())
        {
            if (player.getRank().ordinal() <= rank.ordinal())
            {
                playersWithAtLeastRank.add(player);
            }
        }

        return playersWithAtLeastRank;
    }
}
