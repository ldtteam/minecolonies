package com.minecolonies.achievements;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Created by isfirs
 *
 * @author Isfirs
 * @since 0.1
 */
public class AbstractAchievementPage extends AchievementPage
{
    public AbstractAchievementPage(final String name, final Achievement... achievements)
    {
        super(name, achievements);
    }
}