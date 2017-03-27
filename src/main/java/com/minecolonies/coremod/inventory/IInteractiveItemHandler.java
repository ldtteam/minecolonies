package com.minecolonies.coremod.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

/**
 * Interface describing a IItemHandler that can control who interacts with it.
 */
public interface IInteractiveItemHandler extends IItemHandler
{
    /**
     * Method used to check if the current IItemHandler can be used by a given player.
     *
     * @param player The player to check for.
     * @return True if the interaction is allowed, false if not.
     */
    boolean isUseableByPlayer(EntityPlayer player);

    /**
     * Method used to get the name of a IItemHandler.
     *
     * @return The name of the IItemHandler.
     */
    String getName();
}
