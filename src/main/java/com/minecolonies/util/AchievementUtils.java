package com.minecolonies.util;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.stats.Achievement;

import java.util.List;

/**
 *
 */
public final class AchievementUtils {

    public static void syncAchievements(final Colony colony) {
        final List<Permissions.Player> players = PermissionUtils.getPlayersWithAtleastRank(colony, Permissions.Rank.OFFICER);

        for (Permissions.Player player : players) {
            for (Achievement achievement: colony.getAchievements()) {
                ServerUtils.getPlayerFromUUID(player.getID()).triggerAchievement(achievement);
            }
        }
    }

    /**
     * Private Constructor to deny instances
     */
    private AchievementUtils() {}

}
