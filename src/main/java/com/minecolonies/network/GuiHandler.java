package com.minecolonies.network;

import com.minecolonies.client.gui.GuiHutBuilder;
import com.minecolonies.client.gui.GuiHutWarehouse;
import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.client.gui.GuiTypable;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.inventory.ContainerHut;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.*;
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
        if(tileEntity instanceof TileEntityColonyBuilding)
        {
            return new ContainerHut((TileEntityColonyBuilding) tileEntity, player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityColonyBuilding)
        {
            Building.View building = ((TileEntityColonyBuilding)tileEntity).getBuildingView();
            if (building == null)
            {
                return null;
            }

            return building.getGui(guiId);
        }
        return null;
    }

    public static void showGuiScreen(GuiScreen gui)
    {
        if(FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
        {
            FMLCommonHandler.instance().showGuiScreen(gui);
        }
    }
}
