package com.minecolonies.items;


/**
 * This is a proxy item class for achievements.
 *
 * Achievements can't have custom icons except using an item or block as proxy.
 */
public class ItemAchievementProxy extends AbstractItemMinecolonies
{

    /**
     * Constructor.
     *
     * Creates an item instance with the given name.
     * Sets {@link net.minecraft.item.Item#maxStackSize} default to 1.
     *
     * @param name The item name
     */
    public ItemAchievementProxy(String name)
    {
        super(name);

        this.maxStackSize = 1;
    }

}
