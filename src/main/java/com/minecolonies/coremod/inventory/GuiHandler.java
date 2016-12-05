package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.EntityPlayer;
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
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new Field(tileEntity, player.inventory, world, pos);
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new GuiField(player.inventory, tileEntity, world, pos);
    }
}
