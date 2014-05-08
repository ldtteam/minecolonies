package com.minecolonies.network;

import com.minecolonies.client.gui.GuiHutBuilder;
import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.tileentities.TileEntityHutBuilder;
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
        switch(ID)
        {
            case 0:
                return new GuiTownHall((TileEntityTownHall) world.getTileEntity(x, y, z));
            /*case 1:
                return new GuiTypable((TileEntityTownHall) world.getTileEntity(x, y, z), player, world, x, y, z);*/
            case 2:
                return new GuiHutBuilder((TileEntityHutBuilder) world.getTileEntity(x, y, z));
        }
        return null;
    }
}
