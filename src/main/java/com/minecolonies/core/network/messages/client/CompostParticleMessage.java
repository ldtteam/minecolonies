package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Handles the server causing compost particle effects.
 */
public class CompostParticleMessage implements IMessage
{
    /**
     * Random obj for values.
     */
    public static final Random random = new Random();

    /**
     * The position.
     */
    private BlockPos pos;

    /**
     * Empty constructor used when registering the
     */
    public CompostParticleMessage()
    {
        super();
    }

    /**
     * Sends a message for particle effect.
     *
     * @param pos Coordinates
     */
    public CompostParticleMessage(final BlockPos pos)
    {
        super();
        this.pos = pos;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        final int amount = random.nextInt(15) + 1;
        final BlockState BlockState = world.getBlockState(pos);
        double d0;
        double d1;
        double d2;
        if (BlockState.getMaterial() != Material.AIR)
        {
            for (int i = 0; i < amount; ++i)
            {
                d0 = random.nextGaussian() * 0.02D;
                d1 = random.nextGaussian() * 0.02D;
                d2 = random.nextGaussian() * 0.02D;
                world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                  (double) ((float) pos.getX() + random.nextFloat()),
                  (double) pos.getY() + (double) random.nextFloat() * BlockState.getShape(world, pos).bounds().maxY,
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
