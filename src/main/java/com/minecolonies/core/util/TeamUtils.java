package com.minecolonies.core.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for working with {@link net.minecraft.world.scores.Team instances}.
 */
public class TeamUtils
{

    /**
     * Check or create a team.
     *
     * @param level the level to create the team in.
     * @param name  the team name.
     */
    @Nullable
    public static PlayerTeam checkOrCreateTeam(@Nullable final Level level, final String name)
    {
        return checkOrCreateTeam(level, name, true);
    }

    /**
     * Check or create a team.
     *
     * @param level             the level to create the team in.
     * @param name              the team name.
     * @param allowFriendlyFire whether this team allows friendly fire or not.
     */
    @Nullable
    public static PlayerTeam checkOrCreateTeam(@Nullable final Level level, final String name, boolean allowFriendlyFire)
    {
        if (level == null)
        {
            return null;
        }

        PlayerTeam team = level.getScoreboard().getPlayerTeam(name);
        if (team == null)
        {
            team = level.getScoreboard().addPlayerTeam(name);
        }
        team.setAllowFriendlyFire(allowFriendlyFire);
        return team;
    }
}
