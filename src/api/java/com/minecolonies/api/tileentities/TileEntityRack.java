package com.minecolonies.api.tileentities;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.inventory.container.ContainerRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RACK;

import com.minecolonies.api.tileentities.AbstractTileEntityRack.RackInventory;

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
     * Size multiplier of the inventory. 0 = default value. 1 = 1*9 additional slots, and so on.
     */
    private int size = 0;

    /**
     * Amount of free slots
     */
    private int freeSlots = 0;

    /**
     * Last optional we created.
     */
    private LazyOptional<IItemHandler> lastOptional;

    public TileEntityRack(final TileEntityType<? extends TileEntityRack> type)
    {
        super(type);
    }

    public TileEntityRack()
    {
        super(MinecoloniesTileEntities.RACK);
    }

    @Override
    public void setInWarehouse(final Boolean isInWarehouse)
    {
        this.inWarehouse = isInWarehouse;
    }

    @Override
    public int getFreeSlots()
    {
        return freeSlots;
    }

    @Override
    public boolean hasItemStack(final ItemStack stack, final int count, final boolean ignoreDamageValue)
    {
        final ItemStorage checkItem = new ItemStorage(stack, ignoreDamageValue);

        return content.getOrDefault(checkItem, 0) >= count;
    }

    @Override
    public int getCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT)
    {
        final ItemStorage checkItem = new ItemStorage(stack, ignoreDamageValue, ignoreNBT);
        return content.getOrDefault(checkItem, 0);
    }

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

    @Override
    public boolean hasSimilarStack(@NotNull final ItemStack stack)
    {
        final ItemStorage checkItem = new ItemStorage(stack, true);
        if (content.containsKey(checkItem))
        {
            return true;
        }

        for (final ItemStorage storage : content.keySet())
        {
            for (final ResourceLocation tag : stack.getItem().getTags())
            {
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().enabledModTags.get().contains(tag.toString())
                      && storage.getItemStack().getItem().getTags().contains(tag))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the content of the Rack
     *
     * @return the map of content.
     */
    public Map<ItemStorage, Integer> getAllContent()
    {
        return content;
    }

    @Override
    public void upgradeItemStorage()
    {
        ++size;
        final RackInventory tempInventory = new RackInventory(DEFAULT_SIZE + size * SLOT_PER_LINE);
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            tempInventory.setStackInSlot(slot, inventory.getStackInSlot(slot));
        }

        inventory = tempInventory;
        final BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 0x03);
        invalidateCap();
    }

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

    @Override
    public void updateItemStorage()
    {
        if (level != null && !level.isClientSide)
        {
            final boolean empty = content.isEmpty();
            updateContent();

            if ((empty && !content.isEmpty()) || !empty && content.isEmpty())
            {
                updateBlockState();
            }
            setChanged();
        }
    }

    /**
     * Just do the content update.
     */
    private void updateContent()
    {
        content.clear();
        freeSlots = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);

            if (ItemStackUtils.isEmpty(stack))
            {
                freeSlots++;
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
    }

    @Override
    public void updateBlockState()
    {
        if (level != null && level.getBlockState(worldPosition).getBlock() instanceof AbstractBlockMinecoloniesRack)
        {
            if (!main && !single && getOtherChest() != null && !getOtherChest().isMain())
            {
                setMain(true);
            }

            if (main || single)
            {
                final BlockState typeHere;
                final BlockState typeNeighbor;
                if (content.isEmpty() && (getOtherChest() == null || getOtherChest().isEmpty()))
                {
                    if (getOtherChest() != null && level.getBlockState(this.worldPosition.subtract(relativeNeighbor)).getBlock() instanceof AbstractBlockMinecoloniesRack)
                    {

                        typeHere = level.getBlockState(worldPosition).setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                        typeNeighbor = level.getBlockState(this.worldPosition.subtract(relativeNeighbor)).setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE)
                                         .setValue(AbstractBlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(worldPosition, this.worldPosition.subtract(relativeNeighbor)));
                    }
                    else
                    {
                        typeHere = level.getBlockState(worldPosition).setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
                        typeNeighbor = null;
                    }
                }
                else
                {
                    if (getOtherChest() != null && level.getBlockState(this.worldPosition.subtract(relativeNeighbor)).getBlock() instanceof AbstractBlockMinecoloniesRack)
                    {
                        typeHere = level.getBlockState(worldPosition).setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                        typeNeighbor = level.getBlockState(this.worldPosition.subtract(relativeNeighbor)).setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE)
                                         .setValue(AbstractBlockMinecoloniesRack.FACING, BlockPosUtil.getFacing(worldPosition, this.worldPosition.subtract(relativeNeighbor)));
                    }
                    else
                    {
                        typeHere = level.getBlockState(worldPosition).setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULL);
                        typeNeighbor = null;
                    }
                }

                // This here avoids that two racks can be main at the same time.
                if (this.isMain() && getOtherChest() != null && getOtherChest().isMain())
                {
                    getOtherChest().setMain(false);
                }

                if (!level.getBlockState(worldPosition).equals(typeHere))
                {
                    level.setBlockAndUpdate(worldPosition, typeHere);
                }
                if (typeNeighbor != null)
                {
                    if (!level.getBlockState(this.worldPosition.subtract(relativeNeighbor)).equals(typeNeighbor))
                    {
                        level.setBlockAndUpdate(this.worldPosition.subtract(relativeNeighbor), typeNeighbor);
                    }
                }
            }
            else
            {
                getOtherChest().updateBlockState();
            }
        }
    }

    @Override
    public AbstractTileEntityRack getOtherChest()
    {
        if (relativeNeighbor == null || level == null)
        {
            return null;
        }
        final TileEntity tileEntity = level.getBlockEntity(worldPosition.subtract(relativeNeighbor));
        if (tileEntity instanceof TileEntityRack && !(tileEntity instanceof AbstractTileEntityColonyBuilding))
        {
            if (!this.getBlockPos().equals(((TileEntityRack) tileEntity).getNeighbor()))
            {
                ((AbstractTileEntityRack) tileEntity).setNeighbor(this.getBlockPos());
            }
            return (AbstractTileEntityRack) tileEntity;
        }

        setSingle(true);
        relativeNeighbor = null;
        return null;
    }

    @Override
    public ItemStackHandler createInventory(final int slots)
    {
        return new RackInventory(slots);
    }

    @Override
    public boolean isEmpty()
    {
        return content.isEmpty();
    }

    @Override
    public void load(final BlockState state, final CompoundNBT compound)
    {
        super.load(state, compound);
        if (compound.getAllKeys().contains(TAG_SIZE))
        {
            size = compound.getInt(TAG_SIZE);
            inventory = createInventory(DEFAULT_SIZE + size * SLOT_PER_LINE);
        }

        if (compound.getAllKeys().contains(TAG_RELATIVE_NEIGHBOR))
        {
            relativeNeighbor = BlockPosUtil.read(compound, TAG_RELATIVE_NEIGHBOR);
        }

        if (relativeNeighbor != null)
        {
            if (relativeNeighbor.getY() != 0)
            {
                relativeNeighbor = null;
            }
            else
            {
                setSingle(false);
            }
        }

        final ListNBT inventoryTagList = compound.getList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.size(); i++)
        {
            final CompoundNBT inventoryCompound = inventoryTagList.getCompound(i);
            if (!inventoryCompound.contains(TAG_EMPTY))
            {
                final ItemStack stack = ItemStack.of(inventoryCompound);
                inventory.setStackInSlot(i, stack);
            }
        }

        main = compound.getBoolean(TAG_MAIN);
        updateContent();

        this.inWarehouse = compound.getBoolean(TAG_IN_WAREHOUSE);
        if (compound.contains(TAG_POS))
        {
            this.buildingPos = BlockPosUtil.read(compound, TAG_POS);
        }

        invalidateCap();
    }

    @NotNull
    @Override
    public CompoundNBT save(final CompoundNBT compound)
    {
        super.save(compound);
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
            if (stack.isEmpty())
            {
                inventoryCompound.putBoolean(TAG_EMPTY, true);
            }
            else
            {
                stack.save(inventoryCompound);
            }
            inventoryTagList.add(inventoryCompound);
        }
        compound.put(TAG_INVENTORY, inventoryTagList);
        compound.putBoolean(TAG_MAIN, main);
        compound.putBoolean(TAG_IN_WAREHOUSE, inWarehouse);
        BlockPosUtil.write(compound, TAG_POS, buildingPos);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.save(compound));
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        this.load(getBlockState(), packet.getTag());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.load(state, tag);
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, final Direction dir)
    {
        if (!remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (lastOptional != null && lastOptional.isPresent())
            {
                return lastOptional.cast();
            }

            if (single)
            {
                lastOptional = LazyOptional.of(() ->
                {
                    if (this.isRemoved())
                    {
                        return new RackInventory(0);
                    }

                    return new CombinedItemHandler(RACK, getInventory());
                });
                return lastOptional.cast();
            }
            else
            {
                lastOptional = LazyOptional.of(() ->
                {
                    if (this.isRemoved())
                    {
                        return new RackInventory(0);
                    }

                    final AbstractTileEntityRack other = getOtherChest();
                    if (other == null)
                    {
                        return new CombinedItemHandler(RACK, getInventory());
                    }

                    if (main)
                    {
                        return new CombinedItemHandler(RACK, getInventory(), other.getInventory());
                    }
                    else
                    {
                        return new CombinedItemHandler(RACK, other.getInventory(), getInventory());
                    }
                });

                return lastOptional.cast();
            }
        }
        return super.getCapability(capability, dir);
    }

    /**
     * Get the neighbor of the entity.
     *
     * @return the position, a blockPos.
     */
    @Override
    public BlockPos getNeighbor()
    {
        if (relativeNeighbor == null)
        {
            return null;
        }
        return worldPosition.subtract(relativeNeighbor);
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
            setSingle(true);
            this.relativeNeighbor = null;
            setChanged();
        }
        // Only allow horizontal neighbor's
        else if (this.worldPosition.subtract(neighbor).getY() == 0)
        {
            this.relativeNeighbor = this.worldPosition.subtract(neighbor);
            setSingle(false);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public void setChanged()
    {
        WorldUtil.markChunkDirty(level, worldPosition);
    }

    @Nullable
    @Override
    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
    {
        return new ContainerRack(id, inv, getBlockPos(), getOtherChest() == null ? BlockPos.ZERO : getOtherChest().getBlockPos());
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("Rack");
    }

    @Override
    public void setMain(final boolean main)
    {
        if (main != this.main)
        {
            invalidateCap();
            super.setMain(main);
        }
    }

    @Override
    public void setSingle(final boolean single)
    {
        if (single != this.single)
        {
            invalidateCap();
            super.setSingle(single);
        }
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        invalidateCap();
    }

    @Override
    public void clearCache()
    {
        super.clearCache();
        invalidateCap();
    }

    /**
     * Invalidates the cap
     */
    private void invalidateCap()
    {
        if (lastOptional != null && lastOptional.isPresent())
        {
            lastOptional.invalidate();
        }

        lastOptional = null;
    }
}
