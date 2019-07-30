package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.IBlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.types.RackType;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import net.minecraft.block.state.BlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Tile entity for the warehouse shelves.
 */
public class TileEntityRack extends TileEntity
{
    /**
     * The content of the chest.
     */
    private final Map<ItemStorage, Integer> content = new HashMap<>();

    /**
     * Variable which determines if it is a single or doublechest.
     */
    private boolean single = true;

    /**
     * Neighbor position of the rack (double chest).
     */
    private BlockPos relativeNeighbor = null;

    /**
     * Is this the main chest of the doubleChest.
     */
    private boolean main = false;

    /**
     * Size multiplier of the inventory.
     * 0 = default value.
     * 1 = 1*9 additional slots, and so on.
     */
    private int size = 0;

    /**
     * whether this rack is in a warehouse or not.
     * defaults to not
     * set by the warehouse building upon being built
     */
    private boolean inWarehouse = false;

    /**
     * The inventory of the tileEntity.
     */
    private IItemHandlerModifiable inventory = new ItemStackHandler(DEFAULT_SIZE)
    {
        @Override
        protected void onContentsChanged(final int slot)
        {
            updateItemStorage();

            super.onContentsChanged(slot);
        }

        @Override
        public void setStackInSlot(final int slot, final @Nonnull ItemStack stack)
        {
            super.setStackInSlot(slot, stack);

            if (!ItemStackUtils.isEmpty(stack) && world != null && !world.isRemote && inWarehouse && IColonyManager.getInstance().isCoordinateInAnyColony(world, pos))
            {
                final IColony colony = IColonyManager.getInstance().getClosestColony(world, pos);

                if (colony != null && colony.getRequestManager() != null)
                {
                    colony.getRequestManager()
                            .onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(stack));
                }
            }
        }

        @NotNull
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
        {
            final ItemStack result = super.extractItem(slot, amount, simulate);
            updateItemStorage();
            return result;
        }
    };

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
    public boolean hasItemStack(final ItemStack stack)
    {
        return content.containsKey(new ItemStorage(stack));
    }

    /**
     * Set the value for inWarehouse
     *
     * @param isInWarehouse is this rack in a warehouse?
     */
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
        final BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 0x03);

        if (main && combinedHandler == null && getOtherChest() != null)
        {
            combinedHandler = new CombinedInvWrapper(inventory, getOtherChest().inventory);
        }
    }

    /* Get the amount of items matching a predicate in the inventory.
     * @param predicate the predicate.
     * @return the total count.
     */
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
    private void updateBlockState()
    {
        if (world != null && world.getBlockState(pos).getBlock() instanceof BlockMinecoloniesRack && (main || single))
        {
            final BlockState typeHere;
            final BlockState typeNeighbor;
            if (content.isEmpty() && (getOtherChest() == null || getOtherChest().isEmpty()))
            {
                if (getOtherChest() != null && world.getBlockState(this.pos.subtract(relativeNeighbor)).getBlock() instanceof BlockMinecoloniesRack)
                {

                    typeHere = world.getBlockState(pos).withProperty(IBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                    typeNeighbor = world.getBlockState(this.pos.subtract(relativeNeighbor)).withProperty(IBlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE)
                                     .withProperty(IBlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, this.pos.subtract(relativeNeighbor)));
                }
                else
                {
                    typeHere = world.getBlockState(pos).withProperty(IBlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
                    typeNeighbor = null;
                }
            }
            else
            {
                if (getOtherChest() != null && world.getBlockState(this.pos.subtract(relativeNeighbor)).getBlock() instanceof BlockMinecoloniesRack)
                {
                    typeHere = world.getBlockState(pos).withProperty(IBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                    typeNeighbor = world.getBlockState(this.pos.subtract(relativeNeighbor)).withProperty(IBlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE)
                                     .withProperty(IBlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, this.pos.subtract(relativeNeighbor)));
                }
                else
                {
                    typeHere = world.getBlockState(pos).withProperty(IBlockMinecoloniesRack.VARIANT, RackType.FULL);
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
    public TileEntityRack getOtherChest()
    {
        if (relativeNeighbor == null || world == null)
        {
            return null;
        }
        final TileEntity tileEntity = world.getTileEntity(pos.subtract(relativeNeighbor));
        if (tileEntity instanceof TileEntityRack)
        {
            ((TileEntityRack) tileEntity).setNeighbor(this.getPos());
            return (TileEntityRack) tileEntity;
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
    public boolean isEmpty()
    {
        return content.isEmpty();
    }

    /**
     * Method to change the main attribute of the rack.
     *
     * @param main the boolean value defining it.
     */
    public void setMain(final boolean main)
    {
        this.main = main;
        markDirty();
    }

    /**
     * On neighbor changed this will be called from the block.
     *
     * @param newNeighbor the blockPos which has changed.
     */
    public void neighborChanged(final BlockPos newNeighbor)
    {
        final TileEntity entity = world.getTileEntity(newNeighbor);

        if (relativeNeighbor == null && world.getBlockState(newNeighbor).getBlock() instanceof BlockMinecoloniesRack
              && !(entity instanceof TileEntityRack && ((TileEntityRack) entity).getOtherChest() != null))
        {
            this.relativeNeighbor = this.pos.subtract(newNeighbor);
            single = false;
            if (entity instanceof TileEntityRack)
            {
                if (!((TileEntityRack) entity).isMain())
                {
                    this.main = true;
                    ((TileEntityRack) entity).setMain(false);
                }
                ((TileEntityRack) entity).setNeighbor(this.getPos());
                ((TileEntityRack) entity).setMain(false);
                entity.markDirty();
            }

            updateItemStorage();
            this.markDirty();
        }
        else if (relativeNeighbor != null && this.pos.subtract(relativeNeighbor).equals(newNeighbor) && !(world.getBlockState(newNeighbor)
                                                                                                            .getBlock() instanceof BlockMinecoloniesRack))
        {
            this.relativeNeighbor = null;
            single = true;
            this.main = false;
            updateItemStorage();
        }
    }

    /**
     * Check if this is the main chest of the double chest.
     *
     * @return true if so.
     */
    public boolean isMain()
    {
        return this.main;
    }

    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        super.readFromNBT(compound);
        if (compound.keySet().contains(TAG_SIZE))
        {
            size = compound.getInt(TAG_SIZE);
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

        if (compound.keySet().contains(TAG_NEIGHBOR))
        {
            final BlockPos neighbor = BlockPosUtil.readFromNBT(compound, TAG_NEIGHBOR);
            if (neighbor != BlockPos.ORIGIN)
            {
                relativeNeighbor = pos.subtract(neighbor);
            }
        }
        else if (compound.keySet().contains(TAG_RELATIVE_NEIGHBOR))
        {
            relativeNeighbor = BlockPosUtil.readFromNBT(compound, TAG_RELATIVE_NEIGHBOR);
        }

        if (relativeNeighbor != null)
        {
            single = false;
        }
        final ListNBT inventoryTagList = compound.getList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.size(); ++i)
        {
            final CompoundNBT inventoryCompound = inventoryTagList.getCompound(i);
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
        main = compound.getBoolean(TAG_MAIN);
        updateItemStorage();

        this.inWarehouse = compound.getBoolean(TAG_IN_WAREHOUSE);
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);
        compound.putInt(TAG_SIZE, size);

        if (relativeNeighbor != null)
        {
            BlockPosUtil.write(compound, TAG_RELATIVE_NEIGHBOR, relativeNeighbor);
        }
        @NotNull final ListNBT inventoryTagList = new ListNBT();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final CompoundNBT inventoryCompound = new CompoundNBT();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == ItemStackUtils.EMPTY)
            {
                new ItemStack(Blocks.AIR, 0).write(inventoryCompound);
            }
            else
            {
                stack.write(inventoryCompound);
            }
            inventoryTagList.add(inventoryCompound);
        }
        compound.put(TAG_INVENTORY, inventoryTagList);
        compound.putBoolean(TAG_MAIN, main);
        compound.putBoolean(TAG_IN_WAREHOUSE, inWarehouse);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        return new SPacketUpdateTileEntity(this.pos, 0, this.write(compound));
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, @NotNull final BlockState oldState, @NotNull final BlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public boolean hasCapability(@NotNull final Capability<?> capability, final Direction facing)
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
    public <T> T getCapability(@NotNull final Capability<T> capability, final Direction facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (single)
            {
                return (T) inventory;
            }
            else if (getOtherChest() != null)
            {
                if (main)
                {
                    if (combinedHandler == null)
                    {
                        combinedHandler = new CombinedInvWrapper(inventory, getOtherChest().inventory);
                    }
                    return (T) combinedHandler;
                }
                else
                {
                    if (getOtherChest().main)
                    {
                        return (T) getOtherChest().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    }
                    else
                    {
                        this.main = true;

                        if (combinedHandler == null)
                        {
                            combinedHandler = new CombinedInvWrapper(inventory, getOtherChest().inventory);
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
    public BlockPos getNeighbor()
    {
        return pos.subtract(relativeNeighbor);
    }

    /**
     * Define the neighbor for a block.
     *
     * @param neighbor the neighbor to define.
     */
    public void setNeighbor(final BlockPos neighbor)
    {
        if ((single && neighbor != null) || (!single && neighbor == null))
        {
            single = neighbor == null;
            markDirty();
        }

        if ((this.relativeNeighbor == null && neighbor != null) || (this.relativeNeighbor != null && neighbor != null
                                                                      && !this.relativeNeighbor.equals(this.pos.subtract(neighbor))))
        {
            this.relativeNeighbor = this.pos.subtract(neighbor);
            markDirty();
        }
    }
}
