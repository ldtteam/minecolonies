package com.minecolonies.coremod.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects in a stream between two targets.
 */
public class StreamParticleEffectMessage extends AbstractMessage<StreamParticleEffectMessage, IMessage>
{
    /**
     * Random obj.
     */
    private static final Random RAND = new Random();

    /**
     * The itemStack for the particles.
     */
    private EnumParticleTypes type;

    /**
     * The start position.
     */
    private double sPosX;
    private double sPosY;
    private double sPosZ;

    /**
     * The end position.
     */
    private double ePosX;
    private double ePosY;
    private double ePosZ;

    /**
     * The stage of the transfer.
     */
    private int stage;

    /**
     * The max stage of the transfer.
     */
    private int maxStage;

    /**
     * Empty constructor used when registering the message.
     */
    public StreamParticleEffectMessage()
    {
        super();
    }

    /**
     * Start a particle stream.
     * @param start the starting position.
     * @param end the end position.
     * @param type the particle type.
     */
    public StreamParticleEffectMessage(final Vec3d start, final Vec3d end, final EnumParticleTypes type, final int stage, final int maxStage)
    {
        super();
        this.sPosX = start.x;
        this.sPosY = start.y -0.5;
        this.sPosZ = start.z;

        this.ePosX = end.x;
        this.ePosY = end.y -0.5;
        this.ePosZ = end.z;

        this.stage = stage;
        this.maxStage = maxStage;

        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.sPosX = buf.readDouble();
        this.sPosY = buf.readDouble();
        this.sPosZ = buf.readDouble();

        this.ePosX = buf.readDouble();
        this.ePosY = buf.readDouble();
        this.ePosZ = buf.readDouble();

        this.stage = buf.readInt();
        this.maxStage = buf.readInt();

        this.type = EnumParticleTypes.values()[buf.readInt()];
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeDouble(this.sPosX);
        buf.writeDouble(this.sPosY);
        buf.writeDouble(this.sPosZ);

        buf.writeDouble(this.ePosX);
        buf.writeDouble(this.ePosY);
        buf.writeDouble(this.ePosZ);

        buf.writeInt(this.stage);
        buf.writeInt(this.maxStage);

        buf.writeInt(this.type.ordinal());
    }

    @Override
    protected void messageOnClientThread(final StreamParticleEffectMessage message, final MessageContext ctx)
    {
        final WorldClient world = Minecraft.getMinecraft().world;

        final Vec3d end = new Vec3d(message.ePosX, message.ePosY, message.ePosZ);

        double xDif = ( message.sPosX - message.ePosX ) / message.maxStage ;
        double yDif = ( message.sPosY - message.ePosY ) / message.maxStage;
        double zDif = ( message.sPosZ - message.ePosZ ) / message.maxStage;

        final double curve = message.maxStage / 3.0;

        for (int step = Math.max(0, message.stage - 1); step <= Math.min(message.maxStage, message.stage + 1); step++)
        {
            double minDif = Math.min(step, Math.abs(step-message.maxStage))/curve;

            for (int i = 0; i < 10; ++i)
            {
                final Vec3d randomPos = new Vec3d(RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D);
                final Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
                world.spawnParticle(message.type,
                  end.x + randomOffset.x + xDif * step,
                  end.y + randomOffset.y + yDif *step + minDif,
                  end.z + randomOffset.z + zDif * step,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z);
            }
        }
    }
}
