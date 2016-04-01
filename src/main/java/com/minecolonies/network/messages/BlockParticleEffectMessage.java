package com.minecolonies.network.messages;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

/**
 * Handles the server telling nearby clients to render a particle effect
 * Created: February 10, 2016
 *
 * @author Colton
 */
public class BlockParticleEffectMessage implements IMessage, IMessageHandler<BlockParticleEffectMessage, IMessage>
{
    public static final int BREAK_BLOCK = -1;

    private int x;
    private int y;
    private int z;
    private Block block;
    private int metadata;
    private int side;

    public BlockParticleEffectMessage() {}

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
            Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(message.x, message.y, message.z, message.block, message.metadata);
        }
        else
        {
            FMLClientHandler.instance().getClient().effectRenderer.addBlockHitEffects(x, y, z, message.side);
        }
        return null;
    }
}
