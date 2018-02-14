package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.RackType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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

/**
 * Tile entity for the warehouse shelves.
 */
public class TileEntityRack extends TileEntity
{
    /**
     * Tag used to store the neighbor pos to NBT.
     */
    private static final String TAG_NEIGHBOR = "neighbor";

    /**
     * Tag used to store the inventory to nbt.
     */
    private static final String TAG_INVENTORY = "inventory";

    /**
     * Tag used to store the size.
     */
    private static final String TAG_SIZE = "tagSIze";

    /**
     * Tag used to store if the entity is the main.
     */
    private static final String TAG_MAIN = "main";

    /**
     * Tag compound of forge.
     */
    private static final int TAG_COMPOUND = 10;

    /**
     * Default size of the inventory.
     */
    private static final int DEFAULT_SIZE = 27;

    /**
     * Slots per line.
     */
    private static final int SLOT_PER_LINE = 9;
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
    private BlockPos neighbor = BlockPos.ORIGIN;
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
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
        {
            final ItemStack result = super.extractItem(slot, amount, simulate);
            updateItemStorage();
            return result;
        }
    };

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
        for (final int itemAmount : content.values())
        {
            final double slotsNeeded = (double) itemAmount / Constants.STACKSIZE;
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
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 0x03);
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
            final IBlockState typeHere;
            final IBlockState typeNeighbor;
            if (content.isEmpty() && (getOtherChest() == null || getOtherChest().isEmpty()))
            {
                if (getOtherChest() != null && world.getBlockState(neighbor).getBlock() instanceof BlockMinecoloniesRack)
                {

                    typeHere = world.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                    typeNeighbor = world.getBlockState(neighbor).withProperty(BlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE)
                                     .withProperty(BlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, neighbor));
                }
                else
                {
                    typeHere = world.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
                    typeNeighbor = null;
                }
            }
            else
            {
                if (getOtherChest() != null && world.getBlockState(neighbor).getBlock() instanceof BlockMinecoloniesRack)
                {
                    typeHere = world.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                    typeNeighbor = world.getBlockState(neighbor).withProperty(BlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE)
                                     .withProperty(BlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, neighbor));
                }
                else
                {
                    typeHere = world.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, RackType.FULL);
                    typeNeighbor = null;
                }
            }
            world.setBlockState(pos, typeHere);
            if (typeNeighbor != null)
            {
                world.setBlockState(neighbor, typeNeighbor);
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
        if (neighbor.equals(BlockPos.ORIGIN))
        {
            return null;
        }
        final TileEntity tileEntity = world.getTileEntity(neighbor);
        if (tileEntity instanceof TileEntityRack)
        {
            ((TileEntityRack) tileEntity).setNeighbor(this.getPos());
            return (TileEntityRack) tileEntity;
        }
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
     * @param main the boolean value defining it.
     */
    public void setMain(final boolean main)
    {
        this.main = main;
    }

    /**
     * On neighbor changed this will be called from the block.
     *
     * @param newNeighbor the blockPos which has changed.
     */
    public void neighborChanged(final BlockPos newNeighbor)
    {
        final TileEntity entity = world.getTileEntity(newNeighbor);

        if (!this.neighbor.equals(BlockPos.ORIGIN)
              && this.neighbor.distanceSq(this.pos) > 1
              && entity instanceof TileEntityRack)
        {
            softReset();
        }

        if (this.neighbor.equals(BlockPos.ORIGIN) && world.getBlockState(newNeighbor).getBlock() instanceof BlockMinecoloniesRack
              && !(entity instanceof TileEntityRack && ((TileEntityRack) entity).getOtherChest() != null))
        {
            this.neighbor = newNeighbor;
            single = false;
            if (entity instanceof TileEntityRack && !((TileEntityRack) entity).isMain())
            {
                this.main = true;
                ((TileEntityRack) entity).setMain(false);
            }
            ((TileEntityRack) entity).setNeighbor(this.getPos());
            entity.markDirty();
            updateItemStorage();
            this.markDirty();
        }
        else if (this.neighbor.equals(newNeighbor) && !(world.getBlockState(newNeighbor).getBlock() instanceof BlockMinecoloniesRack))
        {
            this.neighbor = BlockPos.ORIGIN;
            single = true;
            this.main = false;
            updateItemStorage();
        }
    }

    public void softReset()
    {
        this.neighbor = BlockPos.ORIGIN;
        single = true;
        this.main = false;
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
    public void readFromNBT(final NBTTagCompound compound)
    {
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

        neighbor = BlockPosUtil.readFromNBT(compound, TAG_NEIGHBOR);

        if (!neighbor.equals(BlockPos.ORIGIN))
        {
            single = false;
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
        main = compound.getBoolean(TAG_MAIN);
        updateItemStorage();
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setInteger(TAG_SIZE, size);
        BlockPosUtil.writeToNBT(compound, TAG_NEIGHBOR, neighbor);
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
        return super.writeToNBT(compound);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_SIZE, size);
        BlockPosUtil.writeToNBT(compound, TAG_NEIGHBOR, neighbor);

        @NotNull final NBTTagList inventoryTagList = new NBTTagList();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final NBTTagCompound inventoryCompound = new NBTTagCompound();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == ItemStack.EMPTY)
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
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
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
        final NBTTagCompound compound = packet.getNbtCompound();
        size = compound.getInteger(TAG_SIZE);
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
        neighbor = BlockPosUtil.readFromNBT(compound, TAG_NEIGHBOR);
        if (!neighbor.equals(BlockPos.ORIGIN))
        {
            single = false;
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
        main = compound.getBoolean(TAG_MAIN);
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
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
            if (single)
            {
                return (T) inventory;
            }
            else if (getOtherChest() != null)
            {
                if (main && getOtherChest() != null)
                {
                    return (T) new CombinedInvWrapper(inventory, getOtherChest().inventory);
                }
                else
                {
                    return (T) new CombinedInvWrapper(getOtherChest().inventory, inventory);
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
        return neighbor;
    }

    /**
     * Define the neighbor for a block.
     *
     * @param neighbor the neighbor to define.
     */
    public void setNeighbor(final BlockPos neighbor)
    {
        if (!neighbor.equals(BlockPos.ORIGIN))
        {
            this.neighbor = neighbor;
            this.single = false;
        }
        else
        {
            this.neighbor = BlockPos.ORIGIN;
            this.single = true;
        }
    }
}
