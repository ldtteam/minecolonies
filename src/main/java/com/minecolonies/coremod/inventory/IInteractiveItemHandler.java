package com.minecolonies.coremod.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

public interface IInteractiveItemHandler extends IItemHandler {
    boolean isUseableByPlayer(EntityPlayer player);
    String getName();
}
