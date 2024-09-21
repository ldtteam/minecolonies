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
    public static PlayerTeam checkOrCreateTeam(@Nullable Level level, String name)
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
        return team;
    }
}
