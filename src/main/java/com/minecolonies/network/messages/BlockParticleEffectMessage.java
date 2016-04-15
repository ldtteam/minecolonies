package com.minecolonies.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Handles the server telling nearby clients to render a particle effect
 * Created: February 10, 2016
 *
 * @author Colton
 */
public class BlockParticleEffectMessage implements IMessage, IMessageHandler<BlockParticleEffectMessage, IMessage>
{
    public static final int     BREAK_BLOCK = -1;

    private             int     x;
    private             int     y;
    private             int     z;
    private             Block   block;
    private             int     metadata;
    private             int     side;

    public BlockParticleEffectMessage() {}

    /**
     * Sends a message for particle effect
     *
     * @param x         X-coordinate
     * @param y         Y-coordinate
     * @param z         Z-coordinate
     * @param block     Block causing effect
     * @param metadata  Meta data of the block causing effect
     * @param side      Side of the block causing effect
     */
    public BlockParticleEffectMessage(int x, int y, int z, Block block, int metadata, int side)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.metadata = metadata;
        this.side = side;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        block = Block.getBlockById(buf.readInt());
        metadata = buf.readInt();
        side = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(Block.getIdFromBlock(block));
        buf.writeInt(metadata);
        buf.writeInt(side);
    }

    @Override
    public IMessage onMessage(BlockParticleEffectMessage message, MessageContext ctx)
    {
        if(message.side == BREAK_BLOCK)
        {
            Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(new BlockPos(message.x, message.y, message.z), message.block.getDefaultState()); //todo check default state, mw, trans 1.7
        }
        else
        {
            FMLClientHandler.instance().getClient().effectRenderer.addBlockHitEffects(new BlockPos(x, y, z), side); //todo change side to face
        }
        return null;
    }
}
