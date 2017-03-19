package com.minecolonies.coremod.inventory;

import net.minecraftforge.items.ItemStackHandler;

/**
 * Abstract itemstack handler that implements IInteractiveItemHandler.
 */
public abstract class AbstractInteractiveItemStackHandler extends ItemStackHandler implements IInteractiveItemHandler {
    /**
     * Constructor used to create a InteractiveItemStackHandler with a given size.
     *
     * @param size The size of new Handler.
     */
    public AbstractInteractiveItemStackHandler(int size)
    {
        super(size);
    }
}
