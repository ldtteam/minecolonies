package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        pos = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
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
        final ClientWorld world = Minecraft.getInstance().world;
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
                  (double) pos.getY() + (double) random.nextFloat() * BlockState.getShape(world, pos).getBoundingBox().maxY,
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
