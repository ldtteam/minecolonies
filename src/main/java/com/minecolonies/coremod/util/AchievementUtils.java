package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.Achievement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
     * members with at least rank {@link Rank#OFFICER}.
     *
     * @param colony The colony to sync
     */
    public static void syncAchievements(@NotNull final Colony colony)
    {
        @NotNull final List<Player> players = PermissionUtils.getPlayersWithAtLeastRank(colony, Rank.OFFICER);

        @NotNull final List<EntityPlayer> lPlayer = ServerUtils.getPlayersFromPermPlayer(players, colony.getWorld());

        for (@Nullable final EntityPlayer player : lPlayer)
        {
            for (final Achievement achievement : colony.getAchievements())
            {
                if (player == null || ModAchievements.achievementGetSupply == achievement)
                {
                    continue;
                }

                player.addStat(achievement);
            }
        }
    }
}
