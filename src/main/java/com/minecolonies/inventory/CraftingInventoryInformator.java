package com.minecolonies.inventory;

import com.minecolonies.tilentities.TileEntityTownHall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class CraftingInventoryInformator extends Container
{

    public CraftingInventoryInformator(InventoryPlayer inventory, TileEntityTownHall tileEntity){}

    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return true;
    }
}
