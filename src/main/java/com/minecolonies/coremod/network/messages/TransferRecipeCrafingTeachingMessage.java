package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferRecipeCrafingTeachingMessage extends AbstractMessage<TransferRecipeCrafingTeachingMessage, IMessage>
{
    Map<Integer, ItemStack> itemStacks = new HashMap<>();

    public TransferRecipeCrafingTeachingMessage()
    {
    }

    public TransferRecipeCrafingTeachingMessage(final Map<Integer, ItemStack> itemStacks)
    {
        this.itemStacks = itemStacks;
    }

    @Override
    public void messageOnServerThread(final TransferRecipeCrafingTeachingMessage message, final EntityPlayerMP player)
    {
        if (player.openContainer instanceof CraftingGUIBuilding)
        {
            CraftingGUIBuilding container = (CraftingGUIBuilding) player.openContainer;

            container.handleSlotClick(container.getSlot(1), message.itemStacks.containsKey(0) ? message.itemStacks.get(0) : ItemStackUtils.EMPTY);
            container.handleSlotClick(container.getSlot(2), message.itemStacks.containsKey(1) ? message.itemStacks.get(1) : ItemStackUtils.EMPTY);
            container.handleSlotClick(container.getSlot(3), message.itemStacks.containsKey(3) ? message.itemStacks.get(3) : ItemStackUtils.EMPTY);
            container.handleSlotClick(container.getSlot(4), message.itemStacks.containsKey(4) ? message.itemStacks.get(4) : ItemStackUtils.EMPTY);

            container.detectAndSendChanges();
        }
    }

    @Override
    public void fromBytes(final ByteBuf buf)
    {
        itemStacks.clear();
        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            itemStacks.put(buf.readInt(), ByteBufUtils.readItemStack(buf));
        }
    }

    @Override
    public void toBytes(final ByteBuf buf)
    {
        buf.writeInt(itemStacks.size());
        itemStacks.forEach((slot, stack) ->
        {
            buf.writeInt(slot);
            ByteBufUtils.writeItemStack(buf, stack);
        });
    }
}
