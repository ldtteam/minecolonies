package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Tile entity for the warehouse shelves.
 */
public class TileEntityRack extends AbstractTileEntityRack
{
    /**
     * The content of the chest.
     */
    private final Map<ItemStorage, Integer> content = new HashMap<>();

    /**
     * Size multiplier of the inventory.
     * 0 = default value.
     * 1 = 1*9 additional slots, and so on.
     */
    private int size = 0;

    /**
     * The combined inv wrapper for double racks.
     */
    private CombinedInvWrapper combinedHandler;

    /**
     * Check if a certain itemstack is present in the inventory.
     * This method checks the content list, it is therefore extremely fast.
     *
     * @param stack the stack to check.
     * @return true if so.
     */
    @Override
    public boolean hasItemStack(final ItemStack stack)
    {
        return content.containsKey(new ItemStorage(stack));
    }

    /**
     * Set the value for inWarehouse
     *
     * @param isInWarehouse is this rack in a warehouse?
     */
    @Override
    public void setInWarehouse(final Boolean isInWarehouse)
    {
        this.inWarehouse = isInWarehouse;
    }

    /**
     * Checks if the chest is empty.
     * This method checks the content list, it is therefore extremely fast.
     *
     * @return true if so.
     */
    @Override
    public boolean freeStacks()
    {
        return content.isEmpty();
    }

    /**
     * Get the amount of free slots in the inventory.
     * This method checks the content list, it is therefore extremely fast.
     *
     * @return the amount of free slots (an integer).
     */
    @Override
    public int getFreeSlots()
    {
        int freeSlots = inventory.getSlots();
        for (final Map.Entry<ItemStorage, Integer> entry : content.entrySet())
        {
            final double slotsNeeded = (double) entry.getValue() / entry.getKey().getItemStack().getMaxStackSize();
            freeSlots -= (int) Math.ceil(slotsNeeded);
        }
        return freeSlots;
    }

    /**
     * Check if a similar/same item as the stack is in the inventory.
     * This method checks the content list, it is therefore extremely fast.
     *
     * @param stack             the stack to check.
     * @param ignoreDamageValue ignore the damage value.
     * @return true if so.
     */
    @Override
    public boolean hasItemStack(final ItemStack stack, final boolean ignoreDamageValue)
    {
        final ItemStorage compareStorage = new ItemStorage(stack, ignoreDamageValue);
        for (final Map.Entry<ItemStorage, Integer> entry : content.entrySet())
        {
            if (compareStorage.equals(entry.getKey()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a similar/same item as the stack is in the inventory.
     * This method checks the content list, it is therefore extremely fast.
     *
     * @param itemStackSelectionPredicate the predicate to test the stack against.
     * @return true if so.
     */
    @Override
    public boolean hasItemStack(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        for (final Map.Entry<ItemStorage, Integer> entry : content.entrySet())
        {
            if (itemStackSelectionPredicate.test(entry.getKey().getItemStack()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Upgrade the rack by 1. This adds 9 more slots and copies the inventory to the new one.
     */
    @Override
    public void upgradeItemStorage()
    {
        ++size;
        final IItemHandlerModifiable tempInventory = new ItemStackHandler(DEFAULT_SIZE + size * SLOT_PER_LINE)
        {
            @Override
            protected void onContentsChanged(final int slot)
            {
                updateItemStorage();
                super.onContentsChanged(slot);
            }
        };

        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            tempInventory.setStackInSlot(slot, inventory.getStackInSlot(slot));
        }

        inventory = tempInventory;
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 0x03);

        if (main && combinedHandler == null && getOtherChest() != null)
        {
            combinedHandler = new CombinedInvWrapper(inventory, getOtherChest().getInventory());
        }
    }

    /* Get the amount of items matching a predicate in the inventory.
     * @param predicate the predicate.
     * @return the total count.
     */
    @Override
    public int getItemCount(final Predicate<ItemStack> predicate)
    {
        for (final Map.Entry<ItemStorage, Integer> entry : content.entrySet())
        {
            if (predicate.test(entry.getKey().getItemStack()))
            {
                return entry.getValue();
            }
        }
        return 0;
    }

    /**
     * Scans through the whole storage and updates it.
     */
    @Override
    public void updateItemStorage()
    {
        content.clear();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);

            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            final ItemStorage storage = new ItemStorage(stack.copy());
            int amount = ItemStackUtils.getSize(stack);
            if (content.containsKey(storage))
            {
                amount += content.remove(storage);
            }
            content.put(storage, amount);
        }

        updateBlockState();
        markDirty();
    }

    /**
     * Update the blockState of the rack.
     * Switch between connected, single, full and empty texture.
     */
    @Override
    protected void updateBlockState()
    {
        if (world != null && world.getBlockState(pos).getBlock() instanceof AbstractBlockMinecoloniesRack && (main || single))
        {
            final IBlockState typeHere;
            final IBlockState typeNeighbor;
            if (content.isEmpty() && (getOtherChest() == null || getOtherChest().isEmpty()))
            {
                if (getOtherChest() != null && world.getBlockState(this.pos.subtract(relativeNeighbor)).getBlock() instanceof AbstractBlockMinecoloniesRack)
                {

                    typeHere = world.getBlockState(pos).withProperty(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                    typeNeighbor = world.getBlockState(this.pos.subtract(relativeNeighbor)).withProperty(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE)
                                     .withProperty(AbstractBlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, this.pos.subtract(relativeNeighbor)));
                }
                else
                {
                    typeHere = world.getBlockState(pos).withProperty(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
                    typeNeighbor = null;
                }
            }
            else
            {
                if (getOtherChest() != null && world.getBlockState(this.pos.subtract(relativeNeighbor)).getBlock() instanceof AbstractBlockMinecoloniesRack)
                {
                    typeHere = world.getBlockState(pos).withProperty(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                    typeNeighbor = world.getBlockState(this.pos.subtract(relativeNeighbor)).withProperty(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE)
                                     .withProperty(AbstractBlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, this.pos.subtract(relativeNeighbor)));
                }
                else
                {
                    typeHere = world.getBlockState(pos).withProperty(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULL);
                    typeNeighbor = null;
                }
            }

            // This here avoids that two racks can be main at the same time.
            if (this.isMain() && getOtherChest() != null && getOtherChest().isMain())
            {
                getOtherChest().setMain(false);
            }

            world.setBlockState(pos, typeHere);
            if (typeNeighbor != null)
            {
                world.setBlockState(this.pos.subtract(relativeNeighbor), typeNeighbor);
            }
        }
    }

    /**
     * Get the other double chest or null.
     *
     * @return the tileEntity of the other half or null.
     */
    @Override
    public AbstractTileEntityRack getOtherChest()
    {
        if (relativeNeighbor == null || world == null)
        {
            return null;
        }
        final TileEntity tileEntity = world.getTileEntity(pos.subtract(relativeNeighbor));
        if (tileEntity instanceof TileEntityRack)
        {
            ((AbstractTileEntityRack) tileEntity).setNeighbor(this.getPos());
            return (AbstractTileEntityRack) tileEntity;
        }

        single = true;
        relativeNeighbor = null;
        return null;
    }

    /**
     * Checks if the chest is empty.
     * This method checks the content list, it is therefore extremely fast.
     *
     * @return true if so.
     */
    @Override
    public boolean isEmpty()
    {
        return content.isEmpty();
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_SIZE))
        {
            size = compound.getInteger(TAG_SIZE);
            if (size > 0)
            {
                inventory = new ItemStackHandler(DEFAULT_SIZE + size * SLOT_PER_LINE)
                {
                    @Override
                    protected void onContentsChanged(final int slot)
                    {
                        updateItemStorage();
                        super.onContentsChanged(slot);
                    }
                };
            }
        }

        if (compound.hasKey(TAG_NEIGHBOR))
        {
            final BlockPos neighbor = BlockPosUtil.readFromNBT(compound, TAG_NEIGHBOR);
            if (neighbor != BlockPos.ORIGIN)
            {
                relativeNeighbor = pos.subtract(neighbor);
            }
        }
        else if (compound.hasKey(TAG_RELATIVE_NEIGHBOR))
        {
            relativeNeighbor = BlockPosUtil.readFromNBT(compound, TAG_RELATIVE_NEIGHBOR);
        }

        if (relativeNeighbor != null)
        {
            if (relativeNeighbor.getY() != 0)
            {
                relativeNeighbor = null;
            }
            else
            {
                single = false;
            }
        }
        final NBTTagList inventoryTagList = compound.getTagList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.tagCount(); ++i)
        {
            final NBTTagCompound inventoryCompound = inventoryTagList.getCompoundTagAt(i);
            final ItemStack stack = new ItemStack(inventoryCompound);
            if (ItemStackUtils.getSize(stack) <= 0)
            {
                inventory.setStackInSlot(i, ItemStackUtils.EMPTY);
            }
            else
            {
                inventory.setStackInSlot(i, stack);
            }
        }
        
        if (compound.hasKey("Items"))
        {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                if (j >= 0 && j < inventory.getSlots())
                {
                    inventory.setStackInSlot(j, new ItemStack(nbttagcompound));
                }
            }
        }

        main = compound.getBoolean(TAG_MAIN);
        updateItemStorage();

        this.inWarehouse = compound.getBoolean(TAG_IN_WAREHOUSE);
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_SIZE, size);

        if (relativeNeighbor != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_RELATIVE_NEIGHBOR, relativeNeighbor);
        }
        @NotNull final NBTTagList inventoryTagList = new NBTTagList();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final NBTTagCompound inventoryCompound = new NBTTagCompound();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == ItemStackUtils.EMPTY)
            {
                new ItemStack(Blocks.AIR, 0).writeToNBT(inventoryCompound);
            }
            else
            {
                stack.writeToNBT(inventoryCompound);
            }
            inventoryTagList.appendTag(inventoryCompound);
        }
        compound.setTag(TAG_INVENTORY, inventoryTagList);
        compound.setBoolean(TAG_MAIN, main);
        compound.setBoolean(TAG_IN_WAREHOUSE, inWarehouse);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        return new SPacketUpdateTileEntity(this.pos, 0, this.writeToNBT(compound));
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, @NotNull final IBlockState oldState, @NotNull final IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public boolean hasCapability(@NotNull final Capability<?> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public void rotate(final Rotation rotationIn)
    {
        super.rotate(rotationIn);
        if (relativeNeighbor != null)
        {
            relativeNeighbor = relativeNeighbor.rotate(rotationIn);
        }
    }

    @Override
    public <T> T getCapability(@NotNull final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (single)
            {
                return (T) inventory;
            }
            else if (getOtherChest() != null)
            {
                if (isMain())
                {
                    if (combinedHandler == null)
                    {
                        combinedHandler = new CombinedInvWrapper(inventory, getOtherChest().getInventory());
                    }
                    return (T) combinedHandler;
                }
                else
                {
                    if (getOtherChest().isMain())
                    {
                        return (T) getOtherChest().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    }
                    else
                    {
                        this.main = true;

                        if (combinedHandler == null)
                        {
                            combinedHandler = new CombinedInvWrapper(inventory, getOtherChest().getInventory());
                        }
                        markDirty();
                        return (T) combinedHandler;
                    }
                }
            }
        }
        return super.getCapability(capability, facing);
    }

    /**
     * Get the neighbor of the entity.
     *
     * @return the position, a blockPos.
     */
    @Override
    public BlockPos getNeighbor()
    {
        return pos.subtract(relativeNeighbor);
    }

    /**
     * Define the neighbor for a block.
     *
     * @param neighbor the neighbor to define.
     */
    @Override
    public boolean setNeighbor(final BlockPos neighbor)
    {
        if (neighbor == null)
        {
            single = true;
            this.relativeNeighbor = null;
            markDirty();
        }
        // Only allow horizontal neighbor's
        else if (this.pos.subtract(neighbor).getY() == 0)
        {
            this.relativeNeighbor = this.pos.subtract(neighbor);
            single = false;
            markDirty();
            return true;
        }
        return false;
    }
}
