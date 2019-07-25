package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the server telling nearby clients to render a particle effect.
 * Created: February 10, 2016
 *
 * @author Colton
 */
public class BlockParticleEffectMessage extends AbstractMessage<BlockParticleEffectMessage, IMessage>
{
    public static final int BREAK_BLOCK = -1;

    private BlockPos pos;
    private Block    block;
    private int      metadata;
    private int      side;

    /**
     * Empty constructor used when registering the message.
     */
    public BlockParticleEffectMessage()
    {
        super();
    }

    /**
     * Sends a message for particle effect.
     *
     * @param pos   Coordinates
     * @param state Block State
     * @param side  Side of the block causing effect
     */
    public BlockParticleEffectMessage(final BlockPos pos, @NotNull final BlockState state, final int side)
    {
        this.pos = pos;
        this.block = state.getBlock();
        this.metadata = state.getBlock().getMetaFromState(state);
        this.side = side;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        pos = BlockPosUtil.readFromByteBuf(buf);
        block = Block.getBlockById(buf.readInt());
        metadata = buf.readInt();
        side = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(Block.getIdFromBlock(block));
        buf.writeInt(metadata);
        buf.writeInt(side);
    }

    @Override
    protected void messageOnClientThread(final BlockParticleEffectMessage message, final MessageContext ctx)
    {
        if (message.side == BREAK_BLOCK)
        {
            Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(message.pos, message.block.getDefaultState());
        }
        else
        {
            FMLClientHandler.instance().getClient().effectRenderer.addBlockHitEffects(message.pos, Direction.byIndex(message.side));
        }
    }
}
