package com.minecolonies.api.items;

import net.minecraft.world.item.ItemStack;

/**
 * Minecolonies food item.
 */
public interface IMinecoloniesFoodItem
{
    /**
     * Get the corresponding food tier.
     * @return the tier.
     */
    int getTier(ItemStack stack);
}
