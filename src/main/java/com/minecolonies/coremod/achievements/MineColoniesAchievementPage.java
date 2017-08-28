package com.minecolonies.coremod.achievements;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * This class is the superclass of our achievement pages.
 * <p>
 * Constructors exist to make creating new achievement pages easy.
 *
 * @since 0.2
 */
public class MineColoniesAchievementPage extends AchievementPage
{

    /**
     * Create a new achievement page.
     *
     * @param name         The name this page should have
     * @param achievements A list of achievements to display
     */
    public MineColoniesAchievementPage(final String name, final Achievement... achievements)
    {
        super(name, achievements);
    }
}
