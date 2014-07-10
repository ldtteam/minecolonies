package com.minecolonies.inventory;

import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;

/**
 * Handles gui operations on the server
 * Created: June 21, 2014
 *
 * @author Colton
 */
public class ContainerHut extends Container //ContainerChest in future
{
    private TileEntityHut   hut;
    private EntityPlayerMP  player;
    private InventoryPlayer inventoryPlayer;

    private int lastNumberOfCitizens = 0;

    public ContainerHut(TileEntityHut hut, EntityPlayer player)
    {
        this.hut = hut;
        this.player = (EntityPlayerMP) player;
        this.inventoryPlayer = player.inventory;
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
        super.detectAndSendChanges();

        if(hut instanceof TileEntityTownHall)
        {
            int numberOfCitizens = ((TileEntityTownHall) hut).getCitizens().size();
            if(numberOfCitizens != lastNumberOfCitizens)
            {
                Utils.getEntitiesFromUUID(player.worldObj, ((TileEntityTownHall) hut).getCitizens());
                Packet packet = hut.getDescriptionPacket();

                if (packet != null)
                {
                    player.playerNetServerHandler.sendPacket(packet);
                }
                lastNumberOfCitizens = numberOfCitizens;
            }
        }
    }
}
