package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a message to get jei recipes.
 */
public class TransferRecipeCraftingTeachingMessage implements IMessage
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
    public TransferRecipeCraftingTeachingMessage()
    {
        super();
    }

    /**
     * Creates a new message to get jei recipes.
     *
     * @param itemStacks the stack recipes to register.
     * @param complete   whether we're complete
     */
    public TransferRecipeCraftingTeachingMessage(final Map<Integer, ItemStack> itemStacks, final boolean complete)
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
            itemStacks.put(buf.readInt(), buf.readItem());
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
            buf.writeItem(stack);
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
        if (player.containerMenu instanceof ContainerCrafting)
        {
            final ContainerCrafting container = (ContainerCrafting) player.containerMenu;

            if (complete)
            {
                container.handleSlotClick(container.getSlot(1), itemStacks.getOrDefault(0, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(2), itemStacks.getOrDefault(1, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(3), itemStacks.getOrDefault(2, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(4), itemStacks.getOrDefault(3, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(5), itemStacks.getOrDefault(4, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(6), itemStacks.getOrDefault(5, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(7), itemStacks.getOrDefault(6, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(8), itemStacks.getOrDefault(7, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(9), itemStacks.getOrDefault(8, ItemStackUtils.EMPTY));
            }
            else
            {
                container.handleSlotClick(container.getSlot(1), itemStacks.getOrDefault(0, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(2), itemStacks.getOrDefault(1, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(3), itemStacks.getOrDefault(3, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(4), itemStacks.getOrDefault(4, ItemStackUtils.EMPTY));
            }

            container.broadcastChanges();
        }
        else if (player.containerMenu instanceof ContainerCraftingFurnace)
        {
            final ContainerCraftingFurnace container = (ContainerCraftingFurnace) player.containerMenu;

            container.setFurnaceInput(itemStacks.getOrDefault(0, ItemStack.EMPTY));
        }
    }
}
