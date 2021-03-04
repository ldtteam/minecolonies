package com.minecolonies.api.tileentities;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesGrave;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.inventory.container.ContainerGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
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
        if (world != null && !world.isRemote)
        {
            final boolean empty = content.isEmpty();
            updateContent();

            if ((empty && !content.isEmpty()) || !empty && content.isEmpty())
            {
                updateBlockState();
            }
            markDirty();
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
        if (world != null && world.getBlockState(pos).getBlock() instanceof AbstractBlockMinecoloniesGrave)
        {
            final BlockState typeHere = world.getBlockState(pos);
            if (!world.getBlockState(pos).equals(typeHere))
            {
                world.setBlockState(pos, typeHere);
            }
        }
    }

    @Override
    public ItemStackHandler createInventory(final int slots)
    {
        return new GraveInventory(slots);
    }

    @Override
    public boolean isEmpty()
    {
        return content.isEmpty();
    }

    @Override
    public void read(final BlockState state, final CompoundNBT compound)
    {
        super.read(state, compound);
        inventory = createInventory(DEFAULT_SIZE);

        final ListNBT inventoryTagList = compound.getList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.size(); i++)
        {
            final CompoundNBT inventoryCompound = inventoryTagList.getCompound(i);
            if (!inventoryCompound.contains(TAG_EMPTY))
            {
                final ItemStack stack = ItemStack.read(inventoryCompound);
                inventory.setStackInSlot(i, stack);
            }
        }
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

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
                stack.write(inventoryCompound);
            }
            inventoryTagList.add(inventoryCompound);
        }
        compound.put(TAG_INVENTORY, inventoryTagList);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        return new SUpdateTileEntityPacket(this.pos, 0, this.write(compound));
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        this.read(getBlockState(), packet.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.read(state, tag);
    }

    @Override
    public void markDirty()
    {
        WorldUtil.markChunkDirty(world, pos);
    }

    @Nullable
    @Override
    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
    {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBlockPos(this.getPos());

        return new ContainerGrave(id, inv, buffer);
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("Grave");
    }
}
