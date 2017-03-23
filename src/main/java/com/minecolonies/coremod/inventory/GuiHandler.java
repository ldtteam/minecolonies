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
                return getServerElementForCapabilityProvider(player, world.getTileEntity(new BlockPos(x, y, z)));
            }
            case 2:
            {
                return getServerElementForCapabilityProvider(player, world.getEntityByID(x));
            }
            case SCARECROW:
            {
                return getServerScarecrowElement(player, world, x, y, z);
            }
        }

        return null;
    }

    @Nullable
    private static ContainerItemHandler getServerElementForCapabilityProvider(@NotNull final EntityPlayer player, @Nullable ICapabilityProvider provider)
    {
        if (provider != null && provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
            return new ContainerItemHandler(player.inventory, provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
        }
        return null;
    }

    @NotNull
    private static Field getServerScarecrowElement(final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        return new Field(tileEntity, player.inventory, world, pos);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (ID)
        {
            case 1:
            {
                return getClientElementForCapabilityProvider(player, world.getTileEntity(new BlockPos(x, y, z)));
            }
            case 2:
            {
                return getClientElementForCapabilityProvider(player, world.getEntityByID(x));
            }
            case SCARECROW:
            {
                return getClientScarecrowElement(player, world, x, y, z);
            }
        }

        return null;
    }

    @Nullable
    private static GuiItemHandler getClientElementForCapabilityProvider(@NotNull final EntityPlayer player, @Nullable ICapabilityProvider provider)
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

    @Nullable
    private static GuiField getClientScarecrowElement(final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        final BlockPos pos = new BlockPos(x, y, z);
        final ScarecrowTileEntity tileEntity = (ScarecrowTileEntity) world.getTileEntity(pos);
        if (tileEntity != null)
        {
            return new GuiField(player.inventory, tileEntity, world, pos);
        }

        return null;
    }
}
