package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Switch the buildtool with the respective item in the inventory.
 */
public class SwitchBuildingWithToolMessage implements IMessage
{
    /**
     * The stack to switch.
     */
    private ItemStack stack;

    /**
     * Empty constructor used when registering the
     */
    public SwitchBuildingWithToolMessage()
    {
        super();
    }

    /**
     * Switch the stack.
     *
     * @param stack the stack in the hand.
     */
    public SwitchBuildingWithToolMessage(final ItemStack stack)
    {
        super();
        this.stack = stack;
    }

    /**
     * Reads this packet from a {@link PacketBuffer}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        stack = buf.readItemStack();
    }

    /**
     * Writes this packet to a {@link PacketBuffer}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeItemStack(stack);
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
        final ServerPlayerEntity player = ctxIn.getSender();

        int stackSLot = 0;
        int buildToolSlot = 0;
        for (int i = 0; i < 9; i++)
        {
            if (player.inventory.getStackInSlot(i).isItemEqual(stack))
            {
                stackSLot = i;
            }
            else if (player.inventory.getStackInSlot(i).getItem() == ModItems.buildTool.get())
            {
                buildToolSlot = i;
            }
        }

        for (int i = 9; i < player.inventory.getSizeInventory(); i++)
        {
            if (player.inventory.getStackInSlot(i).getItem() == ModItems.buildTool.get())
            {
                buildToolSlot = i;
            }
        }

        if (stackSLot != buildToolSlot)
        {
            player.inventory.setInventorySlotContents(buildToolSlot, player.inventory.getStackInSlot(stackSLot).copy());
            player.inventory.setInventorySlotContents(stackSLot, new ItemStack(ModItems.buildTool.get(), 1));
        }
    }
}
