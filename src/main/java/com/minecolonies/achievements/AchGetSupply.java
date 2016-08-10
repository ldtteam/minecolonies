package com.minecolonies.achievements;

import com.minecolonies.items.ModItems;

/**
 * Achievement: Get Supply Granted for: using the
 * {@link com.minecolonies.items.ModItems#supplyChest}
 *
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchGetSupply extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchGetSupply(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModItems.supplyChest, null);
    }
}
