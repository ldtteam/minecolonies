package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

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
     * Empty constructor used when registering the 
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

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player.openContainer instanceof ContainerCrafting)
        {
            final ContainerCrafting container = (ContainerCrafting) player.openContainer;

            if(complete)
            {
                container.handleSlotClick(container.getSlot(1), itemStacks.containsKey(0) ? itemStacks.get(0) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(2), itemStacks.containsKey(1) ? itemStacks.get(1) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(3), itemStacks.containsKey(2) ? itemStacks.get(2) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(4), itemStacks.containsKey(3) ? itemStacks.get(3) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(5), itemStacks.containsKey(4) ? itemStacks.get(4) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(6), itemStacks.containsKey(5) ? itemStacks.get(5) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(7), itemStacks.containsKey(6) ? itemStacks.get(6) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(8), itemStacks.containsKey(7) ? itemStacks.get(7) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(9), itemStacks.containsKey(8) ? itemStacks.get(8) : ItemStackUtils.EMPTY);
            }
            else
            {
                container.handleSlotClick(container.getSlot(1), itemStacks.containsKey(0) ? itemStacks.get(0) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(2), itemStacks.containsKey(1) ? itemStacks.get(1) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(3), itemStacks.containsKey(3) ? itemStacks.get(3) : ItemStackUtils.EMPTY);
                container.handleSlotClick(container.getSlot(4), itemStacks.containsKey(4) ? itemStacks.get(4) : ItemStackUtils.EMPTY);
            }

            container.detectAndSendChanges();
        }
    }
}
