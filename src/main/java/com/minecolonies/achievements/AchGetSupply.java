package com.minecolonies.achievements;

import com.minecolonies.items.ModItems;

/**
 * Achievement: Get Supply
 * Granted for: using the {@link com.minecolonies.items.ModItems#supplyChest}
 *
 *
 * @author Isfirs
 * @since 0.1
 */
public class AchGetSupply extends AbstractAchievement
{
    public AchGetSupply(final String id,
                        final String name,
                        final int offsetX,
                        final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModItems.supplyChest, null);
    }
}
