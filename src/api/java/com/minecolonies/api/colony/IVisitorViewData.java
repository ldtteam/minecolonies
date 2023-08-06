package com.minecolonies.api.colony;

import net.minecraft.world.item.ItemStack;

/**
 * View data for visitors
 */
public interface IVisitorViewData extends ICitizenDataView
{
    /**
     * Gets the visitors recruitment cost
     *
     * @return stack to pay
     */
    ItemStack getRecruitCost();
}
