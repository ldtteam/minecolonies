package com.minecolonies.coremod.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;

public interface ITileEntityBarrel extends ITickable
{
    /**
     * The number of items it needs to start composting
     */
    int                         MAX_ITEMS      = 64;

    /**
     * Returns the number of items that the block contains
     * @return the number of items
     */
    int getItems();

    boolean isDone();

    boolean checkIfWorking();

    boolean addItem(ItemStack item);

    ItemStack retrieveCompost(double multiplier);
}
