package com.minecolonies.inventory;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles gui operations on the server
 * Created: June 21, 2014
 *
 * @author Colton
 */
public class ContainerHut extends Container //ContainerChest in future
{
    private TileEntityColonyBuilding hut;
    private EntityPlayerMP           player;
    private InventoryPlayer          inventoryPlayer;

    private int lastNumberOfCitizens = 0;

    public ContainerHut(TileEntityColonyBuilding hut, EntityPlayer player)
    {
        this.hut = hut;
        this.player = (EntityPlayerMP) player;
        this.inventoryPlayer = player.inventory;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        assert !hut.getWorldObj().isRemote;

        if (hut.isUseableByPlayer(player))
        {
            return true;
        }
        else
        {
            Colony colony = hut.getColony();

            if (colony != null && !colony.isOwner(player))
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission", colony.getName());
            }
        }
        return false;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }
}
