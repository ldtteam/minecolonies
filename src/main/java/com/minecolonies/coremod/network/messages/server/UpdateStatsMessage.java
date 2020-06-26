package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Update item stats on the server.
 */
public class UpdateStatsMessage implements IMessage
{
    /**
     * The stack which is going to be placed.
     */
    private ItemStack stack;

    /**
     * The count.
     */
    private int count;

    /**
     * Empty constructor used when registering the
     */
    public UpdateStatsMessage()
    {
        super();
    }

    /**
     * Place the building.
     * @param item the item.
     * @param count the count.
     */
    public UpdateStatsMessage(final Item item, final int count)
    {
        super();
        this.stack = new ItemStack(item);
        this.count = count;
    }

    /**
     * Reads this packet from a {@link PacketBuffer}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        count = buf.readInt();
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
        buf.writeInt(count);
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
        player.getStats().setValue(player, Stats.ITEM_USED.get(stack.getItem()), count);
        player.getStats().markAllDirty();
    }
}
