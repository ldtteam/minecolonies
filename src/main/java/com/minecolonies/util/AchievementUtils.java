package com.minecolonies.util;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.Achievement;

import java.util.List;

/**
 *
 */
public final class AchievementUtils {

    public static void syncAchievements(final Colony colony) {
        final List<Permissions.Player> players = PermissionUtils.getPlayersWithAtleastRank(colony, Permissions.Rank.OFFICER);

        final List<EntityPlayer> lPlayer = ServerUtils.getPlayersFromPermPlayer(players);

        for (EntityPlayer player : lPlayer) {
            for (Achievement achievement: colony.getAchievements()) {
                if (player == null)
                    continue;

                player.triggerAchievement(achievement);
            }
        }
    }

    /**
     * Private Constructor to deny instances
     */
    private AchievementUtils() {}

}
