package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.blocks.BlockRack;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Tile entity for the warehouse shelves.
 */
public class TileEntityRack extends TileEntity
{
    /**
     * Variable which determines if it is a single or doublechest.
     */
    private boolean single = true;

    /**
     * Neighbor position of the rack (double chest).
     */
    private BlockPos neighbor = BlockPos.ORIGIN;

    /**
     * The content of the chest.
     */
    private final Map<ItemStorage, Integer> content = new HashMap<>();

    final IItemHandlerModifiable inventory = new ItemStackHandler(27)
    {
        @Override
        protected void onContentsChanged(final int slot)
        {
            updateItemStorage();
            super.onContentsChanged(slot);
        }
    };

    /**
     * Scans through the whole storage and updates it.
     */
    public void updateItemStorage()
    {
        content.clear();
        for(int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);

            if(ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            final ItemStorage storage = new ItemStorage(stack.copy());
            int amount = ItemStackUtils.getSize(stack);
            if(content.containsKey(storage))
            {
                amount += content.remove(storage);
            }
            content.put(storage, amount);
        }

        if(content.isEmpty())
        {
            worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockRack.VARIANT, BlockRack.EnumType.DEFAULT));
        }
        else
        {
            worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockRack.VARIANT, BlockRack.EnumType.FULL));
        }

        for(final Map.Entry<ItemStorage, Integer> entry : content.entrySet())
        {
            Log.getLogger().warn(entry.getKey().getItemStack().getDisplayName() + ": " + entry.getValue());
        }
    }

    public void neighborChanged(final BlockPos neighbor)
    {

    }

    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if(single)
            {
                return (T) inventory;
            }
            else
            {
                return (T) new CombinedInvWrapper(inventory, getOtherChest().inventory);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    public TileEntityRack getOtherChest()
    {
        return null;
    }

}
