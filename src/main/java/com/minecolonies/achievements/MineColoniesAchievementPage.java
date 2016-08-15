package com.minecolonies.achievements;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * A sublcass for this mod.
 *
 * May contain page logic.
 *
 * @author Isfirs
 * @since 0.2
 */
public class MineColoniesAchievementPage extends AchievementPage
{

    /**
     * {@inheritDoc}
     *
     * @param name
     * @param achievements
     */
    public MineColoniesAchievementPage(final String name, final Achievement... achievements)
    {
        super(name, achievements);
    }
}
