package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
    private final ParticleOptions type;

    public VanillaParticleMessage(final double x, final double y, final double z, final ParticleOptions type)
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
        this.type = decode(bufId(byteBuf), byteBuf);
    }

    private static Identifier bufId(final RegistryFriendlyByteBuf buf)
    {
        return buf.readIdentifier();
    }

    private static ParticleOptions decode(final Identifier id, final RegistryFriendlyByteBuf buf)
    {
        if (id.equals(BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.HEART)))
        {
            return ParticleTypes.HEART;
        }
        if (id.equals(BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.ENCHANT)))
        {
            return ParticleTypes.ENCHANT;
        }
        if (id.equals(BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.HAPPY_VILLAGER)))
        {
            return ParticleTypes.HAPPY_VILLAGER;
        }
        if (id.equals(BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.INSTANT_EFFECT)))
        {
            return SpellParticleOption.streamCodec(ParticleTypes.INSTANT_EFFECT).decode(buf);
        }
        if (id.equals(BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.DRAGON_BREATH)))
        {
            return PowerParticleOption.streamCodec(ParticleTypes.DRAGON_BREATH).decode(buf);
        }
        throw new IllegalArgumentException("Unsupported particle type: " + id);
    }

    private static void encode(final RegistryFriendlyByteBuf buf, final ParticleOptions option)
    {
        if (option instanceof final SimpleParticleType simple)
        {
            buf.writeIdentifier(BuiltInRegistries.PARTICLE_TYPE.getKey(simple));
            simple.streamCodec().encode(buf, simple);
            return;
        }
        if (option instanceof final SpellParticleOption spell)
        {
            buf.writeIdentifier(BuiltInRegistries.PARTICLE_TYPE.getKey(spell.getType()));
            spell.getType().streamCodec().encode(buf, spell);
            return;
        }
        if (option instanceof final PowerParticleOption power)
        {
            buf.writeIdentifier(BuiltInRegistries.PARTICLE_TYPE.getKey(power.getType()));
            power.getType().streamCodec().encode(buf, power);
            return;
        }
        throw new IllegalArgumentException("Unsupported particle option: " + option);
    }
    @Override
    protected void toBytes(final RegistryFriendlyByteBuf byteBuf)
    {
        encode(byteBuf, this.type);
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
    private void spawnParticles(ParticleOptions particleType, Level world, double x, double y, double z)
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
