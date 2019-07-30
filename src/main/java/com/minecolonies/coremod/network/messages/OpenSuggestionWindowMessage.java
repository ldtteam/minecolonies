package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

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
     * Empty constructor used when registering the message.
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

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        state = NBTUtil.readBlockState(ByteBufUtils.readTag(buf));
        pos = BlockPosUtil.readFromByteBuf(buf);
        stack = ByteBufUtils.readItemStack(buf);
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, NBTUtil.writeBlockState(new CompoundNBT(), state));
        BlockPosUtil.writeToByteBuf(buf, pos);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    protected void messageOnClientThread(final OpenSuggestionWindowMessage message, final MessageContext ctx)
    {
        MineColonies.proxy.openSuggestionWindow(message.pos, message.state, message.stack);
    }
}
