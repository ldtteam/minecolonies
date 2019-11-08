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
 * Handles spawning item particle effects in a circle around a target.
 */
public class CircleParticleEffectMessage extends AbstractMessage<CircleParticleEffectMessage, IMessage>
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
    private double posX;
    private double posY;
    private double posZ;

    /**
     * The current stage of the circle.
     */
    private int stage;

    /**
     * Empty constructor used when registering the message.
     */
    public CircleParticleEffectMessage()
    {
        super();
    }

    /**
     * Start a circling particle effect.
     *
     * @param pos   the position.
     * @param type  the particle type.
     * @param stage the stage.
     */
    public CircleParticleEffectMessage(final Vec3d pos, final EnumParticleTypes type, final int stage)
    {
        super();
        this.posX = pos.x;
        this.posY = pos.y - 0.5;
        this.posZ = pos.z;
        this.stage = stage;
        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.stage = buf.readInt();

        this.type = EnumParticleTypes.values()[buf.readInt()];
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeInt(this.stage);

        buf.writeInt(this.type.ordinal());
    }

    @Override
    protected void messageOnClientThread(final CircleParticleEffectMessage message, final MessageContext ctx)
    {
        final WorldClient world = Minecraft.getMinecraft().world;

        double x = 1.0 * Math.cos(message.stage * 45.0) + message.posX;
        double z = 1.0 * Math.sin(message.stage * 45.0) + message.posZ;

        for (int i = 0; i < 5; ++i)
        {
            final Vec3d randomPos = new Vec3d(RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D);
            final Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
            world.spawnParticle(message.type,
              x + randomOffset.x,
              message.posY + randomOffset.y,
              z + randomOffset.z,
              randomPos.x,
              randomPos.y + 0.05D,
              randomPos.z);
        }
    }
}
