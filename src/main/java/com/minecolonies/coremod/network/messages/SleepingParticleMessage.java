package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.client.particles.SleepingParticle;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Message for sleeping particles
 */
public class SleepingParticleMessage extends AbstractMessage<SleepingParticleMessage, IMessage>
{
    /**
     * Position the particles spawn at
     */
    private double x;
    private double y;
    private double z;

    public SleepingParticleMessage()
    {
        super();
    }

    public SleepingParticleMessage(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void messageOnClientThread(final SleepingParticleMessage message, final MessageContext ctx)
    {
        Minecraft.getMinecraft().effectRenderer.addEffect(new SleepingParticle(Minecraft.getMinecraft().world,
          message.x,
          message.y,
          message.z,
          1.0f,
          1.0f,
          1.0f));
    }
}
