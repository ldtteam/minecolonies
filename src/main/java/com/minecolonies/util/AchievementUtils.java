package com.minecolonies.util;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
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
     * This method will sync all acquired achievements by this colony to all members with at least rank {@link Permissions.Rank#OFFICER}.
     *
     * @param colony The colony to sync
     */
    public static void syncAchievements(@NotNull final Colony colony)
    {
        final List<Permissions.Player> players = PermissionUtils.getPlayersWithAtLeastRank(colony, Permissions.Rank.OFFICER);

        @NotNull final List<EntityPlayer> lPlayer = ServerUtils.getPlayersFromPermPlayer(players);

        for (@Nullable final EntityPlayer player : lPlayer)
        {
            for (final Achievement achievement : colony.getAchievements())
            {
                if (player == null)
                {
                    continue;
                }

                player.triggerAchievement(achievement);
            }
        }
    }
}
