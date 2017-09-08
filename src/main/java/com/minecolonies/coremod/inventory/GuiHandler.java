package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof ScarecrowTileEntity)
        {
            return new Field((ScarecrowTileEntity) tileEntity, player.inventory, world, pos);
        }
        else if(tileEntity instanceof TileEntityRack)
        {
            return new ContainerRack((TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), player.inventory, pos);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof ScarecrowTileEntity)
        {
            return new GuiField(player.inventory, (ScarecrowTileEntity) tileEntity, world, pos);
        }
        else if(tileEntity instanceof TileEntityRack)
        {
            return new GuiRack(player.inventory, (TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), world, pos);
        }
        return null;
    }
}
