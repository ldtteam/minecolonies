package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT;
import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH;

/**
 * Message for vanilla particles around a citizen, in villager-like shape.
 */
public class VanillaParticleMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "vanilla_particle_message", VanillaParticleMessage::new);

    /**
     * Citizen Position
     */
    private final double x;
    private final double y;
    private final double z;

    /**
     * Particle id
     */
    private final SimpleParticleType type;

    public VanillaParticleMessage(final double x, final double y, final double z, final SimpleParticleType type)
    {
        super(TYPE);
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    protected VanillaParticleMessage(final RegistryFriendlyByteBuf byteBuf, final PlayMessageType<?> type)
    {
        super(byteBuf, type);
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
        this.type = (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(byteBuf.readResourceLocation());
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
        byteBuf.writeResourceLocation(BuiltInRegistries.PARTICLE_TYPE.getKey(this.type));
    }

    @Override
    public void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        spawnParticles(type, player.level(), x, y, z);
    }

    /**
     * Spawns the given particle randomly around the position.
     *
     * @param particleType praticle to spawn
     * @param world        world to use
     * @param x            x pos
     * @param y            y pos
     * @param z            z pos
     */
    private void spawnParticles(SimpleParticleType particleType, Level world, double x, double y, double z)
    {
        final Random rand = new Random();
        for (int i = 0; i < 5; ++i)
        {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            world.addParticle(particleType,
              x + (rand.nextFloat() * CITIZEN_WIDTH * 2.0F) - CITIZEN_WIDTH,
              y + 1.0D + (rand.nextFloat() * CITIZEN_HEIGHT),
              z + (rand.nextFloat() * CITIZEN_WIDTH * 2.0F) - CITIZEN_WIDTH,
              d0,
              d1,
              d2);
        }
    }
}
