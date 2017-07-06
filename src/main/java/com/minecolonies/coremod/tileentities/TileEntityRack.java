package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
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
     * Tag used to store if the entity is the main.
     */
    private static final String TAG_MAIN = "main";

    /**
     * Tag compound of forge.
     */
    private static final int TAG_COMPOUND = 10;

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
    private boolean isMain = false;

    /**
     * The content of the chest.
     */
    private final Map<ItemStorage, Integer> content = new HashMap<>();

    /**
     * The inventory of the tileEntity.
     */
    private final IItemHandlerModifiable inventory = new ItemStackHandler(27)
    {
        @Override
        protected void onContentsChanged(final int slot)
        {
            updateItemStorage();
            super.onContentsChanged(slot);
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
    public boolean isEmpty()
    {
        return content.isEmpty();
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
        for (int itemAmount : content.values())
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

        for (final Map.Entry<ItemStorage, Integer> entry : content.entrySet())
        {
            Log.getLogger().warn(entry.getKey().getItemStack().getDisplayName() + ": " + entry.getValue());
        }

        markDirty();
    }

    /**
     * Update the blockState of the rack.
     * Switch between connected, single, full and empty texture.
     */
    private void updateBlockState()
    {
        if (worldObj != null && worldObj.getBlockState(pos).getBlock() instanceof BlockMinecoloniesRack && (isMain || single))
        {
            final IBlockState typeHere;
            final IBlockState typeNeighbor;
            if (content.isEmpty() && (getOtherChest() == null || getOtherChest().isEmpty()))
            {
                if (getOtherChest() != null && worldObj.getBlockState(neighbor).getBlock() instanceof BlockMinecoloniesRack)
                {

                    typeHere = worldObj.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.EMPTYAIR);
                    typeNeighbor = worldObj.getBlockState(neighbor).withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.DEFAULTDOUBLE)
                            .withProperty(BlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, neighbor));
                }
                else
                {
                    typeHere = worldObj.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.DEFAULT);
                    typeNeighbor = null;
                }
            }
            else
            {
                if (getOtherChest() != null && worldObj.getBlockState(neighbor).getBlock() instanceof BlockMinecoloniesRack)
                {
                    typeHere = worldObj.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.EMPTYAIR);
                    typeNeighbor = worldObj.getBlockState(neighbor).withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.FULLDOUBLE)
                            .withProperty(BlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(pos, neighbor));
                }
                else
                {
                    typeHere = worldObj.getBlockState(pos).withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.FULL);
                    typeNeighbor = null;
                }
            }
            worldObj.setBlockState(pos, typeHere);
            if (typeNeighbor != null)
            {
                worldObj.setBlockState(neighbor, typeNeighbor);
            }
        }
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

    /**
     * Check if this is the main chest of the double chest.
     *
     * @return true if so.
     */
    public boolean isMain()
    {
        return this.isMain;
    }

    /**
     * On neighbor changed this will be called from the block.
     *
     * @param newNeighbor the blockPos which has changed.
     */
    public void neighborChanged(final BlockPos newNeighbor)
    {
        final TileEntity entity = worldObj.getTileEntity(newNeighbor);
        if (this.neighbor.equals(BlockPos.ORIGIN) && worldObj.getBlockState(newNeighbor).getBlock() instanceof BlockMinecoloniesRack
                && !(entity instanceof TileEntityRack && ((TileEntityRack) entity).getOtherChest() != null))
        {
            this.neighbor = newNeighbor;
            single = false;
            if (entity instanceof TileEntityRack && !((TileEntityRack) entity).isMain())
            {
                this.isMain = true;
            }
            updateItemStorage();
        }
        else if (this.neighbor.equals(newNeighbor) && !(worldObj.getBlockState(newNeighbor).getBlock() instanceof BlockMinecoloniesRack))
        {
            this.neighbor = BlockPos.ORIGIN;
            single = true;
            this.isMain = false;
            updateItemStorage();
        }
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
            if (single)
            {
                return (T) inventory;
            }
            else
            {
                if (isMain)
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

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        neighbor = BlockPosUtil.readFromNBT(compound, TAG_NEIGHBOR);

        if (!neighbor.equals(BlockPos.ORIGIN))
        {
            single = false;
        }
        final NBTTagList inventoryTagList = compound.getTagList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.tagCount(); ++i)
        {
            final NBTTagCompound inventoryCompound = inventoryTagList.getCompoundTagAt(i);
            final ItemStack stack = ItemStackUtils.loadItemStackFromNBT(inventoryCompound);
            if (ItemStackUtils.getSize(stack) <= 0)
            {
                inventory.setStackInSlot(i, ItemStackUtils.EMPTY);
            }
            else
            {
                inventory.setStackInSlot(i, stack);
            }
        }
        isMain = compound.getBoolean(TAG_MAIN);
        updateItemStorage();
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        BlockPosUtil.writeToNBT(compound, TAG_NEIGHBOR, neighbor);
        @NotNull final NBTTagList inventoryTagList = new NBTTagList();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final NBTTagCompound inventoryCompound = new NBTTagCompound();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null)
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
        compound.setBoolean(TAG_MAIN, isMain);
        return super.writeToNBT(compound);
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
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
        final TileEntity tileEntity = worldObj.getTileEntity(neighbor);
        if (tileEntity instanceof TileEntityRack)
        {
            ((TileEntityRack) tileEntity).setNeighbor(this.getPos());
            return (TileEntityRack) tileEntity;
        }
        return null;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        BlockPosUtil.writeToNBT(compound, TAG_NEIGHBOR, neighbor);
        @NotNull final NBTTagList inventoryTagList = new NBTTagList();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final NBTTagCompound inventoryCompound = new NBTTagCompound();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null)
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
        compound.setBoolean(TAG_MAIN, isMain);
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
        neighbor = BlockPosUtil.readFromNBT(compound, TAG_NEIGHBOR);

        if (!neighbor.equals(BlockPos.ORIGIN))
        {
            single = false;
        }
        final NBTTagList inventoryTagList = compound.getTagList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.tagCount(); ++i)
        {
            final NBTTagCompound inventoryCompound = inventoryTagList.getCompoundTagAt(i);
            final ItemStack stack = ItemStackUtils.loadItemStackFromNBT(inventoryCompound);
            if (ItemStackUtils.getSize(stack) <= 0)
            {
                inventory.setStackInSlot(i, ItemStackUtils.EMPTY);
            }
            else
            {
                inventory.setStackInSlot(i, stack);
            }
        }
        isMain = compound.getBoolean(TAG_MAIN);
    }

    /**
     * Get the neighbor of the entity.
     * @return the position, a blockPos.
     */
    public BlockPos getNeighbor()
    {
        return neighbor;
    }
}
