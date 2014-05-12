package com.minecolonies.network;

import com.minecolonies.client.gui.GuiHutBuilder;
import com.minecolonies.client.gui.GuiHutDeliveryMan;
import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.client.gui.GuiTypable;
import com.minecolonies.tileentities.TileEntityTownHall;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        switch(ID)
        {
            case 0:
                //TODO
                break;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        Constants.Gui guiID = Constants.Gui.values()[ID];
        switch(guiID)
        {
            case TownHall:
                return new GuiTownHall((TileEntityTownHall) world.getTileEntity(x, y, z), player, world, x, y, z);
            case RenameTown:
                return new GuiTypable((TileEntityTownHall) world.getTileEntity(x, y, z), player, world, x, y, z);
            case HutBuilder:
                return new GuiHutBuilder((TileEntityHutBuilder) world.getTileEntity(x, y, z));
            case HutDeliveryman:
                return new GuiHutDeliveryMan(/*(TileEntityHutDeliveryman) world.getTileEntity(x, y, z), */0, player, world, x, y, z);
            case HutDeliverymanSettings:
                return new GuiHutDeliveryMan(/*(TileEntityHutDeliveryman) world.getTileEntity(x, y, z), */1, player, world, x, y, z);
        }
        return null;
    }
}
