package com.minecolonies.achievements;

import com.minecolonies.items.ModItems;

/**
 * Achievement: Wand of Building
 * Granted for: Looting the {@link ModItems#buildTool}
 *
 * @author Isfirs
 * @since 0.1
 */
public class AchWandOfBuilding extends AbstractAchievement
{
    public AchWandOfBuilding(final String id,
                             final String name,
                             final int offsetX,
                             final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModItems.buildTool, ModAchievements.achGetSupply);
    }
}
