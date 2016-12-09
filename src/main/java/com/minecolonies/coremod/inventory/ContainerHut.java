package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.LanguageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

/**
 * Handles gui operations on the server.
 * Created: June 21, 2014
 * @deprecated remove this when we don't need it anymore or something better is available.
 * @author Colton
 * //TODO undepricate when finished
 *  //ContainerChest in future
 */
@Deprecated
public class ContainerHut extends Container
{
    private final TileEntityColonyBuilding hut;
    private final EntityPlayerMP           player;
    private final InventoryPlayer          inventoryPlayer;

    private final int lastNumberOfCitizens = 0;

    /**
     * Instantiates the container hut.
     * @param hut the hut.
     * @param player the player.
     */
    public ContainerHut(final TileEntityColonyBuilding hut, final EntityPlayer player)
    {
        super();
        this.hut = hut;
        this.player = (EntityPlayerMP) player;
        this.inventoryPlayer = player.inventory;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player)
    {
        assert !hut.getWorld().isRemote;

        if (hut.isUsableByPlayer(player))
        {
            return true;
        }
        else
        {
            final Colony colony = hut.getColony();

            if (colony != null && !colony.getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS))
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission", colony.getName());
            }
        }
        return false;
    }
}
