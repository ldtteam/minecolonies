package com.minecolonies.core.tileentities;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesGrave;
import com.minecolonies.api.blocks.types.GraveType;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.container.ContainerGrave;
import com.minecolonies.api.tileentities.AbstractTileEntityGrave;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DECAYED;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DECAY_TIMER;

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

    public TileEntityGrave(final BlockEntityType<? extends TileEntityGrave> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    public TileEntityGrave(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.GRAVE.get(), pos, state);
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
        return new AbstractTileEntityRack.RackInventory(slots);
    }

    @Override
    public boolean isEmpty()
    {
        updateContent();
        return content.isEmpty();
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);

        decay_timer         = compound.contains(TAG_DECAY_TIMER) ? compound.getInt(TAG_DECAY_TIMER) : DEFAULT_DECAY_TIMER;
        decayed             = compound.contains(TAG_DECAYED) ? compound.getBoolean(TAG_DECAYED) :false;

        if (compound.contains(TAG_GRAVE_DATA))
        {
            graveData = new GraveData();
            graveData.read(compound.getCompound(TAG_GRAVE_DATA));
        }
        else graveData = null;
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.putInt(TAG_DECAY_TIMER, decay_timer);
        compound.putBoolean(TAG_DECAYED, decayed);

        if(graveData != null)
        {
            compound.put(TAG_GRAVE_DATA, graveData.write());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithId();
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        this.load(packet.getTag());
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
    {
        final RegistryFriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(this.getBlockPos());

        return new ContainerGrave(id, inv, buffer);
    }

    @NotNull
    @Override
    public Component getDisplayName()
    {
        return Component.literal("Grave");
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
