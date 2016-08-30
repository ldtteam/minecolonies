package com.minecolonies.inventory;

import com.minecolonies.colony.Field;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        final BlockPos            pos        = new BlockPos(x,y,z);
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new Field(tileEntity.getInventoryField(), player.inventory, world, pos);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        final BlockPos            pos        = new BlockPos(x,y,z);
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new GuiField(player.inventory, tileEntity.getInventoryField(), world, pos);

    }
}
