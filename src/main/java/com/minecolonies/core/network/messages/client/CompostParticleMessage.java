package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles the server causing compost particle effects.
 */
public class CompostParticleMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "compost_particle", CompostParticleMessage::new);

    /**
     * Random obj for values.
     */
    public static final Random random = new Random();

    /**
     * The position.
     */
    private final BlockPos pos;

    /**
     * Sends a message for particle effect.
     *
     * @param pos Coordinates
     */
    public CompostParticleMessage(final BlockPos pos)
    {
        super(TYPE);
        this.pos = pos;
    }

    public CompostParticleMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        pos = buf.readBlockPos();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        final Level world = player.level();
        final int amount = random.nextInt(15) + 1;
        final BlockState state = world.getBlockState(pos);
        double d0;
        double d1;
        double d2;
        if (!state.isAir())
        {
            for (int i = 0; i < amount; ++i)
            {
                d0 = random.nextGaussian() * 0.02D;
                d1 = random.nextGaussian() * 0.02D;
                d2 = random.nextGaussian() * 0.02D;
                world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                  (double) ((float) pos.getX() + random.nextFloat()),
                  (double) pos.getY() + (double) random.nextFloat() * state.getShape(world, pos).bounds().maxY,
                  (double) ((float) pos.getZ() + random.nextFloat()),
                  d0,
                  d1,
                  d2
                );
            }
        }
        else
        {
            for (int i = 0; i < amount; ++i)
            {
                d0 = random.nextGaussian() * 0.02D;
                d1 = random.nextGaussian() * 0.02D;
                d2 = random.nextGaussian() * 0.02D;
                world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                  (double) ((float) pos.getX() + random.nextFloat()),
                  (double) pos.getY() + (double) random.nextFloat() * 1.0D,
                  (double) ((float) pos.getZ() + random.nextFloat()),
                  d0,
                  d1,
                  d2
                );
            }
        }
    }
}
