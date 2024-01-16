package com.minecolonies.api.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Data for colony visitors, based on citizendata
 */
public interface IVisitorData extends ICitizenData
{
    /**
     * Sets the recruitment cost stack
     */
    void setRecruitCosts(final ItemStack cost);

    /**
     * Returns the recruitment cost stack
     *
     * @return itemstack
     */
    ItemStack getRecruitCost();

    /**
     * The position the visitor is sitting on
     *
     * @return sitting pos
     */
    BlockPos getSittingPosition();

    /**
     * Sets the sitting position
     *
     * @param pos sitting pos
     */
    void setSittingPosition(final BlockPos pos);

    void setCustomTexture(UUID texture);
}
