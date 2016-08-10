package com.minecolonies.achievements;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Abstract achievement page
 *
 * @author Isfirs
 * @since 0.2
 */
public abstract class AbstractAchievementPage extends AchievementPage
{
    public AbstractAchievementPage(final String name, final Achievement... achievements)
    {
        super(name, achievements);
    }
}