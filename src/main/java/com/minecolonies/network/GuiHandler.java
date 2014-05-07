package com.minecolonies.network;

import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.tilentities.TileEntityTownHall;
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
        switch(ID)
        {
            case 0:
                return new GuiTownHall((TileEntityTownHall) world.getTileEntity(x, y, z));
                //return new GuiInformator(player.inventory, (TileEntityTownHall) world.getTileEntity());
        }
        return null;
    }
}
