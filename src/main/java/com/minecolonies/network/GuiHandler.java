package com.minecolonies.network;

import com.minecolonies.client.gui.GuiHutBuilder;
import com.minecolonies.client.gui.GuiHutWarehouse;
import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.client.gui.GuiTypable;
import com.minecolonies.inventory.ContainerHut;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.tileentities.TileEntityHutWarehouse;
import com.minecolonies.tileentities.TileEntityTownHall;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(tileEntity instanceof TileEntityHut)
        {
            return new ContainerHut((TileEntityHut) tileEntity, player);
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
            case BUILDER:
                return new GuiHutBuilder((TileEntityHutBuilder) world.getTileEntity(x, y, z), player, world, x, y, z);
            case WAREHOUSE:
                return new GuiHutWarehouse((TileEntityHutWarehouse) world.getTileEntity(x, y, z), player, world, x, y, z);
            default:
                return null;
        }
    }

    public static void showGuiScreen(GuiScreen gui)
    {
        if(FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
        {
            FMLCommonHandler.instance().showGuiScreen(gui);
        }
    }
}
