package com.minecolonies.util;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public final class PermissionUtils {

    public static List<Permissions.Player> getPlayersWithAtleastRank(Colony colony, Permissions.Rank rank) {
        List<Permissions.Player> playersWithAtLeastRank = new ArrayList<Permissions.Player>();

        final Permissions permissions = colony.getPermissions();
        final Map<UUID, Permissions.Player> players = permissions.getPlayers();

        for (Permissions.Player player: players.values()) {
            if (player.getRank().ordinal() <= rank.ordinal()) {
                playersWithAtLeastRank.add(player);
            }
        }

        return playersWithAtLeastRank;
    }

    private PermissionUtils() {}


}
