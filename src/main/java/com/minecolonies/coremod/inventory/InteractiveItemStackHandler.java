package com.minecolonies.coremod.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.ItemStackHandler;

public abstract class InteractiveItemStackHandler extends ItemStackHandler implements IInteractiveItemHandler {
    public InteractiveItemStackHandler(int i) {
        super(i);
    }
}
