package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects in a circle around a target.
 */
public class CircleParticleEffectMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "circle_particle_effect", CircleParticleEffectMessage::new);

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
    private final double posX;
    private final double posY;
    private final double posZ;

    /**
     * The current stage of the circle.
     */
    private final int stage;

    /**
     * Start a circling particle effect.
     *
     * @param pos   the position.
     * @param type  the particle type.
     * @param stage the stage.
     */
    public CircleParticleEffectMessage(final Vec3 pos, final SimpleParticleType type, final int stage)
    {
        super(TYPE);
        this.posX = pos.x;
        this.posY = pos.y - 0.5;
        this.posZ = pos.z;
        this.stage = stage;
        this.type = type;
    }

    public CircleParticleEffectMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.stage = buf.readInt();
        this.type = (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(buf.readResourceLocation());
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeInt(this.stage);
        buf.writeResourceLocation(BuiltInRegistries.PARTICLE_TYPE.getKey(this.type));
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        final double x = 1.0 * Math.cos(stage * 45.0) + posX;
        final double z = 1.0 * Math.sin(stage * 45.0) + posZ;

        for (int i = 0; i < 5; ++i)
        {
            final Vec3 randomPos = new Vec3(RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D);
            final Vec3 randomOffset = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
            player.level().addParticle(type,
              x + randomOffset.x,
              posY + randomOffset.y,
              z + randomOffset.z,
              randomPos.x,
              randomPos.y + 0.05D,
              randomPos.z);
        }
    }
}
