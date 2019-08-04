package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a message to get jei recipes.
 */
public class TransferRecipeCrafingTeachingMessage implements IMessage
{
    /**
     * if the recipe is complete.
     */
    private boolean complete;

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
    public TransferRecipeCrafingTeachingMessage(final Map<Integer, ItemStack> itemStacks, final boolean complete)
    {
        super();
        this.itemStacks = itemStacks;
        this.complete = complete;
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        itemStacks.clear();
        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            itemStacks.put(buf.readInt(), buf.readItemStack());
        }
        complete = buf.readBoolean();
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(itemStacks.size());
        itemStacks.forEach((slot, stack) ->
        {
            buf.writeInt(slot);
            buf.writeItemStack(stack);
        });
        buf.writeBoolean(complete);
    }

    @Override
    public void messageOnServerThread(final TransferRecipeCrafingTeachingMessage message, final ServerPlayerEntity player)
    {
        if (player.openContainer instanceof CraftingGUIBuilding)
        {
            final CraftingGUIBuilding container = (CraftingGUIBuilding) player.openContainer;

            if(message.complete)
            {
                container.handleSlotClick(container.getSlot(1), message.itemStacks.containsKey(0) ? message.itemStacks.get(0) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(2), message.itemStacks.containsKey(1) ? message.itemStacks.get(1) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(3), message.itemStacks.containsKey(2) ? message.itemStacks.get(2) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(4), message.itemStacks.containsKey(3) ? message.itemStacks.get(3) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(5), message.itemStacks.containsKey(4) ? message.itemStacks.get(4) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(6), message.itemStacks.containsKey(5) ? message.itemStacks.get(5) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(7), message.itemStacks.containsKey(6) ? message.itemStacks.get(6) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(8), message.itemStacks.containsKey(7) ? message.itemStacks.get(7) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(9), message.itemStacks.containsKey(8) ? message.itemStacks.get(8) : ItemStackUtils.EMPTY);
            }
            else
            {
                container.handleSlotClick(container.getSlot(1), message.itemStacks.containsKey(0) ? message.itemStacks.get(0) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(2), message.itemStacks.containsKey(1) ? message.itemStacks.get(1) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(3), message.itemStacks.containsKey(3) ? message.itemStacks.get(3) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(4), message.itemStacks.containsKey(4) ? message.itemStacks.get(4) : ItemStackUtils.EMPTY);
            }

            container.detectAndSendChanges();
        }
    }
}
