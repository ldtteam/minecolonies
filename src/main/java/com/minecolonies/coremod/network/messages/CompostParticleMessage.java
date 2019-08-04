package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

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
     * Empty constructor used when registering the message.
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
        pos = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        BlockPosUtil.writeToByteBuf(buf, pos);
    }

    @Override
    protected void messageOnClientThread(final CompostParticleMessage message, final MessageContext ctx)
    {
        final WorldClient world = ctx.getClientHandler().world;
        final int amount = random.nextInt(15) + 1;
        final BlockPos pos = message.pos;
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
                world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                  (double) ((float) pos.getX() + random.nextFloat()),
                  (double) pos.getY() + (double) random.nextFloat() * BlockState.getBoundingBox(world, pos).maxY,
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
                world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
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
