package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Open the suggestion window.
 */
public class OpenSuggestionWindowMessage implements IMessage
{
    /**
     * The state to be placed..
     */
    private BlockState state;

    /**
     * The position to place it at.
     */
    private BlockPos pos;

    /**
     * The stack which is going to be placed.
     */
    private ItemStack stack;

    /**
     * Empty constructor used when registering the 
     */
    public OpenSuggestionWindowMessage()
    {
        super();
    }

    /**
     * Open the window.
     * @param state the state to be placed.
     * @param pos the pos to place it at.
     * @param stack the stack in the hand.
     */
    public OpenSuggestionWindowMessage(final BlockState state, final BlockPos pos, final ItemStack stack)
    {
        super();
        this.state = state;
        this.pos = pos;
        this.stack = stack;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        state = Block.getStateById(buf.readInt());
        pos = buf.readBlockPos();
        stack = buf.readItemStack();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(Block.getStateId(state));
        buf.writeBlockPos(pos);
        buf.writeItemStack(stack);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        MineColonies.proxy.openSuggestionWindow(pos, state, stack);
    }
}
