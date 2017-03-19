package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    public static final int SCARECROW = 3;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (ID)
        {
            case 1:
            {
                final ContainerItemHandler container = getServerElementForCapabilityProvider(player, world.getTileEntity(new BlockPos(x, y, z)));
                if (container != null)
                {
                    return container;
                }

                break;
                }
            case 2:
            {
                final ContainerItemHandler container = getServerElementForCapabilityProvider(player, world.getEntityByID(x));
                if (container != null)
                {
                    return container;
                }

                break;
                }
            case SCARECROW:
            {
                final BlockPos pos = new BlockPos(x, y, z);
                final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
                return new Field(tileEntity, player.inventory, world, pos);
            }
            default:
            {
                return null;
            }
        }

        return null;
    }

    @Nullable
    private ContainerItemHandler getServerElementForCapabilityProvider(@NotNull final EntityPlayer player, @Nullable ICapabilityProvider provider)
    {
        if (provider != null && provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
            return new ContainerItemHandler(player.inventory, provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (ID)
        {
            case 1:
            {
                final GuiItemHandler gui = getClientlementForCapabilityProvider(player, world.getTileEntity(new BlockPos(x, y, z)));
                if (gui != null)
                {
                    return gui;
                }

                break;
            }
            case 2:
            {
                final GuiItemHandler gui = getClientlementForCapabilityProvider(player, world.getEntityByID(x));
                if (gui != null)
                {
                    return gui;
                }

                break;
            }
            case SCARECROW:
            {
                final BlockPos pos = new BlockPos(x, y, z);
                final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
                if (tileEntity != null)
                {
                    return new GuiField(player.inventory, tileEntity, world, pos);
                }

                break;
            }
            default:
            {
                return null;
            }
        }

        return null;
    }

    @Nullable
    private GuiItemHandler getClientlementForCapabilityProvider(@NotNull final EntityPlayer player, @Nullable ICapabilityProvider provider)
    {
        if (provider != null && provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
            final IItemHandler handler = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler instanceof IInteractiveItemHandler)
            {
                return new GuiItemHandler((IInteractiveItemHandler) handler);
            }
            else
            {
                MineColonies.getLogger().warn("Tried to create GUI for non-IInteractiveItemHandler - report to developers!");
                return null;
            }
        }

        return null;
    }
}
