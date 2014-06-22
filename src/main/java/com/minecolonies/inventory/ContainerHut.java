package com.minecolonies.inventory;

import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

/**
 * Handles gui operations on the server
 * Created: June 21, 2014
 *
 * @author Colton
 */
public class ContainerHut extends Container //ContainerChest in future
{
    private TileEntityHut   hut;
    private InventoryPlayer inventoryPlayer;

    public ContainerHut(TileEntityHut hut, InventoryPlayer inventoryPlayer)
    {
        this.hut = hut;
        this.inventoryPlayer = inventoryPlayer;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        if(hut.isUseableByPlayer(player))
        {
            return true;
        }
        else if(hut.getTownHall() != null)
        {
            LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission", hut.getTownHall().getCityName());
        }
        return false;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();//TODO this may be usefull
    }
}
