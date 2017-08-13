package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.client.gui.WindowGuiCrafting;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

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
            return new ContainerRack((TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), player.inventory, world, pos);
        }
        else
        {
            @Nullable final AbstractBuilding.View building = ColonyManager.getBuildingView(pos);
            if (building != null)
            {
                return new CraftingGUIBuilding(player.inventory, world, new BlockPos(x, y, z));
            }
            return null;
        }
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
        else
        {
            @Nullable final AbstractBuilding.View building = ColonyManager.getBuildingView(pos);
            if (building != null)
            {
                return new WindowGuiCrafting(player.inventory, world, new BlockPos(x, y, z), building);
            }
        }

        return null;
    }
}
