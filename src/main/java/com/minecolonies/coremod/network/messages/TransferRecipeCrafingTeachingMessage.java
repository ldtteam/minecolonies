package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a message to get jei recipes.
 */
public class TransferRecipeCrafingTeachingMessage extends AbstractMessage<TransferRecipeCrafingTeachingMessage, IMessage>
{
    /**
     * Recipes to transfer.
     */
    private Map<Integer, ItemStack> itemStacks = new HashMap<>();

    /**
     * Empty constructor used when registering the message.
     */
    public TransferRecipeCrafingTeachingMessage()
    {
        super();
    }

    /**
     * Creates a new message to get jei recipes.
     * @param itemStacks the stack recipes to register.
     */
    public TransferRecipeCrafingTeachingMessage(final Map<Integer, ItemStack> itemStacks)
    {
        super();
        this.itemStacks = itemStacks;
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

    @Override
    public void messageOnServerThread(final TransferRecipeCrafingTeachingMessage message, final EntityPlayerMP player)
    {
        if (player.openContainer instanceof CraftingGUIBuilding)
        {
            final CraftingGUIBuilding container = (CraftingGUIBuilding) player.openContainer;

            container.handleSlotClick(container.getSlot(1), message.itemStacks.containsKey(0) ? message.itemStacks.get(0) : ItemStackUtils.EMPTY);
            container.handleSlotClick(container.getSlot(2), message.itemStacks.containsKey(1) ? message.itemStacks.get(1) : ItemStackUtils.EMPTY);
            container.handleSlotClick(container.getSlot(3), message.itemStacks.containsKey(3) ? message.itemStacks.get(3) : ItemStackUtils.EMPTY);
            container.handleSlotClick(container.getSlot(4), message.itemStacks.containsKey(4) ? message.itemStacks.get(4) : ItemStackUtils.EMPTY);

            container.detectAndSendChanges();
        }
    }
}
