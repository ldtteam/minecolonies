package com.minecolonies.api.colony;

import net.minecraft.world.item.ItemStack;

/**
 * View data for visitors
 */
public interface IVisitorViewData extends ICitizenDataView
{
    /**
     * Returns the visitor's colony
     *
     * @return colony
     */
    public IColonyView getColonyView();

    /**
     * Gets the visitors recruitment cost
     *
     * @return stack to pay
     */
    ItemStack getRecruitCost();
}
