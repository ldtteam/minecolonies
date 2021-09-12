package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesGrave;
import com.minecolonies.api.blocks.types.GraveType;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.api.crafting.ItemStackStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.container.ContainerGrave;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Tile entity for the graves.
 */
public class TileEntityGrave extends AbstractTileEntityGrave
{
    /**
     * The content of the chest.
     */
    private final Map<ItemStorage, Integer> content = new HashMap<>();

    /**
     * NBTTag to store grave data.
     */
    private static final String TAG_GRAVE_DATA = "gravedata";

    public TileEntityGrave(final TileEntityType<? extends TileEntityGrave> type)
    {
        super(type);
    }

    public TileEntityGrave()
    {
        super(MinecoloniesTileEntities.GRAVE);
    }

    /**
     * Gets the content of the gave
     *
     * @return the map of content.
     */
    public Map<ItemStorage, Integer> getAllContent()
    {
        return content;
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
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);

            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            final ItemStorage storage = new ItemStackStorage(stack.copy());
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
        if (level != null && level.getBlockState(worldPosition).getBlock() instanceof AbstractBlockMinecoloniesGrave)
        {
            final BlockState state = level.getBlockState(worldPosition).setValue(AbstractBlockMinecoloniesGrave.VARIANT, decayed ? GraveType.DECAYED : GraveType.DEFAULT);
            if (!level.getBlockState(worldPosition).equals(state))
            {
                level.setBlockAndUpdate(worldPosition, state);
            }
        }
    }

    @Override
    public ItemStackHandler createInventory(final int slots)
    {
        return new RackInventory(slots);
    }

    @Override
    public boolean isEmpty()
    {
        updateContent();
        return content.isEmpty();
    }

    @Override
    public void load(final BlockState state, final CompoundNBT compound)
    {
        super.load(state, compound);
        inventory = createInventory(DEFAULT_SIZE);

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

        decay_timer         = compound.contains(TAG_DECAY_TIMER) ? compound.getInt(TAG_DECAY_TIMER) : DEFAULT_DECAY_TIMER;
        decayed             = compound.contains(TAG_DECAYED) ? compound.getBoolean(TAG_DECAYED) :false;

        if (compound.getAllKeys().contains(TAG_GRAVE_DATA))
        {
            graveData = new GraveData();
            graveData.read(compound.getCompound(TAG_GRAVE_DATA));
        }
        else graveData = null;
    }

    @NotNull
    @Override
    public CompoundNBT save(final CompoundNBT compound)
    {
        super.save(compound);

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
        compound.putInt(TAG_DECAY_TIMER, decay_timer);
        compound.putBoolean(TAG_DECAYED, decayed);

        if(graveData != null)
        {
            compound.put(TAG_GRAVE_DATA, graveData.write());
        }

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
    public void setChanged()
    {
        WorldUtil.markChunkDirty(level, worldPosition);
    }

    @Nullable
    @Override
    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
    {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBlockPos(this.getBlockPos());

        return new ContainerGrave(id, inv, buffer);
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("Grave");
    }

    /**
     * Update the decay of this grave onColonyTick
     * When the timer elapses, decay the grave and reset the timer, if the grave is already decayed - remove the tile entity from the world
     *
     * @param delay number of tick between each call
     * @return true if the grave still exist, false otherwise
     **/
    public boolean onColonyTick(final double delay)
    {
        if (this.hasLevel() && !level.isClientSide && decay_timer != -1)
        {
            decay_timer -= delay;
            if (decay_timer <= 0)
            {
                if (!decayed)
                {
                    decayed = true;
                    decay_timer = DEFAULT_DECAY_TIMER;
                    updateBlockState();
                }
                else
                {
                    InventoryUtils.dropItemHandler(inventory, level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
                    level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
                    return false;
                }
            }
        }

        return true;
    }
}
