package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.apiimp.initializer.ModParticleTypesInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for sleeping particles
 */
public class SleepingParticleMessage implements IMessage
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
    public void fromBytes(final PacketBuffer byteBuf)
    {
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
    }

    @Override
    public void toBytes(final PacketBuffer byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        Minecraft.getInstance().world.addParticle(ModParticleTypesInitializer.SLEEPINGPARTICLE_TYPE,
          x,
          y,
          z,
          1.0f,
          1.0f,
          1.0f);
    }
}
