package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.reference.ModAchievements;
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
    public static void syncAchievements(@NotNull final IColony colony)
    {
        @NotNull final List<Player> players = PermissionUtils.getPlayersWithAtLeastRank(colony, Rank.OFFICER);

        @NotNull final List<EntityPlayer> lPlayer = ServerUtils.getPlayersFromPermPlayer(players, colony.getWorld());

        for (@Nullable final EntityPlayer player : lPlayer)
        {
            if (player == null)
            {
                continue;
            }

            List<Achievement> colonyAchievements = colony.getAchievements();
            colonyAchievements.stream().filter(a -> a != ModAchievements.achievementGetSupply).forEach(player::addStat);
        }
    }
}
