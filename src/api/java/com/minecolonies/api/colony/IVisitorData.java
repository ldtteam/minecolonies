package com.minecolonies.api.colony;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Data for colony visitors, based on citizendata
 */
public interface IVisitorData extends ICitizenData
{
    void setRecruitCosts(final ItemStack cost);

    ItemStack getRecruitCost();

    BlockPos getSittingPosition();

    void setSittingPosition(final BlockPos pos);
}
