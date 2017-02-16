package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    public static final int SCARECROW = 3;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 1: {
                final TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
                if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    return new ContainerItemHandler(player.inventory, tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), player);
                }
            } break;
            case 2: {
                final Entity entity = world.getEntityByID(x);
                if (entity != null && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    return new ContainerItemHandler(player.inventory, entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), player);
                }
            } break;
            case SCARECROW: {
                final BlockPos pos = new BlockPos(x, y, z);
                final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
                return new Field(tileEntity, player.inventory, world, pos);
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 1: {
                final TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
                if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    final IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if (handler instanceof IInteractiveItemHandler) {
                        return new GuiItemHandler((IInteractiveItemHandler) handler);
                    } else {
                        MineColonies.getLogger().warn("Tried to create GUI for non-IInteractiveItemHandler - report to developers!");
                        return null;
                    }
                }
            } break;
            case 2: {
                final Entity entity = world.getEntityByID(x);
                if (entity != null && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    final IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if (handler instanceof IInteractiveItemHandler) {
                        return new GuiItemHandler((IInteractiveItemHandler) handler);
                    } else {
                        MineColonies.getLogger().warn("Tried to create GUI for non-IInteractiveItemHandler - report to developers!");
                        return null;
                    }
                }
            } break;
            case SCARECROW: {
                final BlockPos pos = new BlockPos(x, y, z);
                final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
                if (tileEntity != null) {
                    return new GuiField(player.inventory, tileEntity, world, pos);
                }
            } break;
        }

        return null;
    }
}
