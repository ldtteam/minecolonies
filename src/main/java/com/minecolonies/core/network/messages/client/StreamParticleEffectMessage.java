package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects in a stream between two targets.
 */
public class StreamParticleEffectMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "stream_particle_effect", StreamParticleEffectMessage::new);

    /**
     * Random obj.
     */
    private static final Random RAND = new Random();

    /**
     * The itemStack for the particles.
     */
    private final SimpleParticleType type;

    /**
     * The start position.
     */
    private final double sPosX;
    private final double sPosY;
    private final double sPosZ;

    /**
     * The end position.
     */
    private final double ePosX;
    private final double ePosY;
    private final double ePosZ;

    /**
     * The stage of the transfer.
     */
    private final int stage;

    /**
     * The max stage of the transfer.
     */
    private final int maxStage;

    /**
     * Start a particle stream.
     *
     * @param start    the starting position.
     * @param end      the end position.
     * @param type     the particle type.
     * @param stage    the stage we're at
     * @param maxStage the max stage
     */
    public StreamParticleEffectMessage(final Vec3 start, final Vec3 end, final SimpleParticleType type, final int stage, final int maxStage)
    {
        super(TYPE);
        this.sPosX = start.x;
        this.sPosY = start.y - 0.5;
        this.sPosZ = start.z;

        this.ePosX = end.x;
        this.ePosY = end.y - 0.5;
        this.ePosZ = end.z;

        this.stage = stage;
        this.maxStage = maxStage;

        this.type = type;
    }

    protected StreamParticleEffectMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.sPosX = buf.readDouble();
        this.sPosY = buf.readDouble();
        this.sPosZ = buf.readDouble();

        this.ePosX = buf.readDouble();
        this.ePosY = buf.readDouble();
        this.ePosZ = buf.readDouble();

        this.stage = buf.readInt();
        this.maxStage = buf.readInt();
        this.type = (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(buf.readResourceLocation());
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeDouble(this.sPosX);
        buf.writeDouble(this.sPosY);
        buf.writeDouble(this.sPosZ);

        buf.writeDouble(this.ePosX);
        buf.writeDouble(this.ePosY);
        buf.writeDouble(this.ePosZ);

        buf.writeInt(this.stage);
        buf.writeInt(this.maxStage);
        buf.writeResourceLocation(BuiltInRegistries.PARTICLE_TYPE.getKey(this.type));
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        final Vec3 end = new Vec3(ePosX, ePosY, ePosZ);

        final double xDif = (sPosX - ePosX) / maxStage;
        final double yDif = (sPosY - ePosY) / maxStage;
        final double zDif = (sPosZ - ePosZ) / maxStage;

        final double curve = maxStage / 3.0;

        for (int step = Math.max(0, stage - 1); step <= Math.min(maxStage, stage + 1); step++)
        {
            final double minDif = Math.min(step, Math.abs(step - maxStage)) / curve;

            for (int i = 0; i < 10; ++i)
            {
                final Vec3 randomPos = new Vec3(RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D);
                final Vec3 randomOffset = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
                player.level().addParticle(type,
                  end.x + randomOffset.x + xDif * step,
                  end.y + randomOffset.y + yDif * step + minDif,
                  end.z + randomOffset.z + zDif * step,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z);
            }
        }
    }
}
