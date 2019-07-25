package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.Colony;
import org.jetbrains.annotations.NotNull;

/**
 * Some utiility methods all around achievements.
 */
public final class AchievementUtils
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private AchievementUtils()
    {
    }

    /**
     * This method will sync all acquired achievements by this colony to all
     * members with at least rank the officer rank..
     *
     * @param colony The colony to sync
     */
    public static void syncAchievements(@NotNull final Colony colony)
    {
        /**@NotNull final List<Player> players = PermissionUtils.getPlayersWithAtLeastRank(colony, Rank.OFFICER);
        if (colony.getWorld() != null)
        {
            for (final Advancement achievement : colony.getStatsManager().getAchievements())
            {
                if (player == null || ModAchievements.ad == achievement)
                {
                    if (player == null || ModAchievements.achievementGetSupply == achievement)
                    {
                        continue;
                    }

                    player.addStat(achievement);
                }

                player.addStat(achievement);
            }
        }
        */
    }
}
