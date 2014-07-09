package com.minecolonies.network;

import com.minecolonies.client.gui.GuiHutBuilder;
import com.minecolonies.client.gui.GuiHutDeliveryMan;
import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.client.gui.GuiTypable;
import com.minecolonies.inventory.ContainerHut;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.tileentities.TileEntityTownHall;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{
    private static int nextGuiId = 0;

    public static int getNextGuiId()
    {
        return nextGuiId++;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(tileEntity instanceof TileEntityHut)
        {
            return new ContainerHut((TileEntityHut) tileEntity, player.inventory);
        }
        switch(ID)
        {
            case 0:
                break;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        EnumGUI guiID = EnumGUI.values()[ID];
        switch(guiID)
        {
            case TOWNHALL:
                return new GuiTownHall((TileEntityTownHall) world.getTileEntity(x, y, z), player, world, x, y, z);
            case TOWNHALL_RENAME:
                return new GuiTypable((TileEntityTownHall) world.getTileEntity(x, y, z), player, world, x, y, z);
            case TOWNHALL_INFORMATION:
                return new GuiTownHall((TileEntityTownHall) world.getTileEntity(x, y, z), player, world, x, y, z, GuiTownHall.idInformation);
            case BUILDER:
                return new GuiHutBuilder((TileEntityHutBuilder) world.getTileEntity(x, y, z), player, world, x, y, z);
            case WAREHOUSE:
                return new GuiHutDeliveryMan(0, player, world, x, y, z);
            case WAREHOUSE_SETTINGS:
                return new GuiHutDeliveryMan(1, player, world, x, y, z);
        }
        return null;
    }
}
