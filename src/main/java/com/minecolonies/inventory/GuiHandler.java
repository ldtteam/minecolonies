package com.minecolonies.inventory;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.entity.ai.citizen.farmer.Field;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        final BlockPos            pos        = new BlockPos(x,y,z);
        final Colony              colony = ColonyManager.getColony(world, pos);
        if(colony != null && colony.getField(pos) != null)
        {
            return colony.getField(pos);
        }
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new Field(tileEntity, player.inventory, world, pos);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        final BlockPos            pos        = new BlockPos(x,y,z);
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new GuiField(player.inventory, tileEntity, world, pos);
    }
}
